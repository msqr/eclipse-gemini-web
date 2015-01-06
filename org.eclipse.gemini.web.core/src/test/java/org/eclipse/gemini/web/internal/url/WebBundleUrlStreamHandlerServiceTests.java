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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.junit.Test;

public class WebBundleUrlStreamHandlerServiceTests {

    @Test
    public void testOpenWarConnection() throws Exception {
        WebBundleUrl url = new TestWarUrl("file:target/resources/simple-war.war?Web-ContextPath=/", null);
        URLConnection connection = url.toURL().openConnection();
        assertNotNull(connection);

        try (InputStream inputStream = connection.getInputStream(); JarInputStream jarInputStream = new JarInputStream(inputStream);) {
            Manifest manifest = jarInputStream.getManifest();

            if (manifest != null) {
                Attributes mainAttributes = manifest.getMainAttributes();
                Set<Entry<Object, Object>> entrySet = mainAttributes.entrySet();
                for (Entry<Object, Object> entry : entrySet) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
            }
        }
    }

    @Test
    public void testOpenDirConnection() throws Exception {
        URL directory = new URL("file:target/test-classes/web-app-dir");
        URL tempDirectory = new URL("file:target/test-classes/temp/web-app-dir");

        // Create content
        Path webAppDir = Paths.get(directory.getPath());
        Path webXml = webAppDir.resolve("WEB-INF").resolve("web.xml");
        Files.createDirectories(webXml.getParent());
        Files.createFile(webXml);

        Path signatureFile = webAppDir.resolve("META-INF").resolve("signature.SF");
        Files.createDirectories(signatureFile.getParent());
        Files.createFile(signatureFile);

        // There is no Manifest in the directory
        // Expectation: Manifest will have Web-ContextPath header
        WebBundleUrl url = new TestWarUrl("file:target/test-classes/web-app-dir?Web-ContextPath=/test", null);
        DirTransformingURLConnection connection = (DirTransformingURLConnection) url.toURL().openConnection();
        assertNotNull(connection);
        connection.setTransformedURL(tempDirectory);
        checkContent(connection, "/test", webXml);
        assertTrue(FileUtils.deleteDirectory(Paths.get(tempDirectory.getPath())));

        // Create content
        Path manifest = webAppDir.resolve(JarFile.MANIFEST_NAME);
        Files.createDirectories(manifest.getParent());
        createManifest(manifest, "Manifest-Version: 1.0", "Class-Path: ");

        // There is Manifest in the directory with basic headers
        // Expectation: Manifest will have Web-ContextPath header
        url = new TestWarUrl("file:target/test-classes/web-app-dir?Web-ContextPath=/test1", null);
        connection = (DirTransformingURLConnection) url.toURL().openConnection();
        assertNotNull(connection);
        connection.setTransformedURL(tempDirectory);
        checkContent(connection, "/test1", webXml);
        assertTrue(FileUtils.deleteDirectory(Paths.get(tempDirectory.getPath())));

        // Create content
        createManifest(manifest, "Manifest-Version: 1.0", "Class-Path: ", "Web-ContextPath: /test2");

        // There is Manifest in the directory with basic headers +
        // Web-ContextPath header
        // Expectation: Manifest will have Web-ContextPath header
        url = new TestWarUrl("file:target/test-classes/web-app-dir", null);
        connection = (DirTransformingURLConnection) url.toURL().openConnection();
        assertNotNull(connection);
        connection.setTransformedURL(tempDirectory);
        checkContent(connection, "/test2", webXml);
        assertTrue(FileUtils.deleteDirectory(Paths.get(tempDirectory.getPath())));

        assertTrue(FileUtils.deleteDirectory(Paths.get(directory.getPath())));
    }

    private void checkContent(URLConnection connection, String contextPath, Path webXml) throws Exception {
        try (InputStream inputStream = connection.getInputStream();) {
            assertNotNull(inputStream);
        }

        Path webAppDir = Paths.get(connection.getURL().getPath());
        // Check Manifest
        try (InputStream is = Files.newInputStream(webAppDir.resolve(JarFile.MANIFEST_NAME));) {
            Manifest manifest = new Manifest(is);
            Attributes mainAttributes = manifest.getMainAttributes();
            Set<Entry<Object, Object>> entrySet = mainAttributes.entrySet();
            for (Entry<Object, Object> entry : entrySet) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
                if ("Web-ContextPath".equals(entry.getKey().toString())) {
                    assertTrue(contextPath.equals(entry.getValue().toString()));
                }
            }
        }

        // Check web.xml
        assertEquals(webXml.toFile().length(), webAppDir.resolve("WEB-INF").resolve("web.xml").toFile().length());

        // Check signature file
        Path signatureFile = webAppDir.resolve("META-INF").resolve("signature.SF");
        assertTrue(Files.notExists(signatureFile));
    }

    private void createManifest(Path manifest, String... headers) throws Exception {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(manifest, StandardCharsets.UTF_8));) {
            for (String header : headers) {
                writer.println(header);
            }
            writer.println();
        }
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
