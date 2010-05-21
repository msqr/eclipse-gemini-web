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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.gemini.web.tomcat.internal.TomcatMBeanManager;
import org.junit.Test;


public class TomcatMBeanManagerTests {
    
    @Test
    public void testStartAndStop() throws Exception {
        String domain = "foo";
        
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        
        ObjectName oname = new ObjectName(domain, "bar", "baz");
        server.registerMBean(new Foo(), oname);
        
        TomcatMBeanManager mgr = new TomcatMBeanManager(domain);
        
        assertTrue(server.isRegistered(oname));
        mgr.start();
        assertFalse(server.isRegistered(oname));
        
        server.registerMBean(new Foo(), oname);

        assertTrue(server.isRegistered(oname));
        mgr.stop();
        assertFalse(server.isRegistered(oname));
    }
    
    public static interface FooMBean {
        
    }
    
    public static final class Foo implements FooMBean {
        
    }
}
