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

package org.eclipse.gemini.web.tomcat.internal;

import org.eclipse.gemini.web.core.ConnectorDescriptor;

/**
 * <p>
 * TomcatConnectorDescriptor is the Tomcat specific implementation of {@link ConnectorDescriptor}.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * StandardConnectorDescriptor is threadsafe
 * 
 */
public class TomcatConnectorDescriptor implements ConnectorDescriptor {

    private final int port;

    private final String scheme;

    private final String protocol;

    private final boolean sslEnabled;

    TomcatConnectorDescriptor(String protocol, String scheme, int port, boolean sslEnabled) {
        this.protocol = protocol;
        this.scheme = scheme;
        this.port = port;
        this.sslEnabled = sslEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPort() {
        return this.port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProtocol() {
        return this.protocol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScheme() {
        return this.scheme;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sslEnabled() {
        return this.sslEnabled;
    }

}
