/*******************************************************************************
 * Copyright (c) 2009, 2013 VMware Inc.
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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.naming.directory.Attributes;

import org.apache.naming.resources.ResourceAttributes;

/**
 * {@link BundleEntryAttributes} provides the default {@link Attributes} for a {@link BundleEntry}.
 */
final class BundleEntryAttributes extends ResourceAttributes {

    private static final int CREATION_DATE_UNKNOWN = 0;

    private static final long TIME_NOT_SET = -1L;

    private static final long CONTENT_LENGTH_NOT_SET = -1L;

    private static final long serialVersionUID = 7799793247259935763L;

    private final transient BundleEntry bundleEntry;

    private final String[] attrIds;

    /**
     * Creates a {@link BundleEntryAttributes} for the given {@link BundleEntry} and attribute identifier array.
     * 
     * @param bundleEntry the <code>BundleEntry</code> from which to retrieve attributes
     * @param attrIds the identifiers of the attributes to retrieve, or <code>null</code> if all attributes should be
     *        retrieved
     */
    BundleEntryAttributes(BundleEntry bundleEntry, String[] attrIds, URL url) {
        this.bundleEntry = bundleEntry;
        this.attrIds = attrIds;
        getName();
        if (url != null) {
            setCollection(BundleEntry.isDirectory(url));
        }
        URLConnection urlConnection = getBundleEntryURLConnection(url);
        if (urlConnection != null) {
            long lastModified = getLastModified(urlConnection);
            getCreation(urlConnection, lastModified);
            getContentLength(urlConnection);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCreation() {
        return getCreation(null, TIME_NOT_SET);
    }

    private long getCreation(URLConnection urlConnection, long lastModified) {
        long creation = TIME_NOT_SET;

        if (attrPresent(CREATION_DATE) || attrPresent(ALTERNATE_CREATION_DATE)) {
            creation = super.getCreation();

            if (creation == TIME_NOT_SET) {
                if (urlConnection == null) {
                    urlConnection = getBundleEntryURLConnection(this.bundleEntry.getURL());
                }

                if (urlConnection != null) {
                    creation = determineDate(urlConnection);

                    if (creation == CREATION_DATE_UNKNOWN) {
                        if (lastModified == TIME_NOT_SET) {
                            lastModified = determineLastModified(urlConnection);
                        }

                        creation = lastModified;
                    }
                }

                if (creation != TIME_NOT_SET) {
                    setCreation(creation);
                }
            }
        }

        return creation;
    }

    private long determineDate(URLConnection urlConnection) {
        return urlConnection.getDate();
    }

    private boolean attrPresent(String attrId) {
        if (this.attrIds == null) {
            return true;
        }

        for (String ai : this.attrIds) {
            if (ai.equals(attrId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLastModified() {
        return getLastModified(null);
    }

    private long getLastModified(URLConnection urlConnection) {
        long lastModified = TIME_NOT_SET;

        if (attrPresent(LAST_MODIFIED) || attrPresent(ALTERNATE_LAST_MODIFIED)) {
            lastModified = super.getLastModified();

            if (lastModified == TIME_NOT_SET) {
                if (urlConnection == null) {
                    urlConnection = getBundleEntryURLConnection(this.bundleEntry.getURL());
                }

                if (urlConnection != null) {
                    lastModified = determineLastModified(urlConnection);
                }

                if (lastModified != TIME_NOT_SET) {
                    setLastModified(lastModified);
                }
            }
        }

        return lastModified;
    }

    private long determineLastModified(URLConnection urlConnection) {
        return urlConnection.getLastModified();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        if (!attrPresent(NAME)) {
            return null;
        }

        String name = super.getName();
        if (name == null) {
            name = this.bundleEntry.getName();
        }
        return name;
    }

    @Override
    public long getContentLength() {
        return getContentLength(null);
    }

    private long getContentLength(URLConnection urlConnection) {
        long contentLength = CONTENT_LENGTH_NOT_SET;

        if (attrPresent(CONTENT_LENGTH) || attrPresent(ALTERNATE_CONTENT_LENGTH)) {
            contentLength = super.getContentLength();

            if (contentLength == CONTENT_LENGTH_NOT_SET) {
                if (urlConnection == null) {
                    urlConnection = getBundleEntryURLConnection(this.bundleEntry.getURL());
                }

                contentLength = determineContentLength(urlConnection);

                if (contentLength != CONTENT_LENGTH_NOT_SET) {
                    setContentLength(contentLength);
                }
            }
        }

        return contentLength;
    }

    private long determineContentLength(URLConnection urlConnection) {
        return this.bundleEntry.getContentLength(urlConnection);
    }

    private URLConnection getBundleEntryURLConnection(URL url) {
        try {
            if (url != null) {
                return url.openConnection();
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

}
