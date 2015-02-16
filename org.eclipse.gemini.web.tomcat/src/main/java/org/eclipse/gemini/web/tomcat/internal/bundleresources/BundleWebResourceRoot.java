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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.apache.catalina.Host;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.webresources.EmptyResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.osgi.framework.Bundle;

public class BundleWebResourceRoot extends StandardRoot {

    private final Bundle bundle;

    private final WebResource main;

    private Path docBase;

    public BundleWebResourceRoot(Bundle bundle, String docBaseStr) {
        this.bundle = bundle;
        this.main = new BundleWebResource(this.bundle, this);

        if (docBaseStr != null) {
            this.docBase = Paths.get(docBaseStr);
            if (!this.docBase.isAbsolute()) {
                this.docBase = Paths.get(((Host) getContext().getParent()).getAppBaseFile().getPath()).resolve(this.docBase);
            }
        }
    }

    @Override
    public void createWebResourceSet(ResourceSetType type, String webAppMount, URL url, String internalPath) {
        BaseLocation baseLocation = new BaseLocation(url);
        createWebResourceSet(type, webAppMount, baseLocation.getBasePath(), baseLocation.getArchivePath(), internalPath);
    }

    @Override
    public void createWebResourceSet(ResourceSetType type, String webAppMount, String base, String archivePath, String internalPath) {
        WebResourceSet resourceSet = null;

        if (base.startsWith("bundleentry")) {
            if (archivePath != null) {
                if (archivePath.toLowerCase(Locale.ENGLISH).endsWith(".jar")) {
                    resourceSet = new BundleJarResourceSet(this, webAppMount, base + archivePath, internalPath);
                } else {
                    WebResource entry = ((BundleWebResource) this.main).getNamedEntry(archivePath).getKey();
                    if (entry != null) {
                        resourceSet = new BundleWebResourceSet(entry, this, webAppMount, this.docBase.toAbsolutePath().toString() + archivePath,
                            internalPath);
                    }
                }

                if (type.equals(ResourceSetType.CLASSES_JAR) && resourceSet != null) {
                    resourceSet.setClassLoaderOnly(true);
                }
            }
        } else {
            // TODO need additional testing
            WebResource entry = ((BundleWebResource) this.main).getNamedEntry(base).getKey();
            if (entry != null) {
                resourceSet = new BundleWebResourceSet(entry, this, webAppMount, base, internalPath);

                if (type.equals(ResourceSetType.CLASSES_JAR)) {
                    resourceSet.setClassLoaderOnly(true);
                }
            }
        }

        if (resourceSet != null) {
            switch (type) {
                case PRE:
                    addPreResources(resourceSet);
                    break;
                case CLASSES_JAR:
                    addClassResources(resourceSet);
                    break;
                case RESOURCE_JAR:
                    addJarResources(resourceSet);
                    break;
                case POST:
                    addPostResources(resourceSet);
                    break;
                default:
                    throw new IllegalArgumentException("Unable to create WebResourceSet of unknown type [" + type + "].");
            }
        }
    }

    @Override
    protected String getObjectNameKeyProperties() {
        StringBuilder keyProperties = new StringBuilder("type=BundleWebResourceRoot");
        keyProperties.append(getContext().getMBeanKeyProperties());

        return keyProperties.toString();
    }

    @Override
    protected void registerURLStreamHandlerFactory() {
        // no-op
    }

    @Override
    protected WebResourceSet createMainResourceSet() {
        WebResourceSet mainResourceSet;

        if (this.docBase == null) {
            // TODO When we are in not Equinox case the docBase will be null
            // we need to handle this case also
            mainResourceSet = new EmptyResourceSet(this);
        } else {
            mainResourceSet = new BundleWebResourceSet(this.main, this, "/", this.docBase.toAbsolutePath().toString(), "/");
        }
        return mainResourceSet;
    }

    private static class BaseLocation {

        private String basePath = "";

        private String archivePath = "";

        BaseLocation(URL url) {
            if ("jar".equals(url.getProtocol())) {
                String jarUrl = url.toString();
                int endOfFileUrl = jarUrl.indexOf("!/");
                String fileUrl = jarUrl.substring(4, endOfFileUrl);
                if (fileUrl.startsWith("bundleentry")) {
                    URL file = null;
                    try {
                        file = new URL(fileUrl);
                    } catch (MalformedURLException e) {
                        throw new IllegalArgumentException(e);
                    }
                    this.archivePath = file.getFile();
                    this.basePath = fileUrl.substring(0, fileUrl.indexOf(this.archivePath));
                }
            } else if ("bundleentry".equals(url.getProtocol())) {
                this.archivePath = url.getFile();
                String fileUrl = url.toString();
                this.basePath = fileUrl.substring(0, fileUrl.indexOf(this.archivePath));
            } else {
                throw new IllegalArgumentException("The URL protocol [" + url.getProtocol()
                    + "] is not supported by this web resources implementation");
            }
        }

        String getBasePath() {
            return this.basePath;
        }

        String getArchivePath() {
            return this.archivePath;
        }
    }
}
