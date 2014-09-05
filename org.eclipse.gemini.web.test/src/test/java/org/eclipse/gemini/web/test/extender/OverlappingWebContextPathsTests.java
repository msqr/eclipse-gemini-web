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

package org.eclipse.gemini.web.test.extender;

import java.io.IOException;
import java.net.MalformedURLException;

import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

@RunWith(OsgiTestRunner.class)
public class OverlappingWebContextPathsTests extends ExtenderBase {

    private Bundle extender;

    private Bundle war1;

    private Bundle war2;

    @Before
    public void before() throws BundleException {
        this.extender = installExtender();
        this.extender.start();
        this.war1 = null;
        this.war2 = null;
    }

    @After
    public void after() throws BundleException {
        uninstallBundle(this.war1);
        uninstallBundle(this.war2);
        uninstallBundle(this.extender);
    }

    @Test
    public void testWarWithAGivenWebContextPathOverridesLaterWarWithTheSamePath() throws BundleException, Exception {
        deployWar1();
        checkWar1Responding();
        deployWar2();
        checkWar1Responding();
    }

    @Test
    public void testLaterWarWithOverlappingWebContextPathRespondsWhenEarlierWarIsStoppedAndLaterWarIsUpdated() throws BundleException, Exception {
        deployWar1();
        checkWar1Responding();
        deployWar2();
        this.war1.stop();
        checkWar2Responding();
    }

    private void deployWar1() throws BundleException {
        this.war1 = installBundle(null, "webbundle:file:target/resources/war-with-servlet.war?Web-ContextPath=/overlap", "");
        this.war1.start();
    }

    private void deployWar2() throws BundleException {
        this.war2 = installBundle(null, "webbundle:file:target/resources/war-with-another-servlet.war?Web-ContextPath=/overlap", "");
        this.war2.start();
    }

    private void checkWar1Responding() throws MalformedURLException, IOException, InterruptedException {
        validateURL("http://localhost:8080/overlap/test", "Hello World!");
    }

    private void checkWar2Responding() throws MalformedURLException, IOException, InterruptedException {
        validateURL("http://localhost:8080/overlap/test", "Another");
    }

    @Override
    protected Bundle installWarBundle(String suffix) throws BundleException {
        // no-op
        return null;
    }

    @Override
    protected Bundle installBundle(String location, String bundleUrl, String suffix) throws BundleException {
        return getBundleContext().installBundle(bundleUrl + suffix);
    }

}
