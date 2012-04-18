/*******************************************************************************
 * Copyright (c) 2012 SAP AG
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

package org.eclipse.gemini.web;

import java.lang.instrument.ClassFileTransformer;

import org.apache.tomcat.JarScanner;
import org.eclipse.gemini.web.tomcat.spi.ClassLoaderCustomizer;
import org.eclipse.gemini.web.tomcat.spi.JarScannerCustomizer;
import org.osgi.framework.Bundle;

public class Customizer implements ClassLoaderCustomizer, JarScannerCustomizer {

    @Override
    public ClassLoader[] extendClassLoaderChain(Bundle bundle) {
        return new ClassLoader[] { this.getClass().getClassLoader() };
    }

    @Override
    public void addClassFileTransformer(ClassFileTransformer transformer, Bundle bundle) {
        // no-op
    }

    @Override
    public ClassLoader createThrowawayClassLoader(Bundle bundle) {
        // no-op
        return null;
    }

    @Override
    public JarScanner[] extendJarScannerChain(Bundle bundle) {
        return new JarScanner[] { new BundleJarScanner() };
    }
}