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

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.eclipse.virgo.test.stubs.framework.FindEntriesDelegate;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BundleDirContextTests {

    private static final String FILE_NAME = "sub/one.txt";

    private static final String DIRECTORY_NAME = "sub/";

    private final StubBundle testBundle = new StubBundle();

    private BundleDirContext bundleDirContext;

    @Before
    public void setUp() throws Exception {
        this.testBundle.addEntry("", new File("src/test/resources/").toURI().toURL());
        this.testBundle.addEntry(DIRECTORY_NAME, new File("src/test/resources/sub/").toURI().toURL());
        this.testBundle.addEntry(FILE_NAME, new File("src/test/resources/sub/one.txt").toURI().toURL());
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
                            return BundleDirContextTests.this.testBundle.getEntry(path + "/" + filePattern);
                        }
                        return null;
                    }
                };
            }
        });

        this.bundleDirContext = new BundleDirContext(this.testBundle);
    }

    @Test(expected = NamingException.class)
    public void testDoGetAttributesStringStringArrayNameNotFound() throws NamingException {
        this.bundleDirContext.getAttributes("sub", null);
    }

    @Test
    public void testGetAttributesOfDirectory() throws NamingException {
        Attributes attributes = this.bundleDirContext.getAttributes(DIRECTORY_NAME);

        checkName(attributes, DIRECTORY_NAME.substring(0, DIRECTORY_NAME.length() - 1));

        checkDirectoryResourceType(attributes);

        checkTimes(attributes);

        checkContentLength(attributes);
    }

    private void checkContentLength(Attributes attributes) {
        Attribute contentLength = attributes.get(org.apache.naming.resources.ResourceAttributes.CONTENT_LENGTH);
        Assert.assertNotNull(contentLength);

        contentLength = attributes.get(org.apache.naming.resources.ResourceAttributes.ALTERNATE_CONTENT_LENGTH);
        Assert.assertNotNull(contentLength);
    }

    private void checkName(Attributes attributes, String expectedName) throws NamingException {
        Attribute name = attributes.get(org.apache.naming.resources.ResourceAttributes.NAME);
        Assert.assertNotNull(name);
        NamingEnumeration<?> namingEnumeration = name.getAll();
        Assert.assertTrue(namingEnumeration.hasMore());
        String nameValue = (String) namingEnumeration.next();
        Assert.assertEquals(expectedName, nameValue);
    }

    private void checkDirectoryResourceType(Attributes attributes) throws NamingException {
        Attribute resourceType = attributes.get(org.apache.naming.resources.ResourceAttributes.TYPE);
        checkCollectionResourceType(resourceType);

        resourceType = attributes.get(org.apache.naming.resources.ResourceAttributes.ALTERNATE_TYPE);
        checkCollectionResourceType(resourceType);
    }

    private void checkCollectionResourceType(Attribute resourceType) throws NamingException {
        Assert.assertNotNull(resourceType);
        NamingEnumeration<?> namingEnumeration = resourceType.getAll();
        Assert.assertTrue(namingEnumeration.hasMore());
        String resourceTypeValue = (String) namingEnumeration.next();
        Assert.assertEquals(org.apache.naming.resources.ResourceAttributes.COLLECTION_TYPE, resourceTypeValue);
    }

    @Test
    public void testGetAttributesOfFile() throws NamingException {
        Attributes attributes = this.bundleDirContext.getAttributes(FILE_NAME);

        checkName(attributes, FILE_NAME.split("/")[1]);

        checkNoResourceType(attributes);

        checkTimes(attributes);

        checkContentLength(attributes);
    }

    private void checkNoResourceType(Attributes attributes) {
        Attribute resourceType = attributes.get(org.apache.naming.resources.ResourceAttributes.TYPE);
        Assert.assertNull(resourceType);
    }

    private void checkTimes(Attributes attributes) {
        Attribute creationDate = attributes.get(org.apache.naming.resources.ResourceAttributes.CREATION_DATE);
        Assert.assertNotNull(creationDate);

        creationDate = attributes.get(org.apache.naming.resources.ResourceAttributes.ALTERNATE_CREATION_DATE);
        Assert.assertNotNull(creationDate);

        Attribute lastModified = attributes.get(org.apache.naming.resources.ResourceAttributes.LAST_MODIFIED);
        Assert.assertNotNull(lastModified);

        lastModified = attributes.get(org.apache.naming.resources.ResourceAttributes.ALTERNATE_LAST_MODIFIED);
        Assert.assertNotNull(lastModified);
    }

    @Test
    public void testGetNoAttributesOfFile() throws NamingException {
        Attributes attributes = this.bundleDirContext.getAttributes(FILE_NAME, new String[] {});

        checkNoName(attributes);

        checkNoResourceType(attributes);

        checkNoTimes(attributes);

        checkNoContentLength(attributes);
    }

    private void checkNoContentLength(Attributes attributes) {
        Attribute contentLength = attributes.get(org.apache.naming.resources.ResourceAttributes.CONTENT_LENGTH);
        Assert.assertNull(contentLength);

        contentLength = attributes.get(org.apache.naming.resources.ResourceAttributes.ALTERNATE_CONTENT_LENGTH);
        Assert.assertNull(contentLength);
    }

    /**
     * @throws NamingException
     */
    private void checkNoName(Attributes attributes) throws NamingException {
        Attribute name = attributes.get(org.apache.naming.resources.ResourceAttributes.NAME);
        Assert.assertNull(name);

    }

    private void checkNoTimes(Attributes attributes) {
        Attribute creationDate = attributes.get(org.apache.naming.resources.ResourceAttributes.CREATION_DATE);
        Assert.assertNull(creationDate);

        creationDate = attributes.get(org.apache.naming.resources.ResourceAttributes.ALTERNATE_CREATION_DATE);
        Assert.assertNull(creationDate);

        Attribute lastModified = attributes.get(org.apache.naming.resources.ResourceAttributes.LAST_MODIFIED);
        Assert.assertNull(lastModified);

        lastModified = attributes.get(org.apache.naming.resources.ResourceAttributes.ALTERNATE_LAST_MODIFIED);
        Assert.assertNull(lastModified);
    }

    @Test
    public void testGetSomeAttributesOfFile() throws NamingException {
        Attributes attributes = this.bundleDirContext.getAttributes(FILE_NAME,
            new String[] { org.apache.naming.resources.ResourceAttributes.ALTERNATE_CREATION_DATE });

        checkNoName(attributes);

        checkNoResourceType(attributes);

        checkOnlyCreationDate(attributes);

        checkNoContentLength(attributes);
    }

    private void checkOnlyCreationDate(Attributes attributes) {
        Attribute creationDate = attributes.get(org.apache.naming.resources.ResourceAttributes.CREATION_DATE);
        Assert.assertNotNull(creationDate);

        creationDate = attributes.get(org.apache.naming.resources.ResourceAttributes.ALTERNATE_CREATION_DATE);
        Assert.assertNotNull(creationDate);

        Attribute lastModified = attributes.get(org.apache.naming.resources.ResourceAttributes.LAST_MODIFIED);
        Assert.assertNull(lastModified);

        lastModified = attributes.get(org.apache.naming.resources.ResourceAttributes.ALTERNATE_LAST_MODIFIED);
        Assert.assertNull(lastModified);
    }

}
