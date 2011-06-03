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

package org.eclipse.gemini.web.internal;

import java.net.URL;
import java.util.Dictionary;
import java.util.Locale;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

public final class WebContainerUtils {

    public static final String WEB_BUNDLE_SCHEME = "webbundle";

    /**
     * Constant for the <code>Web-ContextPath</code> manifest header.
     */
    public static final String HEADER_WEB_CONTEXT_PATH = "Web-ContextPath";
    
    /**
     * Constant for the <code>SpringSource-DefaultWABHeaders</code> manifest header.
     */
    public static final String HEADER_DEFAULT_WAB_HEADERS = "org-eclipse-gemini-web-DefaultWABHeaders";
    
    /**
     * Constant for the <code>Web-JSPExtractLocation</code> manifest header.
     */
    public static final String HEADER_WEB_JSP_EXTRACT_LOCATION = "Web-JSPExtractLocation";

    static final String ENTRY_WEB_XML = "/WEB-INF/web.xml";

    static final String WAR_EXTENSION = ".war";

    static final String OSGI_WEB_VERSION = "osgi.web.version";

    static final String OSGI_WEB_SYMBOLICNAME = "osgi.web.symbolicname";

    static final String BUNDLE_VERSION_HEADER = "bundle-version";

    private WebContainerUtils() {
    }

    public static boolean isWebBundle(Bundle bundle) {
        return hasWarExtension(bundle) || hasWarScheme(bundle) || hasWebContextPath(bundle) || hasWebXml(bundle);
    }

    private static boolean hasWarExtension(Bundle bundle) {
        String lowerCaseLocation = bundle.getLocation().toLowerCase(Locale.ENGLISH);
        while (lowerCaseLocation.endsWith("/")) {
            lowerCaseLocation = lowerCaseLocation.substring(0, lowerCaseLocation.length() - 1);
        }
        return lowerCaseLocation.endsWith(WAR_EXTENSION);
    }

    private static boolean hasWarScheme(Bundle bundle) {
        return bundle.getLocation().startsWith(WEB_BUNDLE_SCHEME);
    }

    private static boolean hasWebContextPath(Bundle bundle) {
        return getWebContextPathHeader(bundle) != null;
    }

    private static String getWebContextPathHeader(Bundle bundle) {
        return (String) bundle.getHeaders().get(HEADER_WEB_CONTEXT_PATH);
    }

    private static boolean hasWebXml(Bundle bundle) {
        return bundle.getEntry(ENTRY_WEB_XML) != null;
    }

    public static String getContextPath(Bundle bundle) {
        String contextPath = getWebContextPathHeader(bundle);

        if (contextPath == null) {
            contextPath = getBaseName(bundle.getLocation());
        }

        return contextPath;
    }

    public static String createDefaultBundleSymbolicName(URL source) {
        return getBaseName(source.getPath());
    }

    static String getBaseName(String path) {
        String base = path;
        base = unifySeparators(base);
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        base = stripQuery(base);
        base = stripSchemeAndDrive(base);
        base = stripLeadingPathElements(base);
        base = stripExtension(base);
        return base;
    }

    private static String unifySeparators(String base) {
        return base.replaceAll("\\\\", "/");
    }

    private static String stripExtension(String base) {
        int index;
        index = base.lastIndexOf(".");
        if (index > -1) {
            base = base.substring(0, index);
        }
        return base;
    }

    private static String stripLeadingPathElements(String base) {
        int index = base.lastIndexOf("/");
        if (index > -1) {
            base = base.substring(index + 1);
        }
        return base;
    }

    private static String stripQuery(String path) {
        String result = path;
        int index = result.lastIndexOf("?");
        if (index > -1) {
            result = result.substring(0, index);
        }
        return result;
    }

    private static String stripSchemeAndDrive(String path) {
        String result = path;
        int index = result.indexOf(":");
        while (index > -1 && index < result.length()) {
            result = result.substring(index + 1);
            index = result.indexOf(":");
        }
        return result;
    }

    public static void setServletContextBundleProperties(Dictionary<String, String> properties, Bundle bundle) {
        setServletContextOsgiWebSymbolicNameProperty(properties, bundle);
        setServletContextOsgiWebVersionProperty(properties, bundle);
    }

    private static void setServletContextOsgiWebVersionProperty(Dictionary<String, String> properties, Bundle bundle) {
        if (bundle.getHeaders().get(BUNDLE_VERSION_HEADER) != null) {
            properties.put(OSGI_WEB_VERSION, bundle.getVersion().toString());
        }
    }

    private static void setServletContextOsgiWebSymbolicNameProperty(Dictionary<String, String> properties, Bundle bundle) {
        String symbolicName = bundle.getSymbolicName();
        if (symbolicName != null) {
            properties.put(OSGI_WEB_SYMBOLICNAME, symbolicName);
        }
    }

    /**
     * Determines whether the given manifest represents a web application bundle. According to the R4.2 Enterprise
     * Specification, this is true if and only if the manifest contains any of the headers in Table 128.3:
     * Bundle-SymbolicName, Bundle-Version, Bundle-ManifestVersion, Import-Package, Web-ContextPath. Note: there is no
     * need to validate the manifest as if it is invalid it will cause an error later.
     * 
     * @param manifest the bundle manifest
     * @return <code>true</code> if and only if the given manifest represents a web application bundle
     */
    public static boolean isWebApplicationBundle(BundleManifest manifest) {
        return specifiesBundleSymbolicName(manifest) || specifiesBundleVersion(manifest) || specifiesBundleManifestVersion(manifest)
            || specifiesImportPackage(manifest) || specifiesWebContextPath(manifest);
    }

    private static boolean specifiesBundleSymbolicName(BundleManifest manifest) {
        return manifest.getBundleSymbolicName().getSymbolicName() != null;
    }

    private static boolean specifiesBundleVersion(BundleManifest manifest) {
        return manifest.getHeader(Constants.BUNDLE_VERSION) != null;
    }

    private static boolean specifiesBundleManifestVersion(BundleManifest manifest) {
        return manifest.getBundleManifestVersion() != 1;
    }

    private static boolean specifiesImportPackage(BundleManifest manifest) {
        return !manifest.getImportPackage().getImportedPackages().isEmpty();
    }

    private static boolean specifiesWebContextPath(BundleManifest manifest) {
        return manifest.getHeader(WebContainerUtils.HEADER_WEB_CONTEXT_PATH) != null;
    }
}
