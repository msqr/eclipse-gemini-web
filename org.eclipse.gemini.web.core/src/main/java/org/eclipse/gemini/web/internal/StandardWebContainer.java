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

package org.eclipse.gemini.web.internal;

import org.eclipse.gemini.web.core.WebApplication;
import org.eclipse.gemini.web.core.WebContainer;
import org.eclipse.gemini.web.core.spi.ServletContainer;
import org.eclipse.gemini.web.core.spi.ServletContainerException;
import org.eclipse.gemini.web.core.spi.WebApplicationHandle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard implementation of {@link WebContainer}.
 */
final class StandardWebContainer implements WebContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardWebContainer.class);

    private final EventManager eventManager;

    private final ServletContainer servletContainer;

    private final WebApplicationStartFailureRetryController retryController = new WebApplicationStartFailureRetryController();

    public StandardWebContainer(ServletContainer servletContainer, EventManager eventManager) {
        this.servletContainer = servletContainer;
        this.eventManager = eventManager;
    }

    @Override
    public WebApplication createWebApplication(Bundle bundle) throws BundleException {
        return this.createWebApplication(bundle, null);
    }

    @Override
    public WebApplication createWebApplication(Bundle bundle, Bundle extender) throws BundleException {
        if (!isWebBundle(bundle)) {
            throw new BundleException("Bundle '" + bundle + "' is not a valid web bundle.");
        }
        try {
            WebApplicationHandle handle = this.servletContainer.createWebApplication(WebContainerUtils.getContextPath(bundle), bundle);
            handle.getServletContext().setAttribute(ATTRIBUTE_BUNDLE_CONTEXT, bundle.getBundleContext());
            return new StandardWebApplication(bundle, extender, handle, this.servletContainer, this.eventManager, this.retryController);
        } catch (ServletContainerException ex) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Failed to create web application for bundle '" + bundle + "'", ex);
            }
            throw new BundleException("Failed to create web application for bundle '" + bundle + "'", ex);
        }
    }

    @Override
    public boolean isWebBundle(Bundle bundle) {
        return WebContainerUtils.isWebBundle(bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void halt() {
        this.retryController.clear();
    }

}
