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

import junit.framework.Assert;

import org.junit.Test;

public class PathTests {

    @Test
    public void testNoArgsConstructor() {
        Path path = new Path();
        Assert.assertEquals("", path.toString());
    }

    @Test
    public void testPathStringConstructor() {
        Assert.assertEquals("", new Path("").toString());
        Assert.assertEquals("a", new Path("a").toString());
        Assert.assertEquals("a", new Path("./a").toString());
        Assert.assertEquals("a/b", new Path("a/b").toString());
        Assert.assertEquals("a/b", new Path("./a/b").toString());

        Assert.assertEquals("/a", new Path("/a").toString());
        Assert.assertEquals("/a/b", new Path("/a/b").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPathFails() {
        new Path(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPathWithTrailingSlashFails() {
        new Path("/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPathWithEmptyComponent() {
        new Path("a//b");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPathWithBackSlashFail() {
        new Path("\\");
    }

    @Test
    public void testSimpleRelativePath() {
        Path p = new Path("a");
        Path r = new Path("b");
        Assert.assertEquals(new Path("a/b"), p.applyRelativePath(r));

    }

    @Test
    public void testDotDotRelativePathAppliedToDirectory() {
        Path p = new Path("a");
        Path r = new Path("../b");
        Assert.assertEquals(new Path("b"), p.applyRelativePath(r));
    }

    @Test
    public void testDotDotRelativePathAppliedToSubdirectory() {
        Path p = new Path("a/b");
        Path r = new Path("../c");
        Assert.assertEquals(new Path("a/c"), p.applyRelativePath(r));
    }

    @Test
    public void testMultiDotDotRelativePathAppliedToSubdirectory() {
        Path p = new Path("a/b/c");
        Path r = new Path("../../d");
        Assert.assertEquals(new Path("a/d"), p.applyRelativePath(r));
    }

    @Test
    public void testDeepMultiDotDotRelativePathAppliedToSubdirectory() {
        Path p = new Path("a/b/c");
        Path r = new Path("../../d/e");
        Assert.assertEquals(new Path("a/d/e"), p.applyRelativePath(r));
    }

    @Test
    public void testDotDotRelativePathAppliedToDirectoryWithEmbeddedHere() {
        Path p = new Path("a/./b");
        Path r = new Path("../c");
        // The implementation does not compress out embedded "heres".
        Assert.assertEquals(new Path("a/./c"), p.applyRelativePath(r));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotEscapeEmptyPath() {
        Path p = new Path();
        Path r = new Path("../b");
        p.applyRelativePath(r);
    }

}
