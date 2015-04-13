/*******************************************************************************
 * Copyright (c) 2009, 2015 VMware Inc.
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

import java.util.List;
import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

final class PackageMergeUtils {

    private PackageMergeUtils() {
    }

    static void mergeImportPackage(BundleManifest manifest, String name, Map<String, String> attributes, Map<String, String> directives) {
        ImportedPackage packageImport = findImportedPackage(manifest, name);

        if (packageImport == null) {
            packageImport = manifest.getImportPackage().addImportedPackage(name);
        }

        packageImport.getAttributes().clear();
        packageImport.getAttributes().putAll(attributes);

        packageImport.getDirectives().clear();
        packageImport.getDirectives().putAll(directives);
    }

    static void mergeExportPackage(BundleManifest manifest, String name, Map<String, String> attributes, Map<String, String> directives) {
        String versionAttribute = attributes.get(Constants.VERSION_ATTRIBUTE);
        Version version = versionAttribute == null ? Version.emptyVersion : new Version(versionAttribute);

        ExportedPackage packageExport = findExportedPackage(manifest, name, version);

        if (packageExport == null) {
            packageExport = manifest.getExportPackage().addExportedPackage(name);
        }

        packageExport.getAttributes().clear();
        packageExport.getAttributes().putAll(attributes);

        packageExport.getDirectives().clear();
        packageExport.getDirectives().putAll(directives);
    }

    private static final ExportedPackage findExportedPackage(BundleManifest manifest, String packageName, Version version) {
        List<ExportedPackage> exportedPackages = manifest.getExportPackage().getExportedPackages();
        for (ExportedPackage exportedPackage : exportedPackages) {
            if (packageName.equals(exportedPackage.getPackageName()) && version.equals(exportedPackage.getVersion())) {
                return exportedPackage;
            }
        }
        return null;
    }

    static final ImportedPackage findImportedPackage(BundleManifest manifest, String packageName) {
        List<ImportedPackage> importedPackages = manifest.getImportPackage().getImportedPackages();
        for (ImportedPackage importedPackage : importedPackages) {
            if (packageName.equals(importedPackage.getPackageName())) {
                return importedPackage;
            }
        }
        return null;
    }
}
