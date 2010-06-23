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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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
import org.eclipse.virgo.util.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;


final class Tomcat extends Embedded {

    private static final String ROOT_CONTEXT_PATH = "";

    private static final String ROOT_PATH = "/";
    
    private static final String ROOT_CONTEXT_FILE = "ROOT";

    private static final String CONTEXT_XML = "META-INF/context.xml";
    
    private static final String CONTEXT_PROPERTY = "Context";
    
    private static final String XML_EXTENSION = ".xml";
    
    private static final char SLASH_SEPARATOR = '/';
    
    private static final char HASH_SEPARATOR = '#';
    
    private static final String DEFAULT_CONFIG_DIRECTORY = "config";

    private final static Logger LOGGER = LoggerFactory.getLogger(Tomcat.class);

    private final ExtendCatalina catalina = new ExtendCatalina();

    private final JarScanner jarScanner;

    private Digester digester;
    
    private BundleContext bundleContext;

    Tomcat(BundleContext context, PackageAdmin packageAdmin) {
    	this.bundleContext = context;
        JarScanner bundleDependenciesJarScanner = new BundleDependenciesJarScanner(new PackageAdminBundleDependencyDeterminer(context, packageAdmin),
            BundleFileResolverFactory.createBundleFileResolver());
        JarScanner defaultJarScanner = new DefaultJarScanner();

        this.jarScanner = new ChainingJarScanner(bundleDependenciesJarScanner, defaultJarScanner);
        
        digester = createDigester();
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
        readContextXml(path, docBase, context);
        
        context.setDocBase(docBase);        
        context.setPath(path.equals(ROOT_PATH) ? ROOT_CONTEXT_PATH : path);

        context.setJarScanner(this.jarScanner);

        ContextConfig config = new ExtendedContextConfig();

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
    }
    
    private Digester createDigester() {
        Digester digester = new Digester();
        ClassLoader[] loaders = new ClassLoader[] { Catalina.class.getClassLoader(), Tomcat.class.getClassLoader() };
        digester.setClassLoader(ChainedClassLoader.create(loaders));
        digester.setValidating(false);
        digester.addSetProperties(CONTEXT_PROPERTY);
        return (digester);
    }

    private void readContextXml(String path, String docBase, StandardContext context) {
        // Multi-level context paths may be defined using #, e.g. foo#bar.xml
        // for a context path of /foo/bar.
        if (path.equals(ROOT_PATH)) {
            path = ROOT_CONTEXT_FILE;
        } else if (SLASH_SEPARATOR == path.charAt(0)) {
            path = path.substring(1);
        }
        path = path.replace(SLASH_SEPARATOR, HASH_SEPARATOR);

        // Initialize config directory location
        File configLocation = new File(resolveConfigDir());
        Host host = findHost();
        Container parent = host.getParent();
        if ((parent != null) && (parent instanceof Engine)) {
            configLocation = new File(configLocation, parent.getName());
        }
        configLocation = new File(configLocation, host.getName());

        // Try to find the context.xml in the Tomcat's configuration directory
        File contextXml = new File(configLocation, path + XML_EXTENSION);
        if (contextXml.exists()) {
            configureWebContext(contextXml, context);
            return;
        }

        // Try to find the context.xml in docBase
        File docBaseFile = new File(docBase);
        if (docBaseFile.isDirectory()) {
            contextXml = new File(docBaseFile, CONTEXT_XML);
            if (contextXml.exists()) {
                File destination = new File(configLocation, path + XML_EXTENSION);
                try {
                    copyFile(new FileInputStream(contextXml), destination);
                } catch (IOException e) {
                    throw new ServletContainerException("Cannot copy " + contextXml.getAbsolutePath() + " to "
                            + destination.getAbsolutePath(), e);
                }
                configureWebContext(destination, context);
            }
        } else {
            JarFile jar;
            try {
                jar = new JarFile(docBaseFile);
            } catch (IOException e) {
                throw new ServletContainerException("Cannot open for reading " + docBaseFile.getAbsolutePath(), e);
            }
            ZipEntry contextXmlEntry = jar.getEntry(CONTEXT_XML);
            if (contextXmlEntry != null) {
                File destination = new File(configLocation, path + XML_EXTENSION);
                try {
                    copyFile(jar.getInputStream(contextXmlEntry), destination);
                } catch (IOException e) {
                    throw new ServletContainerException("Cannot copy " + contextXml.getAbsolutePath() + " to "
                            + destination.getAbsolutePath(), e);
                }
                configureWebContext(destination, context);
            }
        }
    }

    private String resolveConfigDir() {
        File configFile = null;

        // Search for the property 'org.eclipse.gemini.web.tomcat.config.path'
        String path = bundleContext.getProperty(TomcatConfigLocator.CONFIG_PATH_FRAMEWORK_PROPERTY);
        if (path != null) {
            configFile = new File(path);
            if (configFile.exists()) {
                return configFile.getParent();
            }
        }

        // Search for the 'config' directory
        configFile = new File(TomcatConfigLocator.DEFAULT_CONFIG_FILE_PATH);
        if (configFile.exists()) {
            return configFile.getParent();
        } else {
            return DEFAULT_CONFIG_DIRECTORY;
        }
    }

    private void configureWebContext(File contextXml, StandardContext context) {
        synchronized (digester) {
            try {
                digester.push(context);
                try {
                    digester.parse(contextXml);
                    context.setConfigFile(contextXml.getAbsolutePath());
                } catch (IOException e) {
                    throw new ServletContainerException("Cannot read " + contextXml.getAbsolutePath()
                            + " for web application.", e);
                } catch (SAXException e) {
                    throw new ServletContainerException("Cannot parse " + contextXml.getAbsolutePath()
                            + " for web application.", e);
                }
            } finally {
                digester.reset();
            }
        }
    }

    private void copyFile(InputStream source, File destination) throws IOException {
        destination.getParentFile().mkdirs();

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = source.read(buffer)) > 0) {
                outputStream.write(buffer, 0, read);
            }
        } finally {
            IOUtils.closeQuietly(source);
            IOUtils.closeQuietly(outputStream);
        }
    }
}
