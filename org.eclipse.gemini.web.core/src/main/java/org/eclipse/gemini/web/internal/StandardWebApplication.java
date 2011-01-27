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

package org.eclipse.gemini.web.internal;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.eclipse.gemini.web.core.WebApplication;
import org.eclipse.gemini.web.core.WebApplicationStartFailedException;
import org.eclipse.gemini.web.core.spi.ServletContainer;
import org.eclipse.gemini.web.core.spi.ServletContainerException;
import org.eclipse.gemini.web.core.spi.WebApplicationHandle;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;

final class StandardWebApplication implements WebApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardWebApplication.class);

    private final BundleContext bundleContext;

    private final Bundle extender;

    private final WebApplicationHandle handle;

    private final ServletContainer container;

    private final ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();

    private final EventManager eventManager;

    private boolean started = false;

    private final Object monitor = new Object();

    private final WebApplicationStartFailureRetryController retryController;

    public StandardWebApplication(BundleContext bundleContext, Bundle extender, WebApplicationHandle handle, ServletContainer container,
        EventManager eventManager, WebApplicationStartFailureRetryController retryController) {
        this.bundleContext = bundleContext;
        this.extender = extender;
        this.handle = handle;
        this.container = container;
        this.eventManager = eventManager;
        this.retryController = retryController;
    }

    public ServletContext getServletContext() {
        return this.handle.getServletContext();
    }

    public ClassLoader getClassLoader() {
        return this.handle.getClassLoader();
    }

    public void start() {
        boolean localStarted;

        synchronized (this.monitor) {
            localStarted = this.started;
        }

        if (!localStarted) {
            String webContextPath = getContextPath();
            this.eventManager.sendDeploying(getBundle(), this.extender, webContextPath);

            try {
                this.container.startWebApplication(this.handle);
                publishServletContext();

                synchronized (this.monitor) {
                    this.started = true;
                }

                this.eventManager.sendDeployed(getBundle(), this.extender, webContextPath);
            } catch (ServletContainerException ex) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Failed to start web application at bundleContext path '" + this.handle.getServletContext().getContextPath() + "'", ex);
                }
                this.retryController.recordFailure(this);
                Set<Long> webContextPathBundleIds = getWebContextPathBundleIds(webContextPath);
                boolean collision = webContextPathBundleIds.size() > 1;
                this.eventManager.sendFailed(getBundle(), this.extender, webContextPath, ex, collision ? webContextPath : null,
                    collision ? webContextPathBundleIds : null);
                throw new WebApplicationStartFailedException(ex);
            }
        }
    }

    private Set<Long> getWebContextPathBundleIds(String webContextPath) {
        Set<Long> bundleIds = new HashSet<Long>();
        for (Bundle bundle : this.bundleContext.getBundles()) {
            if (webContextPath.equals(WebContainerUtils.getContextPath(bundle))) {
                bundleIds.add(bundle.getBundleId());
            }
        }
        return bundleIds;
    }

    public void stop() {
        boolean localStarted;

        synchronized (this.monitor) {
            localStarted = this.started;
            this.started = false;
        }

        if (localStarted) {
            this.eventManager.sendUndeploying(getBundle(), this.extender, getContextPath());
            this.container.stopWebApplication(this.handle);
            this.tracker.unregisterAll();
            this.eventManager.sendUndeployed(getBundle(), this.extender, getContextPath());
        }
        this.retryController.retryFailures(this);
    }

    private void publishServletContext() {
        Dictionary<String, String> properties = constructServletContextProperties();
        this.tracker.track(this.bundleContext.registerService(ServletContext.class, getServletContext(), properties));
    }

    String getContextPath() {
        return this.handle.getServletContext().getContextPath();
    }

    Bundle getBundle() {
        return this.bundleContext.getBundle();
    }

    private Dictionary<String, String> constructServletContextProperties() {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        Bundle bundle = getBundle();
        WebContainerUtils.setServletContextBundleProperties(properties, bundle);
        properties.put("osgi.web.contextpath", getServletContext().getContextPath());
        return properties;
    }

}
