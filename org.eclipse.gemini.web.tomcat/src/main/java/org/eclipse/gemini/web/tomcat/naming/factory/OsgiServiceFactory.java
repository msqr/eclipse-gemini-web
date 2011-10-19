/*******************************************************************************
 * Copyright (c) 2011 SAP AG
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

package org.eclipse.gemini.web.tomcat.naming.factory;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.apache.naming.ResourceRef;

/**
 * Object factory for OSGi Services.
 */
public class OsgiServiceFactory implements ObjectFactory {

    static final String MAPPED_NAME = "mappedName";

    static final String OSGI_JNDI_URLSCHEME = "osgi:";

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        if (obj instanceof ResourceRef) {
            Reference ref = (Reference) obj;
            String mappedName = null;
            RefAddr mappedNameRefAddr = ref.get(MAPPED_NAME);
            if (mappedNameRefAddr != null) {
                Object mappedNameRefAddrContent = mappedNameRefAddr.getContent();
                if (mappedNameRefAddrContent != null) {
                    mappedName = mappedNameRefAddr.getContent().toString();
                }
            }
            if (mappedName != null) {
                return new InitialContext().lookup(OSGI_JNDI_URLSCHEME + mappedName);
            }
        }
        return null;
    }

}
