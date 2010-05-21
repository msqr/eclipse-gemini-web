/*
 * Copyright SpringSource Inc 2010
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.gemini.web.internal.url;

import junit.framework.Assert;

import org.eclipse.gemini.web.internal.url.Path;
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
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullPathFails() {
        new Path(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testPathWithTrailingSlashFails() {
        new Path("/");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testPathWithEmptyComponent() {
        new Path("a//b");
    }
    
    @Test(expected=IllegalArgumentException.class)
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
 
    @Test(expected=IllegalArgumentException.class)
    public void testCannotEscapeEmptyPath() {
        Path p = new Path();
        Path r = new Path("../b");
        p.applyRelativePath(r);
    }
    
}
