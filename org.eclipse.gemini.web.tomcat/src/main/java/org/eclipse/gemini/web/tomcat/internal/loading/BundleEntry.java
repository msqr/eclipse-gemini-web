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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.gemini.web.tomcat.internal.support.BundleFileResolver;
import org.eclipse.gemini.web.tomcat.internal.support.BundleFileResolverFactory;
import org.osgi.framework.Bundle;

public final class BundleEntry {

    private static final String WEB_XML = "web.xml";

    private static final String WEB_INF = "WEB-INF";

    private static final String WEB_INF_DOT = "WEB-INF.";

    private static final String META_INF_DOT = "META-INF.";

    private static final String META_INF = "META-INF";

    private static final String OSGI_INF_DOT = "OSGI-INF.";

    private static final String OSGI_OPT_DOT = "OSGI-OPT.";

    private static final String PATH_SEPARATOR = "/";

    private static final String DOT = ".";

    private final String path;

    private final Bundle bundle;

    private final BundleFileResolver bundleFileResolver = BundleFileResolverFactory.createBundleFileResolver();

    private boolean checkEntryPath;

    public BundleEntry(Bundle bundle) {
        this(bundle, "");
    }

    private BundleEntry(Bundle bundle, String path) {
        this.path = path;
        this.bundle = bundle;
        try {
            this.checkEntryPath = new File(META_INF).getCanonicalPath().equals(new File(META_INF_DOT).getCanonicalPath());
        } catch (IOException e) {
            this.checkEntryPath = true;
        }
    }

    public Bundle getBundle() {
        return this.bundle;
    }

    public List<BundleEntry> list() {
        List<BundleEntry> entries = new ArrayList<BundleEntry>();
        Enumeration<?> paths = getEntryPathsFromBundle();
        if (paths != null) {
            while (paths.hasMoreElements()) {
                String subPath = (String) paths.nextElement();
                entries.add(createBundleEntry(subPath));
            }
        }
        return entries;
    }

    private BundleEntry createBundleEntry(String path) {
        return new BundleEntry(this.bundle, path);
    }

    private Enumeration<?> getEntryPathsFromBundle() {
        final Enumeration<?> ep = this.bundle.getEntryPaths(this.path);

        Set<String> paths = new HashSet<String>();
        if (ep != null) {
            while (ep.hasMoreElements()) {
                paths.add((String) ep.nextElement());
            }
        }

        // Ensure web.xml appears even though it may be supplied by a fragment.
        if (WEB_INF.equals(this.path) && getEntry(WEB_XML) != null) {
            paths.add(WEB_INF + PATH_SEPARATOR + WEB_XML);
        }

        if (paths.isEmpty()) {
            return null;
        }

        final String[] pathArray = paths.toArray(new String[0]);

        return new Enumeration<String>() {

            private int pos = 0;

            @Override
            public boolean hasMoreElements() {
                return this.pos < pathArray.length;
            }

            @Override
            public String nextElement() {
                if (hasMoreElements()) {
                    return pathArray[this.pos++];
                }
                return null;
            }

        };
    }

    public BundleEntry getEntry(String subPath) {
        String finalPath = this.path + subPath;
        if (getEntryFromBundle(finalPath) != null) {
            return createBundleEntry(finalPath);
        } else {
            return null;
        }
    }

    private URL getEntryFromBundle(String path) {
        /*
         * This method has been generalised from this.bundle.getEntry(path) to allow web.xml to be supplied by a
         * fragment.
         */
        if (this.checkEntryPath
            && (checkNotAttemptingToAccess(path, META_INF_DOT) || checkNotAttemptingToAccess(path, WEB_INF_DOT)
                || checkNotAttemptingToAccess(path, OSGI_INF_DOT) || checkNotAttemptingToAccess(path, OSGI_OPT_DOT))) {
            return null;
        }

        if (path.endsWith(PATH_SEPARATOR) || path.length() == 0) {
            return this.bundle.getEntry(path);
        }
        String searchPath;
        String searchFile;
        int lastSlashIndex = path.lastIndexOf(PATH_SEPARATOR);
        if (lastSlashIndex == -1) {
            searchPath = PATH_SEPARATOR;
            searchFile = path;
        } else {
            searchPath = path.substring(0, lastSlashIndex);
            searchFile = path.substring(lastSlashIndex + 1);
        }

        if (searchFile.equals(DOT)) {
            return this.bundle.getEntry(path.substring(0, path.length() - 1));
        }

        Enumeration<?> entries = this.bundle.findEntries(searchPath, searchFile, false);

        if (entries != null) {
            if (entries.hasMoreElements()) {
                return (URL) entries.nextElement();
            }
        }

        return null;
    }

    private boolean checkNotAttemptingToAccess(String path, String prefix) {
        return path.startsWith(prefix + PATH_SEPARATOR) || path.startsWith(PATH_SEPARATOR + prefix + PATH_SEPARATOR)
            || path.startsWith(DOT + PATH_SEPARATOR + prefix + PATH_SEPARATOR);
    }

    public String getName() {
        String name = this.path;

        if (name.endsWith(PATH_SEPARATOR)) {
            name = name.substring(0, this.path.length() - 1);
        }

        int index = name.lastIndexOf(PATH_SEPARATOR);
        if (index > -1) {
            name = name.substring(index + 1);
        }

        if (name.length() == 0) {
            return PATH_SEPARATOR;
        } else {
            return name;
        }
    }

    public URL getURL() {
        return getEntryFromBundle(this.path);
    }

    public String getPath() {
        return this.path;
    }

    public boolean isDirectory() {
        URL entryFromBundle = getEntryFromBundle(this.path);
        return entryFromBundle.getFile().endsWith(PATH_SEPARATOR);
    }

    @Override
    public String toString() {
        return String.format("BundleEntry [bundle=%s,path=%s]", this.bundle, this.path);
    }

    /**
     * Returns the bundle entry size. If the BundleFileResolver is EquinoxBundleFileResolver then we will use equinox
     * specific functionality to get BundleEntry and its size. If the BundleFileResolver is NoOpBundleFileResolver we
     * will use URLConnection.getContentLength(). Note: URLConnection.getContentLength() returns "int", if the bundle
     * entry size exceeds max "int", then the content length will not be correct.
     * 
     * @return the bundle entry size
     */
    public long getContentLength() {
        long size = this.bundleFileResolver.resolveBundleEntrySize(this.bundle, this.path);
        if (size == -1) {
            try {
                size = getURL().openConnection().getContentLength();
            } catch (IOException e) {
            }
        }
        return size;
    }
}
