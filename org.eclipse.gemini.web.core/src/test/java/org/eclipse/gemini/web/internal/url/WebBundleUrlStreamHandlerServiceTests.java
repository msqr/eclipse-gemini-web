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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.io.PathReference;
import org.junit.Test;

public class WebBundleUrlStreamHandlerServiceTests {

    @Test
    public void testOpenWarConnection() throws Exception {
        WebBundleUrl url = new TestWarUrl("file:target/resources/simple-war.war?Web-ContextPath=/", null);
        URLConnection connection = url.toURL().openConnection();
        assertNotNull(connection);

        InputStream inputStream = connection.getInputStream();
        JarInputStream jarInputStream = new JarInputStream(inputStream);
        Manifest manifest = jarInputStream.getManifest();

        if (manifest != null) {
            Attributes mainAttributes = manifest.getMainAttributes();
            Set<Entry<Object, Object>> entrySet = mainAttributes.entrySet();
            for (Entry<Object, Object> entry : entrySet) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
    }

    @Test
    public void testOpenDirConnection() throws Exception {
        URL directory = new URL("file:target/test-classes/web-app-dir");
        URL tempDirectory = new URL("file:target/test-classes/temp/web-app-dir");

        // Create content
        PathReference webAppDir = new PathReference(directory.getPath());
        PathReference webXml = webAppDir.newChild("WEB-INF" + File.separator + "web.xml");
        webXml.createFile();

        PathReference signatureFile = webAppDir.newChild("META-INF" + File.separator + "signature.SF");
        signatureFile.createFile();

        // There is no Manifest in the directory
        // Expectation: Manifest will have Web-ContextPath header
        WebBundleUrl url = new TestWarUrl("file:target/test-classes/web-app-dir?Web-ContextPath=/test", null);
        DirTransformingURLConnection connection = (DirTransformingURLConnection) url.toURL().openConnection();
        assertNotNull(connection);
        connection.setTransformedURL(tempDirectory);
        checkContent(connection, "/test", webXml.toFile());
        assertTrue(FileSystemUtils.deleteRecursively(tempDirectory.getPath()));

        // Create content
        PathReference manifest = webAppDir.newChild(JarFile.MANIFEST_NAME);
        manifest.createFile();
        createManifest(manifest.toFile(), "Manifest-Version: 1.0", "Class-Path: ");

        // There is Manifest in the directory with basic headers
        // Expectation: Manifest will have Web-ContextPath header
        url = new TestWarUrl("file:target/test-classes/web-app-dir?Web-ContextPath=/test1", null);
        connection = (DirTransformingURLConnection) url.toURL().openConnection();
        assertNotNull(connection);
        connection.setTransformedURL(tempDirectory);
        checkContent(connection, "/test1", webXml.toFile());
        assertTrue(FileSystemUtils.deleteRecursively(tempDirectory.getPath()));

        // Create content
        createManifest(manifest.toFile(), "Manifest-Version: 1.0", "Class-Path: ", "Web-ContextPath: /test2");

        // There is Manifest in the directory with basic headers +
        // Web-ContextPath header
        // Expectation: Manifest will have Web-ContextPath header
        url = new TestWarUrl("file:target/test-classes/web-app-dir", null);
        connection = (DirTransformingURLConnection) url.toURL().openConnection();
        assertNotNull(connection);
        connection.setTransformedURL(tempDirectory);
        checkContent(connection, "/test2", webXml.toFile());
        assertTrue(FileSystemUtils.deleteRecursively(tempDirectory.getPath()));

        assertTrue(FileSystemUtils.deleteRecursively(directory.getPath()));
    }

    private void checkContent(URLConnection connection, String contextPath, File webXml) throws Exception {
        InputStream inputStream = connection.getInputStream();
        assertNotNull(inputStream);

        File webAppDir = new File(connection.getURL().getPath());
        // Check Manifest
        InputStream is = null;
        try {
            is = new FileInputStream(new File(webAppDir, JarFile.MANIFEST_NAME));
            Manifest manifest = new Manifest(is);

            if (manifest != null) {
                Attributes mainAttributes = manifest.getMainAttributes();
                Set<Entry<Object, Object>> entrySet = mainAttributes.entrySet();
                for (Entry<Object, Object> entry : entrySet) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                    if ("Web-ContextPath".equals(entry.getKey().toString())) {
                        assertTrue(contextPath.equals(entry.getValue().toString()));
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Check web.xml
        assertEquals(webXml.length(), new File(webAppDir, "WEB-INF" + File.separator + "web.xml").length());

        // Check signature file
        File signatureFile = new File(webAppDir, "META-INF" + File.separator + "signature.SF");
        assertTrue(!signatureFile.exists());
    }

    private void createManifest(File manifest, String... headers) throws Exception {
        OutputStream os = new FileOutputStream(manifest);
        PrintWriter writer = new PrintWriter(os);
        for (String header : headers) {
            writer.println(header);
        }
        writer.println();
        writer.close();
        IOUtils.closeQuietly(os);
    }

    private static class TestWarUrl extends WebBundleUrl {

        public TestWarUrl(String location, Map<String, String> options) throws MalformedURLException {
            super(location, options);
        }

        public TestWarUrl(URL url) {
            super(url);
        }

        @Override
        protected URLStreamHandler createURLStreamHandler() {
            return new WebBundleUrlStreamHandlerService(new SpecificationWebBundleManifestTransformer());
        }
    }
}
