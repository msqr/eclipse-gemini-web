/*******************************************************************************
 * Copyright (c) 2009, 2010 VMware Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *   http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.  
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.gemini.web.tomcat.internal;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


import org.eclipse.gemini.web.core.spi.ContextPathExistsException;
import org.eclipse.gemini.web.core.spi.ServletContainer;
import org.eclipse.gemini.web.core.spi.ServletContainerException;
import org.eclipse.gemini.web.core.spi.WebApplicationHandle;
import org.eclipse.gemini.web.tomcat.internal.loading.BundleDirContext;
import org.eclipse.gemini.web.tomcat.internal.loading.BundleWebappLoader;
import org.eclipse.gemini.web.tomcat.internal.loading.ChainedClassLoader;
import org.eclipse.gemini.web.tomcat.internal.loading.StandardWebBundleClassLoaderFactory;
import org.eclipse.gemini.web.tomcat.internal.support.BundleFileResolver;
import org.eclipse.gemini.web.tomcat.internal.support.BundleFileResolverFactory;
import org.eclipse.gemini.web.tomcat.spi.WebBundleClassLoaderFactory;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;

final class TomcatServletContainer implements ServletContainer {

    private final TomcatMBeanManager mbeanManager;

    private final Tomcat tomcat;

    private final DelegatingClassLoaderCustomizer classLoaderCustomizer;

    private final ServiceRegistrationTracker registrationTracker = new ServiceRegistrationTracker();

    private final BundleContext context;

    public TomcatServletContainer(Tomcat tomcat, BundleContext context) {
        this.classLoaderCustomizer = new DelegatingClassLoaderCustomizer(context);
        this.tomcat = tomcat;
        this.tomcat.init();
        this.mbeanManager = new TomcatMBeanManager(tomcat.findEngine().getName());
        this.context = context;
    }

    public void start() {
        try {
            this.classLoaderCustomizer.open();

            WebBundleClassLoaderFactory classLoaderFactory = new StandardWebBundleClassLoaderFactory(this.classLoaderCustomizer);
            ServiceRegistration registration = this.context.registerService(WebBundleClassLoaderFactory.class.getName(), classLoaderFactory, null);
            this.registrationTracker.track(registration);

            this.mbeanManager.start();
            doStart();
        } catch (LifecycleException e) {
            throw new ServletContainerException("Unable to start Tomcat.", e);
        }
    }

    public void stop() {
        try {
            doStop();
            this.mbeanManager.stop();
            this.registrationTracker.unregisterAll();
            this.classLoaderCustomizer.close();
        } catch (LifecycleException e) {
            throw new ServletContainerException("Error stopping Tomcat", e);
        }
    }

    public WebApplicationHandle createWebApplication(String contextPath, Bundle bundle) {
        contextPath = formatContextPath(contextPath);

        try {
            String docBase = determineDocBase(bundle);

            StandardContext context = (StandardContext) this.tomcat.createContext(contextPath, docBase);

            BundleWebappLoader loader = new BundleWebappLoader(bundle, this.classLoaderCustomizer);
            context.setLoader(loader);
            context.setResources(new BundleDirContext(bundle));

            ServletContext servletContext = context.getServletContext();

            return new TomcatWebApplicationHandle(servletContext, context, loader);
        } catch (Exception ex) {
            throw new ServletContainerException("Unablo te create web application for context path '" + contextPath + "'", ex);
        }
    }

    public void startWebApplication(WebApplicationHandle handle) {
        String contextPath = handle.getServletContext().getContextPath();
        Host host = this.tomcat.findHost();

        checkContextPathIsFree(contextPath, host);

        StandardContext context = extractTomcatContext(handle);

        host.addChild(context);
        if (!context.getAvailable()) {
            host.removeChild(context);
            throw new ServletContainerException("Web application at '" + contextPath + "' failed to start. Check the logs for more details.");
        }

    }

    public void stopWebApplication(WebApplicationHandle handle) {
        StandardContext context = extractTomcatContext(handle);
        try {
            removeContext(context);
        } finally {
            try {
                stopContext(context);
            } finally {
                destroyContext(context);
            }
        }
    }

    private void doStart() throws LifecycleException {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(createThreadContextClassLoader());
            this.tomcat.start();
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
    }

    private void doStop() throws LifecycleException {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(createThreadContextClassLoader());
            this.tomcat.stop();
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
    }

    private ClassLoader createThreadContextClassLoader() {
        return ChainedClassLoader.create(getClass().getClassLoader(), Context.class.getClassLoader());
    }

    private void removeContext(StandardContext context) {
        try {
            Host host = this.tomcat.findHost();
            host.removeChild(context);
        } catch (Exception e) {
            throw new ServletContainerException("Unable to remove web application with context path '" + context.getServletContext().getContextPath()
                + "'");
        }
    }

    private void stopContext(StandardContext context) {
        try {
            context.stop();
        } catch (Exception e) {
            throw new ServletContainerException("Error stopping web application with context path '" + context.getServletContext().getContextPath()
                + "'");
        }
    }

    private void destroyContext(StandardContext context) {
        try {
            context.destroy();
        } catch (Exception e) {
            throw new ServletContainerException("Error destroying web application with context path '" + context.getServletContext().getContextPath()
                + "'");
        }
    }

    /**
     * A context path can only be bound to one application. This method checks to see if a given context path is free,
     * throwing {@link ContextPathExistsException} if not.
     * 
     * @param contextPath the context path
     * @param host the {@link Host} to check for duplicate context paths.
     * @throws ContextPathExistsException if the context path is already used.
     */
    private void checkContextPathIsFree(String contextPath, Host host) {
        Container existingContext = host.findChild(contextPath);
        if (existingContext != null) {
            throw new ContextPathExistsException(contextPath);
        }
    }

    private String determineDocBase(Bundle bundle) {
        BundleFileResolver resolver = BundleFileResolverFactory.createBundleFileResolver();
        File root = resolver.resolve(bundle);
        return root != null ? root.getAbsolutePath() : "";
    }

    private StandardContext extractTomcatContext(WebApplicationHandle handle) {
        if (!(handle instanceof TomcatWebApplicationHandle)) {
            throw new IllegalStateException("Unrecognized handle type '" + handle.getClass() + "'.");
        }
        return ((TomcatWebApplicationHandle) handle).getContext();
    }

    private String formatContextPath(String contextPath) {
        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }
        return contextPath;
    }

    private static class TomcatWebApplicationHandle implements WebApplicationHandle {

        private final ServletContext servletContext;

        private final StandardContext context;
        
        private final BundleWebappLoader webappLoader;

        private TomcatWebApplicationHandle(ServletContext servletContext, StandardContext context, BundleWebappLoader webappLoader) {
            this.servletContext = servletContext;
            this.context = context;
            this.webappLoader = webappLoader;
        }

        public ServletContext getServletContext() {
            return this.servletContext;
        }

        public StandardContext getContext() {
            return context;
        }
        
        public ClassLoader getClassLoader() {
            return this.webappLoader.getClassLoader();
        }

    }
}
