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

package org.eclipse.gemini.web.internal.url;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;


import org.eclipse.gemini.web.core.InstallationOptions;
import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
import org.eclipse.gemini.web.internal.WebContainerUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParserFactory;
import org.eclipse.virgo.util.osgi.manifest.parse.ParserLogger;

/**
 * Applies user installation options onto a {@link BundleManifest}.
 * 
 * 
 */
public final class SpecificationWebBundleManifestTransformer implements WebBundleManifestTransformer {

    private static final int MINIMUM_VALID_BUNDLE_MANIFEST_VERSION = 2;

    public void transform(BundleManifest manifest, URL sourceURL, InstallationOptions options, boolean webBundle) throws IOException {
        if (options == null) {
            options = new InstallationOptions(Collections.<String, String> emptyMap());
        }

        transformBundleSymbolicName(manifest, options, webBundle);
        transformBundleVersion(manifest, options, webBundle);
        transformBundleManifestVersion(manifest, options, webBundle);
        transformBundleClassPath(manifest, options, webBundle);
        transformImportPackage(manifest, options, webBundle);
        transformExportPackage(manifest, options, webBundle);
        transformWebContextPath(manifest, options, webBundle);
    }

    private void transformExportPackage(BundleManifest manifest, InstallationOptions options, boolean isWebApplicationBundle) {
        String epd = options.getExportPackageDeclaration();
        if (epd != null) {
            if (isWebApplicationBundle) {
                throw new IllegalArgumentException("Export-Package URL parameter cannot modify a Web Application Bundle");
            }
            HeaderParser parser = HeaderParserFactory.newHeaderParser(new TransformerParserLogger());
            List<HeaderDeclaration> packageHeader = parser.parsePackageHeader(epd, Constants.EXPORT_PACKAGE);
            for (HeaderDeclaration headerDeclaration : packageHeader) {
                for (String name : headerDeclaration.getNames()) {
                    PackageMergeUtils.mergeExportPackage(manifest, name, headerDeclaration.getAttributes(), headerDeclaration.getDirectives());
                }
            }
        }
    }

    private void transformImportPackage(BundleManifest manifest, InstallationOptions options, boolean isWebApplicationBundle) {
        String ipd = options.getImportPackageDeclaration();
        if (ipd != null) {
            if (isWebApplicationBundle) {
                throw new IllegalArgumentException("Import-Package URL parameter cannot modify a Web Application Bundle");
            }
            HeaderParser parser = HeaderParserFactory.newHeaderParser(new TransformerParserLogger());
            List<HeaderDeclaration> packageHeader = parser.parsePackageHeader(ipd, Constants.IMPORT_PACKAGE);
            for (HeaderDeclaration headerDeclaration : packageHeader) {
                for (String name : headerDeclaration.getNames()) {
                    PackageMergeUtils.mergeImportPackage(manifest, name, headerDeclaration.getAttributes(), headerDeclaration.getDirectives());
                }
            }
        }
    }

    private void transformBundleSymbolicName(BundleManifest manifest, InstallationOptions options, boolean isWebApplicationBundle) {
        if (options.getBundleSymbolicName() != null) {
            if (isWebApplicationBundle) {
                throw new IllegalArgumentException("Bundle-SymbolicName URL parameter cannot modify a Web Application Bundle");
            }
            manifest.getBundleSymbolicName().setSymbolicName(options.getBundleSymbolicName());
        }
    }

    private void transformBundleManifestVersion(BundleManifest manifest, InstallationOptions options, boolean isWebApplicationBundle) {
        if (options.getBundleManifestVersion() != null) {
            if (isWebApplicationBundle) {
                throw new IllegalArgumentException("Bundle-ManifestVersion URL parameter cannot modify a Web Application Bundle");
            }
            manifest.setBundleManifestVersion(parseBundleManifestVersion(options.getBundleManifestVersion()));
        }
    }

    private int parseBundleManifestVersion(String bundleManifestVersion) {
        int result = MINIMUM_VALID_BUNDLE_MANIFEST_VERSION;
        if (bundleManifestVersion != null) {
            try {
                result = Integer.parseInt(bundleManifestVersion);
                if (result < MINIMUM_VALID_BUNDLE_MANIFEST_VERSION) {
                    throw new IllegalArgumentException(Constants.BUNDLE_MANIFESTVERSION + " " + result + " is less than the smallest valid value of "
                        + MINIMUM_VALID_BUNDLE_MANIFEST_VERSION);
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(Constants.BUNDLE_MANIFESTVERSION + " is not a valid integer.", ex);
            }
        }
        return result;
    }

    private void transformBundleVersion(BundleManifest manifest, InstallationOptions options, boolean isWebApplicationBundle) {
        if (options.getBundleVersion() != null) {
            if (isWebApplicationBundle) {
                throw new IllegalArgumentException("Bundle-Version URL parameter cannot modify a Web Application Bundle");
            }
            manifest.setBundleVersion(new Version(options.getBundleVersion()));
        }
    }

    private void transformBundleClassPath(BundleManifest manifest, InstallationOptions options, boolean isWebApplicationBundle) {
        List<String> bundleClassPath = manifest.getBundleClasspath();
        String bundleClassPathOption = options.getBundleClassPath();
        if (bundleClassPathOption != null) {
            if (isWebApplicationBundle) {
                throw new IllegalArgumentException("Bundle-ClassPath URL parameter cannot modify a Web Application Bundle");
            }
            for (String entry : parseBundleClassPath(bundleClassPathOption)) {
                if (!bundleClassPath.contains(entry)) {
                    bundleClassPath.add(entry);
                }
            }
        }
    }

    private static String[] parseBundleClassPath(String bundleClassPath) {
        String[] bundleClassPathEntries = bundleClassPath.split(",");
        minimallyValidateBundleClassPathEntries(bundleClassPathEntries, bundleClassPath);
        return bundleClassPathEntries;
    }

    /**
     * Validates the given bundle class path entries.
     * <p>
     * Trailing slashes are tolerated and removed so the resultant class path entries are more likely to conform
     * strictly to the OSGi specification.
     */
    private static void minimallyValidateBundleClassPathEntries(String[] bundleClassPathEntries, String bundleClassPath) {
        for (int i = 0; i < bundleClassPathEntries.length; i++) {
            String entry = bundleClassPathEntries[i];
            if (entry.length() == 0) {
                diagnoseInvalidEntry(entry, bundleClassPath);
            }
            if (entry.endsWith("/")) {
                if (entry.length() == 1) {
                    diagnoseInvalidEntry(entry, bundleClassPath);
                }
                bundleClassPathEntries[i] = entry.substring(0, entry.length() - 1);
            }
        }
    }

    private static void diagnoseInvalidEntry(String entry, String bundleClassPath) {
        throw new IllegalArgumentException(Constants.BUNDLE_CLASSPATH + "'" + bundleClassPath + "' contains an invalid entry '" + entry + "'");
    }

    /**
     * @param isWebApplicationBundle 
     */
    private void transformWebContextPath(BundleManifest manifest, InstallationOptions options, boolean isWebApplicationBundle) {
        String webContextPathOption = options.getWebContextPath();
        if (webContextPathOption != null) {
            manifest.setHeader(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, validateWebContextPath(webContextPathOption));
        } else if (!options.getDefaultWABHeaders()) {
            String webContextPathHeader = manifest.getHeader(WebContainerUtils.HEADER_WEB_CONTEXT_PATH);
            if (webContextPathHeader == null || webContextPathHeader.trim().length() == 0) {
                throw new IllegalArgumentException(WebContainerUtils.HEADER_WEB_CONTEXT_PATH + " is missing");
            }
        }
    }

    private String validateWebContextPath(String webContextPathOption) {
        String trimmedWebContextPathOption = webContextPathOption.trim();
        if (trimmedWebContextPathOption.length() == 0) {
            throw new IllegalArgumentException(WebContainerUtils.HEADER_WEB_CONTEXT_PATH + " URL parameter value is missing");
        }
        if (trimmedWebContextPathOption.startsWith("/")) {
            return trimmedWebContextPathOption;
        } else {
            return "/" + trimmedWebContextPathOption;
        }
    }

    private static class TransformerParserLogger implements ParserLogger {

        public String[] errorReports() {
            return null;
        }

        public void outputErrorMsg(Exception re, String item) {

        }
    }
}
