/*******************************************************************************
 * Copyright (c) 2010 SAP AG
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.virgo.util.io.PathReference;

/**
 * Implementation of {@link URLConnection} that transforms directory files as they are read.
 * <p/>
 * A {@link URL} is used to source the real connection for the directory data, and a {@link DirTransformer} is used to
 * customize the exact transformations being performed.
 * 
 * @see DirTransformer
 */
final class DirTransformingURLConnection extends URLConnection {

    private static final String TEMP_DIR = "file:temp/";

    private final DirTransformer transformer;

    private final boolean ensureManifestIsPresent;
    
    private final Object monitor = new Object();

    private URL transformedURL;

    /**
     * Creates a new <code>DirTransformingURLConnection</code> that will provide content from the directory identified
     * by <code>url</code> transformed by <code>transformer</code>.
     * 
     * @param url the {@link URL} of the directory.
     * @param transformer the <code>DirTransformer</code> to apply as content is being read.
     * @throws MalformedURLException the exception is thrown in case the new URL, where is the transformed data, cannot
     *         be created.
     * @throws URISyntaxException
     */
    DirTransformingURLConnection(URL url, DirTransformer transformer) throws MalformedURLException {
        this(url, transformer, false);
    }

    /**
     * Creates a new <code>DirTransformingURLConnection</code> that will provide content from the directory identified
     * by <code>url</code> transformed by <code>transformer</code> and that will optionally ensure that a manifest is
     * provided, creating one if necessary.
     * 
     * @param url the {@link URL} of the directory.
     * @param transformer the <code>DirTransformer</code> to apply as content is being read.
     * @param ensureManifestIsPresent <code>true</code> if the presence of a MANIFEST.MF should be ensured.
     * @throws MalformedURLException the exception is thrown in case the new URL, where is the transformed data, cannot
     *         be created.
     * @throws URISyntaxException
     */
    DirTransformingURLConnection(URL url, DirTransformer transformer, boolean ensureManifestIsPresent) throws MalformedURLException {
        super(url);
        this.transformer = transformer;
        this.ensureManifestIsPresent = ensureManifestIsPresent;

        this.transformedURL = new URL(TEMP_DIR + getPath());
        PathReference transformedDir = new PathReference(this.transformedURL.getPath());
        transformedDir.delete(true);
    }

    @Override
    public void connect() throws IOException {
    }

    /**
     * Transform the URL before returning the input stream.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        synchronized (monitor) {
            this.transformer.transform(this.url, this.transformedURL, this.ensureManifestIsPresent);
            return this.transformedURL.openStream();
        }
    }

    private String getPath() {
        String path = this.url.getPath();
        int index = path.lastIndexOf('/');
        if (index > -1) {
            path = path.substring(index + 1);
        }

        return path;
    }

    /**
     * Returns transformed URL instead of the original one.
     */
    @Override
    public URL getURL() {
        synchronized (monitor) {
            return this.transformedURL;
        }
    }

    void setTransformedURL(URL transformedURL) {
        synchronized (monitor) {
            this.transformedURL = transformedURL;
        }
    }

}
