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

package org.eclipse.gemini.web.test.extender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import javax.servlet.ServletContext;

import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.eclipse.virgo.test.framework.TestFrameworkUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

@RunWith(OsgiTestRunner.class)
public class StreamBasedExtenderTests {

    private BundleContext context;

    @Before
    public void before() {
        this.context = TestFrameworkUtils.getBundleContextForTestClass(getClass());
    }

    @Test
    public void testInstallAfterExtender() throws BundleException, Exception {
        Bundle extender = installExtender();
        Bundle war = null;
        try {
            extender.start();

            war = installWarBundle();
            war.start();

            validateURL("http://localhost:8080/simple-war/index.html");

            war.stop();

            validateNotFound("http://localhost:8080/simple-war/index.html");
        } finally {
            extender.uninstall();
            if (war != null) {
                war.uninstall();
            }
        }
    }

    @Test
    public void testInstallBeforeExtender() throws BundleException, Exception {
        Bundle war = installWarBundle();
        war.start();

        Bundle extender = installExtender();
        try {
            extender.start();

            validateURL("http://localhost:8080/simple-war/index.html");

            war.stop();

            validateNotFound("http://localhost:8080/simple-war/index.html");
        } finally {
            extender.uninstall();
            war.uninstall();
        }
    }

    @Test
    public void installWithCustomContextPath() throws BundleException, Exception {
        Bundle war = null;
        Bundle extender = installExtender();
        try {
            extender.start();

            war = installWarBundle("?Web-ContextPath=/custom");
            war.start();

            validateURL("http://localhost:8080/custom/index.html");

            war.stop();

            validateNotFound("http://localhost:8080/custom/index.html");
        } finally {
            extender.uninstall();
            if (war != null) {
                war.uninstall();
            }
        }
    }

    @Test
    public void installWithContextPathSpecifiedInManifest() throws BundleException, Exception {
        Bundle war = null;
        Bundle extender = installExtender();
        try {
            extender.start();

            war = this.context.installBundle("webbundle:file:src/test/resources/specified-context-path-1.war");
            war.start();

            validateURL("http://localhost:8080/specified/test");

            war.stop();

            validateNotFound("http://localhost:8080/specified/test");
        } finally {
            extender.uninstall();
            if (war != null) {
                war.uninstall();
            }
        }
    }

    @Test
    public void installWithDuplicateContextRoots() throws BundleException, Exception {
        Bundle war1 = null;
        Bundle war2 = null;

        Bundle extender = installExtender();

        try {
            extender.start();

            war1 = this.context.installBundle("webbundle:file:src/test/resources/specified-context-path-1.war");
            war1.start();

            validateURL("http://localhost:8080/specified/test");

            Collection<ServiceReference<ServletContext>> serviceReferences = this.context.getServiceReferences(ServletContext.class, null);
            assertNotNull(serviceReferences);
            assertEquals(1, serviceReferences.size());

            war2 = this.context.installBundle("webbundle:file:src/test/resources/specified-context-path-2.war");
            war2.start();

            serviceReferences = this.context.getServiceReferences(ServletContext.class, null);
            assertNotNull(serviceReferences);
            assertEquals(1, serviceReferences.size());

            war2.stop();
            war2.uninstall();

            validateURL("http://localhost:8080/specified/test");

            serviceReferences = this.context.getServiceReferences(ServletContext.class, null);
            assertNotNull(serviceReferences);
            assertEquals(1, serviceReferences.size());

            war1.stop();

            validateNotFound("http://localhost:8080/specified/test");

            serviceReferences = this.context.getServiceReferences(ServletContext.class, null);
            assertNotNull(serviceReferences);
            assertEquals(0, serviceReferences.size());
        } finally {
            extender.uninstall();
            if (war1 != null) {
                war1.uninstall();
            }
        }
    }

    @Test
    public void installWarWithNoManifest() throws BundleException, Exception {
        Bundle war = null;
        Bundle extender = installExtender();
        try {
            extender.start();

            war = this.context.installBundle("webbundle:file:src/test/resources/no-manifest.war?Web-ContextPath=/no-manifest");
            war.start();

            validateURL("http://localhost:8080/no-manifest/test");

            war.stop();

            validateNotFound("http://localhost:8080/no-manifest/test");
        } finally {
            extender.uninstall();
            if (war != null) {
                war.uninstall();
            }
        }
    }

    /**
     * This test expects IllegalArgumentException rather than BundleException because for stream based installation the
     * exception is thrown when the stream is opened and BundleException is not a valid exception for URL.openStream.
     * 
     * @throws BundleException but shouldn't
     * @throws Exception possibly
     */
    @Test(expected = IllegalArgumentException.class)
    public void installWithInvalidBundleVersion() throws BundleException, Exception {
        Bundle extender = installExtender();
        try {
            extender.start();

            installWarBundle("?Bundle-Version=1.2.3.a - b");
        } finally {
            extender.uninstall();
        }
    }

    /**
     * This test expects IllegalArgumentException rather than BundleException because for stream based installation the
     * exception is thrown when the stream is opened and BundleException is not a valid exception for URL.openStream.
     * 
     * @throws BundleException but shouldn't
     * @throws Exception possibly
     */
    @Test(expected = IllegalArgumentException.class)
    public void installWithInvalidBundleManifestVersionx() throws BundleException, Exception {
        Bundle extender = installExtender();
        try {
            extender.start();

            installWarBundle("?Bundle-ManifestVersion=x");
        } finally {
            extender.uninstall();
        }
    }

    /**
     * This test expects IllegalArgumentException rather than BundleException because for stream based installation the
     * exception is thrown when the stream is opened and BundleException is not a valid exception for URL.openStream.
     * 
     * @throws BundleException but shouldn't
     * @throws Exception possibly
     */
    @Test(expected = IllegalArgumentException.class)
    public void installWithInvalidBundleManifestVersion0() throws BundleException, Exception {
        Bundle extender = installExtender();
        try {
            extender.start();

            installWarBundle("?Bundle-ManifestVersion=0");
        } finally {
            extender.uninstall();
        }
    }

    /**
     * This test expects IllegalArgumentException rather than BundleException because for stream based installation the
     * exception is thrown when the stream is opened and BundleException is not a valid exception for URL.openStream.
     * 
     * @throws BundleException but shouldn't
     * @throws Exception possibly
     */
    @Test(expected = IllegalArgumentException.class)
    public void installWithInvalidBundleManifestVersion1() throws BundleException, Exception {
        Bundle extender = installExtender();
        try {
            extender.start();

            installWarBundle("?Bundle-ManifestVersion=1");
        } finally {
            extender.uninstall();
        }
    }

    @Test
    public void installWithBundleManifestVersion2() throws BundleException, Exception {
        Bundle war = null;
        Bundle extender = installExtender();
        try {
            extender.start();

            war = installWarBundle("?Bundle-ManifestVersion=2&Web-ContextPath=/simple-war");

            war.start();

            validateURL("http://localhost:8080/simple-war/index.html");

            war.stop();

            validateNotFound("http://localhost:8080/simple-war/index.html");
        } finally {
            extender.uninstall();
            if (war != null) {
                war.uninstall();
            }
        }
    }

    private Bundle installExtender() throws BundleException {
        return this.context.installBundle("file:../org.eclipse.gemini.web.extender/target/classes");
    }

    private Bundle installWarBundle() throws BundleException {
        return installWarBundle("?Web-ContextPath=/simple-war");
    }

    private Bundle installWarBundle(String suffix) throws BundleException {
        InputStream in = null;
        try {
            URL url = new URL("webbundle:file:../org.eclipse.gemini.web.core/target/resources/simple-war.war" + suffix);
            in = url.openStream();
        } catch (MalformedURLException e) {
            fail("Unexpected exception " + e.getMessage());
        } catch (IOException e) {
            fail("Unexpected exception " + e.getMessage());
        }

        try {
            return this.context.installBundle("simple-war.war", in);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void validateURL(String path) throws MalformedURLException, IOException, InterruptedException {
        InputStream stream = openInputStream(path);
        assertNotNull(stream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } finally {
            reader.close();
        }
    }

    private void validateNotFound(String path) throws Exception {
        URL url = new URL(path);
        try {
            InputStream stream = url.openConnection().getInputStream();
            stream.close();
            fail("URL '" + path + "' is still deployed");
        } catch (IOException e) {
        }
    }

    private InputStream openInputStream(String path) throws MalformedURLException, InterruptedException {
        URL url = new URL(path);
        InputStream stream = null;
        for (int i = 0; i < 5; i++) {
            try {
                stream = url.openConnection().getInputStream();
            } catch (IOException e) {
                Thread.sleep(1000);
            }
        }
        return stream;
    }
}
