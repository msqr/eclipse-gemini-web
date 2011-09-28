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

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.gemini.web.core.InstallationOptions;
import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;


public final class SystemBundleExportsImportingWebBundleManifestTransformer implements WebBundleManifestTransformer {

    private final Map<String, VersionRange> systemBundleExports;

    private final PackagesInWarScanner warPackagesScanner = new PackagesInWarScanner();

    public SystemBundleExportsImportingWebBundleManifestTransformer(Map<String, VersionRange> systemBundleExports) {
        this.systemBundleExports = systemBundleExports;
    }

    public void transform(BundleManifest manifest, URL sourceURL, InstallationOptions options, boolean webBundle) throws IOException {
        if (!webBundle || options.getDefaultWABHeaders()) {
            addImportsForSystemBundleExports(manifest, this.warPackagesScanner.getPackagesContainedInWar(sourceURL));
        }
    }

    protected void addImportsForSystemBundleExports(BundleManifest bundleManifest, Set<String> packagesInWar) {
        for (Entry<String, VersionRange> exportedPackage : this.systemBundleExports.entrySet()) {
            String packageName = exportedPackage.getKey();
            if (!packagesInWar.contains(packageName) && PackageMergeUtils.findImportedPackage(bundleManifest, packageName) == null) {
                bundleManifest.getImportPackage().addImportedPackage(packageName).setVersion(exportedPackage.getValue());
            }
        }
    }
}
