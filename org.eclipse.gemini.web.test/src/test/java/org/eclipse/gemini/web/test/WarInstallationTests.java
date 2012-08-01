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

package org.eclipse.gemini.web.test;

import static org.junit.Assert.assertNotNull;

import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.eclipse.virgo.test.framework.TestFrameworkUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

@RunWith(OsgiTestRunner.class)
public class WarInstallationTests {

    private BundleContext bundleContext;

    @Before
    public void before() {
        this.bundleContext = TestFrameworkUtils.getBundleContextForTestClass(getClass());
    }

    @Test
    public void testInstallSimpleWar() throws BundleException {
        String location = "webbundle:file:../org.eclipse.gemini.web.core/target/resources/simple-war.war?Web-ContextPath=/";

        Bundle bundle = this.bundleContext.installBundle(location);
        assertNotNull(bundle);
        assertNotNull(bundle.getSymbolicName());
        bundle.uninstall();
    }
}
