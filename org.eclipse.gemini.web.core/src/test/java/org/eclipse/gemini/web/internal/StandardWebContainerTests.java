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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.servlet.ServletContext;

import org.eclipse.gemini.web.core.WebContainer;
import org.eclipse.gemini.web.core.spi.ServletContainer;
import org.eclipse.gemini.web.core.spi.ServletContainerException;
import org.eclipse.gemini.web.core.spi.WebApplicationHandle;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.support.ObjectClassFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.service.event.EventAdmin;

public class StandardWebContainerTests {

    private static final String FILTER_EVENT_ADMIN = "(objectClass=org.osgi.service.event.EventAdmin)";

    private static final String HEADER_WEB_CONTEXT_PATH = "Web-ContextPath";

    private static final String CONTEXT_PATH = "context-path";

    private ServletContainer servletContainer;

    private EventManager eventManager;

    private StubBundle thisBundle;

    private Filter filter;

    private StubBundle bundle;

    private StubBundle extender;

    private StandardWebContainer standardWebContainer;

    private WebApplicationHandle webApplicationHandle;

    private ServletContext servletContext;

    @Before
    public void setUp() throws Exception {
        this.webApplicationHandle = createMock(WebApplicationHandle.class);
        this.servletContainer = createMock(ServletContainer.class);
        this.servletContext = createMock(ServletContext.class);
        this.thisBundle = new StubBundle();
        this.filter = new ObjectClassFilter(EventAdmin.class.getName());
        this.extender = new StubBundle();
        this.bundle = new StubBundle();
        ((StubBundleContext) this.thisBundle.getBundleContext()).addFilter(FILTER_EVENT_ADMIN, this.filter);
        this.eventManager = new EventManager(this.thisBundle.getBundleContext());
        this.standardWebContainer = new StandardWebContainer(this.servletContainer, this.eventManager, this.thisBundle.getBundleContext());
    }

    @After
    public void tearDown() throws Exception {
        verify(this.servletContainer, this.webApplicationHandle, this.servletContext);
    }

    @Test
    public void testCreateWebApplicationBundleWithRegularBundle() throws Exception {
        replay(this.servletContainer, this.webApplicationHandle, this.servletContext);

        try {
            this.standardWebContainer.createWebApplication(this.bundle);
            fail("Exception is expected");
        } catch (BundleException e) {
        }

        try {
            this.standardWebContainer.createWebApplication(this.bundle, this.extender);
            fail("Exception is expected");
        } catch (BundleException e) {
        }
    }

    @Test
    public void testCreateWebApplicationBundle() throws Exception {
        expect(this.webApplicationHandle.getServletContext()).andReturn(this.servletContext).anyTimes();
        expect(this.servletContainer.createWebApplication(CONTEXT_PATH, this.bundle)).andReturn(this.webApplicationHandle).anyTimes();
        this.servletContext.setAttribute(WebContainer.ATTRIBUTE_BUNDLE_CONTEXT, this.bundle.getBundleContext());
        expectLastCall().anyTimes();

        this.bundle.addHeader(HEADER_WEB_CONTEXT_PATH, CONTEXT_PATH);

        replay(this.servletContainer, this.webApplicationHandle, this.servletContext);

        assertEquals(this.servletContext, this.standardWebContainer.createWebApplication(this.bundle).getServletContext());

        assertEquals(this.servletContext, this.standardWebContainer.createWebApplication(this.bundle, this.extender).getServletContext());
    }

    @Test
    public void testFailedCreateWebApplicationBundle() throws Exception {
        expect(this.webApplicationHandle.getServletContext()).andReturn(this.servletContext).anyTimes();
        expect(this.servletContainer.createWebApplication(CONTEXT_PATH, this.bundle)).andThrow(new ServletContainerException()).anyTimes();

        this.bundle.addHeader(HEADER_WEB_CONTEXT_PATH, CONTEXT_PATH);

        replay(this.servletContainer, this.webApplicationHandle, this.servletContext);

        try {
            this.standardWebContainer.createWebApplication(this.bundle);
            fail("Exception is expected");
        } catch (BundleException e) {
            assertTrue(e.getCause() instanceof ServletContainerException);
        }

        try {
            this.standardWebContainer.createWebApplication(this.bundle, this.extender);
            fail("Exception is expected");
        } catch (BundleException e) {
            assertTrue(e.getCause() instanceof ServletContainerException);
        }
    }

}
