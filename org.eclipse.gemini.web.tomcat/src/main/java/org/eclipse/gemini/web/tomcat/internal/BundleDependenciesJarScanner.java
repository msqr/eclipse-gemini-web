/*******************************************************************************
 * Copyright (c) 2009, 2012 VMware Inc.
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

package org.eclipse.gemini.web.tomcat.internal;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.eclipse.gemini.web.tomcat.internal.loading.BundleWebappClassLoader;
import org.eclipse.gemini.web.tomcat.internal.support.BundleDependencyDeterminer;
import org.eclipse.gemini.web.tomcat.internal.support.BundleFileResolver;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>JarScanner</code> implementation that passes each of the {@link Bundle}'s dependencies to the
 * {@link JarScannerCallback}.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class BundleDependenciesJarScanner implements JarScanner {

    private static final String COMMA_SEPARATOR = ",";

    /**
     * By default the Bundle Dependencies Jar Scanner will exclude the bundles listed below from the scanning process as
     * they do not provide TLDs and web-fragment.xml files: org.eclipse.osgi, javax.servlet, javax.servlet.jsp,
     * javax.el. The default behavior can be changed with property
     * <code>org.eclipse.gemini.web.tomcat.scanner.skip.bundles</code>. The syntax is
     * <code>org.eclipse.gemini.web.tomcat.scanner.skip.bundles=&lt;bundle-symbolic-name&gt;,&lt;bundle-symbolic-name&gt;,...</code>
     */
    static final String SCANNER_SKIP_BUNDLES_PROPERTY_NAME = "org.eclipse.gemini.web.tomcat.scanner.skip.bundles";

    private static final String SCANNER_SKIP_BUNDLES_PROPERTY_VALUE_DEFAULT = "org.eclipse.osgi,javax.servlet,javax.servlet.jsp,javax.el";

    private static final String JAR_URL_SUFFIX = "!/";

    private static final String JAR_URL_PREFIX = "jar:";

    private static final String REFERENCE_URL_PREFIX = "reference";

    private static final Logger LOGGER = LoggerFactory.getLogger(BundleDependenciesJarScanner.class);

    private final BundleDependencyDeterminer bundleDependencyDeterminer;

    private final BundleFileResolver bundleFileResolver;

    private final Set<String> skipBundles;

    public BundleDependenciesJarScanner(BundleDependencyDeterminer bundleDependencyDeterminer, BundleFileResolver bundleFileResolver,
        BundleContext bundleContext) {
        this.bundleDependencyDeterminer = bundleDependencyDeterminer;
        this.bundleFileResolver = bundleFileResolver;
        this.skipBundles = Collections.unmodifiableSet(getBundlesToSkip(bundleContext));
    }

    @Override
    public void scan(ServletContext context, ClassLoader classLoader, JarScannerCallback callback, Set<String> jarsToSkip) {
        if (classLoader instanceof BundleWebappClassLoader) {
            Bundle bundle = ((BundleWebappClassLoader) classLoader).getBundle();
            scanDependentBundles(bundle, callback);
        }
    }

    private void scanDependentBundles(Bundle rootBundle, JarScannerCallback callback) {
        Set<Bundle> dependencies = this.bundleDependencyDeterminer.getDependencies(rootBundle);

        for (Bundle bundle : dependencies) {
            if (!this.skipBundles.contains(bundle.getSymbolicName())) {
                scanBundle(bundle, callback);
            }
        }
    }

    private void scanBundle(Bundle bundle, JarScannerCallback callback) {
        File bundleFile = this.bundleFileResolver.resolve(bundle);
        if (bundleFile != null) {
            scanBundleFile(bundleFile, callback);
        } else {
            scanJarUrlConnection(bundle, callback);
        }
    }

    private void scanJarUrlConnection(Bundle bundle, JarScannerCallback callback) {
        URL bundleUrl;
        String bundleLocation = bundle.getLocation();
        try {
            bundleUrl = new URL(bundleLocation);
            if (REFERENCE_URL_PREFIX.equals(bundleUrl.getProtocol())) {
                bundleUrl = new URL(JAR_URL_PREFIX + bundleUrl.getFile() + JAR_URL_SUFFIX);
            } else {
                bundleUrl = new URL(JAR_URL_PREFIX + bundleLocation + JAR_URL_SUFFIX);
            }
        } catch (MalformedURLException e) {
            LOGGER.warn("Failed to create jar: url for bundle location " + bundleLocation);
            return;
        }

        scanBundleUrl(bundleUrl, callback);
    }

    private void scanBundleFile(File bundleFile, JarScannerCallback callback) {
        if (bundleFile.isDirectory()) {
            try {
                callback.scan(bundleFile);
            } catch (IOException e) {
                LOGGER.warn("Failure when attempting to scan bundle file '" + bundleFile + "'.", e);
            }
        } else {
            URL bundleUrl;
            try {
                bundleUrl = new URL(JAR_URL_PREFIX + bundleFile.toURI().toURL() + JAR_URL_SUFFIX);
            } catch (MalformedURLException e) {
                LOGGER.warn("Failed to create jar: url for bundle file " + bundleFile);
                return;
            }
            scanBundleUrl(bundleUrl, callback);
        }
    }

    private void scanBundleUrl(URL url, JarScannerCallback callback) {
        try {
            URLConnection connection = url.openConnection();

            if (connection instanceof JarURLConnection) {
                callback.scan((JarURLConnection) connection);
            }
        } catch (IOException e) {
            LOGGER.warn("Failure when attempting to scan bundle via jar URL '" + url + "'.", e);
        }
    }

    private Set<String> getBundlesToSkip(BundleContext bundleContext) {
        Set<String> result = new HashSet<String>();
        String property = bundleContext.getProperty(SCANNER_SKIP_BUNDLES_PROPERTY_NAME);

        if (property == null) {
            property = SCANNER_SKIP_BUNDLES_PROPERTY_VALUE_DEFAULT;
        }

        String[] bundlesNames = property.split(COMMA_SEPARATOR);
        for (int i = 0; bundlesNames != null && i < bundlesNames.length; i++) {
            result.add(bundlesNames[i]);
        }

        return result;
    }
}
