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

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

@RunWith(OsgiTestRunner.class)
public class StreamBasedExtenderTests extends ExtenderBase {

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
    public void installWithContextPathSpecifiedInManifest() throws BundleException, Exception {
        super.installWar3("specified-context-path-1.war", "webbundle:file:src/test/resources/specified-context-path-1.war",
            "http://localhost:8080/specified/test");
    }

    @Test
    public void testInstallWithDuplicateContextRoots() throws BundleException, Exception {
        super.installWithDuplicateContextRoots();
    }

    @Test
    public void testInstallWarWithNoManifest() throws BundleException, Exception {
        super.installWar3("no-manifest.war", "webbundle:file:src/test/resources/no-manifest.war?Web-ContextPath=/no-manifest",
            "http://localhost:8080/no-manifest/test");
    }

    /**
     * This test expects IllegalArgumentException rather than BundleException because for stream based installation the
     * exception is thrown when the stream is opened and BundleException is not a valid exception for URL.openStream.
     *
     * @throws BundleException but shouldn't
     * @throws Exception possibly
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInstallWithInvalidBundleVersion() throws BundleException, Exception {
        super.installWar1("?Bundle-Version=1.2.3.a - b");
    }

    /**
     * This test expects IllegalArgumentException rather than BundleException because for stream based installation the
     * exception is thrown when the stream is opened and BundleException is not a valid exception for URL.openStream.
     *
     * @throws BundleException but shouldn't
     * @throws Exception possibly
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInstallWithInvalidBundleManifestVersionx() throws BundleException, Exception {
        super.installWar1("?Bundle-ManifestVersion=x");
    }

    /**
     * This test expects IllegalArgumentException rather than BundleException because for stream based installation the
     * exception is thrown when the stream is opened and BundleException is not a valid exception for URL.openStream.
     *
     * @throws BundleException but shouldn't
     * @throws Exception possibly
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInstallWithInvalidBundleManifestVersion0() throws BundleException, Exception {
        super.installWar1("?Bundle-ManifestVersion=0");
    }

    /**
     * This test expects IllegalArgumentException rather than BundleException because for stream based installation the
     * exception is thrown when the stream is opened and BundleException is not a valid exception for URL.openStream.
     *
     * @throws BundleException but shouldn't
     * @throws Exception possibly
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInstallWithInvalidBundleManifestVersion1() throws BundleException, Exception {
        super.installWar1("?Bundle-ManifestVersion=1");
    }

    @Test
    public void testInstallWithBundleManifestVersion2() throws BundleException, Exception {
        super.installWar2("?Bundle-ManifestVersion=2&Web-ContextPath=/simple-war", REQUEST_URL);
    }

    @Override
    protected Bundle installWarBundle(String suffix) throws BundleException {
        return installBundle("simple-war.war", "webbundle:file:../org.eclipse.gemini.web.core/target/resources/simple-war.war", suffix);
    }

    @Override
    protected Bundle installBundle(String location, String bundleUrl, String suffix) throws BundleException {
        URL url = null;
        try {
            url = new URL(bundleUrl + suffix);
        } catch (MalformedURLException e) {
            fail("Unexpected exception " + e.getMessage());
        }

        if (url != null) {
            try (InputStream in = url.openStream();) {
                return getBundleContext().installBundle(location, in);
            } catch (IOException e) {
                fail("Unexpected exception " + e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }
}
