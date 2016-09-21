/**
 * Copyright (C) 2013 The DAISY Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.daisy.maven.xspec;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.daisy.maven.xspec.TestResults.Builder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

public class XSpecMojoTest {

	private static PrintStream SYSOUT = System.out;
	private static File resourcesDir = new File(XSpecMojoTest.class
			.getResource("/").getPath());
	private XSpecMojo mojo;
	private TestResults.Builder resultsBuilder;
	@Mock
	private Log log;
	@Mock
	private XSpecRunner runner;

	@Before
	public void setup() {
		System.setOut(new PrintStream(ByteStreams.nullOutputStream()));
		MockitoAnnotations.initMocks(this);
		resultsBuilder = new Builder("results");

		// Initialize the Mojo with default settings
		mojo = new XSpecMojo();
		mojo.setXSpecRunner(runner);
		mojo.setLog(log);
		mojo.setTestSourceDirectory(new File(resourcesDir, "xspec-dummy"));
		mojo.setReportsDirectory(Files.createTempDir());

		// By default, make the XSpec mock return an empty result
		when(runner.run(anyMapOf(String.class, File.class), any(File.class)))
				.thenReturn(resultsBuilder.build());
	}

	@After
	public void tearDown() {
		System.setOut(SYSOUT);
	}

	@Test
	public void testSimple() throws MojoExecutionException,
			MojoFailureException {
		mojo.execute();
		verify(runner, times(1)).run(anyMapOf(String.class, File.class),
				any(File.class));
	}

	@Test
	public void testSkip() throws MojoExecutionException, MojoFailureException {
		mojo.setSkip(true);
		mojo.execute();
		verify(runner, never()).run(anyMapOf(String.class, File.class),
				any(File.class));
	}

	@Test
	public void testSkipTests() throws MojoExecutionException,
			MojoFailureException {
		mojo.setSkipTests(true);
		mojo.execute();
		verify(runner, never()).run(anyMapOf(String.class, File.class),
				any(File.class));
	}

	@Test
	public void testInvalidReportsDir() throws MojoExecutionException,
			MojoFailureException {
		mojo.setReportsDirectory(new File("pom.xml"));
		mojo.execute();
		verify(runner, never()).run(anyMapOf(String.class, File.class),
				any(File.class));
	}

	@Test
	public void testInvalidSourceDir() throws MojoExecutionException,
			MojoFailureException {
		mojo.setTestSourceDirectory(new File("foobar"));
		mojo.execute();
		verify(runner, never()).run(anyMapOf(String.class, File.class),
				any(File.class));
	}

	@Test
	public void testDefaultIncludes() throws MojoExecutionException,
			MojoFailureException {
		mojo.execute();
		Map<String, File> includes = ImmutableMap.of("test", new File(
				resourcesDir, "xspec-dummy/test.xspec"), "sub.other", new File(
				resourcesDir, "xspec-dummy/sub/other.xspec"), "sub.test", new File(
				resourcesDir, "xspec-dummy/sub/test.xspec"), "sub.sub.test",
				new File(resourcesDir, "xspec-dummy/sub/sub/test.xspec"));
		verify(runner, times(1)).run(eq(includes), any(File.class));
	}
	
	@Test
	public void testEmptyIncludes() throws MojoExecutionException,
			MojoFailureException {
		mojo.setIncludes(Collections.<String> emptyList());
		mojo.execute();
		Map<String, File> includes = ImmutableMap.of("test", new File(
				resourcesDir, "xspec-dummy/test.xspec"), "sub.other", new File(
				resourcesDir, "xspec-dummy/sub/other.xspec"), "sub.test", new File(
				resourcesDir, "xspec-dummy/sub/test.xspec"), "sub.sub.test",
				new File(resourcesDir, "xspec-dummy/sub/sub/test.xspec"));
		verify(runner, times(1)).run(eq(includes), any(File.class));
	}
	
	@Test
	public void testIncludes() throws MojoExecutionException,
			MojoFailureException {
		mojo.setIncludes(ImmutableList.of("**/test.xspec"));
		mojo.execute();
		Map<String, File> includes = ImmutableMap.of("test", new File(
				resourcesDir, "xspec-dummy/test.xspec"), "sub.test", new File(
				resourcesDir, "xspec-dummy/sub/test.xspec"), "sub.sub.test",
				new File(resourcesDir, "xspec-dummy/sub/sub/test.xspec"));
		verify(runner, times(1)).run(eq(includes), any(File.class));
	}

	@Test
	public void testExcludes() throws MojoExecutionException,
			MojoFailureException {
		mojo.setExcludes(ImmutableList.of("**/other.xspec"));
		mojo.execute();
		Map<String, File> includes = ImmutableMap.of("test", new File(
				resourcesDir, "xspec-dummy/test.xspec"), "sub.test", new File(
				resourcesDir, "xspec-dummy/sub/test.xspec"), "sub.sub.test",
				new File(resourcesDir, "xspec-dummy/sub/sub/test.xspec"));
		verify(runner, times(1)).run(eq(includes), any(File.class));
	}
	

	@Test
	public void testSingle() throws MojoExecutionException,
			MojoFailureException {
		mojo.setTest("other");
		mojo.execute();
		Map<String, File> includes = ImmutableMap.of("sub.other", new File(
				resourcesDir, "xspec-dummy/sub/other.xspec"));
		verify(runner, times(1)).run(eq(includes), any(File.class));
	}
	
	@Test
	public void testSingleOverridesIncludes() throws MojoExecutionException,
			MojoFailureException {
		mojo.setTest("other");
		mojo.setIncludes(ImmutableList.of("**/test.xspec"));
		mojo.execute();
		Map<String, File> includes = ImmutableMap.of("sub.other", new File(
				resourcesDir, "xspec-dummy/sub/other.xspec"));
		verify(runner, times(1)).run(eq(includes), any(File.class));
	}

	@Test(expected = MojoFailureException.class)
	public void testErrors() throws MojoExecutionException,
			MojoFailureException {
		when(runner.run(anyMapOf(String.class, File.class), any(File.class)))
				.thenReturn(resultsBuilder.addError().build());
		mojo.execute();
	}

	@Test(expected = MojoFailureException.class)
	public void testFailures() throws MojoExecutionException,
			MojoFailureException {
		when(runner.run(anyMapOf(String.class, File.class), any(File.class)))
				.thenReturn(resultsBuilder.addFailure().build());
		mojo.execute();
	}

	@Test(expected = MojoExecutionException.class)
	public void testException() throws MojoExecutionException,
			MojoFailureException {
		when(runner.run(anyMapOf(String.class, File.class), any(File.class)))
				.thenReturn(null);
		mojo.execute();
	}
}
