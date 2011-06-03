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

package org.eclipse.gemini.web.core;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.gemini.web.internal.WebContainerUtils;
import org.osgi.framework.Constants;

/**
 * Simple utility class that parses the user-supplied installation options from a {@link Map}.
 */
public final class InstallationOptions {

    private static final class CaseInsensitiveMap extends HashMap<String, String> {

        private static final long serialVersionUID = -514044030419642872L;

        @Override
        public String get(Object key) {
            if (key instanceof String) {
                return super.get(normaliseOption((String) key));
            }
            return super.get(key);
        }

        @Override
        public String put(String key, String value) {
            return super.put(normaliseOption(key), value);
        }

        private String normaliseOption(String key) {
            return key.toLowerCase(Locale.ENGLISH);
        }

    }

    private final String bundleSymbolicName;

    private final String bundleVersion;

    private final String bundleManifestVersion;

    private final String bundleClassPath;

    private final String importPackageDeclaration;

    private final String exportPackageDeclaration;

    private final String webContextPath;

    private final String webJSPExtractLocation;

    private volatile boolean defaultWABHeaders;

    /**
     * Creates a new <code>InstallationOptions</code> from the supplied <code>options</code> {@link Map}.
     * <p/>
     * Changes to the <code>options</code> <code>Map</code> are not reflected in the new instance.
     * 
     * @param options the options
     */
    public InstallationOptions(Map<String, String> options) {
        Map<String, String> normalisedOptions = normalise(options);

        this.bundleSymbolicName = normalisedOptions.get(Constants.BUNDLE_SYMBOLICNAME);
        this.bundleVersion = normalisedOptions.get(Constants.BUNDLE_VERSION);
        this.bundleManifestVersion = normalisedOptions.get(Constants.BUNDLE_MANIFESTVERSION);
        this.bundleClassPath = normalisedOptions.get(Constants.BUNDLE_CLASSPATH);

        this.importPackageDeclaration = normalisedOptions.get(Constants.IMPORT_PACKAGE);
        this.exportPackageDeclaration = normalisedOptions.get(Constants.EXPORT_PACKAGE);

        this.webContextPath = normalisedOptions.get(WebContainerUtils.HEADER_WEB_CONTEXT_PATH);
        this.webJSPExtractLocation = normalisedOptions.get(WebContainerUtils.HEADER_WEB_JSP_EXTRACT_LOCATION);

        this.defaultWABHeaders = (options.get(WebContainerUtils.HEADER_DEFAULT_WAB_HEADERS) != null);
    }

    private Map<String, String> normalise(Map<String, String> options) {
        Set<String> keys = options.keySet();
        Map<String, String> normalisedOptions = new CaseInsensitiveMap();
        for (String key : keys) {
            normalisedOptions.put(key, options.get(key));
        }
        return normalisedOptions;
    }

    /**
     * Gets the symbolic name installation option. This option overrides the <code>Bundle-SymbolicName</code> set in the
     * web bundle manifest.
     * 
     * @return the symbolic name installation option.
     */
    public String getBundleSymbolicName() {
        return this.bundleSymbolicName;
    }

    /**
     * Get the bundle version installation option. This option overrides the <code>Bundle-Version</code> set in the web
     * bundle manifest.
     * 
     * @return the bundle version installation option.
     */
    public String getBundleVersion() {
        return this.bundleVersion;
    }

    /**
     * Gets the unvalidated bundle manifest version installation option. This option overrides the
     * <code>Bundle-ManifestVersion</code> set in the web bundle manifest.
     * <p/>
     * The bundle manifest version must not be validated early otherwise the IllegalArgumentException will not be turned
     * into a BundleException by the OSGi framework.
     * 
     * @return the unvalidated bundle manifest version installation option.
     */
    public String getBundleManifestVersion() {
        return this.bundleManifestVersion;
    }

    /**
     * Gets the bundle class path installation option. This option overrides the <code>Bundle-ClassPath</code> set in
     * the web bundle manifest.
     * 
     * @return the bundle class path installation option.
     */
    public String getBundleClassPath() {
        return this.bundleClassPath;
    }

    /**
     * Gets the import package installation option.
     * 
     * @return the import package installation option.
     */
    public String getImportPackageDeclaration() {
        return this.importPackageDeclaration;
    }

    /**
     * Gets the export package installation option.
     * 
     * @return the export package installation option.
     */
    public String getExportPackageDeclaration() {
        return this.exportPackageDeclaration;
    }

    /**
     * Gets the web context path installation option. This option overrides the context path specified in the manifest.
     * 
     * @return the web context path installation option.
     */
    public String getWebContextPath() {
        return this.webContextPath;
    }

    /**
     * Gets the JSP extract location installation option. This option controls where the servlet container will extract
     * application JSPs.
     * 
     * @return the JSP extract location installation option.
     */
    public String getWebJSPExtractLocation() {
        return this.webJSPExtractLocation;
    }

    /**
     * Returns whether Web Application Bundle (WAB) manifest headers should be defaulted or not. This should be
     * <code>false</code> for strict compliance with the OSGi Web Container specification and <code>true</code> for
     * compatibility with the behaviour shipped with dm Server 2.0.0.RELEASE.
     * 
     * @return the default WAB headers installation option.
     */
    public boolean getDefaultWABHeaders() {
        return this.defaultWABHeaders;
    }

    public void setDefaultWABHeaders(boolean defaultWABHeaders) {
        this.defaultWABHeaders = defaultWABHeaders;
    }

}
