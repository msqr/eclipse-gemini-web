/*******************************************************************************
 * Copyright (c) 2010, 2015 SAP AG
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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.gemini.web.internal.url.DirTransformer.DirTransformerCallback;
import org.junit.Test;

public class DirTransformerTests {

    private static final String WEB_INF = "WEB-INF";

    private static final Path META_INF = Paths.get("META-INF");

    private static final String WEB_XML = "web.xml";

    private static final Path MANIFEST_MF = Paths.get("MANIFEST.MF");

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
        Path webAppDir = Paths.get(directory.getPath());
        Path webXml = webAppDir.resolve(WEB_INF).resolve(WEB_XML);
        Files.createDirectories(webXml.getParent());
        Files.createFile(webXml);
        Path manifest = webAppDir.resolve(JarFile.MANIFEST_NAME);

        final List<Path> transformedFiles = new ArrayList<>();
        DirTransformer transformer = new DirTransformer(new DirTransformerCallback() {

            @Override
            public boolean transformFile(InputStream inputStream, Path toFile) throws IOException {
                transformedFiles.add(toFile);
                return false;
            }
        });

        Path tempWebAppDir = Paths.get(tempDirectory.getPath());
        transformer.transform(directory, tempDirectory, false);
        assertEquals(1, transformedFiles.size());
        assertTrue(Files.notExists(manifest));
        assertTrue(!transformedFiles.contains(manifest));
        assertTrue(FileUtils.deleteDirectory(tempWebAppDir));
        transformedFiles.clear();

        transformer.transform(directory, tempDirectory, true);
        assertEquals(2, transformedFiles.size());
        assertTrue(Files.notExists(manifest));
        assertTrue(transformedFiles.contains(tempWebAppDir.resolve(JarFile.MANIFEST_NAME)));

        assertTrue(FileUtils.deleteDirectory(tempWebAppDir));
        assertTrue(FileUtils.deleteDirectory(webAppDir));
    }

    @Test
    public void testTransformManifestChanged() throws Exception {
        URL directory = new URL(SOURCE_URL);
        URL tempDirectory = new URL(TARGET_URL);

        // Create content
        Path webAppDir = Paths.get(directory.getPath());
        Path manifest = webAppDir.resolve(JarFile.MANIFEST_NAME);
        Files.createDirectories(manifest.getParent());
        createManifest(manifest, HEADER_1, HEADER_2);

        DirTransformer transformer = new DirTransformer(new DirTransformerCallback() {

            @Override
            public boolean transformFile(InputStream inputStream, Path toFile) throws IOException {
                if (MANIFEST_MF.equals(toFile.getFileName()) && META_INF.equals(toFile.getParent().getFileName())) {
                    Files.createDirectories(toFile.getParent());
                    createManifest(toFile, HEADER_3);
                    return true;
                }
                return false;
            }
        });

        transformer.transform(directory, tempDirectory);
        Path tempWebAppDir = Paths.get(tempDirectory.getPath());
        checkManifest(tempWebAppDir.resolve(JarFile.MANIFEST_NAME));

        assertTrue(FileUtils.deleteDirectory(tempWebAppDir));
        assertTrue(FileUtils.deleteDirectory(webAppDir));
    }

    @Test
    public void testTransformNoChanges() throws Exception {
        URL directory = new URL(SOURCE_URL);
        URL tempDirectory = new URL(TARGET_URL);

        // Create content
        Path webAppDir = Paths.get(directory.getPath());
        Path webXml = webAppDir.resolve(WEB_INF).resolve(WEB_XML);
        Files.createDirectories(webXml.getParent());
        Files.createFile(webXml);

        try {
            new DirTransformer(null);
        } catch (Exception e) {
            assertTrue("Callback must not be null".equals(e.getMessage()));
        }

        DirTransformer transformer = new DirTransformer(new DirTransformerCallback() {

            @Override
            public boolean transformFile(InputStream inputStream, Path toFile) throws IOException {
                return false;
            }
        });

        transformer.transform(directory, tempDirectory);
        Path tempWebAppDir = Paths.get(tempDirectory.getPath());
        assertDirsSame(webAppDir, tempWebAppDir);

        assertTrue(FileUtils.deleteDirectory(tempWebAppDir));
        assertTrue(FileUtils.deleteDirectory(webAppDir));
    }

    private void assertDirsSame(Path webAppDir, Path tempWebAppDir) throws IOException {
        assertEquals(webAppDir.getFileName(), tempWebAppDir.getFileName());
        assertEquals(webAppDir.toFile().length(), tempWebAppDir.toFile().length());

        File[] sourceFiles = webAppDir.toFile().listFiles();
        File[] destinationFiles = tempWebAppDir.toFile().listFiles();
        if (sourceFiles != null && destinationFiles != null) {
            assertEquals(sourceFiles.length, destinationFiles.length);

            for (int i = 0; i < sourceFiles.length; i++) {
                File sourceFile = sourceFiles[i];
                File destinationFile = destinationFiles[i];
                assertEquals(sourceFile.getName(), destinationFile.getName());
                assertEquals(sourceFile.length(), destinationFile.length());
            }
        }
    }

    private void checkManifest(Path manifestFile) throws IOException {
        try (InputStream is = Files.newInputStream(manifestFile);) {
            Manifest manifest = new Manifest(is);
            Attributes attr = manifest.getMainAttributes();
            String value = attr.getValue("Custom-Header");
            assertEquals("test", value);
            assertEquals(1, attr.size());
        }
    }

    private void createManifest(Path toFile, String... headers) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(toFile))) {
            for (String header : headers) {
                writer.println(header);
            }
            writer.println();
        }
    }
}
