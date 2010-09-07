/*******************************************************************************
 * Copyright (c) 2010 SAP AG
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
 *   Violeta Georgieva - initial contribution
 *******************************************************************************/

package org.eclipse.gemini.web.internal.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.gemini.web.internal.url.DirTransformer.DirTransformerCallback;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.io.PathReference;
import org.junit.Test;

public class DirTransformerTest {

    private static final String WEB_INF = "WEB-INF";

    private static final String META_INF = "META-INF";

    private static final String WEB_XML = "web.xml";

    private static final String MANIFEST_MF = "MANIFEST.MF";

    private static final String HEADER_1 = "Manifest-Version: 1.0";

    private static final String HEADER_2 = "Class-Path: ";

    private static final String HEADER_3 = "Custom-Header: test";

    private static final String SOURCE_URL = "file:target/test-classes/web-app-dir";

    private static final String TARGET_URL = "file:target/test-classes/temp/web-app-dir";

    @Test
    public void testTransformManifestAdded() throws Exception {
        URL directory = new URL(SOURCE_URL);
        URL tempDirectory = new URL(TARGET_URL);

        // Create content
        PathReference webAppDir = new PathReference(directory.getPath());
        PathReference webXml = webAppDir.newChild(WEB_INF + File.separator + WEB_XML);
        webXml.createFile();
        PathReference manifest = webAppDir.newChild(JarFile.MANIFEST_NAME);

        final List<PathReference> transformedFiles = new ArrayList<PathReference>();
        DirTransformer transformer = new DirTransformer(new DirTransformerCallback() {

            public boolean transformFile(InputStream inputStream, PathReference toFile) throws IOException {
                transformedFiles.add(toFile);
                return false;
            }
        });

        PathReference tempWebAppDir = new PathReference(tempDirectory.getPath());
        transformer.transform(directory, tempDirectory, false);
        assertEquals(1, transformedFiles.size());
        assertTrue(!manifest.exists());
        assertTrue(!transformedFiles.contains(manifest));
        assertTrue(tempWebAppDir.delete(true));
        transformedFiles.clear();

        transformer.transform(directory, tempDirectory, true);
        assertEquals(2, transformedFiles.size());
        assertTrue(!manifest.exists());
        assertTrue(transformedFiles.contains(tempWebAppDir.newChild(JarFile.MANIFEST_NAME)));

        assertTrue(tempWebAppDir.delete(true));
        assertTrue(webAppDir.delete(true));
    }

    @Test
    public void testTransformManifestChanged() throws Exception {
        URL directory = new URL(SOURCE_URL);
        URL tempDirectory = new URL(TARGET_URL);

        // Create content
        PathReference webAppDir = new PathReference(directory.getPath());
        PathReference manifest = webAppDir.newChild(JarFile.MANIFEST_NAME);
        manifest.getParent().createDirectory();
        createManifest(manifest.toFile(), HEADER_1, HEADER_2);

        DirTransformer transformer = new DirTransformer(new DirTransformerCallback() {

            public boolean transformFile(InputStream inputStream, PathReference toFile) throws IOException {
                if (MANIFEST_MF.equals(toFile.getName()) && META_INF.equals(toFile.getParent().getName())) {
                    toFile.getParent().createDirectory();
                    createManifest(toFile.toFile(), HEADER_3);
                    return true;
                } else {
                    return false;
                }
            }
        });

        transformer.transform(directory, tempDirectory);
        PathReference tempWebAppDir = new PathReference(tempDirectory.getPath());
        checkManifest(tempWebAppDir.newChild(JarFile.MANIFEST_NAME).toFile());

        assertTrue(tempWebAppDir.delete(true));
        assertTrue(webAppDir.delete(true));
    }

    @Test
    public void testTransformNoChanges() throws Exception {
        URL directory = new URL(SOURCE_URL);
        URL tempDirectory = new URL(TARGET_URL);

        // Create content
        PathReference webAppDir = new PathReference(directory.getPath());
        PathReference webXml = webAppDir.newChild(WEB_INF + File.separator + WEB_XML);
        webXml.createFile();

        DirTransformer transformer;
        try {
            transformer = new DirTransformer(null);
        } catch (Exception e) {
            assertTrue("Callback must not be null".equals(e.getMessage()));
        }

        transformer = new DirTransformer(new DirTransformerCallback() {

            public boolean transformFile(InputStream inputStream, PathReference toFile) throws IOException {
                return false;
            }
        });

        transformer.transform(directory, tempDirectory);
        PathReference tempWebAppDir = new PathReference(tempDirectory.getPath());
        assertDirsSame(webAppDir.toFile(), tempWebAppDir.toFile());

        assertTrue(tempWebAppDir.delete(true));
        assertTrue(webAppDir.delete(true));
    }

    private void assertDirsSame(File source, File destination) throws IOException {
        assertEquals(source.getName(), destination.getName());
        assertEquals(source.length(), destination.length());

        File[] sourceFiles = source.listFiles();
        File[] destinationFiles = destination.listFiles();
        assertEquals(sourceFiles.length, destinationFiles.length);

        for (int i = 0; i < sourceFiles.length; i++) {
            File sourceFile = sourceFiles[i];
            File destinationFile = destinationFiles[i];
            assertEquals(sourceFile.getName(), destinationFile.getName());
            assertEquals(sourceFile.length(), destinationFile.length());
        }
    }

    private void checkManifest(File manifestFile) throws IOException {
        InputStream is = new FileInputStream(manifestFile);
        Manifest manifest = new Manifest(is);
        Attributes attr = manifest.getMainAttributes();
        String value = attr.getValue("Custom-Header");
        assertEquals("test", value);
        assertEquals(1, attr.size());
        IOUtils.closeQuietly(is);
    }

    private void createManifest(File manifest, String... headers) throws IOException {
        OutputStream outputStream = null;
        PrintWriter writer = null;
        try {
            outputStream = new FileOutputStream(manifest);
            writer = new PrintWriter(manifest);
            for (String header : headers) {
                writer.println(header);
            }
            writer.println();
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(outputStream);
        }
    }
}
