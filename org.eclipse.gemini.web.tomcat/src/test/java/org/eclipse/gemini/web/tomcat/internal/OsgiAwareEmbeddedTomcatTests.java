/*******************************************************************************
 * Copyright (c) 2011 SAP AG
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

package org.eclipse.gemini.web.tomcat.internal;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;

import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.ObjectFactoryBuilder;

import org.apache.catalina.LifecycleException;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.framework.StubServiceRegistration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.service.packageadmin.PackageAdmin;

public class OsgiAwareEmbeddedTomcatTests {

    private StubBundleContext bundleContext;

    private PackageAdmin packageAdmin;

    @Before
    public void setUp() {
        this.bundleContext = new StubBundleContext();
        this.packageAdmin = createMock(PackageAdmin.class);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testInitNaming() {
        OsgiAwareEmbeddedTomcat tomcat = createTomcat();
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        assertTrue(Boolean.parseBoolean(System.getProperty(OsgiAwareEmbeddedTomcat.CATALINA_USE_NAMING)));

        this.bundleContext.addProperty(OsgiAwareEmbeddedTomcat.USE_NAMING, OsgiAwareEmbeddedTomcat.TOMCAT_NAMING_ENABLED);
        tomcat = createTomcat();
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        assertTrue(Boolean.parseBoolean(System.getProperty(OsgiAwareEmbeddedTomcat.CATALINA_USE_NAMING)));

        this.bundleContext.addProperty(OsgiAwareEmbeddedTomcat.USE_NAMING, OsgiAwareEmbeddedTomcat.NAMING_DISABLED);
        tomcat = createTomcat();
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        assertTrue(!Boolean.parseBoolean(System.getProperty(OsgiAwareEmbeddedTomcat.CATALINA_USE_NAMING)));

        this.bundleContext.addProperty(OsgiAwareEmbeddedTomcat.USE_NAMING, OsgiAwareEmbeddedTomcat.OSGI_NAMING_ENABLED);
        tomcat = createTomcat();
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        assertTrue(Boolean.parseBoolean(System.getProperty(OsgiAwareEmbeddedTomcat.CATALINA_USE_NAMING)));
    }

    @Test
    public void testInitNamingSystemProperty() {
        OsgiAwareEmbeddedTomcat tomcat = createTomcat();
        System.setProperty(OsgiAwareEmbeddedTomcat.USE_NAMING, OsgiAwareEmbeddedTomcat.TOMCAT_NAMING_ENABLED);
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        assertTrue(Boolean.parseBoolean(System.getProperty(OsgiAwareEmbeddedTomcat.CATALINA_USE_NAMING)));

        tomcat = createTomcat();
        System.setProperty(OsgiAwareEmbeddedTomcat.USE_NAMING, OsgiAwareEmbeddedTomcat.NAMING_DISABLED);
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        assertTrue(!Boolean.parseBoolean(System.getProperty(OsgiAwareEmbeddedTomcat.CATALINA_USE_NAMING)));

        tomcat = createTomcat();
        System.setProperty(OsgiAwareEmbeddedTomcat.USE_NAMING, OsgiAwareEmbeddedTomcat.OSGI_NAMING_ENABLED);
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        assertTrue(Boolean.parseBoolean(System.getProperty(OsgiAwareEmbeddedTomcat.CATALINA_USE_NAMING)));
    }

    @Test
    public void testInitNamingOSGIServices() {
        this.bundleContext.addProperty(OsgiAwareEmbeddedTomcat.USE_NAMING, OsgiAwareEmbeddedTomcat.OSGI_NAMING_ENABLED);

        OsgiAwareEmbeddedTomcat tomcat = createTomcat();
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        List<StubServiceRegistration<Object>> serviceRegistrations = this.bundleContext.getServiceRegistrations();
        assertTrue(serviceRegistrations.size() == 3);
        checkOSGIServicesForNamingAreRegistered(serviceRegistrations);
    }

    private void checkOSGIServicesForNamingAreRegistered(List<StubServiceRegistration<Object>> serviceRegistrations) {
        if (serviceRegistrations != null) {
            for (int i = 0; i < serviceRegistrations.size(); i++) {
                Dictionary<String, Object> properties = serviceRegistrations.get(i).getProperties();
                List<String> objectClasses = Arrays.asList((String[]) properties.get(Constants.OBJECTCLASS));
                assertTrue(objectClasses.contains(ObjectFactory.class.getName()) || objectClasses.contains(ObjectFactoryBuilder.class.getName())
                    || objectClasses.contains(InitialContextFactory.class.getName()));
            }
        } else {
            fail("There are no service registrations.");
        }
    }

    private OsgiAwareEmbeddedTomcat createTomcat() {
        return new OsgiAwareEmbeddedTomcat(this.bundleContext, this.packageAdmin);
    }

}
