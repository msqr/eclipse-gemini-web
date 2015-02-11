/*******************************************************************************
 * Copyright (c) 2010, 2015 SAP SE
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

package org.eclipse.gemini.web.tomcat.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.eclipse.gemini.web.core.spi.ServletContainerException;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class WebappConfigLocatorTests {

    private static final String CONTEXT_PATH_1 = "test";

    private static final String CONTEXT_PATH_2 = "test1";

    private static final String CONTEXT_PATH_3 = "/";

    private static final String CONTEXT_PATH_4 = "/test/test";

    private static final String CONTEXT_PATH_5 = "test/test";

    private static final String CONFIG_FILE_LOCATION_1 = "file:src/test/resources/test.xml";

    private static final String CONFIG_FILE_LOCATION_2 = "file:src/test/resources/META-INF/context.xml";

    private static final String CONFIG_FILE_LOCATION_4 = "file:target/test-classes/ROOT.xml";

    private static final String CONFIG_FILE_LOCATION_5 = "file:target/test-classes/test%23test.xml";

    private static final String CONFIG_DIR_LOCATION_1 = "file:src/test/resources";

    private static final String CONFIG_DIR_LOCATION_2 = "file:src/test";

    private static final String CONFIG_DIR_LOCATION_3 = "file:target/test-classes";

    private static final String CORRUPTED_JAR_NAME = "file:target/test-classes/corrupted.jar";

    private static final String JAR_NAME_1 = "file:target/test-classes/test1.jar";

    private static final String JAR_NAME_2 = "file:target/test-classes/test2.jar";

    private static final String JAR_ENTRY_NAME = "META-INF/context.xml";

    private static final String HOST_NAME = "localhost";

    private static final String ENGINE_NAME = "Catalina";

    @Before
    public void setUp() throws Exception {
        URL urlFile2 = new URL(CONFIG_FILE_LOCATION_2);
        URL jarFile1 = new URL(JAR_NAME_1);
        URL jarFile2 = new URL(JAR_NAME_2);

        byte[] buffer = new byte[1024];
        int bytesRead;

        try (OutputStream stream = Files.newOutputStream(Paths.get(jarFile1.getPath()));
            JarOutputStream out = new JarOutputStream(stream, new Manifest());
            InputStream file = Files.newInputStream(Paths.get(urlFile2.getPath()));) {
            JarEntry jarAdd = new JarEntry(JAR_ENTRY_NAME);
            out.putNextEntry(jarAdd);
            while ((bytesRead = file.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.closeEntry();
        }

        try (OutputStream stream = Files.newOutputStream(Paths.get(jarFile2.getPath()));
            JarOutputStream out = new JarOutputStream(stream, new Manifest());) {
        }
    }

    @Test
    public void testResolveWebappContextXml() throws Exception {
        URL urlFile1 = new URL(CONFIG_FILE_LOCATION_1);
        URL urlDir1 = new URL(CONFIG_DIR_LOCATION_1);
        // context.xml exists in the configuration directory
        assertEquals(Paths.get(urlFile1.getPath()).toAbsolutePath().toUri().toURL(),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_1, null, Paths.get(urlDir1.getPath()), null));

        URL urlFile2 = new URL(CONFIG_FILE_LOCATION_2);
        URL urlDir2 = new URL(CONFIG_DIR_LOCATION_2);
        // context.xml does not exist in the configuration directory, but exists
        // in doc base
        // doc base is directory
        assertEquals(Paths.get(urlFile2.getPath()).toAbsolutePath().toUri().toURL(),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_2, urlDir1.getPath(), Paths.get(urlDir1.getPath()), null));
        // context.xml does not exist in the configuration directory and in doc
        // base
        // doc base is directory
        assertEquals(null, WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_2, urlDir2.getPath(), Paths.get(urlDir1.getPath()), null));

        try {
            // context.xml does not exist in the configuration directory, doc
            // base is jar and does not exist
            URL corruptedJarFile = new URL(CORRUPTED_JAR_NAME);
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_2, corruptedJarFile.getPath(), Paths.get(urlDir1.getPath()), null);
        } catch (ServletContainerException e) {
            assertTrue(e.getCause() instanceof IOException);
        }

        URL jarFile1 = new URL(JAR_NAME_1);
        URL jarFile2 = new URL(JAR_NAME_2);
        URL urlDir3 = new URL(CONFIG_DIR_LOCATION_3);
        // context.xml does not exist in the configuration directory, but exists
        // in doc base
        // doc base is jar
        // context.xml will be read from the jar
        assertEquals(new URL(WebappConfigLocator.JAR_SCHEMA + Paths.get(jarFile1.getPath()).toAbsolutePath().toUri().toString()
            + WebappConfigLocator.JAR_TO_ENTRY_SEPARATOR + WebappConfigLocator.CONTEXT_XML),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_2, jarFile1.getPath(), Paths.get(urlDir3.getPath()), null));

        // context.xml does not exist in the configuration directory, but exists in doc base
        // doc base cannot be resolved
        // bundle is not provided
        assertEquals(null,
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_2, WebappConfigLocator.EMPTY_STRING, Paths.get(urlDir3.getPath()), null));
        // context.xml will be read from the bundle
        Bundle bundle = createMock(Bundle.class);
        expect(bundle.getLocation()).andReturn(Paths.get(jarFile1.getPath()).toAbsolutePath().toUri().toString());
        replay(bundle);
        assertEquals(new URL(WebappConfigLocator.JAR_SCHEMA + Paths.get(jarFile1.getPath()).toAbsolutePath().toUri().toString()
            + WebappConfigLocator.JAR_TO_ENTRY_SEPARATOR + WebappConfigLocator.CONTEXT_XML),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_2, WebappConfigLocator.EMPTY_STRING, Paths.get(urlDir3.getPath()), bundle));
        verify(bundle);

        // context.xml does not exist in the configuration directory and in doc
        // base
        // doc base is jar
        assertEquals(null, WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_2, jarFile2.getPath(), Paths.get(urlDir1.getPath()), null));

        URL urlFile4 = new URL(CONFIG_FILE_LOCATION_4);
        URL urlFile5 = new URL(CONFIG_FILE_LOCATION_5);
        // different types of context path
        assertEquals(Paths.get(urlFile4.getPath()).toAbsolutePath().toUri().toURL(),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_3, jarFile1.getPath(), Paths.get(urlDir3.getPath()), null));
        assertEquals(Paths.get(urlFile5.toURI().getSchemeSpecificPart()).toAbsolutePath().toUri().toURL(),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_4, jarFile1.getPath(), Paths.get(urlDir3.getPath()), null));
        assertEquals(Paths.get(urlFile5.toURI().getSchemeSpecificPart()).toAbsolutePath().toUri().toURL(),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_5, jarFile1.getPath(), Paths.get(urlDir3.getPath()), null));
    }

    @Test
    public void testResolveWebappConfigDir() {
        Host mockHost = createMock(Host.class);
        Engine mockEngine = createMock(Engine.class);
        Container mockContainer = createMock(Container.class);
        expect(mockHost.getParent()).andReturn(mockEngine).andReturn(mockContainer).andReturn(null).andReturn(mockEngine).andReturn(mockContainer).andReturn(
            null);
        expect(mockHost.getName()).andReturn(HOST_NAME).times(6);
        expect(mockEngine.getName()).andReturn(ENGINE_NAME).times(2);

        replay(mockHost, mockEngine);

        Path configDir = Paths.get("");
        Path expected = configDir.resolve(ENGINE_NAME);
        expected = expected.resolve(HOST_NAME);
        assertEquals(expected, WebappConfigLocator.resolveWebappConfigDir(configDir, mockHost));

        expected = configDir.resolve(HOST_NAME);
        assertEquals(expected, WebappConfigLocator.resolveWebappConfigDir(configDir, mockHost));
        assertEquals(expected, WebappConfigLocator.resolveWebappConfigDir(configDir, mockHost));

        configDir = Paths.get(WebappConfigLocator.DEFAULT_CONFIG_DIRECTORY);
        expected = configDir.resolve(ENGINE_NAME);
        expected = expected.resolve(HOST_NAME);
        assertEquals(expected, WebappConfigLocator.resolveWebappConfigDir(null, mockHost));

        expected = configDir.resolve(HOST_NAME);
        assertEquals(expected, WebappConfigLocator.resolveWebappConfigDir(null, mockHost));
        assertEquals(expected, WebappConfigLocator.resolveWebappConfigDir(null, mockHost));

        verify(mockHost, mockEngine);
    }

    @Test
    public void testResolveDefaultContextXml() throws Exception {
        assertEquals(null, WebappConfigLocator.resolveDefaultContextXml(null));

        URL urlFile = new URL(CONFIG_FILE_LOCATION_2);
        assertEquals(Paths.get(urlFile.getPath()).toAbsolutePath().toString(),
            WebappConfigLocator.resolveDefaultContextXml(Paths.get(urlFile.getPath()).getParent()));

        assertEquals(null, WebappConfigLocator.resolveDefaultContextXml(Paths.get(urlFile.getPath()).getParent().getParent()));
    }

    public void testResolveDefaultWebXml() throws Exception {
        assertEquals(null, WebappConfigLocator.resolveDefaultWebXml(null));

        URL configDir = new URL(CONFIG_DIR_LOCATION_1);
        assertEquals(configDir.getPath(), WebappConfigLocator.resolveDefaultContextXml(Paths.get(configDir.getPath()).getParent()));

        assertEquals(null, WebappConfigLocator.resolveDefaultContextXml(Paths.get(configDir.getPath()).getParent().getParent()));
    }
}
