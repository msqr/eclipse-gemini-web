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

package org.eclipse.gemini.web.internal.url;

import static java.util.Collections.unmodifiableMap;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gemini.web.internal.WebContainerUtils;

/**
 * Encapsulates the state of a <code>war:</code> URL.
 */
public class WebBundleUrl {

    private static final String AMPERSAND = "&";

    private static final char EQUAL_SIGN = '=';

    private static final char QUESTION_MARK = '?';

    public static final String SCHEME = WebContainerUtils.WEB_BUNDLE_SCHEME;

    private static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";

    public final Object monitor = new Object();

    private final URL url;

    private final String location;

    private volatile Map<String, String> options;

    public WebBundleUrl(String location, Map<String, String> options) throws MalformedURLException {
        this.url = createURL(location, options);
        this.location = location;
        this.options = options == null ? Collections.<String, String> emptyMap() : unmodifiableMap(new HashMap<>(options));
    }

    public WebBundleUrl(URL url) {
        String protocol = url.getProtocol();
        if (!SCHEME.equals(protocol)) {
            throw new IllegalArgumentException("URL '" + url + "' is not a valid WAR URL");
        }
        this.url = url;
        this.location = url.getPath();
    }

    public final String getLocation() {
        return this.location;
    }

    /**
     * Gets the query options from the URL. Note that validation is deferred until this point as doing it earlier does
     * not produce the necessary BundleException when driven under
     * {@link org.osgi.framework.BundleContext#installBundle(String) installBundle(String)}. The query will be decoded
     * in this method.
     * 
     * @return the options in a String->String map
     */
    public final Map<String, String> getOptions() {
        if (this.options == null) {
            synchronized (this.monitor) {
                if (this.options == null) {
                    this.options = parseQueryString(this.url.getQuery());
                }
            }
        }
        return this.options;
    }

    private String toPathString(String location, Map<?, ?> options) {
        StringBuilder sb = new StringBuilder();
        sb.append(location);
        appendQueryStringIfNecessary(options, sb);
        return sb.toString();
    }

    @Override
    public final String toString() {
        return this.url.toString();
    }

    public final URL toURL() {
        return this.url;
    }

    /**
     * Hook method to use in unit testing. Allows for a specific {@link URLStreamHandler} to be added to {@link URL}
     * instances created internally.
     */
    protected URLStreamHandler createURLStreamHandler() {
        return null;
    }

    private URL createURL(String location, Map<?, ?> options) throws MalformedURLException {
        return new URL(SCHEME, null, -1, toPathString(location, options), createURLStreamHandler());
    }

    private static Map<String, String> parseQueryString(String query) {
        Map<String, String> options = new HashMap<>();
        if (query != null) {
            String[] parms = query.split(AMPERSAND);
            for (String parm : parms) {
                int equals = parm.indexOf(EQUAL_SIGN);
                if (equals == -1) {
                    throw new IllegalArgumentException("Missing '=' in URL parameter '" + parm + "'");
                }
                options.put(decode(parm.substring(0, equals)), decode(parm.substring(equals + 1)));
            }
        }
        return unmodifiableMap(options);
    }

    private static String decode(String parm) {
        try {
            return URLDecoder.decode(parm, DEFAULT_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Cannot decode '" + parm + "'.", e);
        }
    }

    private static void appendQueryStringIfNecessary(Map<?, ?> options, StringBuilder sb) {
        if (options != null && !options.isEmpty()) {
            sb.append(QUESTION_MARK);
            for (Map.Entry<?, ?> entry : options.entrySet()) {
                sb.append(entry.getKey()).append(EQUAL_SIGN).append(entry.getValue()).append(AMPERSAND);
            }
            sb.deleteCharAt(sb.length() - 1);
        }
    }
}
