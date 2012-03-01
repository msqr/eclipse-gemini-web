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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.servlet.ServletContext;

import org.eclipse.gemini.web.core.WebApplicationStartFailedException;
import org.eclipse.gemini.web.core.spi.ServletContainer;
import org.eclipse.gemini.web.core.spi.ServletContainerException;
import org.eclipse.gemini.web.core.spi.WebApplicationHandle;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.framework.StubFilter;
import org.eclipse.virgo.teststubs.osgi.framework.StubServiceRegistration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class StandardWebApplicationTests {

    private static final String CONTEXT_PATH = "context-path";

    private StubBundle bundle;

    private StubBundle extender;

    private WebApplicationHandle webApplicationHandle;

    private ServletContainer servletContainer;

    private EventManager eventManager;

    private WebApplicationStartFailureRetryController webApplicationStartFailureRetryController;

    private ServletContext servletContext;

    private StubFilter filter;

    @Before
    public void setUp() throws Exception {
        this.webApplicationHandle = createMock(WebApplicationHandle.class);
        this.servletContainer = createMock(ServletContainer.class);
        this.servletContext = createMock(ServletContext.class);
        this.filter = createMock(StubFilter.class);
        this.extender = new StubBundle();
        this.bundle = new StubBundle();
        ((StubBundleContext) this.bundle.getBundleContext()).addFilter("(objectClass=org.osgi.service.event.EventAdmin)", this.filter);
    }

    @After
    public void tearDown() throws Exception {
        verify(this.webApplicationHandle, this.servletContainer, this.servletContext, this.filter);
    }

    @Test
    public void testStartStop() throws Exception {
        expect(this.webApplicationHandle.getServletContext()).andReturn(this.servletContext).anyTimes();
        expect(this.servletContext.getContextPath()).andReturn(CONTEXT_PATH).anyTimes();
        this.servletContainer.startWebApplication(this.webApplicationHandle);
        expectLastCall();
        this.servletContainer.stopWebApplication(this.webApplicationHandle);
        expectLastCall();

        StandardWebApplication standardWebApplication = createStandardWebApplication();

        standardWebApplication.start();
        List<StubServiceRegistration<Object>> serviceRegistration = ((StubBundleContext) this.bundle.getBundleContext()).getServiceRegistrations();
        assertNotNull(serviceRegistration);
        assertTrue(serviceRegistration.size() == 1);
        assertTrue(this.bundle.getBundleContext().getService(serviceRegistration.get(0).getReference()) instanceof ServletContext);

        standardWebApplication.stop();
        serviceRegistration = ((StubBundleContext) this.bundle.getBundleContext()).getServiceRegistrations();
        assertNotNull(serviceRegistration);
        assertTrue(serviceRegistration.size() == 0);
    }

    @Test
    public void testFailedStart1() throws Exception {
        expect(this.webApplicationHandle.getServletContext()).andReturn(this.servletContext).anyTimes();
        expect(this.servletContext.getContextPath()).andReturn(CONTEXT_PATH).anyTimes();
        this.servletContainer.startWebApplication(this.webApplicationHandle);
        expectLastCall().andThrow(new ServletContainerException("Start failes."));

        StandardWebApplication standardWebApplication = createStandardWebApplication();

        try {
            standardWebApplication.start();
            fail("Exception should be thrown, because startWebApplication failes.");
        } catch (WebApplicationStartFailedException e) {
            System.out.println(e.getMessage());
        }

        List<StubServiceRegistration<Object>> serviceRegistration = ((StubBundleContext) this.bundle.getBundleContext()).getServiceRegistrations();
        assertNotNull(serviceRegistration);
        assertTrue(serviceRegistration.size() == 0);
    }

    @Test
    public void testFailedStart2() throws Exception {
        expect(this.webApplicationHandle.getServletContext()).andReturn(this.servletContext).anyTimes();
        expect(this.servletContext.getContextPath()).andReturn(CONTEXT_PATH).anyTimes();
        this.servletContainer.startWebApplication(this.webApplicationHandle);
        expectLastCall();
        this.servletContainer.stopWebApplication(this.webApplicationHandle);
        expectLastCall();

        StandardWebApplication standardWebApplication = createStandardWebApplication();

        this.bundle.setState(Bundle.RESOLVED);

        try {
            standardWebApplication.start();
            fail("Exception should be thrown because bundle is in RESOLVED state.");
        } catch (WebApplicationStartFailedException e) {
            System.out.println(e.getMessage());
        }

        List<StubServiceRegistration<Object>> serviceRegistration = ((StubBundleContext) this.bundle.getBundleContext()).getServiceRegistrations();
        assertNotNull(serviceRegistration);
        assertTrue(serviceRegistration.size() == 0);
    }

    private StandardWebApplication createStandardWebApplication() {
        replay(this.webApplicationHandle, this.servletContainer, this.servletContext, this.filter);

        this.eventManager = new EventManager(this.bundle.getBundleContext());
        this.webApplicationStartFailureRetryController = new WebApplicationStartFailureRetryController();

        return new StandardWebApplication(this.bundle, this.extender, this.webApplicationHandle, this.servletContainer, this.eventManager,
            this.webApplicationStartFailureRetryController);
    }

}
