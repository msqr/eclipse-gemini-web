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

package org.eclipse.gemini.web.test.extender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

@RunWith(OsgiTestRunner.class)
public class UrlBasedExtenderTests extends ExtenderBase {

    private static final String EMPTY_WAR_WEB_BUNDLE_URL = "webbundle:file:src/test/resources/empty.war";

    @Test
    public void testInstallAfterExtender() throws BundleException, Exception {
        super.installWar2("?Web-ContextPath=/simple-war", REQUEST_URL);
    }

    @Test
    public void testInstallBeforeExtender() throws BundleException, Exception {
        super.installBeforeExtender();
    }

    @Test
    public void testInstallWithCustomContextPath() throws BundleException, Exception {
        super.installWar2("?Web-ContextPath=/custom", "http://localhost:8080/custom/index.html");
    }

    @Test
    public void testInstallWithContextPathSpecifiedInManifest() throws BundleException, Exception {
        super.installWar3(null, "webbundle:file:src/test/resources/specified-context-path-1.war", "http://localhost:8080/specified/test");
    }

    @Test
    public void testInstallWithDuplicateContextRoots() throws BundleException, Exception {
        super.installWithDuplicateContextRoots();
    }

    @Test
    public void testInstallWarWithNoManifest() throws BundleException, Exception {
        super.installWar3(null, "webbundle:file:src/test/resources/no-manifest.war?Web-ContextPath=/no-manifest",
            "http://localhost:8080/no-manifest/test");
    }

    @Test
    public void installWithBundleClassPath() throws BundleException, Exception {
        Bundle war = null;
        Bundle extender = installExtender();
        try {
            extender.start();

            war = installBundle(null, EMPTY_WAR_WEB_BUNDLE_URL, "?Bundle-ClassPath=WEB-INF/classes/&Web-ContextPath=/");

            String bundleClassPath = war.getHeaders().get("Bundle-ClassPath");
            assertEquals("WEB-INF/classes", bundleClassPath);
        } finally {
            uninstallBundle(extender);
            uninstallBundle(war);
        }
    }

    @Test
    public void installWithImportPackage() throws BundleException, Exception {
        Bundle war = null;
        Bundle extender = installExtender();
        try {
            extender.start();

            war = installBundle(null, EMPTY_WAR_WEB_BUNDLE_URL,
                "?Import-Package=javax.servlet;version=2.5,javax.servlet.http;version=2.5&Web-ContextPath=/");

            String importPackage = war.getHeaders().get("Import-Package");
            assertTrue(importPackage.startsWith("javax.servlet;version=\"2.5\",javax.servlet.http;version=\"2.5\""));
        } finally {
            uninstallBundle(extender);
            uninstallBundle(war);
        }
    }

    @Test(expected = BundleException.class)
    public void testInstallWithInvalidBundleVersion() throws BundleException, Exception {
        super.installWar1("?Bundle-Version=1.2.3.a - b");
    }

    @Test(expected = BundleException.class)
    public void testInstallWithInvalidBundleManifestVersionx() throws BundleException, Exception {
        super.installWar1("?Bundle-ManifestVersion=x");
    }

    @Test(expected = BundleException.class)
    public void testInstallWithInvalidBundleManifestVersion0() throws BundleException, Exception {
        super.installWar1("?Bundle-ManifestVersion=0");
    }

    @Test(expected = BundleException.class)
    public void testInstallWithBadParameter() throws BundleException, Exception {
        super.installWar1("?Bundle-ManifestVersion");
    }

    @Test(expected = BundleException.class)
    public void testInstallWithInvalidBundleManifestVersion1() throws BundleException, Exception {
        super.installWar1("?Bundle-ManifestVersion=1");
    }

    @Test
    public void testInstallWithBundleManifestVersion2() throws BundleException, Exception {
        super.installWar2("?Bundle-ManifestVersion=2&Web-ContextPath=/simple-war", REQUEST_URL);
    }

    @Override
    protected Bundle installWarBundle(String suffix) throws BundleException {
        return installBundle(null, "webbundle:file:../org.eclipse.gemini.web.core/target/resources/simple-war.war", suffix);
    }

    @Override
    protected Bundle installBundle(String location, String bundleUrl, String suffix) throws BundleException {
        return getBundleContext().installBundle(bundleUrl + suffix);
    }

}
