/*******************************************************************************
 * Copyright (c) 2009, 2012 VMware Inc.
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
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class WebContainerActivator implements BundleActivator {

    private final ServiceRegistrationTracker regTracker = new ServiceRegistrationTracker();

    private volatile EventManager eventManager;

    private ServiceTracker<ServletContainer, WebContainer> serviceTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        WebBundleManifestTransformer transformer = registerWebBundleManifestTransformer(context);

        registerUrlStreamHandler(context, transformer);

        this.eventManager = new EventManager(context);
        this.eventManager.start();

        this.serviceTracker = new ServiceTracker<ServletContainer, WebContainer>(context, ServletContainer.class, new ServletContainerTracker(
            context, this.eventManager));
        this.serviceTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        this.serviceTracker.close();
        this.regTracker.unregisterAll();
        this.eventManager.stop();
    }

    private WebBundleManifestTransformer registerWebBundleManifestTransformer(BundleContext context) {

        WebBundleManifestTransformer specTransformer = new SpecificationWebBundleManifestTransformer();
        WebBundleManifestTransformer defaultsTransformer = new DefaultsWebBundleManifestTransformer();
        Map<String, VersionRange> systemBundleExports = new SystemBundleExportsResolver(context).getSystemBundleExports();
        WebBundleManifestTransformer systemBundleExportImportingWebBundleManifestTransformer = new SystemBundleExportsImportingWebBundleManifestTransformer(
            systemBundleExports);

        WebBundleManifestTransformer chainingTransformer = new ChainingWebBundleManifestTransformer(specTransformer, defaultsTransformer,
            systemBundleExportImportingWebBundleManifestTransformer);

        ServiceRegistration<WebBundleManifestTransformer> reg = context.registerService(WebBundleManifestTransformer.class, chainingTransformer, null);
        this.regTracker.track(reg);

        return chainingTransformer;
    }

    private void registerUrlStreamHandler(BundleContext context, WebBundleManifestTransformer transformer) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { WebBundleUrl.SCHEME });

        ServiceRegistration<URLStreamHandlerService> reg = context.registerService(URLStreamHandlerService.class,
            new WebBundleUrlStreamHandlerService(transformer), props);
        this.regTracker.track(reg);
    }

    private static final class ServletContainerTracker implements ServiceTrackerCustomizer<ServletContainer, WebContainer> {

        private final ServiceRegistrationTracker regTracker = new ServiceRegistrationTracker();

        private final BundleContext context;

        private final EventManager eventManager;

        public ServletContainerTracker(BundleContext context, EventManager eventManager) {
            this.context = context;
            this.eventManager = eventManager;
        }

        @Override
        public WebContainer addingService(ServiceReference<ServletContainer> reference) {
            ServletContainer container = this.context.getService(reference);

            WebContainer webContainer = new StandardWebContainer(container, this.eventManager, this.context);

            ServiceRegistration<WebContainer> reg = this.context.registerService(WebContainer.class, webContainer, null);
            this.regTracker.track(reg);

            return webContainer;
        }

        @Override
        public void modifiedService(ServiceReference<ServletContainer> reference, WebContainer service) {
        }

        @Override
        public void removedService(ServiceReference<ServletContainer> reference, WebContainer service) {
            this.regTracker.unregisterAll();
            service.halt();
        }

    }
}
