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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.util.osgi.VersionRange;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

final class SystemBundleExportsResolver {

    static final String VERSION = "version";

    private static final String OSGI_RESOLVER_MODE = "osgi.resolverMode";

    private static final String OSGI_RESOLVER_MODE_STRICT = "strict";

    private static final String INTERNAL_DIRECTIVE = "x-internal";

    private static final String FRIENDS_DIRECTIVE = "x-friends";

    private final BundleContext bundleContext;

    SystemBundleExportsResolver(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public Map<String, VersionRange> getSystemBundleExports() {
        final Bundle systemBundle = this.bundleContext.getBundle(0);
        BundleWiring bundleWiring = systemBundle.adapt(BundleRevision.class).getWiring();
        List<BundleCapability> bundleCapabilities = bundleWiring.getCapabilities(BundleRevision.PACKAGE_NAMESPACE);
        boolean isStrictMode = OSGI_RESOLVER_MODE_STRICT.equals(this.bundleContext.getProperty(OSGI_RESOLVER_MODE));
        return combineDuplicateExports(bundleCapabilities, isStrictMode);
    }

    static Map<String, VersionRange> combineDuplicateExports(List<BundleCapability> capabilities, boolean isStrictMode) {
        Map<String, VersionRange> exportedPackages = new HashMap<String, VersionRange>();
        for (BundleCapability exportedPackage : capabilities) {
            if (isStrictMode) {
                Map<String, String> directives = exportedPackage.getDirectives();
                if (Boolean.valueOf(directives.get(INTERNAL_DIRECTIVE)) || directives.get(FRIENDS_DIRECTIVE) != null) {
                    continue;
                }
            }
            String exportedPackageName = (String) exportedPackage.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE);
            Version exportedPackageVersion = (Version) exportedPackage.getAttributes().get(VERSION);
            VersionRange versionRange = exportedPackages.get(exportedPackageName);
            if (versionRange == null) {
                versionRange = VersionRange.createExactRange(exportedPackageVersion);
            } else {
                if (!versionRange.includes(exportedPackageVersion)) {
                    versionRange = expandVersionRange(exportedPackageVersion, versionRange);
                }
            }
            exportedPackages.put(exportedPackageName, versionRange);
        }
        return exportedPackages;
    }

    private static VersionRange expandVersionRange(Version version, VersionRange versionRange) {
        Version ceiling = versionRange.getCeiling();
        if (version.compareTo(ceiling) > 0) {
            return new VersionRange("[" + versionRange.getFloor() + "," + version + "]");
        } else {
            return new VersionRange("[" + version + "," + versionRange.getCeiling() + "]");
        }
    }
}
