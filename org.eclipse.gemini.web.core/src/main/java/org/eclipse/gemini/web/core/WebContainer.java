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

package org.eclipse.gemini.web.core;

import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.event.EventAdmin;

/**
 * A <code>WebContainer</code> provides a mechanism for creating {@link WebApplication WebApplications} from
 * user-supplied web bundles.
 * <p/>
 * The <code>WebContainer</code> provides programmatic access to the web bundle deployment functionality that is
 * typically accessed via the extender.
 * 
 */
public interface WebContainer {

    /**
     * The {@link ServletContext} attribute under which the {@link BundleContext} is available.
     */
    public static final String ATTRIBUTE_BUNDLE_CONTEXT = "osgi-bundlecontext";

    static final String EVENT_NAME_PREFIX = "org/osgi/service/web/";

    /**
     * The {@link EventAdmin} topic for web bundle <code>DEPLOYING</code> events.
     */
    static final String EVENT_DEPLOYING = EVENT_NAME_PREFIX + "DEPLOYING";

    /**
     * The {@link EventAdmin} topic for web bundle <code>DEPLOYED</code> events.
     */
    static final String EVENT_DEPLOYED = EVENT_NAME_PREFIX + "DEPLOYED";

    /**
     * The {@link EventAdmin} topic for web bundle <code>UNDEPLOYING</code> events.
     */
    static final String EVENT_UNDEPLOYING = EVENT_NAME_PREFIX + "UNDEPLOYING";

    /**
     * The {@link EventAdmin} topic for web bundle <code>UNDEPLOYED</code> events.
     */
    static final String EVENT_UNDEPLOYED = EVENT_NAME_PREFIX + "UNDEPLOYED";

    /**
     * The {@link EventAdmin} topic for web bundle <code>FAILED</code> events.
     */
    static final String EVENT_FAILED = EVENT_NAME_PREFIX + "FAILED";

    /**
     * The {@link org.osgi.service.event.Event Event} property for the web application bundle's context path.
     */
    static final String EVENT_PROPERTY_CONTEXT_PATH = "context.path";

    /**
     * The {@link org.osgi.service.event.Event Event} property for the web application bundle's version.
     */
    static final String EVENT_PROPERTY_BUNDLE_VERSION = "bundle.version";

    /**
     * The {@link org.osgi.service.event.Event Event} property for the web container extender bundle.
     */
    static final String EVENT_PROPERTY_EXTENDER_BUNDLE = "extender.bundle";

    /**
     * The {@link org.osgi.service.event.Event Event} property for the web container extender bundle's id.
     */
    static final String EVENT_PROPERTY_EXTENDER_BUNDLE_ID = "extender.bundle.id";

    /**
     * The {@link org.osgi.service.event.Event Event} property for the web container extender bundle's symbolic name.
     */
    static final String EVENT_PROPERTY_EXTENDER_BUNDLE_SYMBOLICNAME = "extender.bundle.symbolicName";

    /**
     * The {@link org.osgi.service.event.Event Event} property for the web container extender bundle's version.
     */
    static final String EVENT_PROPERTY_EXTENDER_BUNDLE_VERSION = "extender.bundle.version";

    /**
     * The {@link org.osgi.service.event.Event Event} property containing a Web-ContextPath which is shared by more than one bundle.
     */
    static final String EVENT_PROPERTY_COLLISION = "collision";

    /**
     * The {@link org.osgi.service.event.Event Event} property listing, in the case of a collsion, the bundle ids, as a
     * <code>Collection&lt;Long&gt;</code>, which share the same Web-ContextPath.
     */
    static final String EVENT_PROPERTY_COLLISION_BUNDLES = "collision.bundles";

    /**
     * Creates a {@link WebApplication} for the supplied web bundle. Equivalent to calling
     * {@link #createWebApplication(Bundle, Bundle) createWebApplication(bundle, null)}.
     * 
     * @param bundle the web bundle
     * 
     * @return the newly created <code>WebApplication</code>.
     * @throws BundleException if the <code>WebApplication</code> cannot be created.
     */
    WebApplication createWebApplication(Bundle bundle) throws BundleException;

    /**
     * Creates a {@link WebApplication} for the supplied web bundle.
     * 
     * @param bundle the web bundle
     * @param extender the extender bundle that has trigger the creation of the web application, or <code>null</code> if
     *        an extender is not involved.
     * @return the newly created <code>WebApplication</code>.
     * @throws BundleException if the <code>WebApplication</code> cannot be created.
     */
    WebApplication createWebApplication(Bundle bundle, Bundle extender) throws BundleException;

    /**
     * Checks to see if the supplied {@link Bundle} is a valid web bundle.
     * 
     * @param bundle the bundle to check.
     * @return <code>true</code> if the supplied bundle is a valid web bundle; otherwise <code>false</code>.
     */
    boolean isWebBundle(Bundle bundle);
    
    /**
     * Stops the web container.
     */
    public void halt();
}
