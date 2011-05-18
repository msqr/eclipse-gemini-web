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

package org.eclipse.gemini.web.tomcat.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.eclipse.gemini.web.core.spi.ServletContainerException;
import org.eclipse.virgo.util.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class WebappConfigLocatorTest {

    private static final String CONTEXT_PATH_1 = "test";

    private static final String CONTEXT_PATH_2 = "test1";

    private static final String CONTEXT_PATH_3 = "/";

    private static final String CONTEXT_PATH_4 = "/test/test";

    private static final String CONTEXT_PATH_5 = "test/test";

    private static final String CONFIG_FILE_LOCATION_1 = "file:src/test/resources/test.xml";

    private static final String CONFIG_FILE_LOCATION_2 = "file:src/test/resources/META-INF/context.xml";

    private static final String CONFIG_FILE_LOCATION_3 = "file:target/test-classes/test1.xml";

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
        URL corruptedJarFile = new URL(CORRUPTED_JAR_NAME);
        new File(corruptedJarFile.getPath()).createNewFile();

        byte[] buffer = new byte[1024];
        int bytesRead;

        FileOutputStream stream = null;
        JarOutputStream out = null;
        FileInputStream file = null;
        try {
            stream = new FileOutputStream(jarFile1.getPath());
            out = new JarOutputStream(stream, new Manifest());
            file = new FileInputStream(new File(urlFile2.getPath()));
            JarEntry jarAdd = new JarEntry(JAR_ENTRY_NAME);
            out.putNextEntry(jarAdd);
            while ((bytesRead = file.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.closeEntry();
        } finally {
            IOUtils.closeQuietly(file);
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(stream);
        }

        try {
            stream = new FileOutputStream(jarFile2.getPath());
            out = new JarOutputStream(stream, new Manifest());
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(stream);
        }
    }

    @Test
    public void testResolveWebappContextXml() throws Exception {
        URL urlFile1 = new URL(CONFIG_FILE_LOCATION_1);
        URL urlDir1 = new URL(CONFIG_DIR_LOCATION_1);
        // context.xml exists in the configuration directory
        assertEquals(new File(urlFile1.getPath()).getAbsoluteFile().toURI().toURL(),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_1, null, new File(urlDir1.getPath())));

        URL urlFile2 = new URL(CONFIG_FILE_LOCATION_2);
        URL urlDir2 = new URL(CONFIG_DIR_LOCATION_2);
        // context.xml does not exist in the configuration directory, but exists
        // in doc base
        // doc base is directory
        assertEquals(new File(urlFile2.getPath()).getAbsoluteFile().toURI().toURL(),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_2, urlDir1.getPath(), new File(urlDir1.getPath())));
        // context.xml does not exist in the configuration directory and in doc
        // base
        // doc base is directory
        assertEquals(null, WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_2, urlDir2.getPath(), new File(urlDir1.getPath())));

        try {
            // context.xml does not exist in the configuration directory, doc
            // base is jar and does not exist
            URL corruptedJarFile = new URL(CORRUPTED_JAR_NAME);
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_2, corruptedJarFile.getPath(), new File(urlDir1.getPath()));
        } catch (ServletContainerException e) {
            assertTrue(e.getCause() instanceof IOException);
        }

        URL jarFile1 = new URL(JAR_NAME_1);
        URL jarFile2 = new URL(JAR_NAME_2);
        URL urlDir3 = new URL(CONFIG_DIR_LOCATION_3);
        URL urlFile3 = new URL(CONFIG_FILE_LOCATION_3);
        // context.xml does not exist in the configuration directory, but exists
        // in doc base
        // doc base is jar
        // copy will be performed
        assertEquals(new File(urlFile3.getPath()).getAbsoluteFile().toURI().toURL(),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_2, jarFile1.getPath(), new File(urlDir3.getPath())));
        // context.xml does not exist in the configuration directory and in doc
        // base
        // doc base is jar
        assertEquals(null, WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_2, jarFile2.getPath(), new File(urlDir1.getPath())));

        URL urlFile4 = new URL(CONFIG_FILE_LOCATION_4);
        URL urlFile5 = new URL(CONFIG_FILE_LOCATION_5);
        // different types of context path
        assertEquals(new File(urlFile4.getPath()).getAbsoluteFile().toURI().toURL(),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_3, jarFile1.getPath(), new File(urlDir3.getPath())));
        assertEquals(new File(urlFile5.toURI().getSchemeSpecificPart()).getAbsoluteFile().toURI().toURL(),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_4, jarFile1.getPath(), new File(urlDir3.getPath())));
        assertEquals(new File(urlFile5.toURI().getSchemeSpecificPart()).getAbsoluteFile().toURI().toURL(),
            WebappConfigLocator.resolveWebappContextXml(CONTEXT_PATH_5, jarFile1.getPath(), new File(urlDir3.getPath())));
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

        File configDir = new File("");
        File expected = new File(configDir, ENGINE_NAME);
        expected = new File(expected, HOST_NAME);
        assertEquals(expected, WebappConfigLocator.resolveWebappConfigDir(configDir, mockHost));

        expected = new File(configDir, HOST_NAME);
        assertEquals(expected, WebappConfigLocator.resolveWebappConfigDir(configDir, mockHost));
        assertEquals(expected, WebappConfigLocator.resolveWebappConfigDir(configDir, mockHost));

        configDir = new File(WebappConfigLocator.DEFAULT_CONFIG_DIRECTORY);
        expected = new File(configDir, ENGINE_NAME);
        expected = new File(expected, HOST_NAME);
        assertEquals(expected, WebappConfigLocator.resolveWebappConfigDir(null, mockHost));

        expected = new File(configDir, HOST_NAME);
        assertEquals(expected, WebappConfigLocator.resolveWebappConfigDir(null, mockHost));
        assertEquals(expected, WebappConfigLocator.resolveWebappConfigDir(null, mockHost));

        verify(mockHost, mockEngine);
    }

    @Test
    public void testResolveDefaultContextXml() throws Exception {
        assertEquals(null, WebappConfigLocator.resolveDefaultContextXml(null));

        URL urlFile = new URL(CONFIG_FILE_LOCATION_2);
        assertEquals(new File(urlFile.getPath()).getAbsolutePath(),
            WebappConfigLocator.resolveDefaultContextXml(new File(urlFile.getPath()).getParentFile()));

        assertEquals(null, WebappConfigLocator.resolveDefaultContextXml(new File(urlFile.getPath()).getParentFile().getParentFile()));
    }

    public void testResolveDefaultWebXml() throws Exception {
        assertEquals(null, WebappConfigLocator.resolveDefaultWebXml(null));

        URL configDir = new URL(CONFIG_DIR_LOCATION_1);
        assertEquals(configDir.getPath(), WebappConfigLocator.resolveDefaultContextXml(new File(configDir.getPath()).getParentFile()));

        assertEquals(null, WebappConfigLocator.resolveDefaultContextXml(new File(configDir.getPath()).getParentFile().getParentFile()));
    }
}
