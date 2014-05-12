/*******************************************************************************
 * Copyright (c) 2012 SAP AG
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

package org.eclipse.gemini.web;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class BundleJarScanner implements JarScanner {

    private static final String JAR_URL_SUFFIX = "!/";

    private static final String JAR_URL_PREFIX = "jar:";

    private static final String REFERENCE_URL_PREFIX = "reference";

    @Override
    public void scan(ServletContext servletContext, ClassLoader classLoader, JarScannerCallback jarScannerCallback, Set<String> jarsToSkip) {
        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        if (bundle != null) {
            scanBundle(bundle, jarScannerCallback);
        }
    }

    private void scanBundle(Bundle bundle, JarScannerCallback callback) {
        File bundleFile = resolve(bundle);
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
            System.out.println("Failed to create jar: url for bundle location " + bundleLocation);
            return;
        }

        scanBundleUrl(bundleUrl, callback);
    }

    private void scanBundleFile(File bundleFile, JarScannerCallback callback) {
        if (bundleFile.isDirectory()) {
            try {
                callback.scan(bundleFile);
            } catch (IOException e) {
                System.out.println("Failure when attempting to scan bundle file '" + bundleFile + "':" + e.getMessage());
            }
        } else {
            URL bundleUrl;
            try {
                bundleUrl = new URL(JAR_URL_PREFIX + bundleFile.toURI().toURL() + JAR_URL_SUFFIX);
            } catch (MalformedURLException e) {
                System.out.println("Failed to create jar: url for bundle file " + bundleFile);
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
            System.out.println("Failure when attempting to scan bundle via jar URL '" + url + "':" + e.getMessage());
        }
    }

    private File resolve(Bundle bundle) {
        BundleFile bundleFile = getBundleFile(bundle);
        if (bundleFile != null) {
            File file = bundleFile.getBaseFile();
            System.out.println("Resolved bundle '" + bundle.getSymbolicName() + "' to file '" + file.getAbsolutePath() + "'");
            return file;
        }
        return null;
    }

    private BundleFile getBundleFile(Bundle bundle) {
        if (bundle instanceof BundleHost) {
            BundleHost bh = (BundleHost) bundle;
            BundleData bundleData = bh.getBundleData();
            if (bundleData instanceof BaseData) {
                return ((BaseData) bundleData).getBundleFile();
            }
        }
        return null;
    }

}
