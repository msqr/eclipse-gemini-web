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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;

public class SystemBundleExportsResolverTests {

    private BundleCapability capability1;

    private BundleCapability capability2;

    private BundleCapability capability3;

    private List<BundleCapability> input;

    @Before
    public void setUp() {
        this.capability1 = createMock(BundleCapability.class);
        this.capability2 = createMock(BundleCapability.class);
        this.capability3 = createMock(BundleCapability.class);
        this.input = new ArrayList<BundleCapability>();
        this.input.add(this.capability1);
        this.input.add(this.capability2);
    }

    @Test
    public void basic() {
        Map<String, Object> attributes1 = new HashMap<String, Object>();
        attributes1.put(BundleRevision.PACKAGE_NAMESPACE, "a");
        attributes1.put(SystemBundleExportsResolver.VERSION, new Version(1, 2, 3));
        expect(this.capability1.getAttributes()).andReturn(attributes1).times(2);

        Map<String, Object> attributes2 = new HashMap<String, Object>();
        attributes2.put(BundleRevision.PACKAGE_NAMESPACE, "b");
        attributes2.put(SystemBundleExportsResolver.VERSION, new Version(2, 3, 4));
        expect(this.capability2.getAttributes()).andReturn(attributes2).times(2);

        replay(this.capability1, this.capability2);

        Map<String, VersionRange> output = SystemBundleExportsResolver.combineDuplicateExports(this.input, false);

        assertEquals(2, output.size());
        assertEquals(new VersionRange("[1.2.3,1.2.3]"), output.get("a"));
        assertEquals(new VersionRange("[2.3.4,2.3.4]"), output.get("b"));

        verify(this.capability1, this.capability2);
    }

    @Test
    public void downwardExpansion() {
        Map<String, Object> attributes1 = new HashMap<String, Object>();
        attributes1.put(BundleRevision.PACKAGE_NAMESPACE, "a");
        attributes1.put(SystemBundleExportsResolver.VERSION, new Version(2, 0, 0));
        expect(this.capability1.getAttributes()).andReturn(attributes1).times(2);

        Map<String, Object> attributes2 = new HashMap<String, Object>();
        attributes2.put(BundleRevision.PACKAGE_NAMESPACE, "a");
        attributes2.put(SystemBundleExportsResolver.VERSION, new Version(1, 0, 0));
        expect(this.capability2.getAttributes()).andReturn(attributes2).times(2);

        replay(this.capability1, this.capability2);

        Map<String, VersionRange> output = SystemBundleExportsResolver.combineDuplicateExports(this.input, false);

        assertEquals(1, output.size());
        assertEquals(new VersionRange("[1.0.0,2.0.0]"), output.get("a"));

        verify(this.capability1, this.capability2);
    }

    @Test
    public void upwardExpansion() {
        Map<String, Object> attributes1 = new HashMap<String, Object>();
        attributes1.put(BundleRevision.PACKAGE_NAMESPACE, "a");
        attributes1.put(SystemBundleExportsResolver.VERSION, new Version(1, 0, 0));
        expect(this.capability1.getAttributes()).andReturn(attributes1).times(2);

        Map<String, Object> attributes2 = new HashMap<String, Object>();
        attributes2.put(BundleRevision.PACKAGE_NAMESPACE, "a");
        attributes2.put(SystemBundleExportsResolver.VERSION, new Version(2, 0, 0));
        expect(this.capability2.getAttributes()).andReturn(attributes2).times(2);

        replay(this.capability1, this.capability2);

        Map<String, VersionRange> output = SystemBundleExportsResolver.combineDuplicateExports(this.input, false);

        assertEquals(1, output.size());
        assertEquals(new VersionRange("[1.0.0,2.0.0]"), output.get("a"));

        verify(this.capability1, this.capability2);
    }

    @Test
    public void expansionInBothDirections() {
        Map<String, Object> attributes1 = new HashMap<String, Object>();
        attributes1.put(BundleRevision.PACKAGE_NAMESPACE, "a");
        attributes1.put(SystemBundleExportsResolver.VERSION, new Version(2, 0, 0));
        expect(this.capability1.getAttributes()).andReturn(attributes1).times(2);

        Map<String, Object> attributes2 = new HashMap<String, Object>();
        attributes2.put(BundleRevision.PACKAGE_NAMESPACE, "a");
        attributes2.put(SystemBundleExportsResolver.VERSION, new Version(3, 0, 0));
        expect(this.capability2.getAttributes()).andReturn(attributes2).times(2);

        Map<String, Object> attributes3 = new HashMap<String, Object>();
        attributes3.put(BundleRevision.PACKAGE_NAMESPACE, "a");
        attributes3.put(SystemBundleExportsResolver.VERSION, new Version(1, 0, 0));
        expect(this.capability3.getAttributes()).andReturn(attributes3).times(2);

        this.input.add(this.capability3);

        replay(this.capability1, this.capability2, this.capability3);

        Map<String, VersionRange> output = SystemBundleExportsResolver.combineDuplicateExports(this.input, false);

        assertEquals(1, output.size());
        assertEquals(new VersionRange("[1.0.0,3.0.0]"), output.get("a"));

        verify(this.capability1, this.capability2, this.capability3);
    }
}
