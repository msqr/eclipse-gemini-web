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

/**
 * <p>
 * ConnectorDescriptor describes a configured connector to the web layer
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * ConnectorDescriptor is thread-safe
 * 
 */
public interface ConnectorDescriptor {

    /**
     * @return The protocol being used, eg. HTTP-1.1
     */
    String getProtocol();

    /**
     * @return The scheme of the connector, eg. http.
     */
    String getScheme();

    /**
     * @return The port the connector is listening on, eg. 8080
     */
    int getPort();

    /**
     * @return true iff ssl is enabled
     */
    boolean sslEnabled();

}
