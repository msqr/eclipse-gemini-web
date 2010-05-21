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

import java.util.Set;

import org.osgi.framework.Bundle;

/**
 * Determines a {@link Bundle Bundle's} dependencies.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface BundleDependencyDeterminer {

    /**
     * Returns the dependencies of the supplied <code>Bundle</code>, i.e. the <code>Bundles</code> to which it is wired.
     * 
     * @param bundle the <code>Bundle</code> for which the dependencies are required.
     * @return the dependencies of the supplied <code>Bundle</code>.
     */
    Set<Bundle> getDependencies(Bundle bundle);
}
