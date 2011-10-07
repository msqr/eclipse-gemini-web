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

package org.eclipse.gemini.web.internal.template;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServiceTemplate<S> {

    private final ServiceTracker<Object, Object> tracker;

    public ServiceTemplate(BundleContext context, Class<S> clazz) {
        this.tracker = new ServiceTracker<Object, Object>(context, clazz.getName(), new ServiceTemplateCustomizer(context));
    }

    public void start() {
        this.tracker.open();
    }

    public void stop() {
        this.tracker.close();
    }

    @SuppressWarnings("unchecked")
    public <T> T executeWithService(ServiceCallback<S, T> callback) {
        Object service = this.tracker.getService();
        if (service != null) {
            return callback.doWithService((S) service);
        }
        return null;
    }

    private static final class ServiceTemplateCustomizer implements ServiceTrackerCustomizer<Object, Object> {

        private final BundleContext context;

        public ServiceTemplateCustomizer(BundleContext context) {
            this.context = context;
        }

        @Override
        public Object addingService(ServiceReference<Object> reference) {
            return this.context.getService(reference);
        }

        @Override
        public void modifiedService(ServiceReference<Object> reference, Object service) {
        }

        @Override
        public void removedService(ServiceReference<Object> reference, Object service) {
            this.context.ungetService(reference);
        }

    }
}
