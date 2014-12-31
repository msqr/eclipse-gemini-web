/*******************************************************************************
 * Copyright (c) 2009, 2014 VMware Inc.
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

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class EquinoxBundleFileResolver implements BundleFileResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(EquinoxBundleFileResolver.class);

    @Override
    public File resolve(Bundle bundle) {
        BundleFile bundleFile = getBundleFile(bundle);
        if (bundleFile != null) {
            File file = bundleFile.getBaseFile();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Resolved bundle '" + bundle.getSymbolicName() + "' to file '" + file.getAbsolutePath() + "'");
            }
            return file;
        }
        return null;
    }

    @Override
    public long resolveBundleEntrySize(Bundle bundle, String path) {
        BundleFile bundleFile = getBundleFile(bundle);
        if (bundleFile != null) {
            BundleEntry bundleEntry = bundleFile.getEntry(path);
            if (bundleEntry != null) {
                return bundleEntry.getSize();
            }
        }
        return -1L;
    }

    public static boolean canUse() {
        try {
            EquinoxBundleFileResolver.class.getClassLoader().loadClass(BundleHost.class.getName());
            return true;
        } catch (Exception | LinkageError _) {
            return false;
        }
    }

    private BundleFile getBundleFile(Bundle bundle) {
        if (bundle instanceof BundleHost) {
            BundleHost bh = (BundleHost) bundle;
            BundleData bundleData = bh.getBundleData();
            if (bundleData instanceof BaseData) {
                return ((BaseData) bundleData).getBundleFile();
            }
        }
        return null;
    }
}
