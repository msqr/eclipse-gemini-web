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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gemini.web.internal.url.DirTransformer.DirTransformerCallback;
import org.junit.Test;

public class DirTransformingURLConnectionTests {

    private static final String WEB_INF = "WEB-INF";

    private static final String WEB_XML = "web.xml";

    private static final String SOURCE_URL = "file:target/test-classes/web-app-dir";

    private static final String TARGET_URL = "file:target/test-classes/temp/web-app-dir";

    @Test
    public void testGetURL() throws Exception {
        URL directory = new URL(SOURCE_URL);
        URL tempDirectory = new URL(TARGET_URL);

        // Create content
        Path webAppDir = Paths.get(directory.getPath());
        Path webXml = webAppDir.resolve(WEB_INF).resolve(WEB_XML);
        Files.createDirectories(webXml.getParent());
        Files.createFile(webXml);

        final List<Path> files = new ArrayList<>();
        DirTransformer transformer = new DirTransformer(new DirTransformerCallback() {

            @Override
            public boolean transformFile(InputStream inputStream, Path toFile) throws IOException {
                files.add(toFile);
                return false;
            }
        });

        DirTransformingURLConnection connection = new DirTransformingURLConnection(directory, transformer);
        connection.setTransformedURL(tempDirectory);
        try (InputStream is = connection.getInputStream();) {
            assertNotNull(is);
        }
        URL url = connection.getURL();
        assertTrue(tempDirectory.equals(url));

        Path tempWebAppDir = Paths.get(tempDirectory.getPath());
        assertTrue(Files.exists(tempWebAppDir.resolve(WEB_INF).resolve(WEB_XML)));
        assertTrue(files.size() == 1);

        assertTrue(FileUtils.deleteDirectory(tempWebAppDir));
        assertTrue(FileUtils.deleteDirectory(webAppDir));
    }
}
