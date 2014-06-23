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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gemini.web.core.InstallationOptions;
import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
import org.eclipse.gemini.web.internal.WebContainerUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleSymbolicName;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

public final class DefaultsWebBundleManifestTransformer implements WebBundleManifestTransformer {

    private static final int MINIMUM_BUNDLE_MANIFEST_VERSION = 2;

    private static final String WEB_INF_CLASSES = "WEB-INF/classes";

    @Override
    public void transform(BundleManifest manifest, URL sourceURL, InstallationOptions options, boolean webBundle) throws IOException {
        if (!webBundle || options.getDefaultWABHeaders()) {
            applyDefaultBundleSymbolicName(sourceURL, manifest);
            applyDefaultBundleManifestVersion(manifest);
            applyBundleClassPath(sourceURL, manifest);
            applyImportPackage(manifest);
        }
    }

    private void applyImportPackage(BundleManifest manifest) {
        addImportInNecessary("javax.servlet", new Version("2.5"), manifest);
        addImportInNecessary("javax.servlet.annotation", new Version("2.6"), manifest);
        addImportInNecessary("javax.servlet.descriptor", new Version("2.6"), manifest);
        addImportInNecessary("javax.servlet.http", new Version("2.5"), manifest);
        addImportInNecessary("javax.servlet.jsp", new Version("2.1"), manifest);
        addImportInNecessary("javax.servlet.jsp.el", new Version("2.1"), manifest);
        addImportInNecessary("javax.servlet.jsp.tagext", new Version("2.1"), manifest);
        addImportInNecessary("javax.el", new Version("1.0"), manifest);
        addImportInNecessary("javax.websocket", new Version("1.0"), manifest);
        addImportInNecessary("javax.websocket.server", new Version("1.0"), manifest);
    }

    private void addImportInNecessary(String packageName, Version version, BundleManifest manifest) {
        List<ImportedPackage> pkgs = manifest.getImportPackage().getImportedPackages();
        for (ImportedPackage pkg : pkgs) {
            if (pkg.getPackageName().equals(packageName)) {
                return;
            }
        }
        ImportedPackage packageImport = manifest.getImportPackage().addImportedPackage(packageName);
        packageImport.getAttributes().put(Constants.VERSION_ATTRIBUTE, version.toString());
    }

    private void applyBundleClassPath(URL source, BundleManifest manifest) throws IOException {
        List<String> bundleClassPath = manifest.getBundleClasspath();

        if (!bundleClassPath.contains(WEB_INF_CLASSES)) {
            bundleClassPath.add(0, WEB_INF_CLASSES);
        }

        final List<String> entries = new ArrayList<String>();
        WebBundleScanner scanner = new WebBundleScanner(source, new WebBundleScannerCallback() {

            @Override
            public void classFound(String entry) {
            }

            @Override
            public void jarFound(String entry) {
                entries.add(entry);
            }
        });
        scanner.scanWar();

        for (String entry : entries) {
            if (!bundleClassPath.contains(entry)) {
                bundleClassPath.add(entry);
            }
        }
    }

    private void applyDefaultBundleManifestVersion(BundleManifest manifest) {
        if (manifest.getBundleManifestVersion() < MINIMUM_BUNDLE_MANIFEST_VERSION) {
            manifest.setBundleManifestVersion(MINIMUM_BUNDLE_MANIFEST_VERSION);
        }
    }

    private void applyDefaultBundleSymbolicName(URL source, BundleManifest manifest) {
        BundleSymbolicName bsn = manifest.getBundleSymbolicName();
        if (bsn.getSymbolicName() == null) {
            bsn.setSymbolicName(WebContainerUtils.createDefaultBundleSymbolicName(source));
        }
    }
}
