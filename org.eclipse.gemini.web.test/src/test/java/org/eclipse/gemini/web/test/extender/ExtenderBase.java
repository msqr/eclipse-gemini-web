/*******************************************************************************
 * Copyright (c) 2014, 2015 SAP AG
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
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import javax.servlet.ServletContext;

import junit.framework.Assert;

import org.eclipse.virgo.test.framework.TestFrameworkUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

public abstract class ExtenderBase {

    protected static final String REQUEST_URL = "http://localhost:8080/simple-war/index.html";

    private final BundleContext context = TestFrameworkUtils.getBundleContextForTestClass(getClass());

    protected BundleContext getBundleContext() {
        return this.context;
    }

    protected void installBeforeExtender() throws BundleException, Exception {
        Bundle war = installWarBundle("?Web-ContextPath=/simple-war");
        assertNotNull(war);
        war.start();

        Bundle extender = installExtender();
        try {
            extender.start();

            validateURL(REQUEST_URL, null);

            war.stop();

            validateNotFound(REQUEST_URL);
        } finally {
            uninstallBundle(extender);
            uninstallBundle(war);
        }
    }

    protected void installWithDuplicateContextRoots() throws BundleException, Exception {
        Bundle war1 = null;
        Bundle war2 = null;

        Bundle extender = installExtender();

        try {
            extender.start();

            war1 = installBundle("specified-context-path-1.war", "webbundle:file:src/test/resources/specified-context-path-1.war", "");
            war1.start();

            validateURL("http://localhost:8080/specified/test", null);

            Collection<ServiceReference<ServletContext>> serviceReferences = getBundleContext().getServiceReferences(ServletContext.class, null);
            assertNotNull(serviceReferences);
            assertEquals(1, serviceReferences.size());

            war2 = installBundle("specified-context-path-2.war", "webbundle:file:src/test/resources/specified-context-path-2.war", "");
            war2.start();

            serviceReferences = getBundleContext().getServiceReferences(ServletContext.class, null);
            assertNotNull(serviceReferences);
            assertEquals(1, serviceReferences.size());

            war2.stop();
            uninstallBundle(war2);

            validateURL("http://localhost:8080/specified/test", null);

            serviceReferences = getBundleContext().getServiceReferences(ServletContext.class, null);
            assertNotNull(serviceReferences);
            assertEquals(1, serviceReferences.size());

            war1.stop();

            validateNotFound("http://localhost:8080/specified/test");

            serviceReferences = getBundleContext().getServiceReferences(ServletContext.class, null);
            assertNotNull(serviceReferences);
            assertEquals(0, serviceReferences.size());
        } finally {
            uninstallBundle(extender);
            uninstallBundle(war1);
        }
    }

    protected void installWar1(String suffix) throws BundleException {
        Bundle extender = installExtender();
        try {
            extender.start();

            installWarBundle(suffix);
        } finally {
            uninstallBundle(extender);
        }
    }

    protected void installWar2(String suffix, String requestURL) throws BundleException, MalformedURLException, IOException, InterruptedException,
        Exception {
        Bundle war = null;
        Bundle extender = installExtender();
        try {
            extender.start();

            war = installWarBundle(suffix);

            validateWar(requestURL, war);
        } finally {
            uninstallBundle(extender);
            uninstallBundle(war);
        }
    }

    protected void installWar3(String location, String bundleURL, String requestURL) throws BundleException, MalformedURLException, IOException,
        InterruptedException, Exception {
        Bundle war = null;
        Bundle extender = installExtender();
        try {
            extender.start();

            war = installBundle(location, bundleURL, "");
            validateWar(requestURL, war);
        } finally {
            uninstallBundle(extender);
            uninstallBundle(war);
        }
    }

    protected Bundle installExtender() throws BundleException {
        return this.context.installBundle("file:../org.eclipse.gemini.web.extender/target/classes");
    }

    protected void uninstallBundle(Bundle bundle) throws BundleException {
        if (bundle != null) {
            bundle.uninstall();
            bundle = null;
        }
    }

    protected abstract Bundle installWarBundle(String suffix) throws BundleException;

    protected abstract Bundle installBundle(String location, String bundleUrl, String suffix) throws BundleException;

    private void validateWar(String requestURL, Bundle war) throws BundleException, MalformedURLException, IOException, InterruptedException,
        Exception {
        assertNotNull(war);
        war.start();

        validateURL(requestURL, null);

        war.stop();

        validateNotFound(requestURL);
    }

    protected void validateURL(String path, String expectedResponse) throws MalformedURLException, IOException, InterruptedException {
        InputStream stream = openInputStream(path);
        assertNotNull(stream);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));) {
            if (expectedResponse == null) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } else {
                Assert.assertEquals(expectedResponse, reader.readLine());
            }
        }
    }

    private void validateNotFound(String path) throws Exception {
        URL url = new URL(path);
        try (InputStream stream = url.openConnection().getInputStream();) {
            fail("URL '" + path + "' is still deployed");
        } catch (IOException e) {
        }
    }

    protected InputStream openInputStream(String path) throws MalformedURLException, InterruptedException {
        URL url = new URL(path);
        InputStream stream = null;
        for (int i = 0; i < 5; i++) {
            try {
                stream = url.openConnection().getInputStream();
                break;
            } catch (IOException e) {
                Thread.sleep(1000);
            }
        }
        return stream;
    }

}