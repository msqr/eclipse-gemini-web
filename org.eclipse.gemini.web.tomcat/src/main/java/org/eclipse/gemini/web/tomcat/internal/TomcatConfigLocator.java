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

package org.eclipse.gemini.web.tomcat.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for loading the master Tomcat configuration.
 * <p/>
 * The location algorithm is as follows:
 * <ol>
 * <li>Check for <code>config/tomcat-server.xml</code> in the current working directory, use if found</code></li>
 * <li>Check this bundle and attached fragments for <code>/META-INF/tomcat/server.xml, use if found</code></li>
 * <li>Check this bundle for <code>/META-INF/tomcat/default-server.xml, use if found</code></li>
 * <li>Throw {@link IllegalStateException} if no configuration is found</li>
 * </ol>
 *
 *
 */
final class TomcatConfigLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatConfigLocator.class);

    static final String CONFIG_PATH_FRAMEWORK_PROPERTY = "org.eclipse.gemini.web.tomcat.config.path";

    private static final String DEFAULT_CONFIG_FILE_PATH = "config" + File.separator + "tomcat-server.xml";

    static final String CONFIG_PATH = "META-INF/tomcat";

    static final String DEFAULT_CONFIG_PATH = CONFIG_PATH + "/default-server.xml";

    static final String USER_CONFIG_PATH = "server.xml";

    static InputStream resolveConfigFile(BundleContext context) throws BundleException {
        Bundle bundle = context.getBundle();

        InputStream is = lookupConfigInFileSystem(context);

        if (is == null) {
            is = lookupConfigInBundle(bundle);
        }
        return is;
    }

    /**
     * Returns the directory where the Tomcat configuration files resides.
     *
     * The location algorithm is as follows:
     * <ol>
     * <li>Check for <code>org.eclipse.gemini.web.tomcat.config.path</code> framework property, use if found</li>
     * <li>Check for <code>config/tomcat-server.xml</code> in the current working directory, use if found</li>
     * <li>If the previous checks do not return a result, return <code>null</code></li>
     * </ol>
     *
     * @param context the bundle context
     * @return the directory where the Tomcat configuration files resides.
     */
    static Path resolveConfigDir(BundleContext context) {
        Path configFile = null;

        /*
         * Search for the framework property 'org.eclipse.gemini.web.tomcat.config.path'
         *
         * Note: this is supposed to search framework and system properties but appears to ignore system properties
         * which are set after the framework has initialised. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=319679.
         */
        String path = context.getProperty(TomcatConfigLocator.CONFIG_PATH_FRAMEWORK_PROPERTY);
        if (path != null) {
            configFile = Paths.get(path);
            if (Files.exists(configFile)) {
                return configFile.getParent();
            }
        }

        // Search for the system property 'org.eclipse.gemini.web.tomcat.config.path'
        path = System.getProperty(TomcatConfigLocator.CONFIG_PATH_FRAMEWORK_PROPERTY);
        if (path != null) {
            configFile = Paths.get(path);
            if (Files.exists(configFile)) {
                return configFile.getParent();
            }
        }

        // Search for the 'config' directory
        configFile = Paths.get(TomcatConfigLocator.DEFAULT_CONFIG_FILE_PATH);
        if (Files.exists(configFile)) {
            return configFile.getParent();
        }

        return null;
    }

    static String resolveHostConfigDir(Path configDir, Host host) {
        if (configDir == null) {
            return null;
        }

        StringBuilder xmlDir;
        try {
            xmlDir = new StringBuilder(configDir.toRealPath().toString());
        } catch (IOException e) {
            return null;
        }
        Container parent = host.getParent();
        if (parent instanceof Engine) {
            xmlDir.append('/');
            xmlDir.append(parent.getName());
        }
        xmlDir.append('/');
        xmlDir.append(host.getName());
        return xmlDir.toString();
    }

    private static InputStream lookupConfigInFileSystem(BundleContext context) {
        InputStream result = null;

        String path = context.getProperty(CONFIG_PATH_FRAMEWORK_PROPERTY);
        if (path != null) {
            result = tryGetStreamForFilePath(path);
        }

        if (result == null) {
            result = tryGetStreamForFilePath(DEFAULT_CONFIG_FILE_PATH);
        }
        return result;
    }

    private static InputStream tryGetStreamForFilePath(String filePath) {
        Path configFile = Paths.get(filePath);
        if (Files.exists(configFile)) {
            try {
                InputStream fis = Files.newInputStream(configFile);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Configuring Tomcat from file [" + configFile + "].");
                }
                return fis;
            } catch (IOException e) {
                LOGGER.warn("Found config file on disk but then received IOException when trying to access.", e);
            }
        }
        return null;
    }

    private static InputStream lookupConfigInBundle(Bundle bundle) throws BundleException {
        URL entry = null;
        Enumeration<URL> entries = bundle.findEntries(CONFIG_PATH, USER_CONFIG_PATH, false);
        if (entries != null && entries.hasMoreElements()) {
            entry = entries.nextElement();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Configuring Tomcat from fragment entry [" + entry + "].");
            }
        } else {
            entry = bundle.getEntry(DEFAULT_CONFIG_PATH);
            if (entry == null) {
                throw new IllegalStateException("Unable to locate default Tomcat configuration. Is the [" + bundle + "] bundle corrupt?");
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Configuring Tomcat from default config file.");
            }
        }

        try {
            return entry.openStream();
        } catch (IOException e) {
            throw new BundleException("Unable to open Tomcat configuration at [" + entry + "].");
        }
    }
}
