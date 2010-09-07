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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gemini.web.internal.url.DirTransformer.DirTransformerCallback;
import org.eclipse.virgo.util.io.PathReference;
import org.junit.Test;

public class DirTransformingURLConnectionTest {

    private static final String WEB_INF = "WEB-INF";

    private static final String WEB_XML = "web.xml";

    private static final String SOURCE_URL = "file:target/test-classes/web-app-dir";

    private static final String TARGET_URL = "file:target/test-classes/temp/web-app-dir";

    @Test
    public void testGetURL() throws Exception {
        URL directory = new URL(SOURCE_URL);
        URL tempDirectory = new URL(TARGET_URL);

        // Create content
        PathReference webAppDir = new PathReference(directory.getPath());
        PathReference webXml = webAppDir.newChild(WEB_INF + File.separator + WEB_XML);
        webXml.createFile();

        final List<PathReference> files = new ArrayList<PathReference>();
        DirTransformer transformer = new DirTransformer(new DirTransformerCallback() {

            public boolean transformFile(InputStream inputStream, PathReference toFile) throws IOException {
                files.add(toFile);
                return false;
            }
        });

        DirTransformingURLConnection connection = new DirTransformingURLConnection(directory, transformer);
        connection.setTransformedURL(tempDirectory);
        InputStream is = connection.getInputStream();
        assertNotNull(is);
        URL url = connection.getURL();
        assertTrue(tempDirectory.equals(url));

        PathReference tempWebAppDir = new PathReference(tempDirectory.getPath());
        assertTrue(tempWebAppDir.newChild(WEB_INF + File.separator + WEB_XML).exists());
        assertTrue(files.size() == 1);

        assertTrue(tempWebAppDir.delete(true));
        assertTrue(webAppDir.delete(true));
    }
}
