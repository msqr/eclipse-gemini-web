/*******************************************************************************
 * Copyright (c) 2009, 2014 VMware Inc.
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

package org.eclipse.gemini.web.tomcat.internal.loading;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.naming.NamingEntry;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BundleDirContext extends AbstractReadOnlyDirContext {

    private final static Logger LOGGER = LoggerFactory.getLogger(BundleDirContext.class);

    private volatile BundleEntry bundleEntry;

    public BundleDirContext(Bundle bundle) {
        this(new BundleEntry(bundle));
    }

    private BundleDirContext(BundleEntry bundleEntry) {
        this.bundleEntry = bundleEntry;
    }

    /**
     * {@inheritDoc}
     * 
     * @return an enumeration of the bindings in this context or null if the name cannot be found. If null is returned
     *         then <code>BaseDirContext.listBidings(String)</code> will continue to search in the alternative
     *         locations.
     */
    @Override
    public List<NamingEntry> doListBindings(String name) throws NamingException {
        try {
            return doSafeList(name);
        } catch (NameNotFoundException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Name '" + name + "' does not exist.", e);
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @return the object bound to name or null if NamingException has occurred. If null is returned then
     *         <code>BaseDirContext.lookup(String)</code> will continue to search in the alternative locations.
     */
    @Override
    public Object doLookup(String name) {
        try {
            Entry<BundleEntry, URL> entry = getNamedEntry(name);
            return entryToResult(entry.getKey(), entry.getValue());
        } catch (NamingException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", e);
            }
            return null;
        }
    }

    private List<NamingEntry> doSafeList(String name) throws NamingException {
        Entry<BundleEntry, URL> entry = getNamedEntry(name);
        return doList(entry.getKey());
    }

    private Entry<BundleEntry, URL> getNamedEntry(String name) throws NamingException {
        checkCanLookup(name);
        Entry<BundleEntry, URL> namedEntry = this.bundleEntry.getEntry(name);
        if (namedEntry == null) {
            throw new NameNotFoundException("Name '" + name + "' does not exist.");
        }
        return namedEntry;
    }

    private List<NamingEntry> doList(BundleEntry bundleEntry) {
        List<BundleEntry> list = bundleEntry.list();
        List<NamingEntry> resources = new ArrayList<>();
        for (BundleEntry entry : list) {
            Object object;
            object = entryToResult(entry);
            resources.add(new NamingEntry(entry.getName(), object, NamingEntry.ENTRY));
        }
        return resources;
    }

    private Object entryToResult(BundleEntry entry) {
        return entryToResult(entry, entry.getURL());
    }

    private Object entryToResult(BundleEntry entry, URL url) {
        Object result;
        if (BundleEntry.isDirectory(url)) {
            result = new BundleDirContext(entry);
        } else {
            result = new URLResource(url);
        }
        return result;
    }

    private void checkCanLookup(String name) throws NamingException {
        BundleEntry entry = this.bundleEntry;
        if (entry == null || entry.getBundle().getState() == Bundle.UNINSTALLED) {
            throw new NamingException("Resource not found '" + name + "'");
        }
        checkNotAttemptingToLookupFromProtectedLocation(name);
    }

    private void checkNotAttemptingToLookupFromProtectedLocation(String name) throws NamingException {
        checkNotAttemptingToLookupFrom(name, "/OSGI-INF/");
        checkNotAttemptingToLookupFrom(name, "/OSGI-OPT/");
    }

    private void checkNotAttemptingToLookupFrom(String name, String prefix) throws NamingException {
        if (name.startsWith(prefix)) {
            throw new NamingException("Resource cannot be obtained from " + prefix);
        }
    }

    @Override
    public void close() throws NamingException {
        super.close();
        this.bundleEntry = null;
    }

    /**
     * Retrieves selected attributes associated with a named object.
     * 
     * @return the requested attributes or null if the specified name does not exists. If null is returned then
     *         <code>BaseDirContext.getAttributes(String, String[])</code> will continue to search in the alternative
     *         locations.
     * @param name the name of the object from which to retrieve attributes
     * @param attrIds the identifiers of the attributes to retrieve. null indicates that all attributes should be
     *        retrieved; an empty array indicates that none should be retrieved
     * @exception NamingException if a naming exception is encountered
     */
    @Override
    protected Attributes doGetAttributes(String name, String[] attrIds) throws NamingException {
        try {
            Entry<BundleEntry, URL> entry = getNamedEntry(name);
            return new BundleEntryAttributes(entry.getKey(), attrIds, entry.getValue());
        } catch (NameNotFoundException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Name '" + name + "' does not exist.", e);
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @return Returns the real path for a given virtual path, if possible; otherwise returns null. If null is returned
     *         then <code>BaseDirContext.getRealPath(String)</code> will continue to search in the alternative
     *         locations.
     */
    @Override
    protected String doGetRealPath(String path) {
        if (this.bundleEntry.isBundleLocationDirectory()) {
            boolean checkInBundleLocation = path != null && path.indexOf("..") >= 0;
            String bundleLocationCanonicalPath = this.bundleEntry.getBundleLocationCanonicalPath();
            File entry = new File(bundleLocationCanonicalPath, path);
            if (checkInBundleLocation) {
                try {
                    if (!entry.getCanonicalPath().startsWith(bundleLocationCanonicalPath)) {
                        return null;
                    }
                } catch (IOException e) {
                    return null;
                }
            }
            return entry.getAbsolutePath();
        }
        return null;
    }
}
