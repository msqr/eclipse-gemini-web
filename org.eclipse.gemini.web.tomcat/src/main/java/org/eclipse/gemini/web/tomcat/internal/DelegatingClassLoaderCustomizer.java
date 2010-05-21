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

    private final ServiceTracker tracker;

    private volatile ClassLoaderCustomizer delegate;

    public DelegatingClassLoaderCustomizer(BundleContext context) {
        this.context = context;
        this.tracker = new ServiceTracker(context, ClassLoaderCustomizer.class.getName(), new Customizer());
    }

    public void open() {
        this.tracker.open();
    }

    public void close() {
        this.tracker.close();
    }

    public void addClassFileTransformer(ClassFileTransformer transformer, Bundle bundle) {
        if (this.delegate != null) {
            this.delegate.addClassFileTransformer(transformer, bundle);
        }
    }

    public ClassLoader createThrowawayClassLoader(Bundle bundle) {
        if (this.delegate != null) {
            return this.delegate.createThrowawayClassLoader(bundle);
        } else {
            return null;
        }
    }

    public ClassLoader[] extendClassLoaderChain(Bundle bundle) {
        if (this.delegate != null) {
            return this.delegate.extendClassLoaderChain(bundle);
        } else {
            return new ClassLoader[0];
        }
    }

    private class Customizer implements ServiceTrackerCustomizer {

        public Object addingService(ServiceReference reference) {
            ClassLoaderCustomizer newDelegate = (ClassLoaderCustomizer) context.getService(reference);

            if (delegate == null) {
                delegate = newDelegate;
            }

            return newDelegate;
        }

        public void modifiedService(ServiceReference reference, Object service) {
            // no-op
        }

        public void removedService(ServiceReference reference, Object service) {
            context.ungetService(reference);
            delegate = null;
        }

    }
}
