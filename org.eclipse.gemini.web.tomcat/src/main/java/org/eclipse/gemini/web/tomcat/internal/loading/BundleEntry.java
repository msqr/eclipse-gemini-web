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

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;

public final class BundleEntry {

	private static final String PATH_SEPARATOR = "/";

	private final String path;

	private final Bundle bundle;

	public BundleEntry(Bundle bundle) {
		this(bundle, "");
	}

	private BundleEntry(Bundle bundle, String path) {
		this.path = path;
		this.bundle = bundle;
	}

	public Bundle getBundle() {
		return this.bundle;
	}

	public List<BundleEntry> list() {
		List<BundleEntry> entries = new ArrayList<BundleEntry>();
		Enumeration<?> paths = getEntryPathsFromBundle();
		if (paths != null) {
			while (paths.hasMoreElements()) {
				String subPath = (String) paths.nextElement();
				entries.add(createBundleEntry(subPath));
			}
		}
		return entries;
	}

	private BundleEntry createBundleEntry(String path) {
		return new BundleEntry(this.bundle, path);
	}

	private Enumeration<?> getEntryPathsFromBundle() {
		final Enumeration<?> ep = this.bundle.getEntryPaths(this.path);

		Set<String> paths = new HashSet<String>();
		if (ep != null) {
			while (ep.hasMoreElements()) {
				paths.add((String) ep.nextElement());
			}
		}
		
		// Ensure web.xml appears even though it may be supplied by a fragment.
		if ("WEB-INF".equals(this.path) && getEntry("web.xml") != null) {
			paths.add("WEB-INF/web.xml");
		}
		
		if (paths.isEmpty()) {
			return null;
		}
		
		final String[] pathArray = paths.toArray(new String[0]);
		
		return new Enumeration<String>() {

			private int pos = 0;

			public boolean hasMoreElements() {
				return pos < pathArray.length;
			}

			public String nextElement() {
				if (hasMoreElements()) {
					return pathArray[pos++];
				}
				return null;
			}

		};
	}

	public BundleEntry getEntry(String subPath) {
		String finalPath = this.path + subPath;
		if (getEntryFromBundle(finalPath) != null) {
			return createBundleEntry(finalPath);
		} else {
			return null;
		}
	}

	private URL getEntryFromBundle(String path) {
		/*
		 * This method has been generalised from this.bundle.getEntry(path) to
		 * allow web.xml to be supplied by a fragment.
		 */
		if (path.endsWith(PATH_SEPARATOR) || path.length() == 0) {
			return this.bundle.getEntry(path);
		}
		String searchPath;
		String searchFile;
		int lastSlashIndex = path.lastIndexOf(PATH_SEPARATOR);
		if (lastSlashIndex == -1) {
			searchPath = PATH_SEPARATOR;
			searchFile = path;
		} else {
			searchPath = path.substring(0, lastSlashIndex);
			searchFile = path.substring(lastSlashIndex + 1);
		}
		Enumeration<?> entries = this.bundle.findEntries(searchPath,
				searchFile, false);

		if (entries != null) {
			if (entries.hasMoreElements()) {
				return (URL) entries.nextElement();
			}
		}

		return null;
	}

	public String getName() {
		String name = this.path;

		if (name.endsWith(PATH_SEPARATOR)) {
			name = name.substring(0, this.path.length() - 1);
		}

		int index = name.lastIndexOf(PATH_SEPARATOR);
		if (index > -1) {
			name = name.substring(index + 1);
		}

		if (name.length() == 0) {
			return PATH_SEPARATOR;
		} else {
			return name;
		}
	}

	public URL getURL() {
		return getEntryFromBundle(this.path);
	}

	public String getPath() {
		return this.path;
	}

	public boolean isDirectory() {
	    URL entryFromBundle = getEntryFromBundle(this.path);
	    return entryFromBundle.getFile().endsWith(PATH_SEPARATOR);
	}

	@Override
	public String toString() {
		return String.format("BundleEntry [bundle=%s,path=%s]", this.bundle,
				this.path);
	}
}
