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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.catalina.Context;
import org.apache.catalina.Loader;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BaseWebappLoader extends LifecycleMBeanBase implements Loader, PropertyChangeListener {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The property change support for this Loader.
     */
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * The "follow standard delegation model" flag that will be used to configure our ClassLoader.
     */
    private boolean delegate = false;

    /**
     * The Context with which this Loader has been associated.
     */
    private Context context = null;

    /**
     * The reloadable flag for this Loader.
     */
    private boolean reloadable = false;

    protected String getCatalinaContextPath(Context context) {
        String contextName = context.getName();
        if (!contextName.startsWith("/")) {
            contextName = "/";
        }
        return contextName;
    }

    // -------------------------------------------------------------------------
    // --- LifecycleMBeanBase
    // -------------------------------------------------------------------------

    @Override
    protected String getObjectNameKeyProperties() {
        StringBuilder name = new StringBuilder("type=Loader");

        name.append(",host=");
        name.append(this.context.getParent().getName());

        name.append(",context=");
        name.append(getCatalinaContextPath(this.context));

        return name.toString();
    }

    @Override
    protected String getDomainInternal() {
        return this.context.getDomain();
    }

    // -------------------------------------------------------------------------
    // --- Loader
    // -------------------------------------------------------------------------

    /**
     * Execute a periodic task, such as reloading, etc. This method will be invoked inside the class loading context of
     * this container. Unexpected throwables will be caught and logged.
     */
    @Override
    public void backgroundProcess() {
        if (this.reloadable && modified()) {
            try {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                if (this.context != null) {
                    this.context.reload();
                }
            } finally {
                if (this.context != null && this.context.getLoader() != null) {
                    Thread.currentThread().setContextClassLoader(this.context.getLoader().getClassLoader());
                }
            }
        }
    }

    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    /**
     * Set the Context with which this Logger has been associated.
     *
     * @param context The associated Context
     */
    @Override
    public void setContext(Context context) {
        if (this.context == context) {
            return;
        }

        if (getState().isAvailable()) {
            throw new IllegalStateException("Setting the Context is not permitted while the loader is started.");
        }

        // Deregister from the old Context (if any)
        if (this.context != null) {
            this.context.removePropertyChangeListener(this);
        }

        // Process this property change
        Context oldContext = this.context;
        this.context = context;
        this.support.firePropertyChange("context", oldContext, this.context);

        // Register with the new Container (if any)
        if (this.context != null) {
            setReloadable(this.context.getReloadable());
            this.context.addPropertyChangeListener(this);
        }
    }

    @Override
    public final Context getContext() {
        return this.context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }

    /**
     * Return the reloadable flag for this Loader.
     */
    @Override
    public boolean getReloadable() {
        return this.reloadable;
    }

    /**
     * Set the reloadable flag for this Loader.
     *
     * @param reloadable The new reloadable flag
     */
    @Override
    public void setReloadable(boolean reloadable) {
        // Process this property change
        boolean oldReloadable = this.reloadable;
        this.reloadable = reloadable;
        this.support.firePropertyChange("reloadable", Boolean.valueOf(oldReloadable), Boolean.valueOf(this.reloadable));
    }

    /**
     * Return the "follow standard delegation model" flag used to configure our ClassLoader.
     */
    @Override
    public boolean getDelegate() {
        return this.delegate;
    }

    /**
     * Set the "follow standard delegation model" flag used to configure our ClassLoader.
     *
     * @param delegate The new flag
     */
    @Override
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
    @Override
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
                this.log.error("Cannot set reloadable property to [" + event.getNewValue().toString() + "].");
            }
        }
    }
}
