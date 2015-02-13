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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Manifest;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.AbstractResource;
import org.apache.juli.logging.Log;
import org.eclipse.gemini.web.tomcat.internal.support.BundleFileResolver;
import org.eclipse.gemini.web.tomcat.internal.support.BundleFileResolverFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

final class BundleWebResource extends AbstractResource {

    private static final String WEB_INF_DOT = "WEB-INF.";

    private static final String META_INF_DOT = "META-INF.";

    private static final String META_INF = "META-INF";

    private static final String OSGI_INF_DOT = "OSGI-INF.";

    private static final String OSGI_OPT_DOT = "OSGI-OPT.";

    private static final String PATH_SEPARATOR = "/";

    private static final String DOT = ".";

    private static final long TIME_NOT_SET = -1L;

    private static final int CREATION_DATE_UNKNOWN = 0;

    private static final long CONTENT_LENGTH_NOT_SET = -1;

    private final WebResourceRoot root;

    private final String path;

    private final Bundle bundle;

    private final List<Bundle> fragments;

    private final BundleFileResolver bundleFileResolver = BundleFileResolverFactory.createBundleFileResolver();

    private final boolean checkEntryPath;

    private String bundleLocationCanonicalPath;

    private boolean isBundleLocationDirectory;

    private long lastModified = TIME_NOT_SET;

    private long creation = TIME_NOT_SET;

    private long contentLength = CONTENT_LENGTH_NOT_SET;

    private URL url;

    BundleWebResource(Bundle bundle, WebResourceRoot root) {
        super(root, "");
        this.root = root;
        this.path = "";
        this.bundle = bundle;
        this.fragments = getFragments(bundle);
        this.checkEntryPath = checkEntryPath();
        File bundleLocation = this.bundleFileResolver.resolve(bundle);
        if (bundleLocation != null) {
            try {
                this.bundleLocationCanonicalPath = bundleLocation.getCanonicalPath();
            } catch (IOException e) {
            }
            if (bundleLocation.isDirectory()) {
                this.isBundleLocationDirectory = true;
            }
        }
    }

    private BundleWebResource(Bundle bundle, WebResourceRoot root, List<Bundle> fragments, String path, boolean checkEntryPath,
        String bundleLocationCanonicalPath, boolean isBundleLocationDirectory) {
        super(root, path);
        this.root = root;
        this.path = path;
        this.bundle = bundle;
        this.fragments = fragments;
        this.checkEntryPath = checkEntryPath;
        this.bundleLocationCanonicalPath = bundleLocationCanonicalPath;
        this.isBundleLocationDirectory = isBundleLocationDirectory;
    }

    Bundle getBundle() {
        return this.bundle;
    }

    List<BundleWebResource> list() {
        List<BundleWebResource> entries = new ArrayList<>();
        Set<String> paths = getEntryPathsFromBundle();
        if (paths != null) {
            Iterator<String> iterator = paths.iterator();
            while (iterator.hasNext()) {
                String subPath = iterator.next();
                entries.add(createBundleEntry(subPath));
            }
        }
        return entries;
    }

    private BundleWebResource createBundleEntry(String path) {
        return new BundleWebResource(this.bundle, this.root, this.fragments, path, this.checkEntryPath, this.bundleLocationCanonicalPath,
            this.isBundleLocationDirectory);
    }

    private Set<String> getEntryPathsFromBundle() {
        Set<String> paths = getEntryPathsFromBundle(this.bundle);

        for (int i = 0; i < this.fragments.size(); i++) {
            paths.addAll(getEntryPathsFromBundle(this.fragments.get(i)));
        }

        if (paths.isEmpty()) {
            return null;
        }

        return paths;
    }

    private Set<String> getEntryPathsFromBundle(Bundle bundle) {
        final Enumeration<String> ep = bundle.getEntryPaths(this.path);

        Set<String> paths = new HashSet<String>();
        if (ep != null) {
            while (ep.hasMoreElements()) {
                paths.add(ep.nextElement());
            }
        }

        return paths;
    }

    Entry<BundleWebResource, URL> getEntry(String subPath) {
        String finalPath = this.path + subPath;
        URL entryURL = getEntryFromBundle(finalPath);
        if (entryURL != null) {
            Map<BundleWebResource, URL> result = new HashMap<>();
            result.put(createBundleEntry(finalPath), entryURL);
            return result.entrySet().iterator().next();
        } else {
            return null;
        }
    }

    /**
     * This method has been generalized from this.bundle.getEntry(path) to allow entries to be supplied by a fragment.
     */
    private URL getEntryFromBundle(String path) {
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

        Enumeration<URL> entries = this.bundle.findEntries(searchPath, searchFile, false);

        if (entries != null) {
            if (entries.hasMoreElements()) {
                return entries.nextElement();
            }
        }

        return null;
    }

    private boolean checkNotAttemptingToAccess(String path, String prefix) {
        return path.startsWith(prefix + PATH_SEPARATOR) || path.startsWith(PATH_SEPARATOR + prefix + PATH_SEPARATOR)
            || path.startsWith(DOT + PATH_SEPARATOR + prefix + PATH_SEPARATOR);
    }

    @Override
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

    @Override
    public URL getURL() {
        if (this.url == null) {
            this.url = getEntryFromBundle(this.path);
        }
        return this.url;
    }

    void setURL(URL url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return String.format("BundleWebResource [bundle=%s,path=%s]", this.bundle, this.path);
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
        long size = this.bundleFileResolver.resolveBundleEntrySize(this.bundle, this.path);
        if (size == -1 && urlConnection != null) {
            size = urlConnection.getContentLength();
        }
        return size;
    }

    private List<Bundle> getFragments(Bundle bundle) {
        List<Bundle> fragments = new ArrayList<Bundle>();
        BundleRevision bundleRevision = bundle.adapt(BundleRevision.class);
        if (bundleRevision != null) {
            BundleWiring bundleWiring = bundleRevision.getWiring();
            List<BundleWire> bundleWires = bundleWiring.getProvidedWires(BundleRevision.HOST_NAMESPACE);
            for (int i = 0; bundleWires != null && i < bundleWires.size(); i++) {
                fragments.add(bundleWires.get(i).getRequirerWiring().getRevision().getBundle());
            }
        }
        return fragments;
    }

    private boolean checkEntryPath() {
        try {
            return Paths.get(META_INF).toRealPath().equals(Paths.get(META_INF_DOT).toRealPath());
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public String getCanonicalPath() {
        if (isBundleLocationDirectory()) {
            boolean checkInBundleLocation = this.path != null && this.path.indexOf("..") >= 0;
            String bundleLocationCanonicalPath = getBundleLocationCanonicalPath();
            Path entry = Paths.get(bundleLocationCanonicalPath, this.path);
            if (checkInBundleLocation) {
                try {
                    if (!entry.toRealPath().startsWith(bundleLocationCanonicalPath)) {
                        return null;
                    }
                } catch (IOException e) {
                    return null;
                }
            }
            return entry.toAbsolutePath().toString();
        }
        return null;
    }

    private String getBundleLocationCanonicalPath() {
        return this.bundleLocationCanonicalPath;
    }

    private boolean isBundleLocationDirectory() {
        return this.isBundleLocationDirectory;
    }

    @Override
    public Certificate[] getCertificates() {
        return null;
    }

    @Override
    public URL getCodeBase() {
        return getURL();
    }

    @Override
    public byte[] getContent() {
        long len = getContentLength();

        if (len > Integer.MAX_VALUE) {
            // Can't create an array that big
            throw new ArrayIndexOutOfBoundsException("Unable to return [" + getWebappPath() + "] as a byte array since the resource is ["
                + Long.valueOf(len) + "] bytes in size which is larger than the maximum size of a byte array.");
        }

        int size = (int) len;
        byte[] result = new byte[size];

        int pos = 0;
        try (InputStream is = getURL().openStream()) {
            while (pos < size) {
                int n = is.read(result, pos, size - pos);
                if (n < 0) {
                    break;
                }
                pos += n;
            }
        } catch (IOException ioe) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Unable to return [" + getWebappPath() + "] as a byte array", ioe);
            }
        }

        return result;
    }

    @Override
    public long getContentLength() {
        return getContentLength(null);
    }

    private long getContentLength(URLConnection urlConnection) {
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

    private URLConnection getURLConnection() {
        try {
            URL url = getURL();
            if (url != null) {
                return url.openConnection();
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public long getCreation() {
        return getCreation(null, TIME_NOT_SET);
    }

    private long getCreation(URLConnection urlConnection, long lastModified) {
        if (this.creation == TIME_NOT_SET) {
            if (urlConnection == null) {
                urlConnection = getURLConnection();
            }

            if (urlConnection != null) {
                this.creation = urlConnection.getDate();

                if (this.creation == CREATION_DATE_UNKNOWN) {
                    if (lastModified == TIME_NOT_SET) {
                        lastModified = urlConnection.getLastModified();
                    }

                    this.creation = lastModified;
                }
            }
        }

        return this.creation;
    }

    @Override
    public long getLastModified() {
        return getLastModified(null);
    }

    private long getLastModified(URLConnection urlConnection) {
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

    @Override
    public Manifest getManifest() {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return getURL().getFile().endsWith(PATH_SEPARATOR);
    }

    @Override
    public boolean isFile() {
        return !getURL().getFile().endsWith(PATH_SEPARATOR);
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    protected InputStream doGetInputStream() {
        try {
            return getURL().openStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected Log getLog() {
        return null;
    }

    Entry<BundleWebResource, URL> getNamedEntry(String name) {
        checkCanLookup(name);
        return getEntry(name);
    }

    private void checkCanLookup(String name) {
        if (getBundle().getState() == Bundle.UNINSTALLED) {
            throw new IllegalArgumentException("Resource not found [" + name + "].");
        }
        checkNotAttemptingToLookupFromProtectedLocation(name);
    }

    private void checkNotAttemptingToLookupFromProtectedLocation(String name) {
        checkNotAttemptingToLookupFrom(name, "/OSGI-INF/");
        checkNotAttemptingToLookupFrom(name, "/OSGI-OPT/");
    }

    private void checkNotAttemptingToLookupFrom(String name, String prefix) {
        if (name.startsWith(prefix)) {
            throw new IllegalArgumentException("Resource cannot be obtained from [" + prefix + "].");
        }
    }
}
