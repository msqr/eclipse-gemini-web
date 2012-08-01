/*******************************************************************************
 * Copyright (c) 2009, 2012 VMware Inc.
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
import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

/**
 * A <code>BundleDependencyDeterminer</code> that uses {@link BundleWiring} to determine a <code>Bundle</code>'s
 * dependencies.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public final class PackageAdminBundleDependencyDeterminer implements BundleDependencyDeterminer {

    public PackageAdminBundleDependencyDeterminer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Bundle> getDependencies(Bundle rootBundle) {
        Set<Bundle> dependencies = new HashSet<Bundle>();

        BundleWiring bundleWiring = rootBundle.adapt(BundleRevision.class).getWiring();

        // Look at imported packages
        dependencies.addAll(getRequiredWires(bundleWiring, BundleRevision.PACKAGE_NAMESPACE));

        // Look at required bundles
        dependencies.addAll(getRequiredWires(bundleWiring, BundleRevision.BUNDLE_NAMESPACE));

        return dependencies;
    }

    private Set<Bundle> getRequiredWires(BundleWiring bundleWiring, String namespace) {
        Set<Bundle> dependencies = new HashSet<Bundle>();

        List<BundleWire> bundleWires = bundleWiring.getRequiredWires(namespace);
        if (bundleWires != null) {
            for (BundleWire wire : bundleWires) {
                dependencies.add(wire.getProviderWiring().getBundle());
            }
        }

        return dependencies;
    }
}
