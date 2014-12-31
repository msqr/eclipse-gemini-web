/*******************************************************************************
 * Copyright (c) 2012, 2014 SAP AG
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
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_COLLISION_BUNDLES;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.servlet.ServletContext;

import org.eclipse.gemini.web.core.WebApplicationStartFailedException;
import org.eclipse.gemini.web.core.spi.ServletContainer;
import org.eclipse.gemini.web.core.spi.ServletContainerException;
import org.eclipse.gemini.web.core.spi.WebApplicationHandle;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.service.event.StubEventAdmin;
import org.eclipse.virgo.test.stubs.support.ObjectClassFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class StandardWebApplicationTests {

    private static final String EVENT_FAILED = "org/osgi/service/web/FAILED";

    private static final String EVENT_UNDEPLOYED = "org/osgi/service/web/UNDEPLOYED";

    private static final String EVENT_DEPLOYED = "org/osgi/service/web/DEPLOYED";

    private static final String FILTER_EVENT_ADMIN = "(objectClass=org.osgi.service.event.EventAdmin)";

    private static final String HEADER_WEB_CONTEXT_PATH = "Web-ContextPath";

    private static final String CONTEXT_PATH = "context-path";

    private StubBundle bundle;

    private StubBundle extender;

    private StubBundle thisBundle;

    private WebApplicationHandle webApplicationHandle;

    private ServletContainer servletContainer;

    private EventManager eventManager;

    private WebApplicationStartFailureRetryController webApplicationStartFailureRetryController;

    private ServletContext servletContext;

    private Filter filter;

    private StubEventAdmin eventAdmin;

    @Before
    public void setUp() throws Exception {
        this.webApplicationHandle = createMock(WebApplicationHandle.class);
        this.servletContainer = createMock(ServletContainer.class);
        this.servletContext = createMock(ServletContext.class);
        this.filter = new ObjectClassFilter(EventAdmin.class.getName());
        this.eventAdmin = new StubEventAdmin();
        this.extender = new StubBundle();
        this.bundle = new StubBundle();
        this.bundle.addHeader(HEADER_WEB_CONTEXT_PATH, CONTEXT_PATH);
        this.thisBundle = new StubBundle();
        expect(this.webApplicationHandle.getServletContext()).andReturn(this.servletContext).anyTimes();
        expect(this.servletContext.getContextPath()).andReturn(CONTEXT_PATH).anyTimes();
        ((StubBundleContext) this.thisBundle.getBundleContext()).addFilter(FILTER_EVENT_ADMIN, this.filter);
        ((StubBundleContext) this.thisBundle.getBundleContext()).registerService(EventAdmin.class, this.eventAdmin, null);
        ((StubBundleContext) this.thisBundle.getBundleContext()).addInstalledBundle(this.bundle);

        this.eventManager = new EventManager(this.thisBundle.getBundleContext());
        this.eventManager.start();
    }

    @After
    public void tearDown() throws Exception {
        this.eventManager.stop();
        verify(this.webApplicationHandle, this.servletContainer, this.servletContext);
    }

    @Test
    public void testStartTwice() throws Exception {
        this.servletContainer.startWebApplication(this.webApplicationHandle);
        expectLastCall().anyTimes();

        replay(this.webApplicationHandle, this.servletContainer, this.servletContext);

        StandardWebApplication standardWebApplication = createStandardWebApplication(true);

        standardWebApplication.start();
        Event event = this.eventAdmin.awaitSendingOfEvent(EVENT_DEPLOYED, 10);
        assertNotNull(event);

        standardWebApplication.start();
        event = this.eventAdmin.awaitSendingOfEvent(EVENT_DEPLOYED, 10);
        assertNull(event);
    }

    @Test
    public void testStopWithoutStart() throws Exception {
        this.servletContainer.startWebApplication(this.webApplicationHandle);
        expectLastCall().anyTimes();

        replay(this.webApplicationHandle, this.servletContainer, this.servletContext);

        StandardWebApplication standardWebApplication = createStandardWebApplication(true);

        standardWebApplication.stop();
        Event event = this.eventAdmin.awaitSendingOfEvent(EVENT_UNDEPLOYED, 10);
        assertNull(event);
    }

    @Test
    public void testStartStop() throws Exception {
        StandardWebApplication standardWebApplication = startStopExpectations();

        startStop(standardWebApplication);

        standardWebApplication = createStandardWebApplication(false);

        startStop(standardWebApplication);
    }

    @Test
    public void testFailedStart1() throws Exception {
        this.servletContainer.startWebApplication(this.webApplicationHandle);
        expectLastCall().andThrow(new ServletContainerException("Start failes."));
        StubBundle otherBundleWithSameContextPath = new StubBundle(2L, "test", new Version("1.0.0"), "test");
        otherBundleWithSameContextPath.addHeader(HEADER_WEB_CONTEXT_PATH, CONTEXT_PATH);
        this.bundle.addHeader(HEADER_WEB_CONTEXT_PATH, CONTEXT_PATH);
        ((StubBundleContext) this.thisBundle.getBundleContext()).addInstalledBundle(otherBundleWithSameContextPath);
        ((StubBundleContext) this.thisBundle.getBundleContext()).addInstalledBundle(this.bundle);

        replay(this.webApplicationHandle, this.servletContainer, this.servletContext);

        StandardWebApplication standardWebApplication = createStandardWebApplication(true);

        try {
            standardWebApplication.start();
            fail("Exception should be thrown, because startWebApplication failes.");
        } catch (WebApplicationStartFailedException e) {
            System.out.println(e.getMessage());
        }

        ServiceReference<?>[] serviceReferences = this.bundle.getBundleContext().getAllServiceReferences(ServletContext.class.getName(), null);
        assertNull(serviceReferences);
        Event event = this.eventAdmin.awaitSendingOfEvent(EVENT_FAILED, 10);
        assertNotNull(event);
        assertNotNull(event.getProperty(EVENT_PROPERTY_COLLISION_BUNDLES));
    }

    @Test
    public void testFailedStart2() throws Exception {
        StandardWebApplication standardWebApplication = startStopExpectations();

        startNegative(standardWebApplication);

        this.bundle.setState(Bundle.ACTIVE);

        standardWebApplication = createStandardWebApplication(false);

        startNegative(standardWebApplication);
    }

    private StandardWebApplication createStandardWebApplication(boolean withExtender) {
        this.webApplicationStartFailureRetryController = new WebApplicationStartFailureRetryController();

        if (withExtender) {
            return new StandardWebApplication(this.bundle, this.extender, this.webApplicationHandle, this.servletContainer, this.eventManager,
                this.webApplicationStartFailureRetryController, this.thisBundle.getBundleContext());
        }
        return new StandardWebApplication(this.bundle, null, this.webApplicationHandle, this.servletContainer, this.eventManager,
            this.webApplicationStartFailureRetryController, this.thisBundle.getBundleContext());
    }

    private StandardWebApplication startStopExpectations() {
        this.servletContainer.startWebApplication(this.webApplicationHandle);
        expectLastCall().anyTimes();
        this.servletContainer.stopWebApplication(this.webApplicationHandle);
        expectLastCall().anyTimes();

        replay(this.webApplicationHandle, this.servletContainer, this.servletContext);

        StandardWebApplication standardWebApplication = createStandardWebApplication(true);
        return standardWebApplication;
    }

    private void startNegative(StandardWebApplication standardWebApplication) {
        this.bundle.setState(Bundle.RESOLVED);

        try {
            standardWebApplication.start();
            fail("Exception should be thrown because bundle is in RESOLVED state.");
        } catch (WebApplicationStartFailedException e) {
            System.out.println(e.getMessage());
        }

        Event event = this.eventAdmin.awaitSendingOfEvent(EVENT_FAILED, 10);
        assertNotNull(event);
    }

    private void startStop(StandardWebApplication standardWebApplication) throws InvalidSyntaxException {
        standardWebApplication.start();
        ServiceReference<?>[] serviceReferences = this.bundle.getBundleContext().getAllServiceReferences(ServletContext.class.getName(), null);
        assertNotNull(serviceReferences);
        assertTrue(serviceReferences.length == 1);
        Event event = this.eventAdmin.awaitSendingOfEvent(EVENT_DEPLOYED, 10);
        assertNotNull(event);

        standardWebApplication.stop();
        serviceReferences = this.bundle.getBundleContext().getAllServiceReferences(ServletContext.class.getName(), null);
        assertNull(serviceReferences);
        event = this.eventAdmin.awaitSendingOfEvent(EVENT_UNDEPLOYED, 10);
        assertNotNull(event);
    }

}
