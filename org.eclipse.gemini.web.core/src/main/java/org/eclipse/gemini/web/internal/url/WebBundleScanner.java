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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.eclipse.gemini.web.internal.WebContainerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class WebBundleScanner {

    private static final String FILE_SCHEME = "file";

    private static final String JAR_SUFFIX = ".jar";

    private static final String CLASS_SUFFIX = ".class";

    private static final String LIB_ENTRY_PREFIX = "WEB-INF/lib/";

    private static final String CLASSES_ENTRY_PREFIX = "WEB-INF/classes/";

    private static final String CLASS_PATH_ATTRIBUTE_NAME = "Class-Path";

    private static final String CLASS_PATH_SEPARATOR = " ";

    private final Object monitor = new Object();

    private final URL source;

    private final String localSourcePath;

    private final Collection<ZipEntry> sourceZipEntries = new HashSet<>();

    private final WebBundleScannerCallback callBack;

    private final boolean findClassesInNestedJars;

    private final Set<String> scannedJars = new HashSet<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(WebBundleScanner.class);

    WebBundleScanner(URL source, WebBundleScannerCallback callBack) {
        this(source, callBack, false);
    }

    /**
     * Creates a WebBundleScanner for a given WAR with a given callBack.
     * 
     * @param source the WAR content
     * @param callBack The callBack to notify of entries in the WAR
     * @param findClassesInNestedJars
     */
    WebBundleScanner(URL source, WebBundleScannerCallback callBack, boolean findClassesInNestedJars) {
        this.source = source;
        this.callBack = callBack;
        this.findClassesInNestedJars = findClassesInNestedJars;
        this.localSourcePath = getLocalSourcePath(source);
    }

    /**
     * Scans the WAR content from <code>source</code>, notifying the callBack of .class entries in WEB-INF/classes and
     * its sub-directories, and .jar entries in WEB-INF/lib.
     * 
     * @throws IOException if the WAR cannot be scanned
     */
    void scanWar() throws IOException {
        synchronized (this.monitor) {
            this.scannedJars.clear();
            this.sourceZipEntries.clear();
            if (WebContainerUtils.isDirectory(this.source)) {
                scanWarDirectory();
            } else {
                scanWarFile();
            }
        }
    }

    private void scanWarDirectory() throws IOException {
        try {
            java.nio.file.Path bundleDir = WebContainerUtils.sourceAsPath(this.source);
            java.nio.file.Path libDir = bundleDir.resolve("WEB-INF").resolve("lib");
            if (Files.isDirectory(libDir)) {
                doScanLibDirectory(libDir);
            }
            java.nio.file.Path classesDir = bundleDir.resolve("WEB-INF").resolve("classes");
            if (Files.isDirectory(classesDir)) {
                doScanClassesDirectory(classesDir);
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unexpected URISyntaxException.", e);
        }
    }

    private void doScanLibDirectory(java.nio.file.Path libDir) throws IOException {
        try (DirectoryStream<java.nio.file.Path> files = Files.newDirectoryStream(libDir)) {
            for (java.nio.file.Path file : files) {
                if (!Files.isDirectory(file) && file.getFileName().toString().endsWith(JAR_SUFFIX)) {
                    String pathToJar = LIB_ENTRY_PREFIX + file.getFileName().toString();
                    if (driveCallBackIfNewJarFound(pathToJar)) {
                        doScanNestedJar(file);
                    }
                }
            }
        }
    }

    private boolean driveCallBackIfNewJarFound(String pathToJar) {
        // Prevent infinite recursion.
        if (this.scannedJars.contains(pathToJar)) {
            return false;
        }
        this.scannedJars.add(pathToJar);
        this.callBack.jarFound(pathToJar);
        return true;
    }

    private void doScanNestedJar(java.nio.file.Path file) throws IOException {
        try (JarInputStream jis = new JarInputStream(Files.newInputStream(file));) {
            doScanNestedJar(file.toAbsolutePath().toString(), jis);
        } catch (IOException e) {
            throw new IOException("Cannot scan " + file.toAbsolutePath().toString(), e);
        }
    }

    private void doScanClassesDirectory(java.nio.file.Path classesDir) throws IOException {
        try (DirectoryStream<java.nio.file.Path> files = Files.newDirectoryStream(classesDir)) {
            for (java.nio.file.Path file : files) {
                if (Files.isDirectory(file)) {
                    doScanClassesDirectory(file);
                } else if (file.getFileName().toString().endsWith(CLASS_SUFFIX)) {
                    String path = normalizePath(file.toString());
                    this.callBack.classFound(path.substring(path.lastIndexOf(CLASSES_ENTRY_PREFIX) + CLASSES_ENTRY_PREFIX.length()));
                }
            }
        }
    }

    private void scanWarFile() throws IOException {
        try (JarInputStream jis = new JarInputStream(this.source.openStream());) {
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.startsWith(LIB_ENTRY_PREFIX) && entryName.endsWith(JAR_SUFFIX)) {
                    if (driveCallBackIfNewJarFound(entryName)) {
                        JarInputStream nestedJis = new JarInputStream(jis);
                        doScanNestedJar(entryName, nestedJis);
                    }
                } else if (entryName.startsWith(CLASSES_ENTRY_PREFIX) && entryName.endsWith(CLASS_SUFFIX)) {
                    this.callBack.classFound(entry.getName().substring(CLASSES_ENTRY_PREFIX.length()));
                }
            }
        }
    }

    private void doScanNestedJar(String jarEntryName, JarInputStream jis) throws IOException {
        Manifest manifest = jis.getManifest();
        if (manifest != null) {
            Attributes mainAttributes = manifest.getMainAttributes();
            if (mainAttributes != null) {
                String classPath = mainAttributes.getValue(CLASS_PATH_ATTRIBUTE_NAME);
                if (classPath != null) {
                    Path jarPathx = getNormalisedDirectoryPath(jarEntryName);

                    String[] classPathItems = classPath.split(CLASS_PATH_SEPARATOR);
                    for (String classPathItem : classPathItems) {
                        try {
                            scanNestedJarInWar(jarPathx, classPathItem);
                        } catch (IllegalArgumentException _) {
                            // skip invalid relative paths which try to escape the WAR
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("", _);
                            }
                        }
                    }
                }
            }
        }

        if (this.findClassesInNestedJars) {
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.endsWith(CLASS_SUFFIX)) {
                    notifyClassFound(entryName);
                }
            }
        }
    }

    private static Path getNormalisedDirectoryPath(String jarEntryName) {
        String jarPath = normalizePath(jarEntryName);
        int lastDirectoryIndex = jarPath.lastIndexOf("/");
        return lastDirectoryIndex == -1 ? new Path() : new Path(jarPath.substring(0, lastDirectoryIndex));
    }

    private void scanNestedJarInWar(Path directoryPath, String jarPath) throws IOException {
        if (WebContainerUtils.isDirectory(this.source)) {
            scanNestedJarInWarDirectory(directoryPath, jarPath);
        } else {
            scanNestedJarInWarFile(directoryPath, jarPath);
        }
    }

    private void scanNestedJarInWarDirectory(Path directoryPath, String jarPath) throws IOException {
        Path entryPath = directoryPath.applyRelativePath(new Path(jarPath));
        try {
            java.nio.file.Path bundleDir = WebContainerUtils.sourceAsPath(this.source);
            java.nio.file.Path nestedJar = Paths.get(entryPath.toString());
            if (!nestedJar.isAbsolute()) {
                nestedJar = bundleDir.resolve(entryPath.toString());
            }
            if (!Files.isDirectory(nestedJar)) {
                URI pathToJar = bundleDir.toUri().relativize(nestedJar.toUri());
                if (pathToJar.equals(nestedJar.toUri())) {
                    // Do nothing - cannot obtain relative path
                    return;
                }
                if (driveCallBackIfNewJarFound(pathToJar.getPath())) {
                    doScanNestedJar(nestedJar);
                }
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unexpected URISyntaxException.", e);
        }
    }

    private void scanNestedJarInWarFile(Path directoryPath, final String jarPath) throws IOException {
        Path entryPath = directoryPath.applyRelativePath(new Path(jarPath));
        if (this.localSourcePath == null) {
            scanNestedJarInWarFileWithStream(entryPath.toString());
            return;
        }

        scanNestedJarInWarFileWithZipFile(entryPath.toString(), this.localSourcePath);
    }

    private String getLocalSourcePath(final URL url) {
        if (!FILE_SCHEME.equals(url.getProtocol())) {
            return null;
        }
        return URLDecoder.decode(url.getPath());
    }

    private void scanNestedJarInWarFileWithStream(String jarPath) throws IOException {
        try (JarInputStream jis = new JarInputStream(this.source.openStream());) {
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                if (jarPath.endsWith(entryName)) {
                    if (driveCallBackIfNewJarFound(entryName)) {
                        JarInputStream nestedJis = new JarInputStream(jis);
                        doScanNestedJar(entryName, nestedJis);
                    }
                }
            }
        }
    }

    private void scanNestedJarInWarFileWithZipFile(String jarPath, String localSourcePath) throws IOException {
        JarFile jarFile = null;
        try {
            InputStream foundInputStream = null;
            String foundZipEntryName = null;
            if (this.sourceZipEntries.isEmpty()) {// then search and cache all entries
                jarFile = new JarFile(localSourcePath);
                Enumeration<JarEntry> jarFileEntries = jarFile.entries();
                while (jarFileEntries.hasMoreElements()) {
                    final ZipEntry zipEntry = jarFileEntries.nextElement();
                    // 1. cache
                    this.sourceZipEntries.add(zipEntry);
                    // 2. search if it is not found still
                    if (foundZipEntryName == null && jarPath.endsWith(zipEntry.getName())) {
                        foundZipEntryName = zipEntry.getName();
                        foundInputStream = jarFile.getInputStream(zipEntry);
                    }
                }
            } else {// search entry in cache
                for (ZipEntry zipEntry : this.sourceZipEntries) {
                    if (jarPath.endsWith(zipEntry.getName())) {
                        jarFile = new JarFile(localSourcePath);
                        foundZipEntryName = zipEntry.getName();
                        foundInputStream = jarFile.getInputStream(zipEntry);
                        break;
                    }

                }
            }

            if (foundZipEntryName != null && driveCallBackIfNewJarFound(foundZipEntryName)) {
                JarInputStream nestedJis = new JarInputStream(foundInputStream);
                doScanNestedJar(foundZipEntryName, nestedJis);
            }
        } finally {// quiet close
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException _) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("", _);
                    }
                }
            }
        }
    }

    private void notifyClassFound(String entryName) {
        this.callBack.classFound(entryName);
    }

    private static String normalizePath(String path) {
        return path.replace('\\', '/');
    }
}
