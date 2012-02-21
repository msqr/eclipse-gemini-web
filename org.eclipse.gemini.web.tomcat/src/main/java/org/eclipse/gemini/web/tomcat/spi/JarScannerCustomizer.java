/*******************************************************************************
 * Copyright (c) 2012 SAP AG
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

package org.eclipse.gemini.web.tomcat.spi;

import org.apache.tomcat.JarScanner;
import org.osgi.framework.Bundle;

public interface JarScannerCustomizer {

    /**
     * Allows extensions to customize the {@link JarScanner} chain created for deployed web applications.
     * 
     * @param bundle the {@link Bundle} being deployed.
     * @return the extra <code>JarScanners</code> that be added to the end of the chain
     */
    JarScanner[] extendJarScannerChain(Bundle bundle);
}
