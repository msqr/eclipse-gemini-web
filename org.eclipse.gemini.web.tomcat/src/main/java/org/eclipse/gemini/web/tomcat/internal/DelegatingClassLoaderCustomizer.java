/*******************************************************************************
 * Copyright (c) 2009, 2013 VMware Inc.
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.gemini.web.tomcat.internal.loading.ChainedClassLoader;
import org.eclipse.gemini.web.tomcat.spi.ClassLoaderCustomizer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

final class DelegatingClassLoaderCustomizer implements ClassLoaderCustomizer {

    private final BundleContext context;

    private final ServiceTracker<ClassLoaderCustomizer, Object> tracker;

    private volatile Set<ClassLoaderCustomizer> delegate;

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
        if (this.delegate != null && this.delegate.size() > 0) {
            for (ClassLoaderCustomizer classLoaderCustomizer : this.delegate) {
                classLoaderCustomizer.addClassFileTransformer(transformer, bundle);
            }
        }
    }

    @Override
    public ClassLoader createThrowawayClassLoader(Bundle bundle) {
        if (this.delegate != null && this.delegate.size() > 0) {
            Set<ClassLoader> result = new HashSet<ClassLoader>();
            for (ClassLoaderCustomizer classLoaderCustomizer : this.delegate) {
                result.add(classLoaderCustomizer.createThrowawayClassLoader(bundle));
            }
            if (result.size() > 0) {
                return ChainedClassLoader.create(result.toArray(new ClassLoader[result.size()]));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public ClassLoader[] extendClassLoaderChain(Bundle bundle) {
        if (this.delegate != null && this.delegate.size() > 0) {
            Set<ClassLoader> result = new LinkedHashSet<ClassLoader>();
            for (ClassLoaderCustomizer classLoaderCustomizer : this.delegate) {
                result.addAll(Arrays.asList(classLoaderCustomizer.extendClassLoaderChain(bundle)));
            }
            return result.toArray(new ClassLoader[result.size()]);
        } else {
            return new ClassLoader[0];
        }
    }

    private class Customizer implements ServiceTrackerCustomizer<ClassLoaderCustomizer, Object> {

        @Override
        public Object addingService(ServiceReference<ClassLoaderCustomizer> reference) {
            ClassLoaderCustomizer newDelegate = DelegatingClassLoaderCustomizer.this.context.getService(reference);

            if (DelegatingClassLoaderCustomizer.this.delegate == null) {
                DelegatingClassLoaderCustomizer.this.delegate = new HashSet<ClassLoaderCustomizer>();
            }

            DelegatingClassLoaderCustomizer.this.delegate.add(newDelegate);

            return newDelegate;
        }

        @Override
        public void modifiedService(ServiceReference<ClassLoaderCustomizer> reference, Object service) {
            // no-op
        }

        @Override
        public void removedService(ServiceReference<ClassLoaderCustomizer> reference, Object service) {
            if (DelegatingClassLoaderCustomizer.this.delegate != null) {
                DelegatingClassLoaderCustomizer.this.delegate.remove(DelegatingClassLoaderCustomizer.this.context.getService(reference));
            }

            DelegatingClassLoaderCustomizer.this.context.ungetService(reference);
        }

    }
}
