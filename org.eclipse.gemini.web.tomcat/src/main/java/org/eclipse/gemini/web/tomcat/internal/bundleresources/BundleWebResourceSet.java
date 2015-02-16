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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.util.ResourceSet;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.EmptyResource;

final class BundleWebResourceSet extends DirResourceSet {

    private final WebResource bundleEntry;

    BundleWebResourceSet(WebResource bundleEntry, WebResourceRoot root, String webAppMount, String base, String internalPath) {
        super(root, webAppMount, base, internalPath);
        this.bundleEntry = bundleEntry;
    }

    @Override
    public WebResource getResource(String path) {
        checkPath(path);
        String webAppMount = getWebAppMount();
        WebResourceRoot root = getRoot();
        if (path.startsWith(webAppMount)) {
            Entry<BundleWebResource, URL> entry = getNamedEntry(path.substring(webAppMount.length()));
            if (entry != null) {
                WebResource bundleEntry = entry.getKey();
                if (bundleEntry != null) {
                    return bundleEntry;
                }
            }
        }
        return new EmptyResource(root, path);
    }

    @Override
    public String[] list(String path) {
        checkPath(path);
        String webAppMount = getWebAppMount();
        if (path.startsWith(webAppMount)) {
            Entry<BundleWebResource, URL> entry = getNamedEntry(path.substring(webAppMount.length()));
            if (entry != null) {
                BundleWebResource bundleEntry = entry.getKey();
                if (bundleEntry != null) {
                    List<BundleWebResource> list = bundleEntry.list();
                    if (list != null) {
                        List<String> resources = new ArrayList<>();
                        for (BundleWebResource resource : list) {
                            resources.add(resource.getName());
                        }
                        return resources.toArray(new String[resources.size()]);
                    }
                }
            }
        } else {
            if (!path.endsWith("/")) {
                path = path + "/";
            }
            if (webAppMount.startsWith(path)) {
                int i = webAppMount.indexOf('/', path.length());
                if (i == -1) {
                    return new String[] { webAppMount.substring(path.length()) };
                }
                return new String[] { webAppMount.substring(path.length(), i) };
            }
        }
        return EMPTY_STRING_ARRAY;
    }

    @Override
    public Set<String> listWebAppPaths(String path) {
        checkPath(path);
        String webAppMount = getWebAppMount();
        ResourceSet<String> result = new ResourceSet<>();
        if (path.startsWith(webAppMount)) {
            Entry<BundleWebResource, URL> entry = getNamedEntry(path.substring(webAppMount.length()));
            if (entry != null) {
                BundleWebResource bundleEntry = entry.getKey();
                if (bundleEntry != null) {
                    List<BundleWebResource> list = bundleEntry.list();
                    if (list != null) {
                        for (BundleWebResource bEntry : list) {
                            StringBuilder sb = new StringBuilder(path);
                            if (path.charAt(path.length() - 1) != '/') {
                                sb.append('/');
                            }
                            sb.append(bEntry.getName());
                            if (bEntry.isDirectory()) {
                                sb.append('/');
                            }
                            result.add(sb.toString());
                        }
                    }
                }
            }
        } else {
            if (!path.endsWith("/")) {
                path = path + "/";
            }
            if (webAppMount.startsWith(path)) {
                int i = webAppMount.indexOf('/', path.length());
                if (i == -1) {
                    result.add(webAppMount + "/");
                } else {
                    result.add(webAppMount.substring(0, i + 1));
                }
            }
        }
        result.setLocked(true);
        return result;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected void checkType(File file) {
        // no-op
    }

    Entry<BundleWebResource, URL> getNamedEntry(String name) {
        return ((BundleWebResource) this.bundleEntry).getNamedEntry(name);
    }
}
