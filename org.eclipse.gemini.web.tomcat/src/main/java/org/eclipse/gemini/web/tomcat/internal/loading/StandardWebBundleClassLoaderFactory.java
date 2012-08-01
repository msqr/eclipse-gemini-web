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

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.eclipse.gemini.web.tomcat.spi.ClassLoaderCustomizer;
import org.eclipse.gemini.web.tomcat.spi.WebBundleClassLoaderFactory;
import org.osgi.framework.Bundle;

/**
 * TODO Document StandardWebBundleClassLoaderFactory
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * TODO Document concurrent semantics of StandardWebBundleClassLoaderFactory
 * 
 */
public class StandardWebBundleClassLoaderFactory implements WebBundleClassLoaderFactory {

    private final ClassLoaderCustomizer classLoaderCustomizer;

    public StandardWebBundleClassLoaderFactory(ClassLoaderCustomizer classLoaderCustomizer) {
        this.classLoaderCustomizer = classLoaderCustomizer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader createWebBundleClassLoader(final Bundle webBundle) {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            @Override
            public ClassLoader run() {
                return new BundleWebappClassLoader(webBundle, StandardWebBundleClassLoaderFactory.this.classLoaderCustomizer);
            }
        });
    }
}
