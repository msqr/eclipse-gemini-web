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

package org.eclipse.gemini.web.internal.url;

import static org.eclipse.gemini.web.internal.ManifestAsserts.assertIncludesExport;
import static org.eclipse.gemini.web.internal.ManifestAsserts.assertIncludesImport;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gemini.web.core.InstallationOptions;
import org.eclipse.gemini.web.internal.WebContainerUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

public class SpecificationWebBundleManifestTransformerTests {

    private URL sourceURL;

    private SpecificationWebBundleManifestTransformer transformer;

    @Before
    public void before() throws MalformedURLException {
        this.sourceURL = new URL("file:src/test/resources/simple-war.war");
        this.transformer = new SpecificationWebBundleManifestTransformer();
    }

    @Test
    public void transformFromNothing() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));

        assertEquals(new Version("0"), manifest.getBundleVersion());
        assertNotNull(manifest.getBundleSymbolicName());
    }

    @Test
    public void testSpecifyBasicDetails() throws Exception {
        String symbolicName = "my.bundle";
        String version = "1.2.3";

        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");
        options.put(Constants.BUNDLE_SYMBOLICNAME, symbolicName);
        options.put(Constants.BUNDLE_VERSION, version);
        options.put(Constants.BUNDLE_CLASSPATH, "foo,bar");
        options.put(Constants.BUNDLE_MANIFESTVERSION, "3");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));

        assertEquals(symbolicName, manifest.getBundleSymbolicName().getSymbolicName());
        assertEquals(new Version(version), manifest.getBundleVersion());
        assertEquals(3, manifest.getBundleManifestVersion());
        assertTrue(manifest.getBundleClasspath().contains("foo"));
        assertTrue(manifest.getBundleClasspath().contains("bar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpecifyBundleSymbolicNameForWAB() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");
        options.put(Constants.BUNDLE_SYMBOLICNAME, "my.bundle");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.setBundleVersion(new Version("0")); // implies WAB

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpecifyBundleVersionForWAB() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");
        options.put(Constants.BUNDLE_VERSION, "0");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.setBundleVersion(new Version("0")); // implies WAB

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpecifyBundleManifestVersionForWAB() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");
        options.put(Constants.BUNDLE_MANIFESTVERSION, "2");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.setBundleVersion(new Version("0")); // implies WAB

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpecifyBundleManifestVersion() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");
        options.put(Constants.BUNDLE_MANIFESTVERSION, "0");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpecifyBundleManifestVersionStringValue() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");
        options.put(Constants.BUNDLE_MANIFESTVERSION, "version");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
    }

    @Test
    public void testSpecifyImports() throws Exception {

        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");
        options.put(Constants.IMPORT_PACKAGE, "p;version=\"1.2.3\",q;version=\"1.2.4\",r;version=\"1.2.3\"");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));

        assertIncludesImport("p", new Version("1.2.3"), manifest);
        assertIncludesImport("q", new Version("1.2.4"), manifest);
        assertIncludesImport("r", new Version("1.2.3"), manifest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpecifyImportsForWAB() throws Exception {

        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");
        options.put(Constants.IMPORT_PACKAGE, "p;version=\"1.2.3\",q;version=\"1.2.4\",r;version=\"1.2.3\""); // implies
                                                                                                              // WAB

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.getImportPackage().addImportedPackage("r").getAttributes().put(Constants.VERSION_ATTRIBUTE, "1.2.0");

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
    }

    @Test
    public void testSpecifyExports() throws Exception {

        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");
        options.put(Constants.EXPORT_PACKAGE, "p;version=\"1.2.3\",q,r;version=\"1.2.3\"");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.getExportPackage().addExportedPackage("r").getAttributes().put(Constants.VERSION_ATTRIBUTE, "1.2.0");

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));

        assertIncludesExport("p", new Version("1.2.3"), manifest);
        assertIncludesExport("q", Version.emptyVersion, manifest);
        assertIncludesExport("r", new Version("1.2.3"), manifest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpecifyExportsForWAB() throws Exception {

        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");
        options.put(Constants.EXPORT_PACKAGE, "p;version=\"1.2.3\",q;version=\"1.2.4\",r;version=\"1.2.3\"");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.getExportPackage().addExportedPackage("r").getAttributes().put(Constants.VERSION_ATTRIBUTE, "1.2.0");
        manifest.setBundleVersion(new Version("0")); // implies WAB

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
    }

    @Test
    public void testSpecifyWebContextPath() throws Exception {

        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/foo");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));

        assertEquals("/foo", manifest.getHeader(WebContainerUtils.HEADER_WEB_CONTEXT_PATH));
    }

    @Test
    public void testSpecifyWebContextPathForWAB() throws Exception {

        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/foo");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.setBundleVersion(new Version("0")); // implies WAB

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));

        assertEquals("/foo", manifest.getHeader(WebContainerUtils.HEADER_WEB_CONTEXT_PATH));
    }

    @Test
    public void testSpecifyWebContextPathWithMissingSlash() throws Exception {

        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "foo");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));

        assertEquals("/foo", manifest.getHeader(WebContainerUtils.HEADER_WEB_CONTEXT_PATH));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpecifyEmptyWebContextPath() throws Exception {

        Map<String, String> options = new HashMap<>();
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, " ");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoNotSpecifyWebContextPath() throws Exception {

        Map<String, String> options = new HashMap<>();

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpecifyBundleClassPathForWAB() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put(Constants.BUNDLE_CLASSPATH, "foo");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.setBundleVersion(new Version("0")); // implies WAB
        manifest.setHeader(Constants.BUNDLE_CLASSPATH, "bar");

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpecifyBundleClassPathWithEmptyEntry() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put(Constants.BUNDLE_CLASSPATH, "foo,,foo");
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.setHeader(Constants.BUNDLE_CLASSPATH, "bar");

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpecifyBundleClassPathWithSlash() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put(Constants.BUNDLE_CLASSPATH, "foo,/,foo");
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.setHeader(Constants.BUNDLE_CLASSPATH, "bar");

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
    }

    @Test
    public void testSpecifyBundleClassPath() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put(Constants.BUNDLE_CLASSPATH, "foo,bar,path/");
        options.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/");

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.setHeader(Constants.BUNDLE_CLASSPATH, "bar,par");

        this.transformer.transform(manifest, this.sourceURL, new InstallationOptions(options), WebContainerUtils.isWebApplicationBundle(manifest));
        assertEquals("bar,par,foo,path", manifest.getHeader(Constants.BUNDLE_CLASSPATH));
    }
}
