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

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;

import org.eclipse.gemini.web.core.spi.ServletContainerException;
import org.eclipse.gemini.web.tomcat.internal.TomcatServletContainer;
import org.eclipse.gemini.web.tomcat.internal.TomcatServletContainerFactory;
import org.junit.Test;
import org.osgi.framework.BundleContext;



public class TomcatServletContainerFactoryTests {

    @Test
    public void testCreateContainerWithConfigFile() throws Exception {
        TomcatServletContainerFactory factory = new TomcatServletContainerFactory();
        TomcatServletContainer container = factory.createContainer(new FileInputStream("src/test/resources/server.xml"), createMock(BundleContext.class), null);
        assertNotNull(container);
    }
    
    @Test(expected=ServletContainerException.class)
    public void testCreateContainerWithInvalidConfigFile() throws Exception {
        TomcatServletContainerFactory factory = new TomcatServletContainerFactory();
        TomcatServletContainer container = factory.createContainer(new FileInputStream("src/test/resources/invalid-server.xml"), null, null);
        assertNotNull(container);
    }
}
