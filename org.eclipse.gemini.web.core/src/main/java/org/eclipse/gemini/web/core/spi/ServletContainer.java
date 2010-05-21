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

import org.osgi.framework.Bundle;

public interface ServletContainer {

    /**
     * Creates a web application for the supplied {@link Bundle}.
     * 
     * @param contextPath the context path the web application should run under.
     * @param bundle the <code>Bundle</code> containing the web application content.
     * @return a handle to the web application that can be used to drive lifecycle events.
     * @throws ServletContainerException if the web application cannot be created.
     */
    WebApplicationHandle createWebApplication(String contextPath, Bundle bundle);

    /**
     * Starts the web application referred to by the supplied {@link WebApplicationHandle}.
     * 
     * @param handle the handle to the web application to start.
     * @throws ContextPathExistsException if the context path is already in use.
     * @throws ServletContainerException if the web application fails to start.
     */
    void startWebApplication(WebApplicationHandle handle);

    /**
     * Stops the web application referred to by the supplied {@link WebApplicationHandle}.
     * 
     * @param handle the handle to the web application to stop.
     * @throws ServletContainerException if the web application fails to stop.
     */
    void stopWebApplication(WebApplicationHandle handle);

}
