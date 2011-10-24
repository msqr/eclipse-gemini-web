/*******************************************************************************
 * Copyright (c) 2011 SAP AG
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
 *   Violeta Georgieva - initial contribution
 *******************************************************************************/

package org.eclipse.gemini.web.tomcat.naming.factory;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.StringRefAddr;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

import org.apache.naming.ResourceRef;
import org.junit.Before;
import org.junit.Test;

public class OsgiServiceFactoryTests {

    private static final String LOOKUP = "some string";

    private ResourceRef resourceRef;

    private InitialContextFactoryBuilder initialContextFactoryBuilder;

    private InitialContextFactory initialContextFactory;

    private Context context;

    @Before
    public void setUp() throws Exception {
        this.resourceRef = new ResourceRef(null, null, null, null, false);
        this.initialContextFactoryBuilder = createMock(InitialContextFactoryBuilder.class);
        this.initialContextFactory = createMock(InitialContextFactory.class);
        this.context = createMock(Context.class);

        NamingManager.setInitialContextFactoryBuilder(this.initialContextFactoryBuilder);
    }

    @Test
    public void testGetObjectInstance() throws Exception {
        expect(this.initialContextFactoryBuilder.createInitialContextFactory(new Hashtable<Object, Object>())).andReturn(this.initialContextFactory);
        expect(this.initialContextFactory.getInitialContext(new Hashtable<Object, Object>())).andReturn(this.context);
        expect(this.context.lookup(OsgiServiceFactory.OSGI_JNDI_URLSCHEME + LOOKUP)).andReturn(new Object());
        replay(this.initialContextFactoryBuilder, this.initialContextFactory, this.context);

        OsgiServiceFactory osgiServiceFactory = new OsgiServiceFactory();
        assertNull(osgiServiceFactory.getObjectInstance(new Object(), null, null, null));
        assertNull(osgiServiceFactory.getObjectInstance(this.resourceRef, null, null, null));
        this.resourceRef.add(new StringRefAddr(OsgiServiceFactory.MAPPED_NAME, null));
        assertNull(osgiServiceFactory.getObjectInstance(this.resourceRef, null, null, null));
        this.resourceRef.add(0, new StringRefAddr(OsgiServiceFactory.MAPPED_NAME, LOOKUP));
        assertNotNull(osgiServiceFactory.getObjectInstance(this.resourceRef, null, null, null));

        verify(this.initialContextFactoryBuilder, this.initialContextFactory, this.context);
    }
}
