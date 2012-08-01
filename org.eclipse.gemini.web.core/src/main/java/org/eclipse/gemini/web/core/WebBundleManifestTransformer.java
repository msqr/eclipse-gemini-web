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

import java.io.IOException;
import java.net.URL;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * Strategy for applying transformations to a web bundle manifest.
 * <p/>
 * The exact set of transformations performed is implementation-dependent, but it is expected that implementations will
 * at least support the transformations mandated by the RFC66 specification.
 * <p/>
 * Transformations that are not defined by the specification should be disabled by default and enabled using an
 * {@link InstallationOptions installation option}.
 * 
 * 
 */
public interface WebBundleManifestTransformer {

    /**
     * Transforms the supplied {@link BundleManifest} in place.
     * 
     * @param manifest the <code>BundleManifest</code> to transform.
     * @param sourceURL the {@link URL} the bundle was installed from.
     * @param options the {@link InstallationOptions}. May be <code>null</code>.
     * @param webBundle whether or not the bundle is deemed to be a bundle as determined by the
     *        {@link org.eclipse.gemini.web.internal.WebContainerUtils#isWebApplicationBundle isWebApplicationBundle}
     *        specification.
     * @throws IOException if transformation fails.
     */
    void transform(BundleManifest manifest, URL sourceURL, InstallationOptions options, boolean webBundle) throws IOException;
}
