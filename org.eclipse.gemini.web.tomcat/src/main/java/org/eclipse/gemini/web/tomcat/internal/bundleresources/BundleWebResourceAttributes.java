/*******************************************************************************
 * Copyright (c) 2015 SAP SE
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

package org.eclipse.gemini.web.tomcat.internal.bundleresources;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

final class BundleWebResourceAttributes {

    private static final long TIME_NOT_SET = -1L;

    private static final int CREATION_DATE_UNKNOWN = 0;

    private static final long CONTENT_LENGTH_NOT_SET = -1;

    private final BundleWebResource resource;

    private long lastModified = TIME_NOT_SET;

    private long creation = TIME_NOT_SET;

    private long contentLength = CONTENT_LENGTH_NOT_SET;

    BundleWebResourceAttributes(BundleWebResource resource) {
        this.resource = resource;
        URLConnection urlConnection = getURLConnection();
        if (urlConnection != null) {
            getLastModified(urlConnection);
            getCreation(urlConnection);
            getContentLength(urlConnection);
        }
    }

    long getContentLength(URLConnection urlConnection) {
        if (this.contentLength == CONTENT_LENGTH_NOT_SET) {
            if (urlConnection == null) {
                urlConnection = getURLConnection();
            }

            if (urlConnection != null) {
                this.contentLength = determineContentLength(urlConnection);
            }
        }

        return this.contentLength;
    }

    long getCreation(URLConnection urlConnection) {
        if (this.creation == TIME_NOT_SET) {
            if (urlConnection == null) {
                urlConnection = getURLConnection();
            }

            if (urlConnection != null) {
                this.creation = urlConnection.getDate();

                if (this.creation == CREATION_DATE_UNKNOWN) {
                    if (this.lastModified == TIME_NOT_SET) {
                        this.lastModified = urlConnection.getLastModified();
                    }

                    this.creation = this.lastModified;
                }
            }
        }

        return this.creation;
    }

    long getLastModified(URLConnection urlConnection) {
        if (this.lastModified == TIME_NOT_SET) {
            if (urlConnection == null) {
                urlConnection = getURLConnection();
            }

            if (urlConnection != null) {
                this.lastModified = urlConnection.getLastModified();
            }
        }

        return this.lastModified;
    }

    private URLConnection getURLConnection() {
        try {
            URL url = this.resource.getURL();
            if (url != null) {
                return url.openConnection();
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Returns the bundle entry size. If the BundleFileResolver is EquinoxBundleFileResolver then we will use equinox
     * specific functionality to get BundleEntry and its size. If the BundleFileResolver is NoOpBundleFileResolver we
     * will use URLConnection.getContentLength(). Note: URLConnection.getContentLength() returns "int", if the bundle
     * entry size exceeds max "int", then the content length will not be correct.
     *
     * @return the bundle entry size
     */
    private long determineContentLength(URLConnection urlConnection) {
        long size = this.resource.resolveBundleWebResourceSize();
        if (size == -1 && urlConnection != null) {
            size = urlConnection.getContentLength();
        }
        return size;
    }
}
