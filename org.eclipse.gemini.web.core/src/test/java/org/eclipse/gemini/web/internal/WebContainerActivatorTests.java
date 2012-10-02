/*******************************************************************************
 * Copyright (c) 2012 SAP AG
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
 *   Violeta Georgieva - initial contribution
 *******************************************************************************/

package org.eclipse.gemini.web.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Dictionary;

import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
import org.eclipse.gemini.web.core.WebContainer;
import org.eclipse.gemini.web.core.spi.ServletContainer;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.url.URLStreamHandlerService;

public class WebContainerActivatorTests {

    private static final String FILTER_EVENT_ADMIN = "(objectClass=org.osgi.service.event.EventAdmin)";

    private static final String FILTER_SERVLET_CONTAINER = "(objectClass=org.eclipse.gemini.web.core.spi.ServletContainer)";

    private BundleContext bundleContext;

    private Bundle systemBundle;

    private BundleRevision bundleRevision;

    private BundleWiring bundleWiring;

    private ServiceRegistration<WebBundleManifestTransformer> serviceRegistration1;

    private ServiceRegistration<WebContainer> serviceRegistration2;

    private ServiceRegistration<URLStreamHandlerService> serviceRegistration3;

    private ServiceReference<ServletContainer> serviceReference;

    private ServletContainer servletContainer;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        this.bundleContext = createMock(BundleContext.class);
        this.systemBundle = createMock(Bundle.class);
        this.bundleRevision = createMock(BundleRevision.class);
        this.bundleWiring = createMock(BundleWiring.class);
        this.serviceRegistration1 = createMock(ServiceRegistration.class);
        this.serviceRegistration2 = createMock(ServiceRegistration.class);
        this.serviceRegistration3 = createMock(ServiceRegistration.class);
        this.serviceReference = createMock(ServiceReference.class);
        this.servletContainer = createMock(ServletContainer.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStart() throws Exception {
        expect(this.bundleContext.getBundle(0)).andReturn(this.systemBundle);
        expect(this.bundleContext.getProperty(SystemBundleExportsResolver.OSGI_RESOLVER_MODE)).andReturn("");
        expect(this.systemBundle.adapt(BundleRevision.class)).andReturn(this.bundleRevision);
        expect(this.bundleRevision.getWiring()).andReturn(this.bundleWiring);
        expect(this.bundleWiring.getCapabilities(BundleRevision.PACKAGE_NAMESPACE)).andReturn(new ArrayList<BundleCapability>());
        expect(
            this.bundleContext.registerService(eq(WebBundleManifestTransformer.class), isA(WebBundleManifestTransformer.class),
                eq((Dictionary<String, ?>) null))).andReturn(this.serviceRegistration1);
        expect(this.bundleContext.registerService(eq(WebContainer.class), isA(WebContainer.class), eq((Dictionary<String, ?>) null))).andReturn(
            this.serviceRegistration2);
        expect(this.bundleContext.registerService(eq(URLStreamHandlerService.class), isA(URLStreamHandlerService.class), isA(Dictionary.class))).andReturn(
            this.serviceRegistration3);
        expect(this.bundleContext.createFilter(FILTER_EVENT_ADMIN)).andReturn(null);
        expect(this.bundleContext.createFilter(FILTER_SERVLET_CONTAINER)).andReturn(null);
        this.bundleContext.addServiceListener(isA(ServiceListener.class), eq(FILTER_EVENT_ADMIN));
        expectLastCall();
        this.bundleContext.addServiceListener(isA(ServiceListener.class), eq(FILTER_SERVLET_CONTAINER));
        expectLastCall();
        expect(this.bundleContext.getServiceReferences(EventAdmin.class.getName(), null)).andReturn(null);
        expect(this.bundleContext.getServiceReferences(ServletContainer.class.getName(), null)).andReturn(
            new ServiceReference[] { this.serviceReference });
        expect(this.bundleContext.getService(this.serviceReference)).andReturn(this.servletContainer);
        this.bundleContext.removeServiceListener(isA(ServiceListener.class));
        expectLastCall().times(2);

        replay(this.bundleContext, this.systemBundle, this.bundleRevision, this.bundleWiring, this.serviceReference);

        WebContainerActivator webContainerActivator = new WebContainerActivator();
        webContainerActivator.start(this.bundleContext);

        webContainerActivator.stop(this.bundleContext);

        verify(this.bundleContext, this.systemBundle, this.bundleRevision, this.bundleWiring, this.serviceReference);
    }

}
