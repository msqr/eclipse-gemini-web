
package org.eclipse.gemini.web.tomcat.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.catalina.LifecycleException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;

public class OsgiAwareEmbeddedTomcatTest {

    private static final String CATALINA_USE_NAMING = "catalina.useNaming";

    private BundleContext bundleContext;

    private PackageAdmin packageAdmin;

    @Before
    public void setUp() {
        this.bundleContext = createMock(BundleContext.class);
        this.packageAdmin = createMock(PackageAdmin.class);
    }

    @After
    public void tearDown() {
        verify(this.bundleContext);
    }

    @Test
    public void testInitNaming() {
        expect(this.bundleContext.getProperty(OsgiAwareEmbeddedTomcat.USE_NAMING)).andReturn(null).andReturn("tomcat").andReturn("disabled");
        replay(this.bundleContext);

        OsgiAwareEmbeddedTomcat tomcat = createTomcat();
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        assertTrue(Boolean.parseBoolean(System.getProperty(CATALINA_USE_NAMING)));

        tomcat = createTomcat();
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        assertTrue(Boolean.parseBoolean(System.getProperty(CATALINA_USE_NAMING)));

        tomcat = createTomcat();
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        assertTrue(!Boolean.parseBoolean(System.getProperty(CATALINA_USE_NAMING)));
    }

    @Test
    public void testInitNamingSystemProperty() {
        expect(this.bundleContext.getProperty(OsgiAwareEmbeddedTomcat.USE_NAMING)).andReturn(null).andReturn(null);
        replay(this.bundleContext);

        OsgiAwareEmbeddedTomcat tomcat = createTomcat();
        System.setProperty(OsgiAwareEmbeddedTomcat.USE_NAMING, "tomcat");
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        assertTrue(Boolean.parseBoolean(System.getProperty(CATALINA_USE_NAMING)));

        tomcat = createTomcat();
        System.setProperty(OsgiAwareEmbeddedTomcat.USE_NAMING, "disabled");
        try {
            tomcat.init();
        } catch (LifecycleException e) {
            fail(e.getMessage());
        }
        assertTrue(!Boolean.parseBoolean(System.getProperty(CATALINA_USE_NAMING)));
    }

    private OsgiAwareEmbeddedTomcat createTomcat() {
        return new OsgiAwareEmbeddedTomcat(this.bundleContext, this.packageAdmin);
    }

}
