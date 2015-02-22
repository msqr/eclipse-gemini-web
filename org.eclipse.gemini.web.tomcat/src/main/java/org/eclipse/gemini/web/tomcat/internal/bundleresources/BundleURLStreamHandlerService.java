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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.catalina.webresources.WarURLStreamHandler;
import org.osgi.service.url.AbstractURLStreamHandlerService;

public class BundleURLStreamHandlerService extends AbstractURLStreamHandlerService {

    private static final String WAR_BUNDLE_ENTRY_SCHEMA = "war:bundle";

    private static final String WAR_TO_ENTRY_SEPARATOR = "\\^/";

    private final ExtendedWarURLStreamHandler handler = new ExtendedWarURLStreamHandler();

    @Override
    public URLConnection openConnection(URL u) throws IOException {
        return new URL(null, u.toExternalForm(), this.handler).openConnection();
    }

    private static class ExtendedWarURLStreamHandler extends WarURLStreamHandler {

        @Override
        protected void parseURL(URL u, String spec, int start, int limit) {
            // Only the path needs to be changed
            if (spec.startsWith(WAR_BUNDLE_ENTRY_SCHEMA)) {
                String path = spec.substring(4);
                path = path.replaceFirst(WAR_TO_ENTRY_SEPARATOR, "");
                setURL(u, u.getProtocol(), "", -1, null, null, path, null, null);
            } else {
                super.parseURL(u, spec, start, limit);
            }
        }

    }
}
