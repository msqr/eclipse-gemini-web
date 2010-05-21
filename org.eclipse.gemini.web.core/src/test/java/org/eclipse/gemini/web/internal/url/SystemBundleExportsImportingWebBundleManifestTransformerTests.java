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

package org.eclipse.gemini.web.internal.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;


import org.eclipse.gemini.web.core.InstallationOptions;
import org.eclipse.gemini.web.internal.WebContainerUtils;
import org.eclipse.gemini.web.internal.url.SystemBundleExportsImportingWebBundleManifestTransformer;
import org.eclipse.virgo.util.osgi.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;

public class SystemBundleExportsImportingWebBundleManifestTransformerTests {

    @Test
    public void emptyTransform() throws IOException {
        Map<String, VersionRange> exports = Collections.<String, VersionRange> emptyMap();
        SystemBundleExportsImportingWebBundleManifestTransformer transformer = new SystemBundleExportsImportingWebBundleManifestTransformer(exports);
        transformer.transform(null, null, null, false);
    }

    @Test
    public void nothingToImport() throws IOException {
        Map<String, VersionRange> exports = Collections.<String, VersionRange> emptyMap();
        SystemBundleExportsImportingWebBundleManifestTransformer transformer = new SystemBundleExportsImportingWebBundleManifestTransformer(exports);

        InstallationOptions options = new InstallationOptions(Collections.<String, String> emptyMap());
        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();
        transformer.transform(bundleManifest, null, options, WebContainerUtils.isWebApplicationBundle(bundleManifest));

        assertEquals(0, bundleManifest.getImportPackage().getImportedPackages().size());
    }

    @Test
    public void importOfExports() throws IOException {
        Map<String, VersionRange> exports = new HashMap<String, VersionRange>();
        VersionRange vr1 = new VersionRange("[1.2.3,2.0.0)");
        VersionRange vr2 = new VersionRange("[2.0.0,3.0.0]");
        exports.put("a", vr1);
        exports.put("b", vr2);

        SystemBundleExportsImportingWebBundleManifestTransformer transformer = new SystemBundleExportsImportingWebBundleManifestTransformer(exports);

        Map<String, String> optionsMap = new HashMap<String, String>();
        InstallationOptions options = new InstallationOptions(optionsMap);
        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();
        transformer.transform(bundleManifest, null, options, WebContainerUtils.isWebApplicationBundle(bundleManifest));

        List<ImportedPackage> importedPackages = bundleManifest.getImportPackage().getImportedPackages();
        assertEquals(2, importedPackages.size());

        for (ImportedPackage importedPackage : importedPackages) {
            if (importedPackage.getPackageName().equals("a")) {
                assertEquals(vr1, importedPackage.getVersion());
            } else if (importedPackage.getPackageName().equals("b")) {
                assertEquals(vr2, importedPackage.getVersion());
            } else {
                fail("Unexpected import of package " + importedPackage);
            }
        }
    }

    @Test
    public void existingImportsShouldNotBeOverridden() throws IOException {
        Map<String, VersionRange> exports = new HashMap<String, VersionRange>();
        VersionRange vr1 = new VersionRange("[1.2.3,2.0.0)");
        VersionRange vr2 = new VersionRange("[2.0.0,3.0.0]");
        exports.put("a", vr1);
        exports.put("b", vr2);

        SystemBundleExportsImportingWebBundleManifestTransformer transformer = new SystemBundleExportsImportingWebBundleManifestTransformer(exports);

        Map<String, String> optionsMap = new HashMap<String, String>();
        InstallationOptions options = new InstallationOptions(optionsMap);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();
        bundleManifest.getImportPackage().addImportedPackage("a").setVersion(new VersionRange("[1.0.0,1.0.0]"));

        transformer.transform(bundleManifest, null, options, WebContainerUtils.isWebApplicationBundle(bundleManifest));

        List<ImportedPackage> importedPackages = bundleManifest.getImportPackage().getImportedPackages();
        assertEquals(1, importedPackages.size());
    }

    @Test
    public void packagesInWarShouldNotBeImported() throws IOException {
        Map<String, VersionRange> exports = new HashMap<String, VersionRange>();
        exports.put("from.classes", new VersionRange("[1.0.0,1.0.0]"));
        exports.put("from.lib", new VersionRange("[1.0.0,1.0.0]"));
        exports.put("javax.sql", new VersionRange("[0,0]"));

        SystemBundleExportsImportingWebBundleManifestTransformer transformer = new SystemBundleExportsImportingWebBundleManifestTransformer(exports);

        Map<String, String> optionsMap = new HashMap<String, String>();
        InstallationOptions options = new InstallationOptions(optionsMap);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        transformer.transform(bundleManifest, new File("src/test/resources/contains-system-bundle-package.war").toURI().toURL(), options,
            WebContainerUtils.isWebApplicationBundle(bundleManifest));

        List<ImportedPackage> importedPackages = bundleManifest.getImportPackage().getImportedPackages();
        assertEquals(1, importedPackages.size());
        assertEquals("javax.sql", importedPackages.get(0).getPackageName());
    }
}
