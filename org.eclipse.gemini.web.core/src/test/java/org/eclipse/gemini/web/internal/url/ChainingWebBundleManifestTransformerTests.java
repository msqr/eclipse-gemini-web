/*******************************************************************************
 * Copyright (c) 2012 SAP AG
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

package org.eclipse.gemini.web.internal.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import org.eclipse.gemini.web.core.InstallationOptions;
import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.junit.Test;

public class ChainingWebBundleManifestTransformerTests {

    @Test
    public void testTransformNoInstallationOptions() throws IOException {
        ChainingWebBundleManifestTransformer chainingWebBundleManifestTransformer = new ChainingWebBundleManifestTransformer(
            new WebBundleManifestTransformer() {

                @Override
                public void transform(BundleManifest manifest, URL sourceURL, InstallationOptions options, boolean webBundle) throws IOException {
                    assertNull(manifest);
                    assertNull(sourceURL);
                    assertNotNull(options);
                    assertTrue(webBundle);
                }
            });
        chainingWebBundleManifestTransformer.transform(null, null, null, true);
    }

    @Test
    public void testTransform() throws IOException {
        final BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();
        final URL url = new URL("file:foo.war");
        final InstallationOptions installationOptions = new InstallationOptions(Collections.<String, String> emptyMap());

        ChainingWebBundleManifestTransformer chainingWebBundleManifestTransformer = new ChainingWebBundleManifestTransformer(
            new WebBundleManifestTransformer() {

                @Override
                public void transform(BundleManifest manifest, URL sourceURL, InstallationOptions options, boolean webBundle) throws IOException {
                    assertEquals(bundleManifest, manifest);
                    assertEquals(url, sourceURL);
                    assertEquals(installationOptions, options);
                    assertTrue(webBundle);
                }
            });
        chainingWebBundleManifestTransformer.transform(bundleManifest, url, installationOptions, true);
    }

}
