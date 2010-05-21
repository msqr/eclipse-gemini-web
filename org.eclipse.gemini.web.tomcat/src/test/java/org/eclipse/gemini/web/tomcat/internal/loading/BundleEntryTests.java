/*******************************************************************************
 * Copyright (c) 2009, 2010 VMware Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.web.tomcat.internal.loading.BundleEntry;
import org.eclipse.virgo.teststubs.osgi.framework.FindEntriesDelegate;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;

public class BundleEntryTests {

    private final StubBundle testBundle = new StubBundle();
    
    @Before
    public void createEntries() throws MalformedURLException {
        testBundle.addEntryPaths("", createPathsEnumeration("sub/"));
        testBundle.addEntryPaths("sub/", createPathsEnumeration("sub/one.txt", "sub/another.sub/"));
        testBundle.addEntryPaths("sub/another.sub/", createPathsEnumeration("sub/another.sub/two.txt"));     
        
        testBundle.addEntry("", new File("src/test/resources/").toURI().toURL());
        testBundle.addEntry("sub/", new File("src/test/resources/sub/").toURI().toURL());
        testBundle.addEntry("sub/one.txt", new File("src/test/resources/sub/one.txt").toURI().toURL());
        testBundle.addEntry("sub/another.sub/", new File("src/test/resources/sub/another.sub/").toURI().toURL());
        testBundle.addEntry("sub/another.sub/two.txt", new File("src/test/resources/sub/another.sub/two.txt").toURI().toURL());
        testBundle.addEntry("a/", new File("src/test/resources/a/").toURI().toURL());
        testBundle.addEntry("a/b/", new File("src/test/resources/a/b/").toURI().toURL());
        testBundle.addEntry("a/b/c.txt", new File("src/test/resources/a/b/c.txt").toURI().toURL());
        
        testBundle.addEntry("/", new File("src/test/resources/").toURI().toURL());
        testBundle.addEntry("/sub/", new File("src/test/resources/sub/").toURI().toURL());
        testBundle.addEntry("/sub/one.txt", new File("src/test/resources/sub/one.txt").toURI().toURL());
        testBundle.addEntry("/sub/another.sub/", new File("src/test/resources/sub/another.sub/").toURI().toURL());
        testBundle.addEntry("/sub/another.sub/two.txt", new File("src/test/resources/sub/another.sub/two.txt").toURI().toURL());
        testBundle.addEntry("/a/", new File("src/test/resources/a/").toURI().toURL());
        testBundle.addEntry("/a/b/", new File("src/test/resources/a/b/").toURI().toURL());
        testBundle.addEntry("/a/b/c.txt", new File("src/test/resources/a/b/c.txt").toURI().toURL());
        testBundle.setFindEntriesDelegate(new FindEntriesDelegate() {

			public Enumeration<?> findEntries(final String path, final String filePattern,
					boolean recurse) {
				return new Enumeration<URL>() {
					
					private boolean hasMore = true;

					public boolean hasMoreElements() {
						return this.hasMore;
					}

					public URL nextElement() {
						if (this.hasMore) {
							this.hasMore = false;
							return testBundle.getEntry(path + "/" + filePattern);
						}
						return null;
					}};
			}});
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
    public void testGetEntry() {
        BundleEntry entry = new BundleEntry(this.testBundle);
        
        assertNotNull(entry.getEntry("sub/"));
        assertNotNull(entry.getEntry("sub/one.txt"));
        assertNotNull(entry.getEntry("sub/another.sub/"));
        assertNotNull(entry.getEntry("sub/another.sub/two.txt"));

        assertTrue(entry.getEntry("sub/").isDirectory());
        assertTrue(entry.getEntry("sub/another.sub/").isDirectory());
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
            if(bundleEntry.getPath().equals(entry)) {
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
