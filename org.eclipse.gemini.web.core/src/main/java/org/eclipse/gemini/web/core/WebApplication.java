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
import org.osgi.service.event.EventAdmin;

/**
 * Represents a web application managed by a {@link WebContainer}.
 * 
 * <p/>
 * 
 * Web applications are created from valid web bundles using {@link WebContainer#createWebApplication(Bundle)}.
 * 
 * 
 */
public interface WebApplication {

    /**
     * Gets the {@link ServletContext} associated with this web application.
     * 
     * @return the <code>ServletContext</code>, never <code>null</code>.
     */
    ServletContext getServletContext();

    /**
     * Gets the {@link ClassLoader} of this web application.
     * 
     * @return the web application's <code>ClassLoader</code>.
     */
    ClassLoader getClassLoader();

    /**
     * Starts this web application under the {@link ServletContext#getContextPath() configured context path}.
     * <p/>
     * If the application fails to start an {@link EventAdmin} event is emitted to the
     * <code>org/osgi/services/web/FAILED</code> topic and a {@link WebApplicationStartFailedException} is thrown.
     * 
     * @throws WebApplicationStartFailedException
     */
    void start() throws WebApplicationStartFailedException;

    /**
     * Stops this web application. After stop the web application is no longer available to serve content.
     */
    void stop();
}
