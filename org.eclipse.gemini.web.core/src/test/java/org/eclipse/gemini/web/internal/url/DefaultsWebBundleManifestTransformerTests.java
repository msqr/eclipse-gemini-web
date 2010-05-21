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

import static org.eclipse.gemini.web.internal.ManifestAsserts.assertIncludesImport;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;


import org.eclipse.gemini.web.core.InstallationOptions;
import org.eclipse.gemini.web.internal.WebContainerUtils;
import org.eclipse.gemini.web.internal.url.DefaultsWebBundleManifestTransformer;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;


public class DefaultsWebBundleManifestTransformerTests {

    private DefaultsWebBundleManifestTransformer defaults = new DefaultsWebBundleManifestTransformer();

    private URL source;

    private InstallationOptions options = new InstallationOptions(Collections.<String, String>emptyMap());
    
    @Before
    public void before() throws MalformedURLException {
        source = new URL("file:src/test/resources/simple-war.war");
    }
    
    @Test
    public void testDefaultBundleManifestVersion() throws Exception{
        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        this.defaults.transform(manifest, source, options, WebContainerUtils.isWebApplicationBundle(manifest));
        assertEquals(2, manifest.getBundleManifestVersion());
        
    }
    
    @Test
    public void testBundleManifestVersionNotOverridden() throws Exception {
        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.setBundleManifestVersion(3);
        this.defaults.transform(manifest, source, options, WebContainerUtils.isWebApplicationBundle(manifest));
        assertEquals(3, manifest.getBundleManifestVersion());
        
    }
    
    @Test
    public void testDefaultBundleSymbolicName() throws Exception{
        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        this.defaults.transform(manifest, source, options, WebContainerUtils.isWebApplicationBundle(manifest));
        assertNotNull(manifest.getBundleSymbolicName());
        assertNotNull(manifest.getBundleSymbolicName().getSymbolicName());
    }
    
    @Test
    public void testBundleSymbolicNameNotOverridden() throws Exception{
        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.getBundleSymbolicName().setSymbolicName("bsn");
        this.defaults.transform(manifest, source, options, WebContainerUtils.isWebApplicationBundle(manifest));
        assertEquals("bsn", manifest.getBundleSymbolicName().getSymbolicName());
    }
    
    @Test
    public void testDefaultBundleClassPath() throws Exception {
        
        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        this.defaults.transform(manifest, source, options, WebContainerUtils.isWebApplicationBundle(manifest));
        List<String> bcp = manifest.getBundleClasspath();
        assertNotNull(bcp);
        assertEquals(4, bcp.size());
        assertEquals("WEB-INF/classes", bcp.get(0));
    }
    
    @Test
    public void testMergeBundleClassPath() throws Exception {
        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.getBundleClasspath().add("test");
        this.defaults.transform(manifest, source, options, WebContainerUtils.isWebApplicationBundle(manifest));
        List<String> bcp = manifest.getBundleClasspath();
        assertNotNull(bcp);
        assertEquals("WEB-INF/classes", bcp.get(0));
        assertEquals("test", bcp.get(1));
    }
    
    @Test
    public void testDefaultImportPackages() throws Exception {
        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        this.defaults.transform(manifest, source, options, WebContainerUtils.isWebApplicationBundle(manifest));
        
        assertIncludesImport("javax.servlet", new Version("2.5"), manifest);
        assertIncludesImport("javax.servlet.http", new Version("2.5"), manifest);
        assertIncludesImport("javax.servlet.jsp", new Version("2.1"), manifest);
        assertIncludesImport("javax.servlet.jsp.tagext", new Version("2.1"), manifest);
        assertIncludesImport("javax.servlet.jsp.el", new Version("2.1"), manifest);
        assertIncludesImport("javax.el", new Version("1.0"), manifest);
    }
    
}
