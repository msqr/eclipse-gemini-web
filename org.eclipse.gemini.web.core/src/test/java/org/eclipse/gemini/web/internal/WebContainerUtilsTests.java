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

package org.eclipse.gemini.web.internal;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.gemini.web.internal.WebContainerUtils;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class WebContainerUtilsTests {

	private static final Version TEST_BUNDLE_VERSION = new Version("467");
	private static final String TEST_SYMBOLIC_NAME = "a";
	private static final Properties EMPTY_PROPERTIES = new Properties();

	@Test
	public void testGetBaseNameFilePath() {
		String name = WebContainerUtils.getBaseName("/path/to/app.war");
		assertEquals("app", name);
	}

	@Test
	public void testGetBaseNameDirPath() {
		String name = WebContainerUtils.getBaseName("/path/to/app.war/");
		assertEquals("app", name);
	}

	@Test
	public void testIsWebBundleWithWarExtension() {
		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getLocation()).andReturn("file:foo.war").anyTimes();
		replay(bundle);
		assertTrue(WebContainerUtils.isWebBundle(bundle));
	}

	@Test
	public void testIsWebBundleWithUpperCaseWarExtension() {
		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getLocation()).andReturn("file:foo.WAR").anyTimes();
		replay(bundle);
		assertTrue(WebContainerUtils.isWebBundle(bundle));
	}

	@Test
	public void testIsWebBundleWithWarExtensionAndTrailingSlash() {
		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getLocation()).andReturn("file:foo.war/").anyTimes();
		replay(bundle);
		assertTrue(WebContainerUtils.isWebBundle(bundle));
	}

	@Test
	public void testIsWebBundleWithWarExtensionAndTrailingSlashes() {
		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getLocation()).andReturn("file:foo.war//").anyTimes();
		replay(bundle);
		assertTrue(WebContainerUtils.isWebBundle(bundle));
	}

	@Test
	public void testIsWebBundleWithUpperCaseWarExtensionAndTrailingSlash() {
		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getLocation()).andReturn("file:foo.WAR/").anyTimes();
		replay(bundle);
		assertTrue(WebContainerUtils.isWebBundle(bundle));
	}

	@Test
	public void testIsWebBundleWithWebBundleScheme() {
		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getLocation()).andReturn("webbundle:foo.jar").anyTimes();
		replay(bundle);
		assertTrue(WebContainerUtils.isWebBundle(bundle));
	}

	@Test
	public void testIsWebBundleWithWebContextPath()
			throws Exception {
		Properties p = new Properties();
		p.setProperty(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/foo");

		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getLocation()).andReturn("file:foo.jar").anyTimes();
		expect(bundle.getHeaders()).andReturn(p).anyTimes();
		replay(bundle);
		assertTrue(WebContainerUtils.isWebBundle(bundle));
	}

	@Test
	public void testIsWebBundleWithWebXml() throws MalformedURLException {
		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getLocation()).andReturn("file:foo.jar").anyTimes();
		expect(bundle.getHeaders()).andReturn(new Properties()).anyTimes();
		expect(bundle.getEntry(WebContainerUtils.ENTRY_WEB_XML)).andReturn(
				new URL("file:foo.txt")).anyTimes();
		replay(bundle);
		assertTrue(WebContainerUtils.isWebBundle(bundle));
	}

	@Test
	public void testNotWebBundle() throws Exception {
		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getLocation()).andReturn("file:foo.jar").anyTimes();
		expect(bundle.getHeaders()).andReturn(new Properties()).anyTimes();
		expect(bundle.getEntry(WebContainerUtils.ENTRY_WEB_XML))
				.andReturn(null).anyTimes();
		replay(bundle);
		assertFalse(WebContainerUtils.isWebBundle(bundle));
	}

	@Test
	public void testContextPathSupplied() throws Exception {
		Properties p = new Properties();
		p.setProperty(WebContainerUtils.HEADER_WEB_CONTEXT_PATH, "/foo");

		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getHeaders()).andReturn(p).anyTimes();
		replay(bundle);

		assertEquals("/foo", WebContainerUtils.getContextPath(bundle));
	}

	@Test
	public void testContextPathDefaulted() throws Exception {
		Properties p = new Properties();

		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getLocation()).andReturn("file:bar.war").anyTimes();
		expect(bundle.getHeaders()).andReturn(p).anyTimes();
		replay(bundle);

		assertEquals("bar", WebContainerUtils.getContextPath(bundle));
	}

	@Test
	public void testContextPathDefaultedWindowsPath() throws Exception {
		Properties p = new Properties();

		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getLocation()).andReturn("file:/C:\\bar\\app.war")
				.anyTimes();
		expect(bundle.getHeaders()).andReturn(p).anyTimes();
		replay(bundle);

		assertEquals("app", WebContainerUtils.getContextPath(bundle));
	}

	@Test
	public void testContextPathDefaultedComplexPath() throws Exception {
		Properties p = new Properties();

		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getLocation()).andReturn(
				"file:../formtags.war?Import-Package:org.foo.bar").anyTimes();
		expect(bundle.getHeaders()).andReturn(p).anyTimes();
		replay(bundle);

		assertEquals("formtags", WebContainerUtils.getContextPath(bundle));
	}

	@Test
	public void testServletContextOsgiWebSymbolicNamePropertyDefault() {
		Properties p = new Properties();

		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getSymbolicName()).andReturn(null).anyTimes();
		expect(bundle.getHeaders()).andReturn(EMPTY_PROPERTIES).anyTimes();
		replay(bundle);

		WebContainerUtils.setServletContextBundleProperties(p, bundle);

		assertFalse(p.containsKey(WebContainerUtils.OSGI_WEB_SYMBOLICNAME));
	}

	@Test
	public void testServletContextOsgiWebSymbolicNamePropertySupplied() {
		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getSymbolicName()).andReturn(TEST_SYMBOLIC_NAME)
				.anyTimes();
		expect(bundle.getHeaders()).andReturn(EMPTY_PROPERTIES).anyTimes();
		replay(bundle);

		Properties p = new Properties();

		WebContainerUtils.setServletContextBundleProperties(p, bundle);

		assertEquals(TEST_SYMBOLIC_NAME, p
				.get(WebContainerUtils.OSGI_WEB_SYMBOLICNAME));
	}

	@Test
	public void testServletContextOsgiWebVersionPropertyDefault() {
		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getVersion()).andReturn(null).anyTimes();
		expect(bundle.getHeaders()).andReturn(EMPTY_PROPERTIES).anyTimes();
		replay(bundle);

		Properties p = new Properties();

		WebContainerUtils.setServletContextBundleProperties(p, bundle);

		assertFalse(p.containsKey(WebContainerUtils.OSGI_WEB_VERSION));
	}

	@Test
	public void testServletContextOsgiWebVersionPropertySupplied() {
		Bundle bundle = createNiceMock(Bundle.class);
		expect(bundle.getVersion()).andReturn(TEST_BUNDLE_VERSION).anyTimes();

		Properties headers = new Properties();
		headers.put(WebContainerUtils.BUNDLE_VERSION_HEADER,
				TEST_BUNDLE_VERSION.toString());
		expect(bundle.getHeaders()).andReturn(headers).anyTimes();

		replay(bundle);

		Properties p = new Properties();

		WebContainerUtils.setServletContextBundleProperties(p, bundle);

		String stringVersion = (String) p
				.get(WebContainerUtils.OSGI_WEB_VERSION);
		assertEquals(TEST_BUNDLE_VERSION, new Version(stringVersion));
	}

}
