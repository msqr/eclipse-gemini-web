/*******************************************************************************
 * Copyright (c) 2014 SAP AG
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

package org.eclipse.gemini.web.tomcat.internal.loading;

import java.net.URL;
import java.util.Enumeration;

import org.eclipse.virgo.test.stubs.framework.FindEntriesDelegate;
import org.osgi.framework.Bundle;

public class FindEntriesDelegateImpl implements FindEntriesDelegate {

    private Bundle testBundle;

    public FindEntriesDelegateImpl(Bundle testBundle) {
        this.testBundle = testBundle;
    }

    @Override
    public Enumeration<?> findEntries(final String path, final String filePattern, boolean recurse) {
        return new Enumeration<URL>() {

            private boolean hasMore = true;

            @Override
            public boolean hasMoreElements() {
                return this.hasMore;
            }

            @Override
            public URL nextElement() {
                if (this.hasMore) {
                    this.hasMore = false;
                    return FindEntriesDelegateImpl.this.testBundle.getEntry(path + "/" + filePattern);
                }
                return null;
            }
        };
    }

}
