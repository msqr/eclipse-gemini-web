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

package org.eclipse.gemini.web.tomcat.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.gemini.web.core.ConnectorDescriptor;
import org.eclipse.gemini.web.tomcat.internal.TomcatWebContainerProperties;
import org.junit.Before;
import org.junit.Test;




/**
 */
public class TomcatWebContainerPropertiesTests {
    
    private static final String CATALINA_TYPE_PROTOCOL_HANDLER = "Catalina:type=ProtocolHandler,port=9999";
    
    private static final String ATTRIBUTE_MODELER_TYPE = "modelerType";
    
    private static final String ATTRIBUTE_SSL_ENABLED = "sSLEnabled";
    
    private static final String ATTRIBUTE_NAME = "name";
    
    /**
     * @throws Exception potentially
     */
    @Before
    public void setUp() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName(CATALINA_TYPE_PROTOCOL_HANDLER);
        mBeanServer.registerMBean(new DummyManagedConnector(), objectName);
        AttributeList attributeList = new AttributeList();
        attributeList.add(new Attribute(ATTRIBUTE_MODELER_TYPE, "foobar"));
        attributeList.add(new Attribute(ATTRIBUTE_SSL_ENABLED, true));
        attributeList.add(new Attribute(ATTRIBUTE_NAME, "http-something"));
        mBeanServer.setAttributes(objectName, attributeList);
    }

    /**
     * Test method for {@link org.eclipse.gemini.web.tomcat.internal.TomcatWebContainerProperties#getConnectorDescriptors()}.
     */
    @Test
    public void testGetConnectorDescriptors() {
        TomcatWebContainerProperties tomcatWebContainerProperties = new TomcatWebContainerProperties();
        Set<ConnectorDescriptor> connectorDescriptors = tomcatWebContainerProperties.getConnectorDescriptors();
        assertNotNull(connectorDescriptors);
        assertEquals(1, connectorDescriptors.size());
        ConnectorDescriptor connectorDescriptor = connectorDescriptors.toArray(new ConnectorDescriptor[1])[0];
        assertEquals("foobar", connectorDescriptor.getProtocol());
        assertEquals("http-something", connectorDescriptor.getScheme());
        assertEquals(9999, connectorDescriptor.getPort());
        assertEquals(true, connectorDescriptor.sslEnabled());
    }


}
