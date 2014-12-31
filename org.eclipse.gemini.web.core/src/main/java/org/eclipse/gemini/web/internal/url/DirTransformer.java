/*******************************************************************************
 * Copyright (c) 2010, 2014 SAP AG
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

package org.eclipse.gemini.web.internal.url;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.jar.JarFile;

import org.eclipse.virgo.util.io.PathReference;

/**
 * Utility class for transforming the files in a directory.
 * <p/>
 * Files cannot be added, only changed or removed. Actual transformation of files is performed by an implementation of
 * the {@link DirTransformerCallback} interface.
 */
final class DirTransformer {

    /**
     * Callback interface used to transform files in a directory.
     * 
     * @see DirTransformer
     */
    static interface DirTransformerCallback {

        /**
         * Transform the supplied file.
         * <p/>
         * File content can be read from the supplied {@link InputStream} and transformed contents can be written to the
         * supplied {@link File}.
         * <p/>
         * Implementations <strong>must</strong> return <code>true</code> if the file was transformed or deleted.
         * Otherwise, <code>false</code> must be returned. No content should be written when not performing a
         * transformation.
         * <p/>
         * Implementations transforming a file must save the file as <code>toFile</code>. Implementations deleting a
         * file must not save the file as <code>toFile</code>.
         * 
         * @param inputStream the {@link InputStream} that will be transformed
         * @param toFile the transformed {@link File}
         * @return <code>true</code> if the file was transformed, otherwise <code>false</code>
         * @throws IOException if transformation fails
         */
        boolean transformFile(InputStream inputStream, PathReference toFile) throws IOException;
    }

    private static final String MANIFEST_VERSION_HEADER = "Manifest-Version: 1.0";

    private final DirTransformerCallback callback;

    /**
     * Creates a new <code>DirTransformer</code> that uses the supplied {@link DirTransformerCallback} for
     * transformation.
     * 
     * @param callback the <code>DirTransformerCallback</code> to use for file transformation.
     */
    DirTransformer(DirTransformerCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback must not be null");
        }
        this.callback = callback;
    }

    /**
     * Transforms the directory content in <code>url</code> and writes the results to <code>transformedUrl</code>.
     * 
     * @param url the {@link URL} of the directory to transform.
     * @param transformedUrl the {@link URL} to write the transformed directory to.
     * @throws IOException if the directory cannot be transformed.
     */
    void transform(URL url, URL transformedUrl) throws IOException {
        transform(url, transformedUrl, false);
    }

    /**
     * Transforms the directory content in <code>url</code> and writes the results to <code>transformedUrl</code>.
     * 
     * @param url the {@link URL} of the directory to transform.
     * @param transformedUrl the {@link URL} to write the transformed directory to.
     * @param ensureManifestIsPresent if <code>true</code> ensures that the transformed directory contains a manifest.
     * @throws IOException if the directory cannot be transformed.
     */
    void transform(URL url, URL transformedUrl, boolean ensureManifestIsPresent) throws IOException {
        PathReference fromDirectory = new PathReference(url.getPath());
        PathReference toDirectory = new PathReference(transformedUrl.getPath());
        transformDir(fromDirectory, toDirectory);

        PathReference manifest = fromDirectory.newChild(JarFile.MANIFEST_NAME);
        if (ensureManifestIsPresent && !manifest.exists()) {
            PathReference toFile = toDirectory.newChild(JarFile.MANIFEST_NAME);
            toFile.getParent().createDirectory();
            try (InputStream defaultManifestStream = getDefaultManifestStream();) {
                this.callback.transformFile(defaultManifestStream, toFile);
            }
        }
    }

    private void transformDir(PathReference fromDirectory, PathReference toDirectory) throws IOException {
        File[] fileList = fromDirectory.toFile().listFiles();
        PathReference fromFile = null;
        for (int i = 0; fileList != null && i < fileList.length; i++) {
            fromFile = new PathReference(fileList[i]);
            PathReference toFile = toDirectory.newChild(fromFile.getName());
            if (!fromFile.isDirectory()) {
                transformFile(fromFile, toFile);
            } else {
                transformDir(fromFile, toFile);
            }
        }
    }

    private void transformFile(PathReference fromFile, PathReference toFile) throws IOException {
        boolean transformed = false;
        try (FileInputStream fis = new FileInputStream(fromFile.toFile());) {
            transformed = this.callback.transformFile(fis, toFile);
        }
        if (!transformed) {
            toFile.getParent().createDirectory();
            fromFile.copy(toFile);
        }
    }

    private InputStream getDefaultManifestStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos);) {
            writer.println(MANIFEST_VERSION_HEADER);
            writer.println();
            return new ByteArrayInputStream(baos.toByteArray());
        }
    }
}
