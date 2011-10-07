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

package org.eclipse.gemini.web.tomcat.internal;

import java.lang.instrument.ClassFileTransformer;

import org.eclipse.gemini.web.tomcat.spi.ClassLoaderCustomizer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

final class DelegatingClassLoaderCustomizer implements ClassLoaderCustomizer {

    private final BundleContext context;

    private final ServiceTracker<ClassLoaderCustomizer, Object> tracker;

    private volatile ClassLoaderCustomizer delegate;

    public DelegatingClassLoaderCustomizer(BundleContext context) {
        this.context = context;
        this.tracker = new ServiceTracker<ClassLoaderCustomizer, Object>(context, ClassLoaderCustomizer.class.getName(), new Customizer());
    }

    public void open() {
        this.tracker.open();
    }

    public void close() {
        this.tracker.close();
    }

    @Override
    public void addClassFileTransformer(ClassFileTransformer transformer, Bundle bundle) {
        if (this.delegate != null) {
            this.delegate.addClassFileTransformer(transformer, bundle);
        }
    }

    @Override
    public ClassLoader createThrowawayClassLoader(Bundle bundle) {
        if (this.delegate != null) {
            return this.delegate.createThrowawayClassLoader(bundle);
        } else {
            return null;
        }
    }

    @Override
    public ClassLoader[] extendClassLoaderChain(Bundle bundle) {
        if (this.delegate != null) {
            return this.delegate.extendClassLoaderChain(bundle);
        } else {
            return new ClassLoader[0];
        }
    }

    private class Customizer implements ServiceTrackerCustomizer<ClassLoaderCustomizer, Object> {

        @Override
        public Object addingService(ServiceReference<ClassLoaderCustomizer> reference) {
            ClassLoaderCustomizer newDelegate = DelegatingClassLoaderCustomizer.this.context.getService(reference);

            if (DelegatingClassLoaderCustomizer.this.delegate == null) {
                DelegatingClassLoaderCustomizer.this.delegate = newDelegate;
            }

            return newDelegate;
        }

        @Override
        public void modifiedService(ServiceReference<ClassLoaderCustomizer> reference, Object service) {
            // no-op
        }

        @Override
        public void removedService(ServiceReference<ClassLoaderCustomizer> reference, Object service) {
            DelegatingClassLoaderCustomizer.this.context.ungetService(reference);
            DelegatingClassLoaderCustomizer.this.delegate = null;
        }

    }
}
