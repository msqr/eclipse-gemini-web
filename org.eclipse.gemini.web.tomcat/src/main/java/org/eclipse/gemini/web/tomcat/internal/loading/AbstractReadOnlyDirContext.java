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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.naming.resources.BaseDirContext;


@SuppressWarnings("unchecked")
abstract class AbstractReadOnlyDirContext extends BaseDirContext {

    public void bind(String name, Object obj, Attributes attrs) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public void modifyAttributes(String name, int mod_op, Attributes attrs) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public void rebind(String name, Object obj, Attributes attrs) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public void bind(String name, Object obj) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public void rebind(String name, Object obj) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public void rename(String oldName, String newName) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public void unbind(String name) throws NamingException {
        throw new UnsupportedOperationException();        
    }

    public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public DirContext getSchema(String name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public DirContext getSchemaClassDefinition(String name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes, String[] attributesToReturn) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs, SearchControls cons) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public String composeName(String name, String prefix) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public Context createSubcontext(String name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public void destroySubcontext(String name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
        throw new UnsupportedOperationException();
    }

    public String getNameInNamespace() throws NamingException {
        throw new UnsupportedOperationException();
    }

    public NameParser getNameParser(String name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    public Object lookupLink(String name) throws NamingException {
        throw new UnsupportedOperationException();
    }

}
