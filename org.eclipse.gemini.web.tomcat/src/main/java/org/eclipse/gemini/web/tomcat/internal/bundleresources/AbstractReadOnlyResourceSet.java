/*******************************************************************************
 * Copyright (c) 2015 SAP SE
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

package org.eclipse.gemini.web.tomcat.internal.bundleresources;

import java.io.InputStream;

import org.apache.catalina.webresources.AbstractResourceSet;

abstract class AbstractReadOnlyResourceSet extends AbstractResourceSet {

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // no-op
    }

    @Override
    public boolean mkdir(String path) {
        checkPath(path);
        return false;
    }

    @Override
    public boolean write(String path, InputStream is, boolean overwrite) {
        checkPath(path);

        if (is == null) {
            throw new NullPointerException("The input stream may not be null.");
        }

        return false;
    }

}
