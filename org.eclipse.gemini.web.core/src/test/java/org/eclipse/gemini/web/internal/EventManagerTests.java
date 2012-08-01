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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.service.event.StubEventAdmin;
import org.eclipse.virgo.teststubs.osgi.support.ObjectClassFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class EventManagerTests {

    private final StubBundleContext bundleContext = new StubBundleContext();

    private EventManager eventManager;

    private final StubEventAdmin eventAdmin = new StubEventAdmin();

    private final StubBundle bundle = new StubBundle();

    private final String contextPath = "/myWebApp";

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
        this.eventManager.sendDeploying(this.bundle, null, this.contextPath);
        Event event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/DEPLOYING", 10);
        assertNotNull(event);
        assertProperties(event);
    }

    @Test
    public void deployed() {
        this.eventManager.sendDeployed(this.bundle, null, this.contextPath);
        Event event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/DEPLOYED", 10);
        assertNotNull(event);
        assertProperties(event);
    }

    @Test
    public void undeploying() {
        this.eventManager.sendUndeploying(this.bundle, null, this.contextPath);
        Event event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/UNDEPLOYING", 10);
        assertNotNull(event);
        assertProperties(event);
    }

    @Test
    public void undeployed() {
        this.eventManager.sendUndeployed(this.bundle, null, this.contextPath);
        Event event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/UNDEPLOYED", 10);
        assertNotNull(event);
        assertProperties(event);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void failed() {
        Exception failure = new Exception();
        Set<Long> collidingBundles = new HashSet<Long>();
        collidingBundles.add(2L);
        collidingBundles.add(3L);
        this.eventManager.sendFailed(this.bundle, null, this.contextPath, failure, "/path", collidingBundles);
        Event event = this.eventAdmin.awaitSendingOfEvent("org/osgi/service/web/FAILED", 10);
        assertNotNull(event);
        assertProperties(event);

        assertEquals("/path", event.getProperty("collision"));

        Collection<Long> cb = (Collection<Long>) event.getProperty("collision.bundles");
        assertEquals(2, cb.size());
        assertTrue(cb.contains(2L));
        assertTrue(cb.contains(3L));
    }

    private void assertProperties(Event event) {
        String contextPath = (String) event.getProperty("context.path");
        assertEquals("/myWebApp", contextPath);
    }
}
