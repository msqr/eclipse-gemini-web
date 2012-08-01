/*******************************************************************************
 * Copyright (c) 2010 VMware Inc.
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

package org.eclipse.gemini.web.internal.url;

import java.util.Arrays;

final class Path {

    private static final String BACK_SLASH = "\\";

    private static final String PATH_SEPARATOR = "/";

    private static final String DOUBLE_PATH_SEPARATOR = PATH_SEPARATOR + PATH_SEPARATOR;

    private static final String PATH_HERE = ".";

    private static final String PATH_UP = "..";

    private final String[] baseComponents;

    Path(String basePath) {
        validatePath(basePath);
        String[] comps = basePath.split(PATH_SEPARATOR);
        while (comps.length > 0 && PATH_HERE.equals(comps[0])) {
            String[] c2 = new String[comps.length - 1];
            System.arraycopy(comps, 1, c2, 0, c2.length);
            comps = c2;
        }
        this.baseComponents = comps;
    }

    private Path(String[] comps) {
        this.baseComponents = comps;
    }

    private String head() {
        if (!isEmpty()) {
            return this.baseComponents[0];
        } else {
            throw new IllegalStateException("head not applicable to an empty path");
        }
    }

    private Path tail() {
        if (!isEmpty()) {
            String[] c = new String[this.baseComponents.length - 1];
            System.arraycopy(this.baseComponents, 1, c, 0, c.length);
            return new Path(c);
        } else {
            throw new IllegalStateException("tail not applicable to an empty path");
        }
    }

    private Path front() {
        if (!isEmpty()) {
            String[] c = new String[this.baseComponents.length - 1];
            System.arraycopy(this.baseComponents, 0, c, 0, c.length);
            return new Path(c);
        } else {
            throw new IllegalStateException("front not applicable to an empty path");
        }
    }

    private static void validatePath(String basePath) {
        if (basePath == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        if (basePath.contains(BACK_SLASH)) {
            throw new IllegalArgumentException("path must not contain '" + BACK_SLASH + "'");
        }
        if (basePath.endsWith(PATH_SEPARATOR)) {
            throw new IllegalArgumentException("path must not end in '" + PATH_SEPARATOR + "'");
        }
        if (basePath.contains(DOUBLE_PATH_SEPARATOR)) {
            throw new IllegalArgumentException("path must not contain '" + DOUBLE_PATH_SEPARATOR + "'");
        }
    }

    public Path() {
        this.baseComponents = new String[0];
    }

    public Path applyRelativePath(Path relativePath) {
        try {
            Path b = this;
            Path r = relativePath;
            while (r.isUp()) {
                r = r.tail();
                b = b.front();
            }
            return b.append(r);
        } catch (IllegalStateException s) {
            throw new IllegalArgumentException("relative path cannot be applied", s);
        }
    }

    private Path append(Path r) {
        if (isEmpty()) {
            return r;
        } else if (r.isEmpty()) {
            return this;
        }
        return new Path(toString() + PATH_SEPARATOR + r.toString());
    }

    private boolean isUp() {
        return !isEmpty() && PATH_UP.equals(head());
    }

    private boolean isEmpty() {
        return this.baseComponents.length == 0;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        boolean first = true;
        for (String c : this.baseComponents) {
            if (!first) {
                s.append(PATH_SEPARATOR);
            }
            first = false;
            s.append(c);
        }

        return s.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.baseComponents);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Path other = (Path) obj;
        if (!Arrays.equals(this.baseComponents, other.baseComponents)) {
            return false;
        }
        return true;
    }

}
