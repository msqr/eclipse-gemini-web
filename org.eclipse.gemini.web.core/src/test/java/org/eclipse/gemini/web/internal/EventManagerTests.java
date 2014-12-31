/*******************************************************************************
 * Copyright (c) 2009, 2014 VMware Inc.
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_EXTENDER_BUNDLE;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_EXTENDER_BUNDLE_ID;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_EXTENDER_BUNDLE_SYMBOLICNAME;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_EXTENDER_BUNDLE_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.service.event.StubEventAdmin;
import org.eclipse.virgo.test.stubs.support.ObjectClassFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;

public class EventManagerTests {

    private final StubBundleContext bundleContext = new StubBundleContext();

    private EventManager eventManager;

    private final StubEventAdmin eventAdmin = new StubEventAdmin();

    private final StubBundle bundle = new StubBundle();

    private final static String CONTEXT_PATH = "/myWebApp";

    @Before
    public void initialise() {
        this.bundleContext.registerService(EventAdmin.class.getName(), this.eventAdmin, null);
        this.bundleContext.addFilter(new ObjectClassFilter(EventAdmin.class));

        this.eventManager = new EventManager(this.bundleContext);
        this.eventManager.start();
    }

    @After
    public void shutdown() {
        this.eventManager.stop();
    }

    @Test
    public void deploying() {
        this.eventManager.sendDeploying(this.bundle, null, CONTEXT_PATH);
        Event event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/DEPLOYING", 10);
        assertNotNull(event);
        assertProperties(event);
    }

    @Test
    public void deployed() {
        this.eventManager.sendDeployed(this.bundle, null, CONTEXT_PATH);
        Event event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/DEPLOYED", 10);
        assertNotNull(event);
        assertProperties(event);
    }

    @Test
    public void undeploying() {
        this.eventManager.sendUndeploying(this.bundle, null, CONTEXT_PATH);
        Event event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/UNDEPLOYING", 10);
        assertNotNull(event);
        assertProperties(event);
    }

    @Test
    public void undeployed() {
        this.eventManager.sendUndeployed(this.bundle, null, CONTEXT_PATH);
        Event event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/UNDEPLOYED", 10);
        assertNotNull(event);
        assertProperties(event);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void failed() {
        Exception failure = new Exception();
        Set<Long> collidingBundles = new HashSet<>();
        collidingBundles.add(2L);
        collidingBundles.add(3L);
        this.eventManager.sendFailed(this.bundle, null, CONTEXT_PATH, failure, "/path", collidingBundles);
        Event event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/FAILED", 10);
        assertNotNull(event);
        assertProperties(event);

        assertEquals("/path", event.getProperty("collision"));

        Collection<Long> cb = (Collection<Long>) event.getProperty("collision.bundles");
        assertEquals(2, cb.size());
        assertTrue(cb.contains(2L));
        assertTrue(cb.contains(3L));
    }

    @Test
    public void deployedBundleWithDifferentSymbolicNames() {
        Bundle bundle = createBundleMock();

        replay(bundle);

        this.eventManager.sendDeployed(bundle, null, CONTEXT_PATH);
        Event event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/DEPLOYED", 10);
        assertNotNull(event);
        assertNull(event.getProperty(EventConstants.BUNDLE_SYMBOLICNAME));

        this.eventManager.sendDeployed(bundle, null, CONTEXT_PATH);
        event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/DEPLOYED", 10);
        assertNotNull(event);
        assertEquals("symbolic-name", event.getProperty(EventConstants.BUNDLE_SYMBOLICNAME));

        verify(bundle);
    }

    @Test
    public void deployedBundleWithExtenderBundle() {
        Bundle extenderBundle = createBundleMock();

        replay(extenderBundle);

        this.eventManager.sendDeployed(this.bundle, extenderBundle, CONTEXT_PATH);
        Event event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/DEPLOYED", 10);
        assertNotNull(event);
        assertNull(event.getProperty(EVENT_PROPERTY_EXTENDER_BUNDLE_SYMBOLICNAME));
        assertExtenderProperties(event, extenderBundle);

        this.eventManager.sendDeployed(this.bundle, extenderBundle, CONTEXT_PATH);
        event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/DEPLOYED", 10);
        assertNotNull(event);
        assertEquals("symbolic-name", event.getProperty(EVENT_PROPERTY_EXTENDER_BUNDLE_SYMBOLICNAME));
        assertExtenderProperties(event, extenderBundle);

        verify(extenderBundle);
    }

    private Bundle createBundleMock() {
        Bundle bundle = createMock(Bundle.class);
        expect(bundle.getSymbolicName()).andReturn(null).andReturn("symbolic-name").andReturn("symbolic-name");
        expect(bundle.getBundleId()).andReturn(1L).times(2);
        expect(bundle.getVersion()).andReturn(new Version("1.0.0")).times(2);
        return bundle;
    }

    private void assertExtenderProperties(Event event, Bundle extenderBundle) {
        assertEquals(extenderBundle, event.getProperty(EVENT_PROPERTY_EXTENDER_BUNDLE));
        assertEquals(1L, event.getProperty(EVENT_PROPERTY_EXTENDER_BUNDLE_ID));
        assertEquals(new Version("1.0.0"), event.getProperty(EVENT_PROPERTY_EXTENDER_BUNDLE_VERSION));
    }

    private void assertProperties(Event event) {
        String contextPath = (String) event.getProperty("context.path");
        assertEquals("/myWebApp", contextPath);
    }
}
