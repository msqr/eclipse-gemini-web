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

import java.util.Set;

/**
 * <p>
 * WebContainerProperties allows applications running on this RFC66 implementation to obtain the properties used to
 * configure it.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * thread-safe
 * 
 */
public interface WebContainerProperties {

    /**
     * The port that this webcontainer is listening on.
     * 
     * @return the port number
     */
    Set<ConnectorDescriptor> getConnectorDescriptors();

}
