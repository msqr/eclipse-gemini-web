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

package org.eclipse.gemini.web.tomcat.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.tomcat.JarScannerCallback;
import org.eclipse.gemini.web.tomcat.internal.loading.BundleWebappClassLoader;
import org.eclipse.gemini.web.tomcat.internal.support.BundleDependencyDeterminer;
import org.eclipse.gemini.web.tomcat.internal.support.BundleFileResolver;
import org.eclipse.gemini.web.tomcat.spi.ClassLoaderCustomizer;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 */
public class BundleDependenciesJarScannerTests {

    private final BundleDependencyDeterminer dependencyDeterminer = createMock(BundleDependencyDeterminer.class);

    private final BundleFileResolver bundleFileResolver = createMock(BundleFileResolver.class);

    private final StubBundleContext bundleContext = new StubBundleContext();

    private final BundleDependenciesJarScanner scanner = new BundleDependenciesJarScanner(this.dependencyDeterminer, this.bundleFileResolver,
        this.bundleContext);

    private final Bundle bundle = createMock(Bundle.class);

    private final JarScannerCallback callback = createMock(JarScannerCallback.class);

    private final ClassLoaderCustomizer classLoaderCustomizer = createNiceMock(ClassLoaderCustomizer.class);

    private final Bundle dependency = createMock(Bundle.class);

    @Test
    public void noDependencies() {
        expect(this.bundle.getHeaders()).andReturn(new Hashtable<String, String>());
        expect(this.dependencyDeterminer.getDependencies(this.bundle)).andReturn(Collections.<Bundle> emptySet());

        replay(this.dependencyDeterminer, this.bundleFileResolver, this.bundle, this.callback);

        ClassLoader classLoader = new BundleWebappClassLoader(this.bundle, this.classLoaderCustomizer);

        this.scanner.scan(null, classLoader, this.callback, null);

        verify(this.dependencyDeterminer, this.bundleFileResolver, this.bundle, this.callback);
    }

    @Test
    public void scanDirectory() throws IOException {
        expect(this.bundle.getHeaders()).andReturn(new Hashtable<String, String>());
        expect(this.dependencyDeterminer.getDependencies(this.bundle)).andReturn(new HashSet<Bundle>(Arrays.asList(this.dependency)));

        File dependencyFile = new File("src/test/resources");
        expect(this.bundleFileResolver.resolve(this.dependency)).andReturn(dependencyFile);
        this.callback.scan(dependencyFile);

        replay(this.dependencyDeterminer, this.bundleFileResolver, this.bundle, this.callback);

        ClassLoader classLoader = new BundleWebappClassLoader(this.bundle, this.classLoaderCustomizer);

        this.scanner.scan(null, classLoader, this.callback, null);

        verify(this.dependencyDeterminer, this.bundleFileResolver, this.bundle, this.callback);
    }

    @Test
    public void scanFile() throws IOException {
        expect(this.bundle.getHeaders()).andReturn(new Hashtable<String, String>());
        expect(this.dependencyDeterminer.getDependencies(this.bundle)).andReturn(new HashSet<Bundle>(Arrays.asList(this.dependency)));

        File dependencyFile = new File("");
        expect(this.bundleFileResolver.resolve(this.dependency)).andReturn(dependencyFile);
        this.callback.scan(isA(JarURLConnection.class));

        replay(this.dependencyDeterminer, this.bundleFileResolver, this.bundle, this.callback);

        ClassLoader classLoader = new BundleWebappClassLoader(this.bundle, this.classLoaderCustomizer);

        this.scanner.scan(null, classLoader, this.callback, null);

        verify(this.dependencyDeterminer, this.bundleFileResolver, this.bundle, this.callback);
    }

    @Test
    public void scanJarUrlConnection() throws IOException {
        expect(this.bundle.getHeaders()).andReturn(new Hashtable<String, String>());
        expect(this.dependencyDeterminer.getDependencies(this.bundle)).andReturn(new HashSet<Bundle>(Arrays.asList(this.dependency))).times(2);
        expect(this.dependency.getLocation()).andReturn("file:src/test/resources/bundle.jar").andReturn(
            "reference:file:src/test/resources/bundle.jar");
        expect(this.dependency.getSymbolicName()).andReturn("bundle").anyTimes();

        expect(this.bundleFileResolver.resolve(this.dependency)).andReturn(null).times(2);
        this.callback.scan(isA(JarURLConnection.class));

        replay(this.dependencyDeterminer, this.bundleFileResolver, this.bundle, this.callback, this.dependency);

        ClassLoader classLoader = new BundleWebappClassLoader(this.bundle, this.classLoaderCustomizer);

        this.scanner.scan(null, classLoader, this.callback, null);

        this.scanner.scan(null, classLoader, this.callback, null);

        verify(this.dependencyDeterminer, this.bundleFileResolver, this.bundle, this.callback);
    }
}
