/*******************************************************************************
 * Copyright (c) 2009, 2014 VMware Inc.
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

package org.eclipse.gemini.web.internal;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class WebContainerUtilsTests {

    private static final Version TEST_BUNDLE_VERSION = new Version("467");

    private static final String TEST_SYMBOLIC_NAME = "a";

    private static final Dictionary<String, String> EMPTY_PROPERTIES = new Hashtable<String, String>();

    @Test
    public void testGetBaseNameNoExtension() {
        String name = WebContainerUtils.getBaseName("/path/to/dir/", false);
        assertEquals("dir", name);
    }

    @Test
    public void testGetBaseNameFilePath() {
        String name = WebContainerUtils.getBaseName("/path/to/app.war", false);
        assertEquals("app", name);
    }

    @Test
    public void testGetBaseNameDirPath() {
        String name = WebContainerUtils.getBaseName("/path/to/app.war/", true);
        assertEquals("app.war", name);
    }

    @Test
    public void testIsWebBundleWithWarExtension() throws Exception {
        testIsBundleWith("file:foo.war", EMPTY_PROPERTIES, null, "foo", true);
    }

    @Test
    public void testIsWebBundleWithUpperCaseWarExtension() throws Exception {
        testIsBundleWith("file:foo.WAR", EMPTY_PROPERTIES, null, "foo", true);
    }

    @Test
    public void testIsWebBundleWithWarExtensionAndTrailingSlash() throws Exception {
        testIsBundleWith("file:foo.war/", EMPTY_PROPERTIES, null, "foo", true);
    }

    @Test
    public void testIsWebBundleWithWarExtensionAndTrailingSlashes() throws Exception {
        testIsBundleWith("file:foo.war//", null, null, "foo", true);
    }

    @Test
    public void testIsWebBundleWithUpperCaseWarExtensionAndTrailingSlash() throws Exception {
        testIsBundleWith("file:foo.WAR/", null, null, "foo", true);
    }

    @Test
    public void testIsWebBundleWithWebBundleScheme() throws Exception {
        testIsBundleWith("webbundle:foo.jar", null, null, "foo", true);
    }

    @Test
    public void testIsWebBundleWithWebContextPath() throws Exception {
        Dictionary<String, String> p = new Hashtable<String, String>();
        p.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/foo");

        testIsBundleWith("file:foo.jar", p, null, "/foo", true);
    }

    @Test
    public void testIsWebBundleWithWebXml() throws MalformedURLException {
        testIsBundleWith("file:foo.jar", EMPTY_PROPERTIES, "file:foo.txt", "foo", true);
    }

    @Test
    public void testNotWebBundle() throws Exception {
        Bundle bundle = createNiceMock(Bundle.class);
        expect(bundle.getLocation()).andReturn("file:foo.jar").anyTimes();
        expect(bundle.getHeaders()).andReturn(new Hashtable<String, String>()).anyTimes();
        expect(bundle.getEntry(WebContainerUtils.ENTRY_WEB_XML)).andReturn(null).anyTimes();
        replay(bundle);
        assertFalse(WebContainerUtils.isWebBundle(bundle));
    }

    @Test
    public void testContextPathSupplied() throws Exception {
        Dictionary<String, String> p = new Hashtable<String, String>();
        p.put(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/foo");

        testIsBundleWith(null, p, null, "/foo", false);
    }

    @Test
    public void testContextPathDefaulted() throws Exception {
        testIsBundleWith("file:bar.war", EMPTY_PROPERTIES, null, "bar", true);
    }

    @Test
    public void testContextPathDefaultedWindowsPath() throws Exception {
        testIsBundleWith("file:/C:\\bar\\app.war", EMPTY_PROPERTIES, null, "app", true);
    }

    @Test
    public void testContextPathDefaultedComplexPath() throws Exception {
        Dictionary<String, String> p = new Hashtable<String, String>();

        Bundle bundle = createNiceMock(Bundle.class);
        expect(bundle.getLocation()).andReturn("file:../formtags.war?Import-Package:org.foo.bar").andReturn("initial@file:../formtags.war").andReturn(
            "file:../formtags.war#fragment");
        expect(bundle.getHeaders()).andReturn(p).anyTimes();
        replay(bundle);

        assertEquals("formtags", WebContainerUtils.getContextPath(bundle));
        assertEquals("formtags", WebContainerUtils.getContextPath(bundle));
        assertEquals("formtags", WebContainerUtils.getContextPath(bundle));
    }

    @Test
    public void testServletContextOsgiWebSymbolicNamePropertyDefault() {
        testSymbolicName(null, EMPTY_PROPERTIES);
    }

    @Test
    public void testServletContextOsgiWebSymbolicNamePropertySupplied() {
        testSymbolicName(TEST_SYMBOLIC_NAME, EMPTY_PROPERTIES);
    }

    @Test
    public void testServletContextOsgiWebVersionPropertyDefault() {
        testVersion(null, EMPTY_PROPERTIES);
    }

    @Test
    public void testServletContextOsgiWebVersionPropertySupplied() {
        Dictionary<String, String> headers = new Hashtable<String, String>();
        headers.put(WebContainerUtils.BUNDLE_VERSION_HEADER, TEST_BUNDLE_VERSION.toString());

        testVersion(TEST_BUNDLE_VERSION, headers);
    }

    @Test
    public void testIsDirectory() throws Exception {
        URL fileURL = new URL("file:foo.war");
        assertEquals("foo", WebContainerUtils.createDefaultBundleSymbolicName(fileURL));

        URL dirURL = new URL("file:src/test/resources/contains-system-bundle-package.war");
        assertEquals("contains-system-bundle-package.war", WebContainerUtils.createDefaultBundleSymbolicName(dirURL));

        URL jarURL = new URL("jar:file:foo.war!/");
        assertEquals("foo", WebContainerUtils.createDefaultBundleSymbolicName(jarURL));
    }

    private void testIsBundleWith(String location, Dictionary<String, String> headers, String entry, String contextPath, boolean checkIsWebBundle)
        throws MalformedURLException {
        Bundle bundle = createNiceMock(Bundle.class);
        if (location != null) {
            expect(bundle.getLocation()).andReturn(location).anyTimes();
        }
        if (headers != null) {
            expect(bundle.getHeaders()).andReturn(headers).anyTimes();
        }
        if (entry != null) {
            expect(bundle.getEntry(WebContainerUtils.ENTRY_WEB_XML)).andReturn(new URL(entry)).anyTimes();
        }
        replay(bundle);
        if (checkIsWebBundle) {
            assertTrue(WebContainerUtils.isWebBundle(bundle));
        }
        if (headers != null) {
            assertEquals(contextPath, WebContainerUtils.getContextPath(bundle));
        }
    }

    private void testSymbolicName(String symbolicName, Dictionary<String, String> headers) {
        Bundle bundle = createNiceMock(Bundle.class);
        expect(bundle.getSymbolicName()).andReturn(symbolicName).anyTimes();
        expect(bundle.getHeaders()).andReturn(headers).anyTimes();
        replay(bundle);

        Dictionary<String, String> p = new Hashtable<String, String>();
        WebContainerUtils.setServletContextBundleProperties(p, bundle);
        if (symbolicName == null) {
            assertNull(p.get(WebContainerUtils.OSGI_WEB_SYMBOLICNAME));
        } else {
            assertEquals(symbolicName, p.get(WebContainerUtils.OSGI_WEB_SYMBOLICNAME));
        }
    }

    private void testVersion(Version version, Dictionary<String, String> headers) {
        Bundle bundle = createNiceMock(Bundle.class);
        expect(bundle.getVersion()).andReturn(version).anyTimes();
        expect(bundle.getHeaders()).andReturn(headers).anyTimes();
        replay(bundle);

        Dictionary<String, String> p = new Hashtable<String, String>();
        WebContainerUtils.setServletContextBundleProperties(p, bundle);
        if (version == null) {
            assertNull(p.get(WebContainerUtils.OSGI_WEB_VERSION));
        } else {
            String stringVersion = p.get(WebContainerUtils.OSGI_WEB_VERSION);
            assertEquals(version, new Version(stringVersion));
        }
    }
}
