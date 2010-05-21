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

import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import org.eclipse.gemini.web.tomcat.internal.loading.ChainedClassLoader;
import org.junit.Test;



public class ChainedClassLoaderTests {

    @Test
    public void testLoadClass() throws Exception {
        ChainedClassLoader loader = new ChainedClassLoader(classLoaderFor(Test.class), classLoaderFor(ChainedClassLoaderTests.class));
        assertNotNull(loader.loadClass(Test.class.getName()));
        assertNotNull(loader.loadClass(ChainedClassLoaderTests.class.getName()));
    }
    
    @Test
    public void testGetResource() throws Exception {
        ChainedClassLoader loader = new ChainedClassLoader(classLoaderFor(Test.class), classLoaderFor(ChainedClassLoaderTests.class));
        URL resource = loader.getResource("invalid-server.xml");
        assertNotNull(resource);
    }
    
    @Test
    public void testGetResources() throws Exception {
        ChainedClassLoader loader = new ChainedClassLoader(classLoaderFor(Test.class), classLoaderFor(ChainedClassLoaderTests.class));
        Enumeration<URL> resources = loader.getResources("META-INF/MANIFEST.MF");
        assertNotNull(resources);
    }
    
    private ClassLoader classLoaderFor(Class<?> cls) {
        URL location = cls.getProtectionDomain().getCodeSource().getLocation();
        return new URLClassLoader(new URL[]{location}, null);
    }
}
