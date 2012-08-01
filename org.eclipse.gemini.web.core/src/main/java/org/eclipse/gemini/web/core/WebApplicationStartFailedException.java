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

/**
 * Thrown to signal that a {@link WebApplication} has failed to {@link WebApplication#start()}.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public final class WebApplicationStartFailedException extends RuntimeException {

    private static final long serialVersionUID = -1722479683094175136L;

    /**
     * Creates a new WebApplicationStartFailedException with the supplied cause
     * 
     * @param cause The cause of the failure
     */
    public WebApplicationStartFailedException(Throwable cause) {
        super(cause);
    }

}
