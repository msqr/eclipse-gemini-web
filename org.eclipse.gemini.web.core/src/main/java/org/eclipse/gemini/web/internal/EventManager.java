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

package org.eclipse.gemini.web.internal;

import static org.eclipse.gemini.web.core.WebContainer.EVENT_DEPLOYED;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_DEPLOYING;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_FAILED;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_BUNDLE_VERSION;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_COLLISION;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_COLLISION_BUNDLES;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_CONTEXT_PATH;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_EXTENDER_BUNDLE;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_EXTENDER_BUNDLE_ID;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_EXTENDER_BUNDLE_SYMBOLICNAME;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_PROPERTY_EXTENDER_BUNDLE_VERSION;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_UNDEPLOYED;
import static org.eclipse.gemini.web.core.WebContainer.EVENT_UNDEPLOYING;

import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.gemini.web.internal.template.ServiceCallback;
import org.eclipse.gemini.web.internal.template.ServiceTemplate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;

/**
 * TODO: Need a better implementation of this - abstraction is poor :)
 * 
 */
final class EventManager {

    private final ServiceTemplate<EventAdmin> template;

    public EventManager(BundleContext context) {
        if (isEventAdminAvailable()) {
            this.template = new ServiceTemplate<EventAdmin>(context, EventAdmin.class);
        } else {
            this.template = null;
        }
    }

    public void start() {
        if (this.template != null) {
            this.template.start();
        }
    }

    public void stop() {
        if (this.template != null) {
            this.template.stop();
        }
    }

    public void sendDeploying(Bundle applicationBundle, Bundle extenderBundle, String contextPath) {
        sendEvent(EVENT_DEPLOYING, applicationBundle, extenderBundle, contextPath, null, null, null);
    }

    public void sendDeployed(Bundle applicationBundle, Bundle extenderBundle, String contextPath) {
        sendEvent(EVENT_DEPLOYED, applicationBundle, extenderBundle, contextPath, null, null, null);
    }

    public void sendUndeploying(Bundle applicationBundle, Bundle extenderBundle, String contextPath) {
        sendEvent(EVENT_UNDEPLOYING, applicationBundle, extenderBundle, contextPath, null, null, null);
    }

    public void sendUndeployed(Bundle applicationBundle, Bundle extenderBundle, String contextPath) {
        sendEvent(EVENT_UNDEPLOYED, applicationBundle, extenderBundle, contextPath, null, null, null);
    }

    public void sendFailed(Bundle applicationBundle, Bundle extenderBundle, String contextPath, Exception ex, String collidingWebContextPath,
        Set<Long> collisionBundles) {
        sendEvent(EVENT_FAILED, applicationBundle, extenderBundle, contextPath, ex, collidingWebContextPath, collisionBundles);
    }

    private void sendEvent(final String eventName, final Bundle applicationBundle, final Bundle extenderBundle, final String contextPath,
        final Throwable ex, final String collidingWebContextPath, final Set<Long> collisionBundles) {
        if (this.template != null) {
            this.template.executeWithService(new ServiceCallback<EventAdmin, Void>() {

                @Override
                public Void doWithService(EventAdmin eventAdmin) {
                    Dictionary<String, Object> props = new Hashtable<String, Object>();
                    if (applicationBundle.getSymbolicName() != null) {
                        props.put(EventConstants.BUNDLE_SYMBOLICNAME, applicationBundle.getSymbolicName());
                    }
                    props.put(EventConstants.BUNDLE_ID, applicationBundle.getBundleId());
                    props.put(EventConstants.BUNDLE, applicationBundle);
                    props.put(EVENT_PROPERTY_BUNDLE_VERSION, applicationBundle.getVersion());
                    props.put(EventConstants.TIMESTAMP, System.currentTimeMillis());
                    props.put(EVENT_PROPERTY_CONTEXT_PATH, contextPath);

                    if (extenderBundle != null) {
                        props.put(EVENT_PROPERTY_EXTENDER_BUNDLE, extenderBundle);
                        props.put(EVENT_PROPERTY_EXTENDER_BUNDLE_ID, extenderBundle.getBundleId());
                        if (extenderBundle.getSymbolicName() != null) {
                            props.put(EVENT_PROPERTY_EXTENDER_BUNDLE_SYMBOLICNAME, extenderBundle.getSymbolicName());
                        }
                        props.put(EVENT_PROPERTY_EXTENDER_BUNDLE_VERSION, extenderBundle.getVersion());
                    }

                    if (ex != null) {
                        props.put(EventConstants.EXCEPTION, ex);
                    }

                    if (collidingWebContextPath != null) {
                        props.put(EVENT_PROPERTY_COLLISION, collidingWebContextPath);

                        /*
                         * Prevent event handlers modifying the set of collision bundles.
                         * 
                         * Note: OSGi specs prefer Collection to Set even when there cannot be duplicates.
                         */
                        Collection<Long> immutableCollisionBundles = Collections.unmodifiableCollection(collisionBundles);
                        props.put(EVENT_PROPERTY_COLLISION_BUNDLES, immutableCollisionBundles);
                    }

                    eventAdmin.sendEvent(new Event(eventName, props));
                    return null;
                }

            });
        }
    }

    private boolean isEventAdminAvailable() {
        try {
            getClass().getClassLoader().loadClass(EventAdmin.class.getName());
            return true;
        } catch (NoClassDefFoundError ex) {
            return false;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
