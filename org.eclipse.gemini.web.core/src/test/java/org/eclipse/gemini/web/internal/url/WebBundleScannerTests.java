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

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.easymock.EasyMock;
import org.eclipse.virgo.util.io.JarUtils;
import org.eclipse.virgo.util.io.PathReference;
import org.junit.Test;

public class WebBundleScannerTests {

    private static final File WAR_FILE = new File("target/resources/simple-war.war");

    private static final File WAR_CLASSPATHDEPS = new File("../org.eclipse.gemini.web.test/src/test/resources/classpathdeps.war");

    @Test
    public void testScanClasspathDeps() throws IOException {
        final WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);

        setExpectationsClasspathDeps(callback);

        scan(WAR_CLASSPATHDEPS.toURI().toURL(), callback);
    }

    @Test
    public void testScanLib() throws IOException {
        WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);

        setExpectations(callback);

        scan(WAR_FILE.toURI().toURL(), callback);
    }

    private void setExpectationsClasspathDeps(WebBundleScannerCallback callback) {
        callback.jarFound("WEB-INF/lib/jar1.jar");
        callback.jarFound("j2/jar2.jar");
        callback.jarFound("j3/jar3.jar");
        callback.jarFound("j4/jar4.jar");
    }

    private void setExpectations(WebBundleScannerCallback callback) {
        callback.jarFound("WEB-INF/lib/com.springsource.slf4j.api-1.6.1.jar");
        callback.classFound("foo/bar/Doo.class");
    }

    private void setExpectationsIncludingNestedJars(WebBundleScannerCallback callback) {
        setExpectations(callback);

        callback.classFound("org/slf4j/ILoggerFactory.class");
        callback.classFound("org/slf4j/IMarkerFactory.class");
        callback.classFound("org/slf4j/Logger.class");
        callback.classFound("org/slf4j/LoggerFactory.class");
        callback.classFound("org/slf4j/Marker.class");
        callback.classFound("org/slf4j/MarkerFactory.class");
        callback.classFound("org/slf4j/MDC.class");
        callback.classFound("org/slf4j/helpers/BasicMarker.class");
        callback.classFound("org/slf4j/helpers/BasicMarkerFactory.class");
        callback.classFound("org/slf4j/helpers/BasicMDCAdapter.class");
        callback.classFound("org/slf4j/helpers/FormattingTuple.class");
        callback.classFound("org/slf4j/helpers/MarkerIgnoringBase.class");
        callback.classFound("org/slf4j/helpers/MessageFormatter.class");
        callback.classFound("org/slf4j/helpers/NamedLoggerBase.class");
        callback.classFound("org/slf4j/helpers/NOPLogger.class");
        callback.classFound("org/slf4j/helpers/NOPLoggerFactory.class");
        callback.classFound("org/slf4j/helpers/NOPMDCAdapter.class");
        callback.classFound("org/slf4j/helpers/SubstituteLoggerFactory.class");
        callback.classFound("org/slf4j/helpers/Util.class");
        callback.classFound("org/slf4j/spi/LocationAwareLogger.class");
        callback.classFound("org/slf4j/spi/LoggerFactoryBinder.class");
        callback.classFound("org/slf4j/spi/MarkerFactoryBinder.class");
        callback.classFound("org/slf4j/spi/MDCAdapter.class");
    }

    @Test
    public void testScanLibIncludingNestedJars() throws IOException {

        WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);

        setExpectationsIncludingNestedJars(callback);

        scan(WAR_FILE.toURI().toURL(), callback, true);
    }

    @Test
    public void testScanDir() throws Exception {
        PathReference pr = unpackToDir(WAR_FILE);
        try {
            WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);

            setExpectations(callback);

            scan(pr.toURI().toURL(), callback);
        } finally {
            pr.delete(true);
        }
    }

    @Test
    public void testScanDirIncludingNestedJars() throws Exception {
        PathReference pr = unpackToDir(WAR_FILE);
        try {
            WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);

            setExpectationsIncludingNestedJars(callback);

            scan(pr.toURI().toURL(), callback, true);
        } finally {
            pr.delete(true);
        }
    }

    @Test
    public void testScanDirIncludingClasspathDeps() throws Exception {
        PathReference pr = unpackToDir(WAR_CLASSPATHDEPS);
        try {
            WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);

            setExpectationsClasspathDeps(callback);

            scan(pr.toURI().toURL(), callback, true);
        } finally {
            pr.delete(true);
        }
    }

    private void scan(final URL url, final WebBundleScannerCallback callback) throws IOException {
        this.scan(url, callback, false);
    }

    private void scan(final URL url, final WebBundleScannerCallback callback, final boolean findClassesInNestedJars) throws IOException {
        replay(callback);

        final WebBundleScanner scanner = new WebBundleScanner(url, callback, findClassesInNestedJars);
        scanner.scanWar();

        verify(callback);
    }

    private PathReference unpackToDir(File warFile) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        PathReference dest = new PathReference(new File(tmpDir, "unpack-" + System.currentTimeMillis()));
        PathReference src = new PathReference(warFile);
        JarUtils.unpackTo(src, dest);
        return dest;
    }
}
