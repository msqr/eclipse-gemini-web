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

package org.eclipse.gemini.web.tomcat.spi;

import java.lang.instrument.ClassFileTransformer;

import org.osgi.framework.Bundle;

public interface ClassLoaderCustomizer {

    /**
     * Allows extensions to customize the {@link ClassLoader} chain created for deployed web applications.
     * 
     * @param bundle the {@link Bundle} being deployed.
     * @return the extra <code>ClassLoaders</code> that be added to the end of the chain
     */
    ClassLoader[] extendClassLoaderChain(Bundle bundle);

    void addClassFileTransformer(ClassFileTransformer transformer, Bundle bundle);

    ClassLoader createThrowawayClassLoader(Bundle bundle);
}
