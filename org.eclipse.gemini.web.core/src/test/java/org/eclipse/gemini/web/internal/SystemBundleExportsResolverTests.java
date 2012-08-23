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

package org.eclipse.gemini.web.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

public class SystemBundleExportsResolverTests {

    @Test
    public void basic() {
        BundleCapability capability1 = createBundleCapabilityMock("", "", "a", new Version(1, 2, 3));

        BundleCapability capability2 = createBundleCapabilityMock("", "", "b", new Version(2, 3, 4));

        List<BundleCapability> input = new ArrayList<BundleCapability>();
        input.add(capability1);
        input.add(capability2);

        replay(capability1, capability2);

        Map<String, VersionRange> output = SystemBundleExportsResolver.combineDuplicateExports(input, false);

        assertEquals(2, output.size());
        assertEquals(new VersionRange("[1.2.3,1.2.3]"), output.get("a"));
        assertEquals(new VersionRange("[2.3.4,2.3.4]"), output.get("b"));

        verify(capability1, capability2);
    }

    @Test
    public void downwardExpansion() {
        BundleCapability capability1 = createBundleCapabilityMock("", "", "a", new Version(2, 0, 0));

        BundleCapability capability2 = createBundleCapabilityMock("", "", "a", new Version(1, 0, 0));

        List<BundleCapability> input = new ArrayList<BundleCapability>();
        input.add(capability1);
        input.add(capability2);

        replay(capability1, capability2);

        Map<String, VersionRange> output = SystemBundleExportsResolver.combineDuplicateExports(input, false);

        assertEquals(1, output.size());
        assertEquals(new VersionRange("[1.0.0,2.0.0]"), output.get("a"));

        verify(capability1, capability2);
    }

    @Test
    public void upwardExpansion() {
        BundleCapability capability1 = createBundleCapabilityMock("", "", "a", new Version(1, 0, 0));

        BundleCapability capability2 = createBundleCapabilityMock("", "", "a", new Version(2, 0, 0));

        List<BundleCapability> input = new ArrayList<BundleCapability>();
        input.add(capability1);
        input.add(capability2);

        replay(capability1, capability2);

        Map<String, VersionRange> output = SystemBundleExportsResolver.combineDuplicateExports(input, false);

        assertEquals(1, output.size());
        assertEquals(new VersionRange("[1.0.0,2.0.0]"), output.get("a"));

        verify(capability1, capability2);
    }

    @Test
    public void expansionInBothDirections() {
        BundleCapability capability1 = createBundleCapabilityMock("", "", "a", new Version(2, 0, 0));

        BundleCapability capability2 = createBundleCapabilityMock("", "", "a", new Version(3, 0, 0));

        BundleCapability capability3 = createBundleCapabilityMock("", "", "a", new Version(1, 0, 0));

        BundleCapability capability4 = createBundleCapabilityMock("", "", "a", new Version(1, 2, 0));

        List<BundleCapability> input = new ArrayList<BundleCapability>();
        input.add(capability1);
        input.add(capability2);
        input.add(capability3);
        input.add(capability4);

        replay(capability1, capability2, capability3, capability4);

        Map<String, VersionRange> output = SystemBundleExportsResolver.combineDuplicateExports(input, false);

        assertEquals(1, output.size());
        assertEquals(new VersionRange("[1.0.0,3.0.0]"), output.get("a"));

        verify(capability1, capability2, capability3, capability4);
    }

    @Test
    public void testGetSystemBundleExports() {
        BundleContext bundleContext = createMock(BundleContext.class);
        Bundle bundle = createMock(Bundle.class);
        BundleRevision bundleRevision = createMock(BundleRevision.class);
        BundleWiring bundleWiring = createMock(BundleWiring.class);

        BundleCapability capability1 = createBundleCapabilityMock("", "", "a", new Version("1.0.0"));
        BundleCapability capability2 = createBundleCapabilityMock(SystemBundleExportsResolver.INTERNAL_DIRECTIVE, "true", "b", new Version("1.0.0"));
        BundleCapability capability3 = createBundleCapabilityMock(SystemBundleExportsResolver.FRIENDS_DIRECTIVE, "", "c", new Version("1.0.0"));
        List<BundleCapability> input = new ArrayList<BundleCapability>();
        input.add(capability1);
        input.add(capability2);
        input.add(capability3);

        expect(bundleContext.getBundle(0)).andReturn(bundle).times(2);
        expect(bundleContext.getProperty(SystemBundleExportsResolver.OSGI_RESOLVER_MODE)).andReturn(
            SystemBundleExportsResolver.OSGI_RESOLVER_MODE_STRICT).andReturn("");
        expect(bundle.adapt(BundleRevision.class)).andReturn(bundleRevision).times(2);
        expect(bundleRevision.getWiring()).andReturn(bundleWiring).times(2);
        expect(bundleWiring.getCapabilities(BundleRevision.PACKAGE_NAMESPACE)).andReturn(input).times(2);

        replay(bundleContext, bundle, bundleRevision, bundleWiring, capability1, capability2, capability3);

        SystemBundleExportsResolver systemBundleExportsResolver = new SystemBundleExportsResolver(bundleContext);

        Map<String, VersionRange> exports = systemBundleExportsResolver.getSystemBundleExports();
        assertTrue(exports.size() == 1);
        assertTrue(exports.containsKey("a"));
        assertEquals("[1.0.0, 1.0.0]", exports.get("a").toParseString());

        exports = systemBundleExportsResolver.getSystemBundleExports();
        assertTrue(exports.size() == 3);
        assertTrue(exports.containsKey("a"));
        assertTrue(exports.containsKey("b"));
        assertTrue(exports.containsKey("c"));

        verify(bundleContext, bundle, bundleRevision, bundleWiring, capability1, capability2, capability3);
    }

    private BundleCapability createBundleCapabilityMock(String directive, String directiveValue, String exportPackage, Version version) {
        BundleCapability bundleCapability = createMock(BundleCapability.class);

        Map<String, String> directives = new HashMap<String, String>();
        directives.put(directive, directiveValue);
        expect(bundleCapability.getDirectives()).andReturn(directives).anyTimes();

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(BundleRevision.PACKAGE_NAMESPACE, exportPackage);
        attributes.put(SystemBundleExportsResolver.VERSION, version);
        expect(bundleCapability.getAttributes()).andReturn(attributes).anyTimes();

        return bundleCapability;
    }
}
