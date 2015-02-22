/*******************************************************************************
 * Copyright (c) 2015 SAP SE
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

package org.eclipse.gemini.web.tomcat.internal.bundleresources;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.catalina.LifecycleState;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.eclipse.gemini.web.tomcat.internal.loader.FindEntriesDelegateImpl;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.junit.Before;
import org.junit.Test;

public class BundleWebResourceSetTests {

    private static final String FILE_NAME = "/sub/one.txt";

    private static final String DIRECTORY_NAME = "/sub/";

    private final StubBundle testBundle = new StubBundle();

    private BundleWebResourceSet bundleWebResourceSet;

    private WebResourceRoot root;

    @Before
    public void setUp() throws Exception {
        this.testBundle.addEntry("", new File("src/test/resources/").toURI().toURL());
        this.testBundle.addEntry(DIRECTORY_NAME, new File("src/test/resources/sub/").toURI().toURL());
        this.testBundle.addEntry(FILE_NAME, new File("src/test/resources/sub/one.txt").toURI().toURL());
        this.testBundle.setFindEntriesDelegate(new FindEntriesDelegateImpl(this.testBundle));

        this.root = createMock(WebResourceRoot.class);
        expect(this.root.getState()).andReturn(LifecycleState.STARTED);
    }

    @Test
    public void testGetAttributesOfDirectory() {
        replay(this.root);

        this.bundleWebResourceSet = new BundleWebResourceSet(new BundleWebResource(this.testBundle, this.root), this.root, "/", null, "/");
        WebResource webResource = this.bundleWebResourceSet.getResource(DIRECTORY_NAME);

        assertEquals(webResource.getName(), DIRECTORY_NAME.substring(1, DIRECTORY_NAME.length() - 1));

        assertTrue(webResource.isDirectory());

        assertTrue(webResource.getCreation() != -1);
        assertTrue(webResource.getLastModified() != -1);

        assertTrue(webResource.getContentLength() != -1);

        verify(this.root);
    }

    @Test
    public void testGetAttributesOfFile() {
        replay(this.root);

        this.bundleWebResourceSet = new BundleWebResourceSet(new BundleWebResource(this.testBundle, this.root), this.root, "/", null, "/");
        WebResource webResource = this.bundleWebResourceSet.getResource(FILE_NAME);

        assertEquals(webResource.getName(), FILE_NAME.split("/")[2]);

        assertTrue(webResource.isFile());

        assertTrue(webResource.getCreation() != -1);
        assertTrue(webResource.getLastModified() != -1);

        assertTrue(webResource.getContentLength() != -1);

        verify(this.root);
    }

}
