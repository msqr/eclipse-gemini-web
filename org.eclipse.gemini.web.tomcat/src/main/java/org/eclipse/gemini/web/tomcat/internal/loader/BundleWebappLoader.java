/*******************************************************************************
 * Copyright (c) 2009, 2015 VMware Inc.
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

package org.eclipse.gemini.web.tomcat.internal.loader;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletContext;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.modeler.Registry;
import org.eclipse.gemini.web.tomcat.spi.ClassLoaderCustomizer;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleWebappLoader extends BaseWebappLoader {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The OSGi {@link Bundle bundle} which will back the {@link ClassLoader} we will create.
     */
    private volatile Bundle bundle;

    private volatile ClassLoaderCustomizer classLoaderCustomizer;

    /**
     * The time stamp which is used to track when the bundle's {@link Bundle#getLastModified() last modified} time stamp
     * was last checked.
     */
    private long bundleModificationCheckTimestamp;

    private final Object bundleModificationLock = new Object();

    /**
     * The class loader being managed by this Loader.
     */
    private BundleWebappClassLoader classLoader = null;

    // -------------------------------------------------------------------------
    // --- Constructors
    // -------------------------------------------------------------------------

    public BundleWebappLoader(Bundle bundle, ClassLoaderCustomizer classLoaderCustomizer) {
        this.bundle = bundle;
        this.bundleModificationCheckTimestamp = this.bundle.getLastModified();
        this.classLoaderCustomizer = classLoaderCustomizer;
    }

    // -------------------------------------------------------------------------
    // --- OsgiWebappLoader-specific implementation
    // -------------------------------------------------------------------------

    /**
     * @return
     * @see #getClassLoaderName()
     */
    private BundleWebappClassLoader createClassLoader() {
        return new BundleWebappClassLoader(this.bundle, this.classLoaderCustomizer);
    }

    // -------------------------------------------------------------------------
    // --- Loader
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean modified() {

        synchronized (this.bundleModificationLock) {
            // If the current last modification time stamp is newer than the time stamp from the last time we checked,
            // there's been change!
            final long lastModified = this.bundle.getLastModified();
            if (lastModified > this.bundleModificationCheckTimestamp) {
                this.bundleModificationCheckTimestamp = lastModified;
                return true;
            }
        }

        return false;
    }

    // -------------------------------------------------------------------------
    // --- LifecycleBase
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void startInternal() throws LifecycleException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("Starting this loader.");
        }

        if (getContext().getResources() == null) {
            this.log.info("No resources for [" + getContext() + "].");
            setState(LifecycleState.STARTING);
            return;
        }

        // Construct a class loader based on our current repositories list
        try {

            this.classLoader = createClassLoader();
            this.classLoader.start();

            registerClassLoaderMBean();

        } catch (Throwable t) {
            t = ExceptionUtils.unwrapInvocationTargetException(t);
            ExceptionUtils.handleThrowable(t);
            this.log.error("LifecycleException ", t);
            throw new LifecycleException("start: ", t);
        }

        setState(LifecycleState.STARTING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopInternal() throws LifecycleException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("Stopping this loader.");
        }

        setState(LifecycleState.STOPPING);

        // Remove context attributes as appropriate
        ServletContext servletContext = getContext().getServletContext();
        servletContext.removeAttribute(Globals.CLASS_PATH_ATTR);

        // Throw away our current class loader
        try {
            this.classLoader.stop();
        } finally {
            this.classLoader.destroy();
        }

        unregisterClassLoaderMBean();

        this.classLoader = null;
        this.bundle = null;
        this.classLoaderCustomizer = null;
    }

    private void registerClassLoaderMBean() throws MalformedObjectNameException, Exception {
        ObjectName classLoaderObjectName = createClassLoaderObjectName(getContext());
        Registry.getRegistry(null, null).registerComponent(this.classLoader, classLoaderObjectName, null);
    }

    private void unregisterClassLoaderMBean() {
        try {
            ObjectName classLoaderObjectName = createClassLoaderObjectName(getContext());
            Registry.getRegistry(null, null).unregisterComponent(classLoaderObjectName);
        } catch (Throwable t) {
            this.log.error("LifecycleException ", t);
        }
    }

    private ObjectName createClassLoaderObjectName(Context ctx) throws MalformedObjectNameException {
        return new ObjectName(ctx.getDomain() + ":type=" + this.classLoader.getClass().getSimpleName() + ",host=" + ctx.getParent().getName()
            + ",context=" + getCatalinaContextPath(ctx));
    }

}
