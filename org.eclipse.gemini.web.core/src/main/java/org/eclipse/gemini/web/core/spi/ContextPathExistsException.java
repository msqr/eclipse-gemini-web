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

/**
 * Exception signalling that a context path referred to by a web application is already in use by another web
 * application.
 * 
 */
public class ContextPathExistsException extends ServletContainerException {

    private static final long serialVersionUID = 1846326281773843365L;

    private final String contextPath;

    public ContextPathExistsException(String contextPath) {
        super("Context path '" + contextPath + "' already exists");
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;
    }
}
