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

package org.eclipse.gemini.web.internal.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.eclipse.gemini.web.internal.url.PackagesInWarScanner;
import org.junit.Test;

public class PackagesInWarScannerTests {
    
    @Test
    public void scanning() throws MalformedURLException, IOException {
        PackagesInWarScanner scanner = new PackagesInWarScanner();
        Set<String> packagesContainedInWar = scanner.getPackagesContainedInWar(new URL("file:src/test/resources/simple-war.war"));
        
        assertEquals(47, packagesContainedInWar.size()); // was 6 (see below)  bug?:
        
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.boolex"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.db"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.filter"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.html"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.jmx"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.joran"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.joran.action"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.log4j"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.net"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.pattern"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.selector"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.selector.servlet"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.sift"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.spi"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.turbo"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.classic.util"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.boolex"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.db"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.db.dialect"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.filter"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.helpers"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.html"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.joran"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.joran.action"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.joran.event"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.joran.spi"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.layout"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.net"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.pattern"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.pattern.parser"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.pattern.util"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.read"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.rolling"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.rolling.helper"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.sift"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.spi"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.status"));
        assertTrue(packagesContainedInWar.contains("ch.qos.logback.core.util"));
        assertTrue(packagesContainedInWar.contains("foo"));
        assertTrue(packagesContainedInWar.contains("foo.bar"));
        assertTrue(packagesContainedInWar.contains("goo"));
        assertTrue(packagesContainedInWar.contains("org.slf4j"));
        assertTrue(packagesContainedInWar.contains("org.slf4j.helpers"));
        assertTrue(packagesContainedInWar.contains("org.slf4j.impl"));
        assertTrue(packagesContainedInWar.contains("org.slf4j.spi"));

        
//        assertTrue(packagesContainedInWar.contains("foo"));
//        assertTrue(packagesContainedInWar.contains("foo.bar"));
//        assertTrue(packagesContainedInWar.contains("goo"));
//        assertTrue(packagesContainedInWar.contains("org.slf4j"));
//        assertTrue(packagesContainedInWar.contains("org.slf4j.helpers"));
//        assertTrue(packagesContainedInWar.contains("org.slf4j.spi"));
    }
}
