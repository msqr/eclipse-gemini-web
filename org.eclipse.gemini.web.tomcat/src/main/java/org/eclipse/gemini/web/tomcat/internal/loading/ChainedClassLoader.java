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

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is immutable and therefore thread safe.
 */
public final class ChainedClassLoader extends ClassLoader {

	/** list of loaders */
	private final List<ClassLoader> loaders;

	/**
	 * Constructs a new <code>ChainedClassLoader</code> instance.
	 * 
	 * @param loaders
	 *            array of non-null class loaders
	 * @param parent
	 *            parent class loader (can be null)
	 */
	ChainedClassLoader(ClassLoader... loaders) {
		
		List<ClassLoader> l = new ArrayList<ClassLoader>();

		for (int i = 0; i < loaders.length; i++) {
			ClassLoader classLoader = loaders[i];
			if (!l.contains(classLoader)) {
				l.add(classLoader);
			}
		}
		
		this.loaders = Collections.unmodifiableList(l);
	}

	public static ChainedClassLoader create(final ClassLoader... loaders) {
		return AccessController
				.doPrivileged(new PrivilegedAction<ChainedClassLoader>() {

					public ChainedClassLoader run() {
						return new ChainedClassLoader(loaders);
					}

				});
	}

	@Override
	public URL getResource(final String name) {
		if (System.getSecurityManager() != null) {
			return AccessController.doPrivileged(new PrivilegedAction<URL>() {

				public URL run() {
					return doGetResource(name);
				}
			});
		} else {
			return doGetResource(name);
		}
	}

	@Override
	public Enumeration<URL> getResources(final String name) throws IOException {
		if (System.getSecurityManager() != null) {
			try {
				return AccessController
						.doPrivileged(new PrivilegedExceptionAction<Enumeration<URL>>() {

							public Enumeration<URL> run() throws Exception {
								return doGetResources(name);
							}

						});
			} catch (PrivilegedActionException e) {
				Exception exception = e.getException();
				if (exception instanceof RuntimeException) {
					throw (RuntimeException) exception;
				} else if (exception instanceof IOException) {
					throw (IOException) exception;
				} else {
					throw new IllegalStateException(
							"Unexpected Exception from privileged action.",
							exception);
				}
			}
		} else {
			return doGetResources(name);
		}
	}

	private Enumeration<URL> doGetResources(String name) throws IOException {
		Map<String, URL> urls = new HashMap<String, URL>();
		for (ClassLoader loader : this.loaders) {
			Enumeration<URL> resources = loader.getResources(name);
			if (resources != null) {
				while (resources.hasMoreElements()) {
					URL url = (URL) resources.nextElement();
					urls.put(url.getFile(), url);
				}
			}
		}
		return Collections.enumeration(urls.values());
	}

	private URL doGetResource(String name) {
		URL url = null;
		for (int i = 0; i < loaders.size(); i++) {
			ClassLoader loader = (ClassLoader) loaders.get(i);
			url = loader.getResource(name);
			if (url != null)
				return url;
		}
		return url;
	}

	public Class<?> loadClass(final String name) throws ClassNotFoundException {

		if (System.getSecurityManager() != null) {
			try {
				return AccessController
						.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {

							public Class<?> run() throws Exception {
								return doLoadClass(name);
							}
						});
			} catch (PrivilegedActionException pae) {
				throw (ClassNotFoundException) pae.getException();
			}
		} else {
			return doLoadClass(name);
		}
	}

	private Class<?> doLoadClass(String name) throws ClassNotFoundException {
		Class<?> clazz = null;

		for (int i = 0; i < loaders.size(); i++) {
			ClassLoader loader = (ClassLoader) loaders.get(i);
			try {
				clazz = loader.loadClass(name);
				return clazz;
			} catch (ClassNotFoundException e) {
				// keep moving through the class loaders
			}
		}

		throw new ClassNotFoundException(name);
	}

}
