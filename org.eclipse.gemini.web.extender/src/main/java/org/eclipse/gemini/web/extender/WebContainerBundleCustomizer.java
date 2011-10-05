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

import org.eclipse.gemini.web.core.WebApplication;
import org.eclipse.gemini.web.core.WebApplicationStartFailedException;
import org.eclipse.gemini.web.core.WebContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.util.tracker.BundleTrackerCustomizer;

final class WebContainerBundleCustomizer implements BundleTrackerCustomizer<Object> {

    private final WebContainer container;

    private final Bundle extenderBundle;

    public WebContainerBundleCustomizer(WebContainer container, Bundle extenderBundle) {
        this.container = container;
        this.extenderBundle = extenderBundle;
    }

    @Override
    public Object addingBundle(Bundle bundle, BundleEvent event) {
        Object handle = null;
        if (this.container.isWebBundle(bundle)) {
            try {
                WebApplication webApplication = this.container.createWebApplication(bundle, this.extenderBundle);
                handle = webApplication;
                webApplication.start();
            } catch (BundleException e) {
                e.printStackTrace();
            } catch (WebApplicationStartFailedException _) {
                // ignore in order to track this bundle
            }
        }
        return handle;
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
        // no-op
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
        if (this.container.isWebBundle(bundle)) {
            ((WebApplication) object).stop();
        }
    }

}
