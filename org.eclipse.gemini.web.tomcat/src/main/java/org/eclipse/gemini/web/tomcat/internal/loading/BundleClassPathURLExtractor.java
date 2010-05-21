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

package org.eclipse.gemini.web.tomcat.internal.loading;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BundleClassPathURLExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BundleClassPathURLExtractor.class);

    private static final String JAR_EXTENSION = ".jar";

    public static Set<URI> extractBundleClassPathURLs(Bundle bundle) {
        Set<URI> results = new HashSet<URI>();
        String bcp = (String) bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_CLASSPATH);
        if (bcp != null) {
            String[] entries = bcp.split(",");
            for (String entry : entries) {
                if (isJarEntry(entry)) {
                    URL entryUrl = bundle.getEntry(entry);
                    if (entryUrl != null) {
                        try {
                            URI entryAsJarUrl = new URI("jar", entryUrl.toString() + "!/", null);                        
                            results.add(entryAsJarUrl);
                        } catch (URISyntaxException e) {
                            if (LOGGER.isWarnEnabled()) {
                                LOGGER.warn("Skipping: " + entryUrl, e);
                            }
                        }
                    } else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Bundle-ClassPath entry '" + entry + "' is not present in bundle " + bundle.getSymbolicName() + " " + bundle.getVersion() + " and has been skipped");
                        }
                    }
                }
            }
        }
        return results;
    }

    private static boolean isJarEntry(String entry) {
        return entry.endsWith(JAR_EXTENSION);
    }

}
