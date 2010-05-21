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

package org.eclipse.gemini.web.internal.url;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.eclipse.gemini.web.internal.url.SpecificationWebBundleManifestTransformer;
import org.eclipse.gemini.web.internal.url.WebBundleUrl;
import org.eclipse.gemini.web.internal.url.WebBundleUrlStreamHandlerService;
import org.junit.Test;

public class WebBundleUrlStreamHandlerServiceTests {

    @Test
    public void testOpenWarConnection() throws Exception {
        WebBundleUrl url = new TestWarUrl("file:src/test/resources/simple-war.war?Web-ContextPath=/", null);
        URLConnection connection = url.toURL().openConnection();
        assertNotNull(connection);
        
        InputStream inputStream = connection.getInputStream();
        JarInputStream jarInputStream = new JarInputStream(inputStream);
        Manifest manifest = jarInputStream.getManifest();
        
        if (manifest != null) {
            Attributes mainAttributes = manifest.getMainAttributes();
            Set<Entry<Object, Object>> entrySet = mainAttributes.entrySet();
            for (Entry<Object, Object> entry : entrySet) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
    }
    
    private static class TestWarUrl extends WebBundleUrl {

        public TestWarUrl(String location, Map<String, String> options) throws MalformedURLException {
            super(location, options);
        }

        public TestWarUrl(URL url) {
            super(url);
        }

        @Override
        protected URLStreamHandler createURLStreamHandler() {
            return new WebBundleUrlStreamHandlerService(new SpecificationWebBundleManifestTransformer());
        }                
    }
}
