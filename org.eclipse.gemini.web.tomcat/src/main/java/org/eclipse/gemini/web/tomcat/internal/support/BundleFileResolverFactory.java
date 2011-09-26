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

package org.eclipse.gemini.web.tomcat.internal.support;

import java.io.File;

import org.osgi.framework.Bundle;

/**
 * Factory for {@link BundleFileResolver} implementations.
 * 
 */
public final class BundleFileResolverFactory {

    public static BundleFileResolver createBundleFileResolver() {
        if (EquinoxBundleFileResolver.canUse()) {
            return new EquinoxBundleFileResolver();
        } else {
            return new NoOpBundleFileResolver();
        }
    }

    private static class NoOpBundleFileResolver implements BundleFileResolver {

        @Override
        public File resolve(Bundle bundle) {
            return null;
        }

        @Override
        public long resolveBundleEntrySize(Bundle bundle, String path) {
            return -1L;
        }

    }
}
