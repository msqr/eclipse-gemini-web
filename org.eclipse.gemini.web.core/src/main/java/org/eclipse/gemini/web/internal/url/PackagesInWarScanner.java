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
import java.util.HashSet;
import java.util.Set;

class PackagesInWarScanner {

    Set<String> getPackagesContainedInWar(URL warURL) throws IOException {
        final Set<String> packagesInWar = new HashSet<String>();
        if (warURL != null) {
            WebBundleScanner scanner = new WebBundleScanner(warURL, new WebBundleScannerCallback() {

                @Override
                public void classFound(String entry) {
                    int lastSlashIndex = entry.lastIndexOf('/');
                    if (lastSlashIndex >= 0) {
                        packagesInWar.add(entry.substring(0, lastSlashIndex).replace('/', '.'));
                    }
                }

                @Override
                public void jarFound(String entry) {
                }
            }, true);
            scanner.scanWar();
        }

        return packagesInWar;
    }
}
