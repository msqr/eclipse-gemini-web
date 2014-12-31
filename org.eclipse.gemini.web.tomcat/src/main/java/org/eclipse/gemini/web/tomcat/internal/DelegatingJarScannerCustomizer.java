/*******************************************************************************
 * Copyright (c) 2012, 2014 SAP AG
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.tomcat.JarScanner;
import org.eclipse.gemini.web.tomcat.spi.JarScannerCustomizer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

final class DelegatingJarScannerCustomizer implements JarScannerCustomizer {

    private final BundleContext context;

    private final ServiceTracker<JarScannerCustomizer, Object> tracker;

    private volatile Set<JarScannerCustomizer> delegate;

    DelegatingJarScannerCustomizer(BundleContext context) {
        this.context = context;
        this.tracker = new ServiceTracker<>(context, JarScannerCustomizer.class.getName(), new Customizer());
    }

    void open() {
        this.tracker.open();
    }

    void close() {
        this.tracker.close();
    }

    @Override
    public JarScanner[] extendJarScannerChain(Bundle bundle) {
        if (this.delegate != null && this.delegate.size() > 0) {
            Set<JarScanner> jarScanners = new HashSet<>();
            for (JarScannerCustomizer jarScannerCustomizer : this.delegate) {
                jarScanners.addAll(Arrays.asList(jarScannerCustomizer.extendJarScannerChain(bundle)));
            }
            return jarScanners.toArray(new JarScanner[jarScanners.size()]);
        }
        return new JarScanner[0];
    }

    private class Customizer implements ServiceTrackerCustomizer<JarScannerCustomizer, Object> {

        @Override
        public Object addingService(ServiceReference<JarScannerCustomizer> reference) {
            JarScannerCustomizer newDelegate = DelegatingJarScannerCustomizer.this.context.getService(reference);

            if (DelegatingJarScannerCustomizer.this.delegate == null) {
                DelegatingJarScannerCustomizer.this.delegate = new HashSet<>();
            }

            DelegatingJarScannerCustomizer.this.delegate.add(newDelegate);

            return newDelegate;
        }

        @Override
        public void modifiedService(ServiceReference<JarScannerCustomizer> reference, Object service) {
            // no-op
        }

        @Override
        public void removedService(ServiceReference<JarScannerCustomizer> reference, Object service) {
            if (DelegatingJarScannerCustomizer.this.delegate != null) {
                DelegatingJarScannerCustomizer.this.delegate.remove(DelegatingJarScannerCustomizer.this.context.getService(reference));
            }
            DelegatingJarScannerCustomizer.this.context.ungetService(reference);
        }

    }

}
