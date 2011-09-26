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

package org.eclipse.gemini.web.tomcat.internal.support;

import java.io.File;

import org.osgi.framework.Bundle;

/**
 * Strategy interface for resolving the {@link File} content of an installed {@link Bundle}. This is the
 * <code>File</code> that the framework is actually using - it may not correspond to the location the
 * <code>Bundle</code> was installed from.
 * 
 */
public interface BundleFileResolver {

    /**
     * Attempts to resolve the supplied {@link Bundle} to the {@link File} its content is being loaded from.
     * 
     * @param bundle the <code>Bundle</code>.
     * @return the <code>File</code> or <code>null</code> if no <code>File</code> could be found.
     */
    File resolve(Bundle bundle);

    /**
     * Attempts to resolve the size of the specified bundle entry from the given bundle.
     * 
     * @param bundle the bundle that contains the specified bundle entry.
     * @param path the specified bundle entry.
     * @return the size of the specified bundle entry from the given bundle, or -1 if it cannot be determined.
     */
    long resolveBundleEntrySize(Bundle bundle, String path);
}
