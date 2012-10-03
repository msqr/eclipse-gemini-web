/*******************************************************************************
 * Copyright (c) 2009, 2012 VMware Inc.
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

package org.eclipse.gemini.web.internal.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class WebBundleUrlTests {

    private static final String FILE_LOCATION = "file:/tmp/foo.jar";

    @Test
    public void encodeBasicFileUrl() throws MalformedURLException {
        WebBundleUrl url = new TestWarUrl(FILE_LOCATION, null);
        URL u = url.toURL();
        assertNotNull(u);
        assertEquals(WebBundleUrl.SCHEME, u.getProtocol());
        assertEquals(FILE_LOCATION, u.getPath());
        assertNotNull(url.getOptions());
        assertEquals(FILE_LOCATION, url.getLocation());
    }

    @Test
    public void encodeWithOptions() throws Exception {
        Map<String, String> options = new TreeMap<String, String>();
        options.put("Web-ContextPath", "/foo");
        options.put("name", "Rob Harrop");
        WebBundleUrl url = new TestWarUrl(FILE_LOCATION, options);
        URL u = url.toURL();

        assertNotNull(u);

        assertEquals(WebBundleUrl.SCHEME, u.getProtocol());
        assertEquals(FILE_LOCATION, u.getPath());
        assertEquals("Web-ContextPath=/foo&name=Rob Harrop", u.getQuery());
    }

    @Test
    public void createFromUrl() throws Exception {
        URL url = new URL(WebBundleUrl.SCHEME, null, -1, FILE_LOCATION + "?Web-ContextPath=/foo", new DummyHandler());
        WebBundleUrl warUrl = new WebBundleUrl(url);
        assertEquals(FILE_LOCATION, warUrl.getLocation());
        assertEquals("/foo", warUrl.getOptions().get("Web-ContextPath"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFromUrlWithWrongParams() throws Exception {
        URL url = new URL(WebBundleUrl.SCHEME, null, -1, FILE_LOCATION + "?Web-ContextPath", new DummyHandler());
        WebBundleUrl warUrl = new WebBundleUrl(url);
        warUrl.getOptions();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFromUrlWithWrongProtocol() throws Exception {
        URL url = new URL("file:foo.war");
        new WebBundleUrl(url);
    }

    @Test
    public void createFromUrlWithVersionedImports() throws Exception {
        URL url = new URL(WebBundleUrl.SCHEME, null, -1, FILE_LOCATION + "?Import-Package=x;version=1,y;version=2", new DummyHandler());
        WebBundleUrl warUrl = new WebBundleUrl(url);
        assertEquals(FILE_LOCATION, warUrl.getLocation());
        assertEquals("x;version=1,y;version=2", warUrl.getOptions().get("Import-Package"));

        url = new URL(WebBundleUrl.SCHEME, null, -1, FILE_LOCATION + "?Bundle-SymbolicName=test%26test&Import-Package=x;%20version=1,y;%20version=2",
            new DummyHandler());
        warUrl = new WebBundleUrl(url);
        assertEquals(FILE_LOCATION, warUrl.getLocation());
        assertEquals("x; version=1,y; version=2", warUrl.getOptions().get("Import-Package"));
        assertEquals("test&test", warUrl.getOptions().get("Bundle-SymbolicName"));

        url = new URL(WebBundleUrl.SCHEME, null, -1, FILE_LOCATION + "?Bundle-SymbolicName=test+test&Import-Package=x;%20version=1,y;%20version=2",
            new DummyHandler());
        warUrl = new WebBundleUrl(url);
        assertEquals(FILE_LOCATION, warUrl.getLocation());
        assertEquals("x; version=1,y; version=2", warUrl.getOptions().get("Import-Package"));
        assertEquals("test test", warUrl.getOptions().get("Bundle-SymbolicName"));
    }

    private static class TestWarUrl extends WebBundleUrl {

        public TestWarUrl(String location, Map<String, String> options) throws MalformedURLException {
            super(location, options);
        }

        /**
         * @param url to test
         * @throws URISyntaxException thrown by superclass, possibly
         */
        public TestWarUrl(URL url) throws URISyntaxException {
            super(url);
        }

        @Override
        protected URLStreamHandler createURLStreamHandler() {
            return new DummyHandler();
        }

    }

    private static final class DummyHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return null;
        }
    }

}
