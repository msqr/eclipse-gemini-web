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

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.ExportedPackage;

import org.eclipse.gemini.web.internal.SystemBundleExportsResolver;
import org.eclipse.virgo.util.osgi.VersionRange;

public class SystemBundleExportsResolverTests {

    @Test
    public void basic() {
        ExportedPackage[] input = new ExportedPackage[] { new ExportedPackageImpl("a", new Version(1, 2, 3)),
            new ExportedPackageImpl("b", new Version(2, 3, 4)) };
        
        Map<String, VersionRange> output = SystemBundleExportsResolver.combineDuplicateExports(input);
        
        assertEquals(2, output.size());
        assertEquals(new VersionRange("[1.2.3,1.2.3]"), output.get("a"));
        assertEquals(new VersionRange("[2.3.4,2.3.4]"), output.get("b"));
    }
    
    @Test
    public void downwardExpansion() {
        ExportedPackage[] input = new ExportedPackage[] { new ExportedPackageImpl("a", new Version(2, 0, 0)),
            new ExportedPackageImpl("a", new Version(1, 0, 0)) };
        
        Map<String, VersionRange> output = SystemBundleExportsResolver.combineDuplicateExports(input);
        
        assertEquals(1, output.size());
        assertEquals(new VersionRange("[1.0.0,2.0.0]"), output.get("a"));        
    }
    
    @Test
    public void upwardExpansion() {
        ExportedPackage[] input = new ExportedPackage[] { new ExportedPackageImpl("a", new Version(1, 0, 0)),
            new ExportedPackageImpl("a", new Version(2, 0, 0)) };
        
        Map<String, VersionRange> output = SystemBundleExportsResolver.combineDuplicateExports(input);
        
        assertEquals(1, output.size());
        assertEquals(new VersionRange("[1.0.0,2.0.0]"), output.get("a"));        
    }
    
    @Test
    public void expansionInBothDirections() {
        ExportedPackage[] input = new ExportedPackage[] { new ExportedPackageImpl("a", new Version(2, 0, 0)),
            new ExportedPackageImpl("a", new Version(3, 0, 0)), new ExportedPackageImpl("a", new Version(1, 0, 0)) };
        
        Map<String, VersionRange> output = SystemBundleExportsResolver.combineDuplicateExports(input);
        
        assertEquals(1, output.size());
        assertEquals(new VersionRange("[1.0.0,3.0.0]"), output.get("a"));
    }

    private static final class ExportedPackageImpl implements ExportedPackage {

        private final String name;

        private final Version version;

        private ExportedPackageImpl(String name, Version version) {
            this.name = name;
            this.version = version;
        }

        public Bundle getExportingBundle() {
            throw new UnsupportedOperationException();
        }

        public Bundle[] getImportingBundles() {
            throw new UnsupportedOperationException();
        }

        public String getName() {
            return this.name;
        }

        public String getSpecificationVersion() {
            throw new UnsupportedOperationException();
        }

        public Version getVersion() {
            return this.version;
        }

        public boolean isRemovalPending() {
            throw new UnsupportedOperationException();
        }

    }
}
