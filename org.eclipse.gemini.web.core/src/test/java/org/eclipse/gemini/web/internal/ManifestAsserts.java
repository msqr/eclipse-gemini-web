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

package org.eclipse.gemini.web.internal;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

public final class ManifestAsserts {

    public static void assertIncludesImport(String packageName, Version version, BundleManifest manifest) {
        List<ImportedPackage> importedPackages = manifest.getImportPackage().getImportedPackages();
        for (ImportedPackage packageImport : importedPackages) {
            String range = packageImport.getAttributes().get(Constants.VERSION_ATTRIBUTE);
            VersionRange vr = range == null ? VersionRange.NATURAL_NUMBER_RANGE : new VersionRange(range);
            String importName = packageImport.getPackageName();
            if (importName.equals(packageName)) {
                if (vr.includes(version)) {
                    return;
                }
                fail("Package '" + packageName + "' not found at version '" + version + "'. Found range :" + vr);
            }
        }
        fail("Import-Package '" + packageName + "' not found at any version");
    }

    public static void assertIncludesExport(String packageName, Version version, BundleManifest manifest) {
        List<ExportedPackage> exportedPackages = manifest.getExportPackage().getExportedPackages();
        List<Version> nearMatches = new ArrayList<>();
        for (ExportedPackage packageExport : exportedPackages) {
            String v = packageExport.getAttributes().get(Constants.VERSION_ATTRIBUTE);
            Version pv = v == null ? Version.emptyVersion : new Version(v);
            if (packageExport.getPackageName().equals(packageName)) {
                if (pv.equals(version)) {
                    return;
                }
                nearMatches.add(pv);
            }
        }
        if (nearMatches.isEmpty()) {
            fail("Export-Package for '" + packageName + "' not found at any version");
        } else {
            fail("Export-Package for '" + packageName + "' not found at '" + version + "'. Found '" + nearMatches + "'");
        }
    }
}
