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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class TomcatConfigLocatorTests {

    @Test
    public void testStartAndStop() throws Exception {
        startAndStop(null);
    }

    @Test
    public void testStartAndStopWithUserConfig() throws Exception {
        URL url = new URL("file:src/test/resources/server.xml");
        Vector<URL> v = new Vector<>();
        v.add(url);

        startAndStop(v.elements());
    }

    @Test(expected = IllegalStateException.class)
    public void testStartWithNoConfig() throws Exception {
        Bundle mockBundle = createMock(Bundle.class);
        expect(mockBundle.findEntries(TomcatConfigLocator.CONFIG_PATH, TomcatConfigLocator.USER_CONFIG_PATH, false)).andReturn(null);
        expect(mockBundle.getEntry(TomcatConfigLocator.DEFAULT_CONFIG_PATH)).andReturn(null);

        BundleContext mockContext = createNiceMock(BundleContext.class);
        expect(mockContext.getBundle()).andReturn(mockBundle).anyTimes();

        replay(mockBundle, mockContext);

        resolveConfigFile(mockContext);
    }

    @Test
    public void testResolveConfigDir() throws Exception {
        URL existingUrl = new URL("file:src/test/resources/server.xml");
        URL nonexistingUrl = new URL("file:src/test/resources/server1.xml");

        BundleContext mockContext = createMock(BundleContext.class);
        expect(mockContext.getProperty(TomcatConfigLocator.CONFIG_PATH_FRAMEWORK_PROPERTY)).andReturn(
            new File(existingUrl.getPath()).getAbsolutePath()).andReturn(new File(nonexistingUrl.getPath()).getAbsolutePath()).andReturn(null);

        replay(mockContext);

        String result = "src" + File.separator + "test" + File.separator + "resources";

        assertTrue(TomcatConfigLocator.resolveConfigDir(mockContext).toAbsolutePath().endsWith(result));
        assertEquals(null, TomcatConfigLocator.resolveConfigDir(mockContext));
        assertEquals(null, TomcatConfigLocator.resolveConfigDir(mockContext));

        verify(mockContext);
    }

    private BundleContext createMockBundleContext(Bundle mockBundle) {
        BundleContext mockContext = createNiceMock(BundleContext.class);
        expect(mockContext.getBundle()).andReturn(mockBundle);
        return mockContext;
    }

    private void resolveConfigFile(BundleContext mockContext) throws BundleException {
        InputStream is = null;
        try {
            is = TomcatConfigLocator.resolveConfigFile(mockContext);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void startAndStop(Enumeration<URL> userConfigPath) throws MalformedURLException, BundleException {
        Bundle mockBundle = createMock(Bundle.class);
        expect(mockBundle.findEntries(TomcatConfigLocator.CONFIG_PATH, TomcatConfigLocator.USER_CONFIG_PATH, false)).andReturn(userConfigPath);
        if (userConfigPath == null) {
            expect(mockBundle.getEntry(TomcatConfigLocator.DEFAULT_CONFIG_PATH)).andReturn(new URL("file:src/test/resources/server.xml"));
        }

        BundleContext mockContext = createMockBundleContext(mockBundle);

        replay(mockBundle, mockContext);

        resolveConfigFile(mockContext);

        verify(mockBundle, mockContext);
    }

}
