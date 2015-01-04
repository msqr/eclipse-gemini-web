/*******************************************************************************
 * Copyright (c) 2015 SAP AG
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
 *   Violeta Georgieva - initial contribution
 *******************************************************************************/

package org.eclipse.gemini.web.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FileUtils {

    private FileUtils() {
    }

    public static boolean deleteDirectory(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static Path unpackToDir(Path source, Path destination) throws IOException {
        if (Files.exists(destination)) {
            deleteDirectory(destination);
        }
        Files.createDirectories(destination);
        try (ZipFile zip = new ZipFile(source.toFile());) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path entryPath = destination.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zip.getInputStream(entry), entryPath);
                }
            }
        }
        return destination;
    }

    public static void copy(Path inPath, Path outPath) throws IOException {
        try (BufferedReader in = Files.newBufferedReader(inPath, StandardCharsets.UTF_8);
            BufferedWriter out = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8);) {
            String line = null;
            while ((line = in.readLine()) != null) {
                out.write(line);
            }
            out.flush();
        }
    }
}