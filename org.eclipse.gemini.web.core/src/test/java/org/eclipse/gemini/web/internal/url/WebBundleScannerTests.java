/*******************************************************************************
 * Copyright (c) 2009, 2015 VMware Inc.
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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.easymock.EasyMock;
import org.junit.Test;

public class WebBundleScannerTests {

    private static final Path WAR_FILE = Paths.get("target/resources/simple-war.war");

    private static final Path WAR_CLASSPATHDEPS = Paths.get("../org.eclipse.gemini.web.test/src/test/resources/classpathdeps.war");

    private static final Path WAR_WITH_CORRUPTED_JAR = Paths.get("src/test/resources/contains-jar-with-bad-formated-manifest");

    @Test
    public void testScanClasspathDeps() throws IOException {
        final WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);

        setExpectationsClasspathDeps(callback);

        scan(WAR_CLASSPATHDEPS.toUri().toURL(), callback);
    }

    @Test
    public void testScanLib() throws IOException {
        WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);

        setExpectations(callback);

        scan(WAR_FILE.toUri().toURL(), callback);
    }

    private void setExpectationsClasspathDeps(WebBundleScannerCallback callback) {
        callback.jarFound("WEB-INF/lib/jar1.jar");
        callback.jarFound("j2/jar2.jar");
        callback.jarFound("j3/jar3.jar");
        callback.jarFound("j4/jar4.jar");
    }

    private void setExpectations(WebBundleScannerCallback callback) {
        callback.jarFound("WEB-INF/lib/org.slf4j.api-1.7.2.v20121108-1250.jar");
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

        scan(WAR_FILE.toUri().toURL(), callback, true);
    }

    @Test
    public void testScanDir() throws Exception {
        Path pr = unpackToDir(WAR_FILE);
        try {
            WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);

            setExpectations(callback);

            scan(pr.toUri().toURL(), callback);
        } finally {
            FileUtils.deleteDirectory(pr);
        }
    }

    @Test
    public void testScanDirIncludingNestedJars() throws Exception {
        Path pr = unpackToDir(WAR_FILE);
        try {
            WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);

            setExpectationsIncludingNestedJars(callback);

            scan(pr.toUri().toURL(), callback, true);
        } finally {
            FileUtils.deleteDirectory(pr);
        }
    }

    @Test
    public void testScanDirIncludingClasspathDeps() throws Exception {
        Path pr = unpackToDir(WAR_CLASSPATHDEPS);
        try {
            WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);

            setExpectationsClasspathDeps(callback);

            scan(pr.toUri().toURL(), callback, true);
        } finally {
            FileUtils.deleteDirectory(pr);
        }
    }

    @Test(expected = IOException.class)
    public void testScanDirWithCorruptedNestedJars() throws Exception {
        WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);
        callback.jarFound("WEB-INF/lib/jarfile.jar");
        scan(WAR_WITH_CORRUPTED_JAR.toUri().toURL(), callback, true);
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

    private Path unpackToDir(Path warFile) throws IOException {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"), "unpack-" + System.currentTimeMillis());
        Files.createDirectories(destination);
        try (ZipFile zip = new ZipFile(warFile.toFile());) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path entryPath = destination.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.copy(zip.getInputStream(entry), entryPath);
                }
            }
        }
        return destination;
    }
}
