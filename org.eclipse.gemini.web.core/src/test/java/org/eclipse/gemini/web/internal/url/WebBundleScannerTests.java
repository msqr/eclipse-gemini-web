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

package org.eclipse.gemini.web.internal.url;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;

import org.easymock.EasyMock;
import org.junit.Test;

import org.eclipse.gemini.web.internal.url.WebBundleScanner;
import org.eclipse.gemini.web.internal.url.WebBundleScannerCallback;
import org.eclipse.virgo.util.io.JarUtils;
import org.eclipse.virgo.util.io.PathReference;

public class WebBundleScannerTests {

	private static final File WAR_FILE = new File("target/resources/simple-war.war");

    @Test
    public void testScanLib() throws IOException {
        WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);
        
        setExpectations(callback);
                
        replay(callback);
        
        WebBundleScanner scanner = new WebBundleScanner(WAR_FILE.toURI().toURL(), callback);
        scanner.scanWar();
        
        verify(callback);        
    }
    
    private void setExpectations(WebBundleScannerCallback callback) {
        callback.jarFound("WEB-INF/lib/com.springsource.slf4j.api-1.6.1.jar");        
        callback.classFound("foo/bar/Doo.class");
    }
    
    private void setExpectationsIncludingNestedJars(WebBundleScannerCallback callback) {
        setExpectations(callback);

        callback.classFound("org/slf4j/ILoggerFactory.class");
        callback.classFound("org/slf4j/IMarkerFactory.class");
        callback.classFound("org/slf4j/Logger.class");
        callback.classFound("org/slf4j/LoggerFactory.class");
        callback.classFound("org/slf4j/Marker.class");
        callback.classFound("org/slf4j/MarkerFactory.class");
        callback.classFound("org/slf4j/MDC.class");
        callback.classFound("org/slf4j/helpers/BasicMarker.class");
        callback.classFound("org/slf4j/helpers/BasicMarkerFactory.class");
        callback.classFound("org/slf4j/helpers/BasicMDCAdapter.class");
        callback.classFound("org/slf4j/helpers/FormattingTuple.class");
        callback.classFound("org/slf4j/helpers/MarkerIgnoringBase.class");
        callback.classFound("org/slf4j/helpers/MessageFormatter.class");
        callback.classFound("org/slf4j/helpers/NamedLoggerBase.class");
        callback.classFound("org/slf4j/helpers/NOPLogger.class");
        callback.classFound("org/slf4j/helpers/NOPLoggerFactory.class");
        callback.classFound("org/slf4j/helpers/NOPMDCAdapter.class");
        callback.classFound("org/slf4j/helpers/SubstituteLoggerFactory.class");
        callback.classFound("org/slf4j/helpers/Util.class");
        callback.classFound("org/slf4j/spi/LocationAwareLogger.class");
        callback.classFound("org/slf4j/spi/LoggerFactoryBinder.class");
        callback.classFound("org/slf4j/spi/MarkerFactoryBinder.class");
        callback.classFound("org/slf4j/spi/MDCAdapter.class");
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testScanLibIncludingNestedJars() throws IOException {
        
        WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);
        
        setExpectationsIncludingNestedJars(callback);
        
        replay(callback);
        
        WebBundleScanner scanner = new WebBundleScanner(WAR_FILE.toURL(), callback, true);
        scanner.scanWar();
        
        verify(callback);        
    }
    
    @Test
    public void testScanDir() throws Exception {
        PathReference pr = unpackToDir();
        try {
            WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);
            
            setExpectations(callback);
            
            replay(callback);
            
            WebBundleScanner scanner = new WebBundleScanner(pr.toURI().toURL(), callback);            
            scanner.scanWar();
            
            verify(callback);
        } finally {
            pr.delete(true);
        }
    }
    
    @Test
    public void testScanDirIncludingNestedJars() throws Exception {
        PathReference pr = unpackToDir();
        try {
            WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);
            
            setExpectationsIncludingNestedJars(callback);
            
            replay(callback);
            
            WebBundleScanner scanner = new WebBundleScanner(pr.toURI().toURL(), callback, true);            
            scanner.scanWar();
            
            verify(callback);
        } finally {
            pr.delete(true);
        }
    }
    
    private PathReference unpackToDir() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        PathReference dest = new PathReference(new File(tmpDir, "unpack-" + System.currentTimeMillis()));
        PathReference src = new PathReference(WAR_FILE);
        JarUtils.unpackTo(src, dest);
        return dest;
    }
}
