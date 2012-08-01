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

package org.eclipse.gemini.web.core.spi;

import javax.servlet.ServletContext;

/**
 * Handle to a web application deployed in a {@link ServletContainer}. <code>ServletContainer</code> implementations
 * will create custom subclasses of this interface and return them from {@link ServletContainer#createWebApplication}.
 * The <code>ServletContainer</code> can store any state need during
 * {@link ServletContainer#startWebApplication(WebApplicationHandle) start} and
 * {@link ServletContainer#stopWebApplication(WebApplicationHandle) stop} in this custom implementation.
 * <p/>
 * Client code <strong>must</strong> return the correct handle to the <code>ServletContainer</code> when starting or
 * stopping an application.
 * 
 */
public interface WebApplicationHandle {

    /**
     * Gets the {@link ServletContext} of the deployed web application.
     * 
     * @return the <code>ServletContext</code>.
     */
    ServletContext getServletContext();

    /**
     * Gets the {@link ClassLoader} of the deployed web application. May be <code>null</code> if the web application has
     * not yet been {@link ServletContainer#startWebApplication(WebApplicationHandle) started}.
     * 
     * @return the <code>ClassLoader</code>.
     */
    ClassLoader getClassLoader();

}
