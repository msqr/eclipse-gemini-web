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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.Constants;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.Registry;

abstract class BaseWebappLoader implements Loader, Lifecycle, PropertyChangeListener, MBeanRegistration {

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(Constants.Package);

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * The lifecycle event support for this Loader.
     */
    protected final LifecycleSupport lifecycle = new LifecycleSupport(this);

    /**
     * The property change support for this Loader.
     */
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * The "follow standard delegation model" flag that will be used to configure our ClassLoader.
     */
    private boolean delegate = false;

    /**
     * The Container with which this Loader has been associated.
     */
    private Container container = null;

    private boolean initialized = false;

    private ObjectName controller;

    private ObjectName objectName;

    /**
     * The reloadable flag for this Loader.
     */
    private boolean reloadable = false;
    

    /**
     * Ensures that the grandparent {@link Container} of the supplied {@link StandardContext context} is an
     * {@link Engine}.
     * 
     * @throws IllegalStateException if the grandparent is not an <code>Engine</code>
     */
    protected final void ensureGrandparentIsAnEngine(StandardContext standardContext) {
        if (!(standardContext.getParent().getParent() instanceof Engine)) {
            throw new IllegalStateException("Grandparent of [" + standardContext + "] is not an instanceof [" + Engine.class.getName() + "].");
        }
    }

    protected String getCatalinaContextPath(StandardContext context) {
        String contextPath = context.getPath();
        if (contextPath.equals("")) {
            contextPath = "/";
        }
        return contextPath;
    }

    protected void init() {
        if (this.initialized) {
            return;
        }

        this.initialized = true;

        if (this.objectName == null) {
            // not registered yet - stand-alone or API
            if (this.container instanceof StandardContext) {
                // Register ourself. The container must be a webapp
                try {
                    StandardContext ctx = (StandardContext) this.container;
                    ensureGrandparentIsAnEngine(ctx);
                    this.objectName = new ObjectName(ctx.getEngineName() + ":type=Loader,path=" + getCatalinaContextPath(ctx) + ",host="
                        + ctx.getParent().getName());
                    Registry.getRegistry(null, null).registerComponent(this, this.objectName, null);
                    this.controller = this.objectName;
                } catch (Exception e) {
                    log.error("Error registering loader", e);
                }
            }
        }
    }

    protected void destroy() {
        if (this.controller == this.objectName) {
            // Self-registration, undo it
            Registry.getRegistry(null, null).unregisterComponent(this.objectName);
            this.objectName = null;
        }
        this.initialized = false;
    }

    protected final ObjectName getObjectName() {
        return this.objectName;
    }

    // -------------------------------------------------------------------------
    // --- Loader
    // -------------------------------------------------------------------------

    /**
     * Execute a periodic task, such as reloading, etc. This method will be invoked inside the class loading context of
     * this container. Unexpected throwables will be caught and logged.
     */
    public void backgroundProcess() {
        if (this.reloadable && modified()) {
            try {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                if (getContainer() instanceof StandardContext) {
                    ((StandardContext) getContainer()).reload();
                }
            } finally {
                if (getContainer().getLoader() != null) {
                    Thread.currentThread().setContextClassLoader(getContainer().getLoader().getClassLoader());
                }
            }
        }
    }

    /**
     * Add a property change listener to this component.
     * 
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    /**
     * Set the Container with which this Logger has been associated.
     * 
     * @param container The associated Container
     */
    public void setContainer(Container container) {

        // unregister from the old Container (if any)
        if (this.container != null && this.container instanceof Context) {
            ((Context) this.container).removePropertyChangeListener(this);
        }

        // Process this property change
        Container oldContainer = this.container;
        this.container = container;
        this.support.firePropertyChange("container", oldContainer, this.container);

        // Register with the new Container (if any)
        if (this.container != null && this.container instanceof Context) {
            setReloadable(((Context) this.container).getReloadable());
            ((Context) this.container).addPropertyChangeListener(this);
        }
    }

    public final Container getContainer() {
        return this.container;
    }

    /**
     * {@inheritDoc}
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }

    /**
     * Return the reloadable flag for this Loader.
     */
    public boolean getReloadable() {
        return this.reloadable;
    }

    /**
     * Set the reloadable flag for this Loader.
     * 
     * @param reloadable The new reloadable flag
     */
    public void setReloadable(boolean reloadable) {
        // Process this property change
        boolean oldReloadable = this.reloadable;
        this.reloadable = reloadable;
        this.support.firePropertyChange("reloadable", Boolean.valueOf(oldReloadable), Boolean.valueOf(this.reloadable));
    }

    /**
     * Return the "follow standard delegation model" flag used to configure our ClassLoader.
     */
    public boolean getDelegate() {
        return this.delegate;
    }

    /**
     * Set the "follow standard delegation model" flag used to configure our ClassLoader.
     * 
     * @param delegate The new flag
     */
    public void setDelegate(boolean delegate) {
        boolean oldDelegate = this.delegate;
        this.delegate = delegate;
        this.support.firePropertyChange("delegate", Boolean.valueOf(oldDelegate), Boolean.valueOf(this.delegate));
    }

    // -------------------------------------------------------------------------
    // --- PropertyChangeListener
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void propertyChange(PropertyChangeEvent event) {

        // Validate the source of this event
        if (!(event.getSource() instanceof Context)) {
            return;
        }

        // Process a relevant property change
        if (event.getPropertyName().equals("reloadable")) {
            try {
                setReloadable(((Boolean) event.getNewValue()).booleanValue());
            } catch (Exception e) {
                log.error(sm.getString("webappLoader.reloadable", event.getNewValue().toString()));
            }
        }
    }
    
    // -------------------------------------------------------------------------
    // --- Lifecycle
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void addLifecycleListener(LifecycleListener listener) {
        this.lifecycle.addLifecycleListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    public LifecycleListener[] findLifecycleListeners() {
        return this.lifecycle.findLifecycleListeners();
    }

    /**
     * {@inheritDoc}
     */
    public void removeLifecycleListener(LifecycleListener listener) {
        this.lifecycle.removeLifecycleListener(listener);
    }

    // -------------------------------------------------------------------------
    // --- MBeanRegistration
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void postDeregister() {
        /* no-op */
    }

    /**
     * {@inheritDoc}
     */
    public void postRegister(Boolean registrationDone) {
        /* no-op */
    }

    /**
     * {@inheritDoc}
     */
    public void preDeregister() throws Exception {
        /* no-op */
    }

    /**
     * {@inheritDoc}
     */
    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
        this.objectName = objectName;
        return objectName;
    }

    // -------------------------------------------------------------------------
}
