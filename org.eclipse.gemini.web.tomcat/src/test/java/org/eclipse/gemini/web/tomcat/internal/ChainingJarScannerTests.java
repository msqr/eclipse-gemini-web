/*******************************************************************************
 * Copyright (c) 2012, 2015 SAP AG
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
 *   Violeta Georgieva - initial contribution
 *******************************************************************************/

package org.eclipse.gemini.web.tomcat.internal;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.junit.Test;

public class ChainingJarScannerTests {

    private final List<String> scannedResources = new ArrayList<>();

    @Test
    public void testScan() {
        ChainingJarScanner chainingJarScanner = new ChainingJarScanner();
        chainingJarScanner.scan(null, null, null);
        assertTrue(this.scannedResources.size() == 0);

        String resource1 = "resource1";
        String resource2 = "resource2";
        chainingJarScanner = new ChainingJarScanner(jarScannerFor(resource1), jarScannerFor(resource2));
        chainingJarScanner.scan(null, null, null);
        assertTrue(this.scannedResources.size() == 2);
        assertTrue(resource1.equals(this.scannedResources.get(0)));
        assertTrue(resource2.equals(this.scannedResources.get(1)));
    }

    private JarScanner jarScannerFor(final String resource) {
        return new JarScanner() {

            @Override
            public void scan(JarScanType jarScanType, ServletContext context, JarScannerCallback callback) {
                ChainingJarScannerTests.this.scannedResources.add(resource);
            }

            @Override
            public JarScanFilter getJarScanFilter() {
                return null;
            }

            @Override
            public void setJarScanFilter(JarScanFilter arg0) {
                // no-op
            }

        };
    }
}
