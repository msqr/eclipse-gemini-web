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

import java.io.InputStream;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;


import org.eclipse.gemini.web.core.WebContainerProperties;
import org.eclipse.gemini.web.core.spi.ServletContainer;
import org.eclipse.gemini.web.tomcat.internal.loading.DirContextURLStreamHandlerService;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;

public class Activator implements BundleActivator {

    private final Object monitor = new Object();

    private final ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();

    private TomcatServletContainer container;

    public void start(BundleContext context) throws Exception {
        registerURLStreamHandler(context);
        registerConnectorDescriptors(context);
        
        TomcatServletContainer container = createContainer(context);
        container.start();

        ServiceRegistration sr = context.registerService(ServletContainer.class.getName(), container, null);
        this.tracker.track(sr);

        synchronized (this.monitor) {
            this.container = container;
        }
    }
    
    private void registerConnectorDescriptors(BundleContext context) {
        TomcatWebContainerProperties tomcatWebContainerProperties = new TomcatWebContainerProperties();
        ServiceRegistration registration = context.registerService(WebContainerProperties.class.getName(), tomcatWebContainerProperties, null);
        this.tracker.track(registration);
    }

    private void registerURLStreamHandler(BundleContext context) {
        Properties properties = new Properties();
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, "jndi");

        DirContextURLStreamHandlerService handler = new DirContextURLStreamHandlerService();
        ServiceRegistration reg = context.registerService(URLStreamHandlerService.class.getName(), handler, properties);
        this.tracker.track(reg);
    }

    public void stop(BundleContext context) throws Exception {
        TomcatServletContainer container;
        synchronized (this.monitor) {
            container = this.container;
            this.container = null;
        }
        if (container != null) {
            container.stop();
        }
        this.tracker.unregisterAll();
    }

    private TomcatServletContainer createContainer(BundleContext context) throws BundleException {
        TomcatServletContainerFactory factory = new TomcatServletContainerFactory();
        InputStream configFile = resolveConfigFile(context);
        try {
            return factory.createContainer(configFile, context, getPackageAdmin(context));
        } finally {
            IOUtils.closeQuietly(configFile);
        }
    }

    private InputStream resolveConfigFile(BundleContext context) throws BundleException {
        return TomcatConfigLocator.resolveConfigFile(context);
    }
    
    private PackageAdmin getPackageAdmin(BundleContext bundleContext) {
        ServiceReference serviceReference = bundleContext.getServiceReference(PackageAdmin.class.getName());
        if (serviceReference != null) {
            PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(serviceReference);
            if (packageAdmin != null) {
                return packageAdmin;
            }
        }
        
        throw new IllegalStateException("PackageAdmin not available in the service registry");
    }
}
