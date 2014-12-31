/*******************************************************************************
 * Copyright (c) 2009, 2014 VMware Inc.
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

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.gemini.web.core.ConnectorDescriptor;
import org.eclipse.gemini.web.core.WebContainerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * TODO Document TomcatWebContainerProperties
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * TODO Document concurrent semantics of TomcatWebContainerProperties
 * 
 */
final class TomcatWebContainerProperties implements WebContainerProperties {

    private static final String CATALINA_TYPE_PROTOCOL_HANDLER = "Catalina:type=ProtocolHandler,*";

    private final static Logger LOGGER = LoggerFactory.getLogger(TomcatWebContainerProperties.class);

    private static final String ATTRIBUTE_MODELER_TYPE = "modelerType";

    private static final String ATTRIBUTE_SSL_ENABLED = "sSLEnabled";

    private static final String ATTRIBUTE_NAME = "name";

    private static final String ATTRIBUTE_PORT = "port";

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ConnectorDescriptor> getConnectorDescriptors() {
        Set<ConnectorDescriptor> connectorDescriptors = new HashSet<>();
        MBeanServer mBeanServer = this.getMBeanServer();
        try {
            ObjectName portNamesQuery = new ObjectName(CATALINA_TYPE_PROTOCOL_HANDLER);
            Set<ObjectName> portMBeanNames = mBeanServer.queryNames(portNamesQuery, null);
            for (ObjectName objectName : portMBeanNames) {

                Object attribute = this.getAttribute(mBeanServer, objectName, ATTRIBUTE_MODELER_TYPE);
                Object modler = attribute == null ? "" : attribute;

                attribute = this.getAttribute(mBeanServer, objectName, ATTRIBUTE_SSL_ENABLED);
                Object sslEnabled = attribute == null ? false : attribute;

                attribute = this.getAttribute(mBeanServer, objectName, ATTRIBUTE_NAME);
                Object name = attribute == null ? "" : attribute;

                attribute = objectName.getKeyProperty(ATTRIBUTE_PORT);
                Object port = attribute == null ? -1 : attribute;

                connectorDescriptors.add(new TomcatConnectorDescriptor(modler.toString(), name.toString(), Integer.valueOf(port.toString()),
                    Boolean.valueOf(sslEnabled.toString())));
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to obtain the Tomcat port number from its MBeans", e);
        }
        return connectorDescriptors;
    }

    private MBeanServer getMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }

    private Object getAttribute(MBeanServer mBeanServer, ObjectName objectName, String attributeName) {
        try {
            return mBeanServer.getAttribute(objectName, attributeName);
        } catch (Exception e) {
            return null;
        }
    }

}
