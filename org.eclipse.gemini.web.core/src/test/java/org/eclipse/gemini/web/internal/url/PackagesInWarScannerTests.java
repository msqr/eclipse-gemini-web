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
        Set<String> packagesContainedInWar = scanner.getPackagesContainedInWar(new URL("file:target/resources/simple-war.war"));
        
        assertEquals(4, packagesContainedInWar.size());
        
        assertTrue(packagesContainedInWar.contains("org.slf4j"));
        assertTrue(packagesContainedInWar.contains("org.slf4j.helpers"));
        assertTrue(packagesContainedInWar.contains("org.slf4j.spi"));
        assertTrue(packagesContainedInWar.contains("foo.bar"));

    }
}
