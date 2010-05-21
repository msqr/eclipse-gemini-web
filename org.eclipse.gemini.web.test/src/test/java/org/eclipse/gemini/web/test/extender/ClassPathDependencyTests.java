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

package org.eclipse.gemini.web.test.extender;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.eclipse.virgo.test.framework.TestFrameworkUtils;

@RunWith(OsgiTestRunner.class)
public class ClassPathDependencyTests {

    private BundleContext context;

    private Bundle extender;

    private Bundle war;

    @Before
    public void before() throws BundleException {
        this.context = TestFrameworkUtils.getBundleContextForTestClass(getClass());
        this.extender = installExtender();
        this.extender.start();
        this.war = null;
    }

    @After
    public void after() throws BundleException {
        if (this.war != null) {
            uninstallWar();
        }
        if (this.extender != null) {
            this.extender.uninstall();
            this.extender = null;
        }
    }

    @Test
    public void testWarWithClassPathDependencies() throws BundleException, Exception {
        deployWar();
        checkWarBundleClassPath();
    }

    private void deployWar() throws BundleException {
        this.war = this.context.installBundle("webbundle:file:src/test/resources/classpathdeps.war?Web-ContextPath=/classpathdeps");
        this.war.start();
    }

    private void checkWarBundleClassPath() {
        String bundleClassPath = (String) this.war.getHeaders().get("Bundle-ClassPath");
        Assert.assertEquals("WEB-INF/classes,WEB-INF/lib/jar1.jar,j2/jar2.jar,j3/jar3.jar,j4/jar4.jar", bundleClassPath);
    }
    
    @Test
    public void testWarWithCyclicClassPathDependencies() throws BundleException, Exception {
        deployCyclicWar();
        checkWarBundleClassPath();
    }

    private void deployCyclicWar() throws BundleException {
        this.war = this.context.installBundle("webbundle:file:src/test/resources/cyclicclasspathdeps.war?Web-ContextPath=/cyclicclasspathdeps");
        this.war.start();
    }


    private void uninstallWar() throws BundleException {
        this.war.uninstall();
        this.war = null;
    }

    private Bundle installExtender() throws BundleException {
        return this.context.installBundle("file:../org.eclipse.gemini.web.extender/target/classes");
    }

}
