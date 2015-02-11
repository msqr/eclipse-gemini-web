/*******************************************************************************
 * Copyright (c) 2009, 2015 VMware Inc.
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

package org.eclipse.gemini.web.tomcat.internal;

import javax.servlet.ServletContext;

import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;

/**
 * A <code>JarScanner</code> implementation that delegates to a chain of <code>JarScanner</code>s.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
final class ChainingJarScanner implements JarScanner {

    private final JarScanner[] jarScanners;

    ChainingJarScanner(JarScanner... jarScanners) {
        this.jarScanners = jarScanners;
    }

    @Override
    public void scan(JarScanType jarScanType, ServletContext context, JarScannerCallback callback) {
        for (JarScanner jarScanner : this.jarScanners) {
            jarScanner.scan(jarScanType, context, callback);
        }
    }

    @Override
    public JarScanFilter getJarScanFilter() {
        return null;
    }

    @Override
    public void setJarScanFilter(JarScanFilter jarScanFilter) {
        // no-op
    }

}
