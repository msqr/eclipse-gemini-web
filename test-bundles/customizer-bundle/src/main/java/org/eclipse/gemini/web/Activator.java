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

import org.eclipse.gemini.web.tomcat.spi.ClassLoaderCustomizer;
import org.eclipse.gemini.web.tomcat.spi.JarScannerCustomizer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    private ServiceRegistration<?> sr;

    @Override
    public void start(BundleContext context) throws Exception {
        this.sr = context.registerService(new String[] { ClassLoaderCustomizer.class.getName(), JarScannerCustomizer.class.getName() },
            new Customizer(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        this.sr.unregister();
    }

}
