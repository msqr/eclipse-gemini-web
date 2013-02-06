/*******************************************************************************
 * Copyright (c) 2009, 2013 VMware Inc.
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

package org.eclipse.gemini.web.tomcat.internal.loading;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.eclipse.virgo.test.stubs.framework.FindEntriesDelegate;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

public class BundleEntryTests {

    private final StubBundle testBundle = new StubBundle();

    @Before
    public void createEntries() throws MalformedURLException {
        this.testBundle.addEntryPaths("", createPathsEnumeration("sub/"));
        this.testBundle.addEntryPaths("sub/", createPathsEnumeration("sub/one.txt", "sub/another.sub/"));
        this.testBundle.addEntryPaths("sub/another.sub/", createPathsEnumeration("sub/another.sub/two.txt"));

        this.testBundle.addEntry("", new File("src/test/resources/").toURI().toURL());
        this.testBundle.addEntry("sub/", new File("src/test/resources/sub/").toURI().toURL());
        this.testBundle.addEntry("sub/one.txt", new File("src/test/resources/sub/one.txt").toURI().toURL());
        this.testBundle.addEntry("sub/another.sub/", new File("src/test/resources/sub/another.sub/").toURI().toURL());
        this.testBundle.addEntry("sub/another.sub/two.txt", new File("src/test/resources/sub/another.sub/two.txt").toURI().toURL());
        this.testBundle.addEntry("a/", new File("src/test/resources/a/").toURI().toURL());
        this.testBundle.addEntry("a/b/", new File("src/test/resources/a/b/").toURI().toURL());
        this.testBundle.addEntry("a/b/c.txt", new File("src/test/resources/a/b/c.txt").toURI().toURL());

        this.testBundle.addEntry("/", new File("src/test/resources/").toURI().toURL());
        this.testBundle.addEntry("/sub/", new File("src/test/resources/sub/").toURI().toURL());
        this.testBundle.addEntry("/sub/one.txt", new File("src/test/resources/sub/one.txt").toURI().toURL());
        this.testBundle.addEntry("/sub/another.sub/", new File("src/test/resources/sub/another.sub/").toURI().toURL());
        this.testBundle.addEntry("/sub/another.sub/two.txt", new File("src/test/resources/sub/another.sub/two.txt").toURI().toURL());
        this.testBundle.addEntry("/a/", new File("src/test/resources/a/").toURI().toURL());
        this.testBundle.addEntry("/a/b/", new File("src/test/resources/a/b/").toURI().toURL());
        this.testBundle.addEntry("/a/b/c.txt", new File("src/test/resources/a/b/c.txt").toURI().toURL());
        this.testBundle.setFindEntriesDelegate(new FindEntriesDelegate() {

            @Override
            public Enumeration<?> findEntries(final String path, final String filePattern, boolean recurse) {
                return new Enumeration<URL>() {

                    private boolean hasMore = true;

                    @Override
                    public boolean hasMoreElements() {
                        return this.hasMore;
                    }

                    @Override
                    public URL nextElement() {
                        if (this.hasMore) {
                            this.hasMore = false;
                            return BundleEntryTests.this.testBundle.getEntry(path + "/" + filePattern);
                        }
                        return null;
                    }
                };
            }
        });
    }

    @Test
    public void testList() {
        BundleEntry entry = new BundleEntry(this.testBundle);
        List<BundleEntry> list = entry.list();

        BundleEntry subEntry = findByPath(list, "sub/");
        assertNotNull(subEntry);

        list = subEntry.list();
        assertNotNull(findByPath(list, "sub/one.txt"));
        assertNotNull(findByPath(list, "sub/another.sub/"));
    }

    @Test
    public void testListBundleWithFragment() {
        Bundle bundle = createMock(Bundle.class);
        BundleRevision bundleRevision = createMock(BundleRevision.class);
        BundleWiring bundleWiring = createMock(BundleWiring.class);
        BundleWire bundleWire = createMock(BundleWire.class);
        Bundle fbundle = createMock(Bundle.class);
        BundleRevision fbundleRevision = createMock(BundleRevision.class);
        BundleWiring fbundleWiring = createMock(BundleWiring.class);
        expect(bundle.getEntryPaths("")).andReturn(createPathsEnumeration("sub/"));
        expect(bundle.getEntryPaths("sub/")).andReturn(createPathsEnumeration("sub/one.txt"));
        expect(bundle.adapt(BundleRevision.class)).andReturn(bundleRevision);
        expect(bundleRevision.getWiring()).andReturn(bundleWiring);
        expect(bundleWiring.getProvidedWires(BundleRevision.HOST_NAMESPACE)).andReturn(Arrays.asList(new BundleWire[] { bundleWire }));
        expect(bundleWire.getRequirerWiring()).andReturn(fbundleWiring);
        expect(fbundleWiring.getRevision()).andReturn(fbundleRevision);
        expect(fbundleRevision.getBundle()).andReturn(fbundle);
        expect(fbundle.getEntryPaths("")).andReturn(createPathsEnumeration("sub/"));
        expect(fbundle.getEntryPaths("sub/")).andReturn(createPathsEnumeration("sub/another.sub/"));

        replay(bundle, bundleRevision, bundleWiring, bundleWire, fbundle, fbundleRevision, fbundleWiring);

        BundleEntry entry = new BundleEntry(bundle);
        List<BundleEntry> list = entry.list();

        BundleEntry subEntry = findByPath(list, "sub/");
        assertNotNull(subEntry);

        list = subEntry.list();
        assertNotNull(findByPath(list, "sub/one.txt"));
        assertNotNull(findByPath(list, "sub/another.sub/"));

        verify(bundle, bundleRevision, bundleWiring, bundleWire, fbundle, fbundleRevision, fbundleWiring);
    }

    @Test
    public void testGetEntry() {
        BundleEntry entry = new BundleEntry(this.testBundle);

        assertNotNull(entry.getEntry("sub/"));
        assertNotNull(entry.getEntry("sub/one.txt"));
        assertNotNull(entry.getEntry("sub/another.sub/"));
        assertNotNull(entry.getEntry("sub/another.sub/two.txt"));
        assertNotNull(entry.getEntry("."));
        assertNotNull(entry.getEntry("sub/."));
        assertNotNull(entry.getEntry(""));
        assertNotNull(entry.getEntry("/"));

        assertTrue(BundleEntry.isDirectory(entry.getEntry("sub/").getURL()));
        assertTrue(BundleEntry.isDirectory(entry.getEntry("sub/another.sub/").getURL()));
        assertTrue(BundleEntry.isDirectory(entry.getEntry(".").getURL()));
        assertTrue(BundleEntry.isDirectory(entry.getEntry("sub/.").getURL()));
        assertTrue(BundleEntry.isDirectory(entry.getEntry("").getURL()));
        assertTrue(BundleEntry.isDirectory(entry.getEntry("/").getURL()));
    }

    @Test
    public void testNames() {
        BundleEntry entry = new BundleEntry(this.testBundle);

        BundleEntry e = entry.getEntry("/");
        assertEquals("/", e.getName());

        e = entry.getEntry("/sub/");
        assertEquals("sub", e.getName());

        e = entry.getEntry("/sub/one.txt");
        assertEquals("one.txt", e.getName());

        e = entry.getEntry("");
        assertEquals("/", e.getName());

        e = entry.getEntry("sub/");
        assertEquals("sub", e.getName());

        e = entry.getEntry("sub/one.txt");
        assertEquals("one.txt", e.getName());

        e = entry.getEntry("/a/");
        assertEquals("a", e.getName());

        e = entry.getEntry("/a/b/");
        assertEquals("b", e.getName());

        e = entry.getEntry("/a/b/c.txt");
        assertEquals("c.txt", e.getName());

        e = entry.getEntry("a/");
        assertEquals("a", e.getName());

        e = entry.getEntry("a/b/");
        assertEquals("b", e.getName());

        e = entry.getEntry("a/b/c.txt");
        assertEquals("c.txt", e.getName());
    }

    private BundleEntry findByPath(List<BundleEntry> entries, String entry) {
        for (BundleEntry bundleEntry : entries) {
            if (bundleEntry.getPath().equals(entry)) {
                return bundleEntry;
            }
        }
        return null;
    }

    private Enumeration<String> createPathsEnumeration(String... paths) {
        Vector<String> vector = new Vector<String>();

        for (String path : paths) {
            vector.add(path);
        }

        return vector.elements();
    }
}
