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

package org.eclipse.gemini.web.tomcat.internal.loading;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.naming.NamingContextEnumeration;
import org.apache.naming.NamingEntry;
import org.eclipse.gemini.web.tomcat.internal.support.BundleFileResolver;
import org.eclipse.gemini.web.tomcat.internal.support.BundleFileResolverFactory;
import org.osgi.framework.Bundle;

public final class BundleDirContext extends AbstractReadOnlyDirContext {

    private volatile BundleEntry bundleEntry;

    private final BundleFileResolver bundleFileResolver = BundleFileResolverFactory.createBundleFileResolver();

    public BundleDirContext(Bundle bundle) {
        this(new BundleEntry(bundle));
    }

    private BundleDirContext(BundleEntry bundleEntry) {
        this.bundleEntry = bundleEntry;
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        List<NamingEntry> resources = doSafeList(name);
        return new NamingContextEnumeration(resources.iterator());
    }

    public List<NamingEntry> doListBindings(String name) throws NamingException {
        return doSafeList(name);
    }

    public Object doLookup(String name) {
        try {
            return entryToResult(getNamedEntry(name));
        } catch (NamingException e) {
            return null;
        }
    }

    private List<NamingEntry> doSafeList(String name) throws NamingException {
        return doList(getNamedEntry(name));
    }

    private BundleEntry getNamedEntry(String name) throws NamingException {
        checkCanLookup(name);
        BundleEntry bundleEntry = this.bundleEntry.getEntry(name);
        if (bundleEntry == null) {
            throw new NamingException("Name '" + name + "' does not exist.");
        }
        return bundleEntry;
    }

    private List<NamingEntry> doList(BundleEntry bundleEntry) {
        List<BundleEntry> list = bundleEntry.list();
        List<NamingEntry> resources = new ArrayList<NamingEntry>();
        for (BundleEntry entry : list) {
            Object object;
            object = entryToResult(entry);
            resources.add(new NamingEntry(entry.getName(), object, NamingEntry.ENTRY));
        }
        return resources;
    }

    private Object entryToResult(BundleEntry entry) {
        Object result;
        if (entry.isDirectory()) {
            result = new BundleDirContext(entry);
        } else {
            result = new URLResource(entry.getURL());
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
     * @return the requested attributes; never null
     * @param name the name of the object from which to retrieve attributes
     * @param attrIds the identifiers of the attributes to retrieve. null indicates that all attributes should be
     *        retrieved; an empty array indicates that none should be retrieved
     * @exception NamingException if a naming exception is encountered
     */
    @Override
    protected Attributes doGetAttributes(String name, String[] attrIds) throws NamingException {
        return new BundleEntryAttributes(getNamedEntry(name), attrIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String doGetRealPath(String path) {
        if (this.bundleEntry.getEntry(path) != null) {
            File bundleLocation = this.bundleFileResolver.resolve(this.bundleEntry.getBundle());
            if (bundleLocation != null && bundleLocation.isDirectory()) {
                return new File(bundleLocation, path).getAbsolutePath();
            }
        }
        return null;
    }
}
