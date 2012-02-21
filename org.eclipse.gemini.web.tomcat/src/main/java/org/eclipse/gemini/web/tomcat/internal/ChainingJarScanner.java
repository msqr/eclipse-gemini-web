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

package org.eclipse.gemini.web.tomcat.internal;

import java.util.Set;

import javax.servlet.ServletContext;

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
    public void scan(ServletContext context, ClassLoader classloader, JarScannerCallback callback, Set<String> jarsToSkip) {
        for (JarScanner jarScanner : this.jarScanners) {
            jarScanner.scan(context, classloader, callback, jarsToSkip);
        }
    }

}
