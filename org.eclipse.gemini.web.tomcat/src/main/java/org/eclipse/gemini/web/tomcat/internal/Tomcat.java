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

package org.eclipse.gemini.web.tomcat.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.DefaultJarScanner;
import org.apache.catalina.startup.Embedded;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.digester.Digester;
import org.eclipse.gemini.web.core.spi.ServletContainerException;
import org.eclipse.gemini.web.tomcat.internal.loading.ChainedClassLoader;
import org.eclipse.gemini.web.tomcat.internal.support.BundleFileResolverFactory;
import org.eclipse.gemini.web.tomcat.internal.support.PackageAdminBundleDependencyDeterminer;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;


final class Tomcat extends Embedded {

    private static final String ROOT_CONTEXT_PATH = "";

    private static final String ROOT_PATH = "/";

    private final static Logger LOGGER = LoggerFactory.getLogger(Tomcat.class);

    private final ExtendCatalina catalina = new ExtendCatalina();

    private final JarScanner jarScanner;
    
    private BundleContext bundleContext;

    private File configDir;

    private String defaultWeb;

    Tomcat(BundleContext context, PackageAdmin packageAdmin) {
        this.bundleContext = context;
        JarScanner bundleDependenciesJarScanner = new BundleDependenciesJarScanner(new PackageAdminBundleDependencyDeterminer(context, packageAdmin),
            BundleFileResolverFactory.createBundleFileResolver());
        JarScanner defaultJarScanner = new DefaultJarScanner();

        this.jarScanner = new ChainingJarScanner(bundleDependenciesJarScanner, defaultJarScanner);
    }

    public void start() throws LifecycleException {
        super.start();

        Server server = getServer();
        if (server instanceof Lifecycle) {
            ((Lifecycle) server).start();
        }
    }

    public void stop() throws LifecycleException {
        Server server = getServer();

        stopConnectorsAndContainers(server);                

        super.stop();
    }

    /**
     * 
     */
    private void stopConnectorsAndContainers(Server server) throws LifecycleException {
        Service[] services = server.findServices();
        if (services != null) {
            for (Service service : services) {
                Connector[] connectors = service.findConnectors();
                for (Connector connector : connectors) {
                    connector.stop();
                }
                Container container = service.getContainer();
                if (container instanceof Lifecycle) {
                    ((Lifecycle)container).stop();
                }
            }
        }
    }

    public Engine findEngine() {
        Server server = getServer();
        Service[] findServices = server.findServices();
        for (Service service : findServices) {
            Container container = service.getContainer();
            if (container instanceof Engine) {
                return (Engine) container;
            }
        }
        throw new IllegalStateException("Unable to locate Engine.");
    }

    public Host findHost() {
        Engine engine = findEngine();
        Container[] children = engine.findChildren();
        for (Container container : children) {
            if (container instanceof Host) {
                return (Host) container;
            }
        }
        throw new IllegalStateException("Unable to locate Host.");
    }

    @Override
    public void initialize() throws LifecycleException {
        getServer().initialize();
    }

    @Override
    public Context createContext(String path, String docBase) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating context '" + path + "' with docBase '" + docBase + "'");
        }
        
        StandardContext context = new ExtendedStandardContext();
        
        ExtendedContextConfig config = new ExtendedContextConfig();

        if (configDir == null) {
            // Allocate the tomcat's configuration directory
            configDir = TomcatConfigLocator.resolveConfigDir(bundleContext);
        }
        config.setConfigBase(configDir);

        if (defaultWeb == null) {
            // Allocate the default web.xml
            defaultWeb = WebappConfigLocator.resolveDefaultWebXml(configDir);
        }
        config.setDefaultWebXml(defaultWeb);

        // If default context.xml is existing, set it to the ContextConfig
        String defaultContextXml = WebappConfigLocator.resolveDefaultContextXml(configDir);
        if (defaultContextXml != null) {
            config.setDefaultContextXml(defaultContextXml);
        }

        // Allocate the web application's configuration directory
        File configLocation = WebappConfigLocator.resolveWebappConfigDir(configDir, findHost());

        // If web application's context.xml is existing, set it to the
        // StandardContext
        File contextXml = WebappConfigLocator.resolveWebappContextXml(path, docBase, configLocation);
        if (contextXml != null) {
            context.setConfigFile(contextXml.getAbsolutePath());
        }
        
        context.setDocBase(docBase);        
        context.setPath(path.equals(ROOT_PATH) ? ROOT_CONTEXT_PATH : path);

        context.setJarScanner(this.jarScanner);

        config.setCustomAuthenticators(this.authenticators);
        ((Lifecycle) context).addLifecycleListener(config);

        return (context);
    }

    public void configure(InputStream configuration) {
        Digester digester = this.catalina.createStartDigester();
        digester.push(this);

        ClassLoader[] loaders = new ClassLoader[] { Catalina.class.getClassLoader(), getClass().getClassLoader() };
        digester.setClassLoader(ChainedClassLoader.create(loaders));

        try {
            digester.parse(configuration);
        } catch (IOException e) {
            throw new ServletContainerException("Error reading Tomcat configuration file.", e);
        } catch (SAXException e) {
            throw new ServletContainerException("Error parsing Tomcat XML configuration.", e);
        }

        // Allocate the tomcat's configuration directory
        configDir = TomcatConfigLocator.resolveConfigDir(bundleContext);

        // Allocate the default web.xml
        defaultWeb = WebappConfigLocator.resolveDefaultWebXml(configDir);
    }

    /**
     * Overrides {@link Catalina} to provide public access to {@link Catalina#createStartDigester}.
     * 
     * 
     */
    private static class ExtendCatalina extends Catalina {

        @Override
        public Digester createStartDigester() {
            return super.createStartDigester();
        }

    }

    /**
     * Extends the Tomcat {@link StandardContext} to add custom functionality.
     * 
     * 
     */
    private static class ExtendedStandardContext extends StandardContext {

        private static final long serialVersionUID = 6914580440115519171L;

        /**
         * Returns <code>true</code> for exploded bundles.
         */
        @Override
        public boolean isFilesystemBased() {
            String docBase = getDocBase();
            File f = new File(docBase);
            return f.isDirectory();
        }

    }

    /**
     * Override {@link ContextConfig}. This changes the {@link ClassLoader} used to load the web-embed.xml to the
     * <code>ClassLoader</code> of this bundle.
     * 
     * 
     */
    private static class ExtendedContextConfig extends ContextConfig {
        private File configDir;

        /**
         * If there is not configuration directory, return custom configuration
         * directory. It is used to resolve the context.xml.default
         */
        @Override
        protected File getConfigBase() {
            File configBase = super.getConfigBase();
            if (configBase != null) {
                return configBase;
            }

            if (configDir != null) {
                return configDir;
            }

            return null;
        }

        protected void setConfigBase(File configDir) {
            this.configDir = configDir;
        }
    }

}
