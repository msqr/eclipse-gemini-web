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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gemini.web.core.InstallationOptions;
import org.eclipse.gemini.web.internal.WebContainerUtils;
import org.junit.Test;
import org.osgi.framework.Constants;

public class InstallationOptionsTests {

    @Test
    public void testBundleManifestVersion() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.BUNDLE_MANIFESTVERSION, "2");
        InstallationOptions options = new InstallationOptions(map);
        assertEquals("2", options.getBundleManifestVersion());
    }

    @Test
    public void testBundleClassPath() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.BUNDLE_CLASSPATH, "foo,bar");
        InstallationOptions options = new InstallationOptions(map);

        assertNotNull(options.getBundleClassPath());
        assertEquals("foo,bar", options.getBundleClassPath());
    }

    @Test
    public void testEmptyMap() {
        InstallationOptions options = new InstallationOptions(Collections.<String, String> emptyMap());

        assertNull(options.getBundleSymbolicName());
        assertNull(options.getBundleVersion());
        assertNull(options.getBundleManifestVersion());
        assertNull(options.getBundleClassPath());
        assertNull(options.getImportPackageDeclaration());
        assertNull(options.getExportPackageDeclaration());
        assertNull(options.getWebContextPath());
        assertNull(options.getWebJSPExtractLocation());
    }

    @Test
    public void testPopulatedMap() {
        String symbolicName = "sym.name";
        String bundleManifestVersion = "2";
        String bundleVersion = "1.0.0";

        String importPackage = "p,q";
        String exportPackage = "r";

        String contextPath = "/test";
        String extractLocation = "/tmp";

        Map<String, String> map = new HashMap<String, String>();

        map.put(Constants.BUNDLE_SYMBOLICNAME, symbolicName);
        map.put(Constants.BUNDLE_MANIFESTVERSION, bundleManifestVersion);
        map.put(Constants.BUNDLE_VERSION, bundleVersion);
        map.put(Constants.IMPORT_PACKAGE, importPackage);
        map.put(Constants.EXPORT_PACKAGE, exportPackage);

        map.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, contextPath);
        map.put(WebContainerUtils.HEADER_WEB_JSP_EXTRACT_LOCATION, extractLocation);

        InstallationOptions options = new InstallationOptions(map);
        assertEquals(symbolicName, options.getBundleSymbolicName());
        assertEquals(bundleManifestVersion, options.getBundleManifestVersion());
        assertEquals(bundleVersion, options.getBundleVersion());
        assertEquals(importPackage, options.getImportPackageDeclaration());
        assertEquals(exportPackage, options.getExportPackageDeclaration());
        assertEquals(contextPath, options.getWebContextPath());
        assertEquals(extractLocation, options.getWebJSPExtractLocation());
    }

    @Test
    public void testNonCamelCaseOption() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("bundle-manifestversion", "3");
        InstallationOptions options = new InstallationOptions(map);
        assertEquals("3", options.getBundleManifestVersion());
    }
}
