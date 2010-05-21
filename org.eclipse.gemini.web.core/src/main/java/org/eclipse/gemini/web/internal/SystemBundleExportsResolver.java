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
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import org.eclipse.gemini.web.internal.template.ServiceCallback;
import org.eclipse.gemini.web.internal.template.ServiceTemplate;
import org.eclipse.virgo.util.osgi.VersionRange;

final class SystemBundleExportsResolver {
    
    private final BundleContext bundleContext;
    
    SystemBundleExportsResolver(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
    
    public Map<String, VersionRange> getSystemBundleExports() {
        final Bundle systemBundle = bundleContext.getBundle(0);
        ServiceTemplate<PackageAdmin> packageAdminTemplate = new ServiceTemplate<PackageAdmin>(bundleContext, PackageAdmin.class);
        packageAdminTemplate.start();
        ExportedPackage[] systemBundleExports = packageAdminTemplate.executeWithService(new ServiceCallback<PackageAdmin, ExportedPackage[]>() {            
            public ExportedPackage[] doWithService(PackageAdmin packageAdmin) {
                return packageAdmin.getExportedPackages(systemBundle);
            }           
        });     
        packageAdminTemplate.stop();
        return combineDuplicateExports(systemBundleExports);
    }
    
    static Map<String, VersionRange> combineDuplicateExports(ExportedPackage[] allExportedPackages) {                   
        Map<String, VersionRange> exportedPackages = new HashMap<String, VersionRange>();
        for (ExportedPackage exportedPackage : allExportedPackages) {
            VersionRange versionRange = exportedPackages.get(exportedPackage.getName());
            if (versionRange == null) {
                versionRange = VersionRange.createExactRange(exportedPackage.getVersion());             
            } else {
                Version version = exportedPackage.getVersion();
                if (!versionRange.includes(version)) {
                    versionRange = expandVersionRange(version, versionRange);
                }               
            }
            exportedPackages.put(exportedPackage.getName(), versionRange);
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
