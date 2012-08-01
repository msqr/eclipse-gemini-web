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

package org.eclipse.gemini.web.core.spi;

public class ServletContainerException extends RuntimeException {

    private static final long serialVersionUID = -4955082179218908312L;

    public ServletContainerException() {
    }

    public ServletContainerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServletContainerException(String message) {
        super(message);
    }

    public ServletContainerException(Throwable cause) {
        super(cause);
    }

}
