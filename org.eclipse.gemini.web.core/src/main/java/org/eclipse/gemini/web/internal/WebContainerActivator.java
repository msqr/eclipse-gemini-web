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
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;


import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
import org.eclipse.gemini.web.core.WebContainer;
import org.eclipse.gemini.web.core.spi.ServletContainer;
import org.eclipse.gemini.web.internal.url.ChainingWebBundleManifestTransformer;
import org.eclipse.gemini.web.internal.url.DefaultsWebBundleManifestTransformer;
import org.eclipse.gemini.web.internal.url.SpecificationWebBundleManifestTransformer;
import org.eclipse.gemini.web.internal.url.SystemBundleExportsImportingWebBundleManifestTransformer;
import org.eclipse.gemini.web.internal.url.WebBundleUrl;
import org.eclipse.gemini.web.internal.url.WebBundleUrlStreamHandlerService;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.eclipse.virgo.util.osgi.VersionRange;

public class WebContainerActivator implements BundleActivator {

    private final ServiceRegistrationTracker regTracker = new ServiceRegistrationTracker();

    private volatile EventManager eventManager;
    
    private ServiceTracker serviceTracker;

    public void start(BundleContext context) throws Exception {
        WebBundleManifestTransformer transformer = registerWebBundleManifestTransformer(context);
        
        registerUrlStreamHandler(context, transformer);
        
        this.eventManager = new EventManager(context);
        this.eventManager.start();
        
        this.serviceTracker = new ServiceTracker(context, ServletContainer.class.getName(), new ServletContainerTracker(context, this.eventManager));
        this.serviceTracker.open();
    }

    public void stop(BundleContext context) throws Exception {
        this.serviceTracker.close();
        this.regTracker.unregisterAll();
        this.eventManager.stop();
    }

    private WebBundleManifestTransformer registerWebBundleManifestTransformer(BundleContext context) {
            	
        WebBundleManifestTransformer specTransformer = new SpecificationWebBundleManifestTransformer();
        WebBundleManifestTransformer defaultsTransformer = new DefaultsWebBundleManifestTransformer();        
        Map<String, VersionRange> systemBundleExports = new SystemBundleExportsResolver(context).getSystemBundleExports();
        WebBundleManifestTransformer systemBundleExportImportingWebBundleManifestTransformer = new SystemBundleExportsImportingWebBundleManifestTransformer(systemBundleExports);
        
        WebBundleManifestTransformer chainingTransformer = new ChainingWebBundleManifestTransformer(specTransformer, defaultsTransformer, systemBundleExportImportingWebBundleManifestTransformer);
                
        ServiceRegistration reg = context.registerService(WebBundleManifestTransformer.class.getName(), chainingTransformer, null);
        this.regTracker.track(reg);
        
        return chainingTransformer;
    }
    
    
    private void registerUrlStreamHandler(BundleContext context, WebBundleManifestTransformer transformer) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(URLConstants.URL_HANDLER_PROTOCOL, WebBundleUrl.SCHEME);

        ServiceRegistration reg = context.registerService(URLStreamHandlerService.class.getName(), new WebBundleUrlStreamHandlerService(transformer), props);
        this.regTracker.track(reg);
    }

    private static final class ServletContainerTracker implements ServiceTrackerCustomizer {

        private final ServiceRegistrationTracker regTracker = new ServiceRegistrationTracker();

        private final BundleContext context;

        private final EventManager eventManager;

        public ServletContainerTracker(BundleContext context, EventManager eventManager) {
            this.context = context;
            this.eventManager = eventManager;
        }

        public Object addingService(ServiceReference reference) {
            ServletContainer container = (ServletContainer) this.context.getService(reference);
            
            WebContainer webContainer = new StandardWebContainer(container, this.eventManager);
            
            ServiceRegistration reg = context.registerService(WebContainer.class.getName(), webContainer, null);
            this.regTracker.track(reg);
            
            return webContainer;
        }

        public void modifiedService(ServiceReference reference, Object service) {
        }

        public void removedService(ServiceReference reference, Object service) {
            this.regTracker.unregisterAll();
            if (service instanceof WebContainer) {
                ((WebContainer)service).halt();
            }
        }

    }
}
