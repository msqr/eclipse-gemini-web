/******************************************************************************
 * Copyright (c) 2006, 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.web.tomcat.internal.loading;

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * This class is based on the one provided by Eclipse Gemini Blueprint.
 * 
 * @author Costin Leau
 * @author Violeta Georgieva
 */
public class BundleDelegatingClassLoaderTests extends TestCase {

    private BundleDelegatingClassLoader classLoader;

    private IMocksControl bundleCtrl;

    private Bundle bundle;

    private ClassLoader bridge;

    @Override
    @Before
    protected void setUp() throws Exception {
        this.bundleCtrl = createStrictControl();
        this.bundle = this.bundleCtrl.createMock(Bundle.class);
        this.bundleCtrl.reset();
        this.bridge = new TestClassLoader();
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        this.bundleCtrl.verify();
        this.classLoader = null;
        this.bundleCtrl = null;
        this.bundle = null;
        this.bridge = null;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindClass() throws Exception {
        String className1 = "foo.bar";
        String className2 = "foo1.bar";
        String className3 = "foo2.bar";

        expect(this.bundle.loadClass(className1));
        this.bundleCtrl.andReturn(Object.class);
        expect(this.bundle.loadClass(className1)).andThrow(new ClassNotFoundException()).andThrow(new NoClassDefFoundError()).andThrow(
            new ClassNotFoundException());
        expect(this.bundle.loadClass(className2)).andThrow(new ClassNotFoundException());
        expect(this.bundle.loadClass(className3)).andThrow(new ClassNotFoundException());
        this.bundleCtrl.replay();

        this.classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(this.bundle);

        assertSame(Object.class, this.classLoader.findClass(className1));

        try {
            this.classLoader.findClass(className1);
        } catch (ClassNotFoundException ex) {
            assertTrue(ex.getMessage().equals("foo.bar not found from bundle [" + this.bundle + "]"));
        }

        try {
            this.classLoader.findClass(className1);
        } catch (NoClassDefFoundError ex) {
            assertTrue(ex.getMessage().equals("foo.bar not found from bundle [" + this.bundle + "]"));
        }

        this.classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(this.bundle, this.bridge);

        assertSame(Object.class, this.classLoader.findClass(className1));

        try {
            this.classLoader.findClass(className2);
        } catch (ClassNotFoundException ex) {
            assertTrue(ex.getMessage().equals("foo1.bar not found from bundle [" + this.bridge + "]"));
        }

        try {
            this.classLoader.findClass(className3);
        } catch (NoClassDefFoundError ex) {
            assertTrue(ex.getMessage().equals("foo2.bar not found from bundle [" + this.bridge + "]"));
        }
    }

    @Test
    public void testGetResource() throws Exception {
        String resource = "file://bla-bla";
        URL url = new URL("file://bla-bla");

        String resource1 = "file://bla-bla1";

        expect(this.bundle.getResource(resource)).andReturn(url).andReturn(null).andReturn(url).andReturn(null);
        expect(this.bundle.getResource(resource1)).andReturn(null);
        this.bundleCtrl.replay();

        this.classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(this.bundle);

        assertSame(url, this.classLoader.getResource(resource));
        assertNull(this.classLoader.getResource(resource));

        this.classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(this.bundle, this.bridge);

        assertSame(url, this.classLoader.getResource(resource));
        URL resourceUrl = this.classLoader.getResource(resource);
        assertNotNull(resourceUrl);
        assertTrue(resource.equals(resourceUrl.toString()));
        assertNull(this.classLoader.getResource(resource1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindResources() throws Exception {
        String resource = "file://bla-bla";
        String resource1 = "file://bla-bla1";
        IMocksControl enumCtrl = createStrictControl();
        Enumeration<URL> enumeration = enumCtrl.createMock(Enumeration.class);

        expect(this.bundle.getResources(resource)).andReturn(enumeration).andReturn(null).andReturn(enumeration).andReturn(null);
        expect(this.bundle.getResources(resource1)).andReturn(null);
        this.bundleCtrl.replay();

        this.classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(this.bundle);

        assertSame(enumeration, this.classLoader.findResources(resource));
        assertNull(this.classLoader.findResources(resource));

        this.classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(this.bundle, this.bridge);

        assertSame(enumeration, this.classLoader.findResources(resource));
        Enumeration<URL> resources = this.classLoader.findResources(resource);
        assertNotNull(resources);
        assertTrue(resource.equals(resources.nextElement().toString()));
        assertNull(this.classLoader.findResources(resource1));
    }

    private static class TestClassLoader extends ClassLoader {

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if ("foo.bar".equals(name)) {
                return Object.class;
            } else if ("foo1.bar".equals(name)) {
                throw new ClassNotFoundException();
            } else {
                throw new NoClassDefFoundError();
            }
        }

        @Override
        public URL getResource(String name) {
            if ("file://bla-bla".equals(name)) {
                try {
                    return new URL("file://bla-bla");
                } catch (MalformedURLException e) {
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            if ("file://bla-bla".equals(name)) {
                List<URL> resources = new ArrayList<URL>();
                resources.add(new URL("file://bla-bla"));
                return Collections.enumeration(resources);
            } else {
                return null;
            }
        }

    }
}