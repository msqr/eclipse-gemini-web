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
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;

import org.eclipse.gemini.web.core.spi.ServletContainer;
import org.eclipse.gemini.web.core.spi.WebApplicationHandle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class WebApplicationStartFailureRetryControllerTests {

    private static final String SYMBOLIC_NAME = "symbolic-name";

    private static final String FIELD_NAME = "failures";

    private static final String FILTER = "(objectClass=org.osgi.service.event.EventAdmin)";

    private static final String CONTEXT_PATH_1 = "context-path-1";

    private static final String CONTEXT_PATH_2 = "context-path-2";

    private Bundle bundle1;

    private Bundle bundle2;

    private Bundle extender;

    private WebApplicationHandle handle;

    private ServletContainer container;

    private BundleContext thisBundleContext;

    private ServletContext servletContext;

    private EventManager eventManager;

    @Before
    public void setUp() throws Exception {
        this.bundle1 = createMock(Bundle.class);
        this.bundle2 = createMock(Bundle.class);
        this.extender = createMock(Bundle.class);
        this.handle = createMock(WebApplicationHandle.class);
        this.container = createMock(ServletContainer.class);
        this.thisBundleContext = createMock(BundleContext.class);
        this.servletContext = createMock(ServletContext.class);

        expect(this.thisBundleContext.createFilter(FILTER)).andReturn(null);
        expect(this.handle.getServletContext()).andReturn(this.servletContext).anyTimes();
    }

    @After
    public void tearDown() throws Exception {
        verify(this.bundle1, this.bundle2, this.extender, this.handle, this.container, this.thisBundleContext, this.servletContext);
    }

    @Test
    public void testRecordFailureNoContextPath() throws Exception {
        expect(this.servletContext.getContextPath()).andReturn(null);

        WebApplicationStartFailureRetryController webApplicationStartFailureRetryController = createWebApplicationStartFailureRetryController();
        webApplicationStartFailureRetryController.recordFailure(createStandardWebApplication(this.bundle1, webApplicationStartFailureRetryController));

        Field field = webApplicationStartFailureRetryController.getClass().getDeclaredField(FIELD_NAME);
        field.setAccessible(true);
        assertTrue(((ConcurrentMap<?, ?>) field.get(webApplicationStartFailureRetryController)).size() == 0);
        field.setAccessible(false);
    }

    @Test
    public void testRecordFailureWithContextPath() throws Exception {
        expect(this.servletContext.getContextPath()).andReturn(CONTEXT_PATH_1).andReturn(CONTEXT_PATH_1).andReturn(CONTEXT_PATH_1).andReturn(
            CONTEXT_PATH_2);

        WebApplicationStartFailureRetryController webApplicationStartFailureRetryController = createWebApplicationStartFailureRetryController();
        StandardWebApplication failedWebApplication = createStandardWebApplication(this.bundle1, webApplicationStartFailureRetryController);
        webApplicationStartFailureRetryController.recordFailure(failedWebApplication);
        webApplicationStartFailureRetryController.recordFailure(failedWebApplication);
        webApplicationStartFailureRetryController.recordFailure(createStandardWebApplication(this.bundle1, webApplicationStartFailureRetryController));
        webApplicationStartFailureRetryController.recordFailure(createStandardWebApplication(this.bundle1, webApplicationStartFailureRetryController));

        Field field = webApplicationStartFailureRetryController.getClass().getDeclaredField(FIELD_NAME);
        field.setAccessible(true);

        ConcurrentMap<?, ?> failures = (ConcurrentMap<?, ?>) field.get(webApplicationStartFailureRetryController);
        assertTrue(failures.size() == 2);
        assertTrue(((Set<?>) failures.get(CONTEXT_PATH_1)).size() == 2);
        assertTrue(((Set<?>) failures.get(CONTEXT_PATH_2)).size() == 1);

        webApplicationStartFailureRetryController.clear();
        assertTrue(((ConcurrentMap<?, ?>) field.get(webApplicationStartFailureRetryController)).size() == 0);

        field.setAccessible(false);
    }

    @Test
    public void testRetryFailuresNoContextPath() throws Exception {
        expect(this.servletContext.getContextPath()).andReturn(null).anyTimes();

        WebApplicationStartFailureRetryController webApplicationStartFailureRetryController = createWebApplicationStartFailureRetryController();
        StandardWebApplication failedWebApplication = createStandardWebApplication(this.bundle1, webApplicationStartFailureRetryController);
        webApplicationStartFailureRetryController.recordFailure(failedWebApplication);

        Field field = webApplicationStartFailureRetryController.getClass().getDeclaredField(FIELD_NAME);
        field.setAccessible(true);

        ConcurrentMap<?, ?> failures = (ConcurrentMap<?, ?>) field.get(webApplicationStartFailureRetryController);
        assertTrue(failures.size() == 0);

        webApplicationStartFailureRetryController.retryFailures(failedWebApplication);

        failures = (ConcurrentMap<?, ?>) field.get(webApplicationStartFailureRetryController);
        assertTrue(failures.size() == 0);

        field.setAccessible(false);
    }

    @Test
    public void testRetryFailuresTwoBundlesWithDifferentContextPaths() throws Exception {
        expect(this.servletContext.getContextPath()).andReturn(CONTEXT_PATH_1).andReturn(CONTEXT_PATH_2).andReturn(CONTEXT_PATH_2);
        expect(this.bundle2.getBundleId()).andReturn(3L).anyTimes();

        WebApplicationStartFailureRetryController webApplicationStartFailureRetryController = createWebApplicationStartFailureRetryController();
        webApplicationStartFailureRetryController.recordFailure(createStandardWebApplication(this.bundle1, webApplicationStartFailureRetryController));
        StandardWebApplication failedWebApplication = createStandardWebApplication(this.bundle2, webApplicationStartFailureRetryController);
        webApplicationStartFailureRetryController.recordFailure(failedWebApplication);

        Field field = webApplicationStartFailureRetryController.getClass().getDeclaredField(FIELD_NAME);
        field.setAccessible(true);

        ConcurrentMap<?, ?> failures = (ConcurrentMap<?, ?>) field.get(webApplicationStartFailureRetryController);
        assertTrue(failures.size() == 2);
        assertTrue(((Set<?>) failures.get(CONTEXT_PATH_1)).size() == 1);
        assertTrue(((Set<?>) failures.get(CONTEXT_PATH_2)).size() == 1);

        webApplicationStartFailureRetryController.retryFailures(failedWebApplication);

        failures = (ConcurrentMap<?, ?>) field.get(webApplicationStartFailureRetryController);
        assertTrue(failures.size() == 1);
        assertTrue(((Set<?>) failures.get(CONTEXT_PATH_1)).size() == 1);

        field.setAccessible(false);
    }

    @Test
    public void testRetryFailuresTwoBundlesWithSameContextPaths() throws Exception {
        createExpectations(true);

        WebApplicationStartFailureRetryController webApplicationStartFailureRetryController = createWebApplicationStartFailureRetryController();
        webApplicationStartFailureRetryController.recordFailure(createStandardWebApplication(this.bundle1, webApplicationStartFailureRetryController));
        StandardWebApplication failedWebApplication = createStandardWebApplication(this.bundle2, webApplicationStartFailureRetryController);
        webApplicationStartFailureRetryController.recordFailure(failedWebApplication);

        checkExpectations(webApplicationStartFailureRetryController, failedWebApplication);
    }

    @Test
    public void testRetryFailuresOneBundleWithTwoFailures() throws Exception {
        createExpectations(false);

        WebApplicationStartFailureRetryController webApplicationStartFailureRetryController = createWebApplicationStartFailureRetryController();
        webApplicationStartFailureRetryController.recordFailure(createStandardWebApplication(this.bundle1, webApplicationStartFailureRetryController));
        StandardWebApplication failedWebApplication = createStandardWebApplication(this.bundle1, webApplicationStartFailureRetryController);
        webApplicationStartFailureRetryController.recordFailure(failedWebApplication);

        checkExpectations(webApplicationStartFailureRetryController, failedWebApplication);
    }

    @Test
    public void testRetryFailuresOneBundleWithTwoEqualFailures() throws Exception {
        expect(this.servletContext.getContextPath()).andReturn(CONTEXT_PATH_1).anyTimes();
        expect(this.bundle1.getBundleId()).andReturn(3L).anyTimes();

        WebApplicationStartFailureRetryController webApplicationStartFailureRetryController = createWebApplicationStartFailureRetryController();
        StandardWebApplication failedWebApplication = createStandardWebApplication(this.bundle1, webApplicationStartFailureRetryController);
        webApplicationStartFailureRetryController.recordFailure(failedWebApplication);
        webApplicationStartFailureRetryController.recordFailure(failedWebApplication);

        Field field = webApplicationStartFailureRetryController.getClass().getDeclaredField(FIELD_NAME);
        field.setAccessible(true);

        ConcurrentMap<?, ?> failures = (ConcurrentMap<?, ?>) field.get(webApplicationStartFailureRetryController);
        assertTrue(failures.size() == 1);
        assertTrue(((Set<?>) failures.get(CONTEXT_PATH_1)).size() == 1);

        webApplicationStartFailureRetryController.retryFailures(failedWebApplication);

        failures = (ConcurrentMap<?, ?>) field.get(webApplicationStartFailureRetryController);
        assertTrue(failures.size() == 0);

        field.setAccessible(false);
    }

    private WebApplicationStartFailureRetryController createWebApplicationStartFailureRetryController() {
        replay(this.bundle1, this.bundle2, this.extender, this.handle, this.container, this.thisBundleContext, this.servletContext);
        this.eventManager = new EventManager(this.thisBundleContext);
        return new WebApplicationStartFailureRetryController();
    }

    private StandardWebApplication createStandardWebApplication(Bundle bundle,
        WebApplicationStartFailureRetryController webApplicationStartFailureRetryController) {
        return new StandardWebApplication(bundle, this.extender, this.handle, this.container, this.eventManager,
            webApplicationStartFailureRetryController, this.thisBundleContext);
    }

    private void createExpectations(boolean twoBundles) {
        expect(this.servletContext.getContextPath()).andReturn(CONTEXT_PATH_1).anyTimes();
        expect(this.bundle1.getBundleId()).andReturn(3L).anyTimes();
        if (twoBundles) {
            expect(this.bundle2.getBundleId()).andReturn(2L).anyTimes();
        }
        expect(this.bundle1.getSymbolicName()).andReturn(SYMBOLIC_NAME);
        expect(this.bundle1.getHeaders()).andReturn(new Hashtable<String, String>());
        BundleContext bundleContext = createMock(BundleContext.class);
        expect(this.bundle1.getBundleContext()).andReturn(bundleContext);
        this.container.startWebApplication(this.handle);
        expectLastCall();
    }

    private void checkExpectations(WebApplicationStartFailureRetryController webApplicationStartFailureRetryController,
        StandardWebApplication failedWebApplication) throws NoSuchFieldException, IllegalAccessException {
        Field field = webApplicationStartFailureRetryController.getClass().getDeclaredField(FIELD_NAME);
        field.setAccessible(true);

        ConcurrentMap<?, ?> failures = (ConcurrentMap<?, ?>) field.get(webApplicationStartFailureRetryController);
        assertTrue(failures.size() == 1);
        assertTrue(((Set<?>) failures.get(CONTEXT_PATH_1)).size() == 2);

        webApplicationStartFailureRetryController.retryFailures(failedWebApplication);

        failures = (ConcurrentMap<?, ?>) field.get(webApplicationStartFailureRetryController);
        assertTrue(failures.size() == 0);

        field.setAccessible(false);
    }

}
