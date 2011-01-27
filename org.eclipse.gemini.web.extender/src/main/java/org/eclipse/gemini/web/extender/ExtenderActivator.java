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

package org.eclipse.gemini.web.extender;

import org.eclipse.gemini.web.core.WebContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;




public final class ExtenderActivator implements BundleActivator {

    private ServiceTracker<WebContainer, String> serviceTracker;
    
    public void start(BundleContext context) {
        this.serviceTracker = new ServiceTracker(context, WebContainer.class, new ExtendedWebContainerTracker(context));
        this.serviceTracker.open();
    }

    public void stop(BundleContext context) {
        this.serviceTracker.close();
    }
    
    private static final class ExtendedWebContainerTracker implements ServiceTrackerCustomizer<WebContainer, String> {

        private final BundleContext context;
        
        private BundleTracker<String> bundleTracker;
        
        public ExtendedWebContainerTracker(BundleContext context) {
            this.context = context;
        }

        public String addingService(ServiceReference<WebContainer> reference) {
            if(this.bundleTracker == null) {
                WebContainer container = (WebContainer) this.context.getService(reference);
                this.bundleTracker = new BundleTracker(this.context, Bundle.ACTIVE, new WebContainerBundleCustomizer(container, context.getBundle()));
            }
            this.bundleTracker.open();
            return reference.getBundle().getSymbolicName();
        }

        public void modifiedService(ServiceReference<WebContainer> reference, String service) {
        }

        public void removedService(ServiceReference<WebContainer> reference, String service) {
            this.bundleTracker.close();
        }
        
    }

}
