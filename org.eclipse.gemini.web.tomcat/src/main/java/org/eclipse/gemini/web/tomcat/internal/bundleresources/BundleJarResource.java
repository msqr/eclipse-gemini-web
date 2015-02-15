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
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.JarResource;

final class BundleJarResource extends JarResource {

    BundleJarResource(WebResourceRoot root, String webAppPath, String base, String baseUrl, JarEntry jarEntry, String internalPath, Manifest manifest) {
        super(root, webAppPath, base, baseUrl, jarEntry, internalPath, manifest);
    }

    @Override
    protected JarInputStreamWrapper getJarInputStreamWrapper() {
        URLConnection conn = null;
        try {
            conn = new URL(getBase()).openConnection();
        } catch (IOException e) {
            return null;
        }

        JarInputStream jarIs = null;
        JarEntry entry = null;
        try {
            // Need to create a new JarEntry so the certificates can be read
            jarIs = new JarInputStream(conn.getInputStream());
            entry = jarIs.getNextJarEntry();
            while (entry != null && !entry.getName().equals(getResource().getName())) {
                entry = jarIs.getNextJarEntry();
            }

            if (entry != null) {
                return new ExtendedJarInputStreamWrapper(null, entry, jarIs);
            } else {
                return null;
            }
        } catch (IOException e) {
            if (getLog().isDebugEnabled()) {
                getLog().debug(
                    "Unable to obtain an InputStream for the resource [" + getResource().getName() + "] located in the JAR [" + getBaseUrl() + "]", e);
            }
            return null;
        } finally {
            if (entry == null && jarIs != null) {
                try {
                    jarIs.close();
                } catch (IOException ioe) {
                    // Ignore
                }
            }
        }
    }

    private class ExtendedJarInputStreamWrapper extends JarInputStreamWrapper {

        private final InputStream is;

        public ExtendedJarInputStreamWrapper(JarFile jarFile, JarEntry jarEntry, InputStream is) {
            super(jarFile, jarEntry, is);
            this.is = is;
        }

        @Override
        public void close() throws IOException {
            // Closing the JarInputStream releases the file lock on the JAR and also
            // closes input stream created from the URLConnection.
            this.is.close();
        }
    }

}
