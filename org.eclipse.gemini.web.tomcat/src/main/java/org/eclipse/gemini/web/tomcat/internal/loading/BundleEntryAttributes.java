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

import java.io.IOException;
import java.net.URLConnection;

import javax.naming.directory.Attributes;

import org.apache.naming.resources.ResourceAttributes;

/**
 * {@link BundleEntryAttributes} provides the default {@link Attributes} for a {@link BundleEntry}.
 */
final class BundleEntryAttributes extends ResourceAttributes {

    private static final int CREATION_DATE_UNKNOWN = 0;

    private static final long TIME_NOT_SET = -1L;

    private static final long serialVersionUID = 7799793247259935763L;

    private final transient BundleEntry bundleEntry;

    private final String[] attrIds;

    private long lastModified = TIME_NOT_SET;

    /**
     * Creates a {@link BundleEntryAttributes} for the given {@link BundleEntry} and attribute identifier array.
     * 
     * @param bundleEntry the <code>BundleEntry</code> from which to retrieve attributes
     * @param attrIds the identifiers of the attributes to retrieve, or <code>null</code> if all attributes should be
     *        retrieved
     */
    BundleEntryAttributes(BundleEntry bundleEntry, String[] attrIds) {
        this.bundleEntry = bundleEntry;
        this.attrIds = attrIds;
        setCollection(this.bundleEntry.isDirectory());
        getName();
        getLastModified();
        getCreation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCreation() {
        long creation = TIME_NOT_SET;

        if (attrPresent(CREATION_DATE) || attrPresent(ALTERNATE_CREATION_DATE)) {
            creation = super.getCreation();

            if (creation == TIME_NOT_SET) {
                try {
                    URLConnection urlConnection = this.bundleEntry.getURL().openConnection();
                    creation = urlConnection.getDate();
                    if (creation == CREATION_DATE_UNKNOWN) {
                        creation = determineLastModified();
                    }
                    setCreation(creation);
                } catch (IOException _) {
                }
            }
        }

        return creation;
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
        long lastModified = TIME_NOT_SET;

        if (attrPresent(LAST_MODIFIED) || attrPresent(ALTERNATE_LAST_MODIFIED)) {
            lastModified = super.getLastModified();

            if (lastModified == TIME_NOT_SET) {
                lastModified = determineLastModified();

                if (lastModified != TIME_NOT_SET) {
                    setLastModified(lastModified);
                }
            }
        }

        return lastModified;
    }

    private long determineLastModified() {
        if (this.lastModified == TIME_NOT_SET) {
            try {
                URLConnection urlConnection = this.bundleEntry.getURL().openConnection();
                this.lastModified = urlConnection.getLastModified();
            } catch (IOException _) {
            }
        }
        return this.lastModified;
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

}
