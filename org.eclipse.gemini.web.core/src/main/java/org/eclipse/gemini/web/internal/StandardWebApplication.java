/*******************************************************************************
 * Copyright (c) 2009, 2014 VMware Inc.
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

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.ServletContext;

import org.eclipse.gemini.web.core.WebApplication;
import org.eclipse.gemini.web.core.WebApplicationStartFailedException;
import org.eclipse.gemini.web.core.spi.ServletContainer;
import org.eclipse.gemini.web.core.spi.WebApplicationHandle;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class StandardWebApplication implements WebApplication {

    private static final String BUNDLE_STATE_UNKNOWN = "UNKNOWN";

    private static final String BUNDLE_STATE_UNINSTALLED = "UNINSTALLED";

    private static final String BUNDLE_STATE_STOPPING = "STOPPING";

    private static final String BUNDLE_STATE_STARTING = "STARTING";

    private static final String BUNDLE_STATE_RESOLVED = "RESOLVED";

    private static final String BUNDLE_STATE_INSTALLED = "INSTALLED";

    private static final String BUNDLE_STATE_ACTIVE = "ACTIVE";

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardWebApplication.class);

    private final Bundle bundle;

    private final Bundle extender;

    private final BundleContext thisBundleContext;

    private final WebApplicationHandle handle;

    private final ServletContainer container;

    private final ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();

    private final EventManager eventManager;

    private boolean started = false;

    private final Object monitor = new Object();

    private final WebApplicationStartFailureRetryController retryController;

    public StandardWebApplication(Bundle bundle, Bundle extender, WebApplicationHandle handle, ServletContainer container, EventManager eventManager,
        WebApplicationStartFailureRetryController retryController, BundleContext thisBundleContext) {
        this.bundle = bundle;
        this.extender = extender;
        this.handle = handle;
        this.container = container;
        this.eventManager = eventManager;
        this.retryController = retryController;
        this.thisBundleContext = thisBundleContext;
    }

    @Override
    public ServletContext getServletContext() {
        return this.handle.getServletContext();
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.handle.getClassLoader();
    }

    @Override
    public void start() {
        boolean localStarted;

        synchronized (this.monitor) {
            localStarted = this.started;
        }

        if (!localStarted) {
            String webContextPath = getContextPath();
            this.eventManager.sendDeploying(getBundle(), this.extender, webContextPath);

            boolean startOK = false;
            try {
                this.container.startWebApplication(this.handle);
                startOK = true;

                publishServletContext();

                synchronized (this.monitor) {
                    this.started = true;
                    localStarted = this.started;
                }

                this.eventManager.sendDeployed(getBundle(), this.extender, webContextPath);
            } catch (RuntimeException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Failed to start web application at context path '" + webContextPath + "'", e);
                }
                try {
                    this.retryController.recordFailure(this);
                    Set<Long> webContextPathBundleIds = getWebContextPathBundleIds(webContextPath);
                    boolean collision = webContextPathBundleIds.size() > 1;
                    this.eventManager.sendFailed(getBundle(), this.extender, webContextPath, e, collision ? webContextPath : null,
                        collision ? webContextPathBundleIds : null);
                } finally {
                    if (!localStarted) {
                        if (startOK) {
                            this.container.stopWebApplication(this.handle);
                        }
                    }
                }
                throw new WebApplicationStartFailedException(e);
            }
        }
    }

    private Set<Long> getWebContextPathBundleIds(String webContextPath) {
        Set<Long> bundleIds = new HashSet<>();
        // Use this bundle context to retrieve all bundles
        // Extender bundle cannot be used because it might be null
        // Web app bundle cannot be use because its bundle context might not be a valid one and RuntimeException will be
        // thrown
        for (Bundle bundle : this.thisBundleContext.getBundles()) {
            if (webContextPath.equals(WebContainerUtils.getContextPath(bundle))) {
                bundleIds.add(bundle.getBundleId());
            }
        }
        return bundleIds;
    }

    @Override
    public void stop() {
        boolean localStarted;

        synchronized (this.monitor) {
            localStarted = this.started;
            this.started = false;
        }

        if (localStarted) {
            this.eventManager.sendUndeploying(getBundle(), this.extender, getContextPath());
            this.container.stopWebApplication(this.handle);
            this.tracker.unregisterAll();
            this.eventManager.sendUndeployed(getBundle(), this.extender, getContextPath());
        }
        this.retryController.retryFailures(this);
    }

    private void publishServletContext() {
        Dictionary<String, String> properties = constructServletContextProperties();
        BundleContext bundleContext = getBundle().getBundleContext();
        if (bundleContext != null) {
            this.tracker.track(bundleContext.registerService(ServletContext.class, getServletContext(), properties));
        } else {
            throw new IllegalStateException(
                "Cannot register ServletContext as OSGi service. BundleContext is not available. Possible reason is a bundle refresh. Current bundle state is "
                    + getStateAsString(this.bundle.getState()) + ".");
        }
    }

    String getContextPath() {
        return this.handle.getServletContext().getContextPath();
    }

    Bundle getBundle() {
        return this.bundle;
    }

    private Dictionary<String, String> constructServletContextProperties() {
        Dictionary<String, String> properties = new Hashtable<>();
        WebContainerUtils.setServletContextBundleProperties(properties, getBundle());
        properties.put("osgi.web.contextpath", getContextPath());
        return properties;
    }

    private String getStateAsString(int state) {
        String stateAsString;
        switch (state) {
            case Bundle.ACTIVE:
                stateAsString = BUNDLE_STATE_ACTIVE;
                break;
            case Bundle.INSTALLED:
                stateAsString = BUNDLE_STATE_INSTALLED;
                break;
            case Bundle.RESOLVED:
                stateAsString = BUNDLE_STATE_RESOLVED;
                break;
            case Bundle.STARTING:
                stateAsString = BUNDLE_STATE_STARTING;
                break;
            case Bundle.STOPPING:
                stateAsString = BUNDLE_STATE_STOPPING;
                break;
            case Bundle.UNINSTALLED:
                stateAsString = BUNDLE_STATE_UNINSTALLED;
                break;
            default:
                stateAsString = BUNDLE_STATE_UNKNOWN;
                break;
        }
        return stateAsString;
    }
}
