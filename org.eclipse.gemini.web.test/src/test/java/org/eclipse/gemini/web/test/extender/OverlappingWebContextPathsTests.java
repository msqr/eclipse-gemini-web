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

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

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
public class OverlappingWebContextPathsTests {

    private BundleContext context;

    private Bundle extender;

    private Bundle war1;

    private Bundle war2;

    @Before
    public void before() throws BundleException {
        this.context = TestFrameworkUtils.getBundleContextForTestClass(getClass());
        this.extender = installExtender();
        this.extender.start();
        this.war1 = null;
        this.war2 = null;
    }

    @After
    public void after() throws BundleException {
        if (this.war1 != null) {
            uninstallWar1();
        }
        if (this.war2 != null) {
            uninstallWar2();
        }
        if (this.extender != null) {
            this.extender.uninstall();
            this.extender = null;
        }
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
        this.war1 = this.context.installBundle("webbundle:file:target/resources/war-with-servlet.war?Web-ContextPath=/overlap");
        this.war1.start();
    }

    private void deployWar2() throws BundleException {
        this.war2 = this.context.installBundle("webbundle:file:target/resources/war-with-another-servlet.war?Web-ContextPath=/overlap");
        this.war2.start();
    }

    private void uninstallWar1() throws BundleException {
        this.war1.uninstall();
        this.war1 = null;
    }

    private void uninstallWar2() throws BundleException {
        this.war2.uninstall();
        this.war2 = null;
    }

    private void checkWar1Responding() throws MalformedURLException, IOException, InterruptedException {
        validateURL("http://localhost:8080/overlap/test", "Hello World!");
    }

    private void checkWar2Responding() throws MalformedURLException, IOException, InterruptedException {
        validateURL("http://localhost:8080/overlap/test", "Another");
    }

    private Bundle installExtender() throws BundleException {
        return this.context.installBundle("file:../org.eclipse.gemini.web.extender/target/classes");
    }

    private void validateURL(String path, String expectedResponse) throws MalformedURLException, IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(openInputStream(path)));
        try {
            Assert.assertEquals(expectedResponse, reader.readLine());
        } finally {
            reader.close();
        }
    }

    private InputStream openInputStream(String path) throws MalformedURLException, InterruptedException {
        URL url = new URL(path);
        InputStream stream = null;
        for (int i = 0; i < 5; i++) {
            try {
                stream = url.openConnection().getInputStream();
            } catch (IOException e) {
                Thread.sleep(1000);
            }
        }
        assertNotNull(stream);
        return stream;
    }

}
