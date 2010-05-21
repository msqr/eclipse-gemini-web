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

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;


/**
 * A <code>BundleDependencyDeterminer</code> that uses {@link PackageAdmin} to
 * determine a <code>Bundle</code>'s dependencies.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 *
 */
public final class PackageAdminBundleDependencyDeterminer implements BundleDependencyDeterminer {
    
    private final BundleContext bundleContext;
    
    private final PackageAdmin packageAdmin;
    
    public PackageAdminBundleDependencyDeterminer(BundleContext bundleContext, PackageAdmin packageAdmin) {    
        this.bundleContext = bundleContext;
        this.packageAdmin = packageAdmin;
    }
    
    /** 
     * {@inheritDoc}
     */
    public Set<Bundle> getDependencies(Bundle rootBundle) {
        Set<Bundle> dependencies = new HashSet<Bundle>();
        
        Bundle[] bundles = this.bundleContext.getBundles();
        
        if (bundles != null) {        
            for (Bundle bundle : bundles) {
                ExportedPackage[] exportedPackages = this.packageAdmin.getExportedPackages(bundle);
                if (exportedPackages != null) {
                    for (ExportedPackage exportedPackage : exportedPackages) {
                        Bundle[] importers = exportedPackage.getImportingBundles();
                        if (importers != null) {
                            for (Bundle importer : importers) {
                                if (importer.equals(rootBundle)) {
                                    dependencies.add(bundle);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return dependencies;
    }
}
