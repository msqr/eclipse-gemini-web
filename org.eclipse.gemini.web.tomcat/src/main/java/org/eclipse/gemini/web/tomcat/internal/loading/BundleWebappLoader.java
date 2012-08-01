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

package org.eclipse.gemini.web.tomcat.internal.loading;

import java.beans.PropertyChangeListener;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletContext;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.mbeans.MBeanUtils;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.tomcat.util.modeler.Registry;
import org.eclipse.gemini.web.tomcat.spi.ClassLoaderCustomizer;
import org.osgi.framework.Bundle;

public class BundleWebappLoader extends BaseWebappLoader implements Loader, PropertyChangeListener {

    private static Log log = LogFactory.getLog(BundleWebappLoader.class);

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
    private ClassLoader classLoader = null;

    /**
     * The descriptive information about this Loader implementation.
     */
    private static final String INFO = BaseWebappLoader.class.getName() + "/1.0";

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
    private ClassLoader createClassLoader() {
        return new BundleWebappClassLoader(this.bundle, this.classLoaderCustomizer);
    }

    // -------------------------------------------------------------------------
    // --- Loader
    // -------------------------------------------------------------------------

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public void addRepository(String repository) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support addRepository(String)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] findRepositories() {
        return new String[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * Return descriptive information about this Loader implementation and the corresponding version number, in the
     * format <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    @Override
    public String getInfo() {
        return INFO;
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
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("webappLoader.starting"));
        }

        if (getContainer().getResources() == null) {
            log.info("No resources for " + getContainer());
            setState(LifecycleState.STARTING);
            return;
        }

        // Construct a class loader based on our current repositories list
        try {

            this.classLoader = createClassLoader();
            if (this.classLoader instanceof Lifecycle) {
                ((Lifecycle) this.classLoader).start();
            }

            DirContextURLStreamHandler.bind(this.classLoader, getContainer().getResources());

            registerClassLoaderMBean();

        } catch (Throwable t) {
            log.error("LifecycleException ", t);
            throw new LifecycleException("start: ", t);
        }

        setState(LifecycleState.STARTING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopInternal() throws LifecycleException {
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("webappLoader.stopping"));
        }

        setState(LifecycleState.STOPPING);

        // Remove context attributes as appropriate
        if (getContainer() instanceof Context) {
            ServletContext servletContext = ((Context) getContainer()).getServletContext();
            servletContext.removeAttribute(Globals.CLASS_PATH_ATTR);
        }

        // Throw away our current class loader
        if (this.classLoader instanceof Lifecycle) {
            ((Lifecycle) this.classLoader).stop();
        }

        DirContextURLStreamHandler.unbind(this.classLoader);

        unregisterClassLoaderMBean();

        this.classLoader = null;
        this.bundle = null;
        this.classLoaderCustomizer = null;
    }

    private void registerClassLoaderMBean() throws MalformedObjectNameException, Exception {
        StandardContext ctx = (StandardContext) getContainer();
        ObjectName classLoaderObjectName = createClassLoaderObjectName(ctx);
        Registry.getRegistry(null, null).registerComponent(this.classLoader, classLoaderObjectName, null);
    }

    private void unregisterClassLoaderMBean() {
        try {
            StandardContext ctx = (StandardContext) getContainer();
            ObjectName classLoaderObjectName = createClassLoaderObjectName(ctx);
            Registry.getRegistry(null, null).unregisterComponent(classLoaderObjectName);
        } catch (Throwable t) {
            log.error("LifecycleException ", t);
        }
    }

    private ObjectName createClassLoaderObjectName(StandardContext ctx) throws MalformedObjectNameException {
        return new ObjectName(MBeanUtils.getDomain(ctx) + ":type=OsgiWebappClassLoader,context=" + getCatalinaContextPath(ctx) + ",host="
            + ctx.getParent().getName());
    }

}
