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

import org.eclipse.gemini.web.core.InstallationOptions;
import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;


public class ChainingWebBundleManifestTransformer implements WebBundleManifestTransformer {
    
    private final WebBundleManifestTransformer[] manifestTransformers;
    
    public ChainingWebBundleManifestTransformer(WebBundleManifestTransformer... transformers) {
        this.manifestTransformers = transformers;
    }

    public void transform(BundleManifest manifest, URL sourceURL, InstallationOptions options, boolean webBundle) throws IOException {
        if (options == null) {
            options = new InstallationOptions(Collections.<String, String> emptyMap());
        }
        for (WebBundleManifestTransformer manifestTransformer : this.manifestTransformers) {
            manifestTransformer.transform(manifest, sourceURL, options, webBundle);
        }
    }
}
