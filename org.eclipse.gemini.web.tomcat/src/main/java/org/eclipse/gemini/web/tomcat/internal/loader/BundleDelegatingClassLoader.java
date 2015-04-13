/******************************************************************************
 * Copyright (c) 2006, 2015 VMware Inc., Oracle Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *   VMware Inc.
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.web.tomcat.internal.loader;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;

import org.eclipse.virgo.util.common.Assert;
import org.osgi.framework.Bundle;

/**
 * ClassLoader backed by an OSGi bundle. Provides the ability to use a separate class loader as fall back.
 *
 * This class is based on the one provided by Eclipse Gemini Blueprint.
 *
 * @author Adrian Colyer
 * @author Andy Piper
 * @author Costin Leau
 * @author Violeta Georgieva
 */
class BundleDelegatingClassLoader extends ClassLoader {

    private final ClassLoader bridge;

    private final Bundle backingBundle;

    /**
     * Factory method for creating a class loader over the given bundle.
     *
     * @param aBundle bundle to use for class loading and resource acquisition
     * @return class loader adapter over the given bundle
     */
    static BundleDelegatingClassLoader createBundleClassLoaderFor(Bundle aBundle) {
        return createBundleClassLoaderFor(aBundle, null);
    }

    /**
     * Factory method for creating a class loader over the given bundle and with a given class loader as fall-back. In
     * case the bundle cannot find a class or locate a resource, the given class loader will be used as fall back.
     *
     * @param bundle bundle used for class loading and resource acquisition
     * @param bridge class loader used as fall back in case the bundle cannot load a class or find a resource. Can be
     *        <code>null</code>
     * @return class loader adapter over the given bundle and class loader
     */
    static BundleDelegatingClassLoader createBundleClassLoaderFor(final Bundle bundle, final ClassLoader bridge) {
        return AccessController.doPrivileged(new PrivilegedAction<BundleDelegatingClassLoader>() {

            @Override
            public BundleDelegatingClassLoader run() {
                return new BundleDelegatingClassLoader(bundle, bridge);
            }
        });
    }

    /**
     * Private constructor.
     *
     * Constructs a new <code>BundleDelegatingClassLoader</code> instance.
     *
     * @param bundle
     * @param bridgeLoader
     */
    private BundleDelegatingClassLoader(Bundle bundle, ClassLoader bridgeLoader) {
        super(null);
        Assert.notNull(bundle, "bundle should be non-null");
        this.backingBundle = bundle;
        this.bridge = bridgeLoader;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            clazz = this.backingBundle.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            if (this.bridge != null) {
                try {
                    clazz = this.bridge.loadClass(name);
                } catch (ClassNotFoundException e) {
                    throw new ClassNotFoundException(name + " not found from bundle [" + this.bridge + "]", cnfe);
                } catch (NoClassDefFoundError e) {
                    NoClassDefFoundError ex = new NoClassDefFoundError(name + " not found from bundle [" + this.bridge + "]");
                    ex.initCause(e);
                    throw ex;
                }
            } else {
                throw new ClassNotFoundException(name + " not found from bundle [" + this.backingBundle + "]", cnfe);
            }
        } catch (NoClassDefFoundError ncdfe) {
            NoClassDefFoundError e = new NoClassDefFoundError(name + " not found from bundle [" + this.backingBundle + "]");
            e.initCause(ncdfe);
            throw e;
        }
        return clazz;
    }

    @Override
    protected URL findResource(String name) {
        URL resource = this.backingBundle.getResource(name);
        if (this.bridge != null && resource == null) {
            resource = this.bridge.getResource(name);
        }
        return resource;
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> resources = this.backingBundle.getResources(name);
        if (this.bridge != null && resources == null) {
            resources = this.bridge.getResources(name);
        }
        return resources;
    }

    @Override
    public URL getResource(String name) {
        return findResource(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = findClass(name);
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    @Override
    public String toString() {
        return "BundleDelegatingClassLoader for [" + this.backingBundle + "]";
    }

    /**
     * Returns the bundle to which this class loader delegates calls to.
     *
     * @return the backing bundle
     */
    public Bundle getBundle() {
        return this.backingBundle;
    }
}