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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

public class XSpecRunnerTest {

	private static PrintStream SYSOUT = System.out;
	private static File testsDir = new File(new File(XSpecRunnerTest.class
			.getResource("/").getPath()), "xspec-real");
	private XSpecRunner xspecRunner;
	private File reportDir;

	@Before
	public void setup() {
		System.setOut(new PrintStream(ByteStreams.nullOutputStream()));
		xspecRunner = new XSpecRunner();
		xspecRunner.init();
		reportDir = Files.createTempDir();
		reportDir.deleteOnExit();
	}

	public void tearDown() {
		System.setOut(SYSOUT);
		for (File file : reportDir.listFiles()) {
			file.delete();
		}
		reportDir.delete();
	}

	@Test
	public void testInit() {
		Assert.assertTrue(true);
	}

	@Test
	public void testSimple() {
		Map<String, File> tests = ImmutableMap.of("test", new File(testsDir,
				"test.xspec"));
		TestResults results = xspecRunner.run(tests, reportDir);
		assertThat(results.getName(), isEmptyString());
		assertThat(results.getRuns(), is(1L));
		assertThat(results.getErrors(), is(0L));
		assertThat(results.getFailures(), is(0L));
		assertThat(results.getSkipped(), is(0L));
		assertThat(
				results.toString(),
				startsWith("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: "));
		assertThat(results.toString(), not(endsWith("<<< FAILURE!")));
	}

	@Test
	public void testSkipped() {
		Map<String, File> tests = ImmutableMap.of("test", new File(testsDir,
				"skipped.xspec"));
		TestResults results = xspecRunner.run(tests, reportDir);
		assertThat(results.getSkipped(), is(1L));
		assertThat(results.toString(),
				startsWith("Tests run: 1, Failures: 0, Errors: 0, Skipped: 1"));
		assertThat(results.toString(), not(endsWith("<<< FAILURE!")));
	}

	@Test
	public void testFailure() {
		Map<String, File> tests = ImmutableMap.of("test", new File(testsDir,
				"failure.xspec"));
		TestResults results = xspecRunner.run(tests, reportDir);
		assertThat(results.getFailures(), is(1L));
		assertThat(results.toString(),
				startsWith("Tests run: 1, Failures: 1, Errors: 0, Skipped: 0"));
		assertThat(results.toString(), endsWith("<<< FAILURE!"));
	}

	@Test
	public void testError() {
		Map<String, File> tests = ImmutableMap.of("test", new File(testsDir,
				"error.xspec"));
		TestResults results = xspecRunner.run(tests, reportDir);
		assertThat(results.getErrors(), is(1L));
		assertThat(results.toString(),
				startsWith("Tests run: 0, Failures: 0, Errors: 1, Skipped: 0"));
		assertThat(results.toString(), endsWith("<<< FAILURE!"));
	}

	@Test
	public void testReports() {
		Map<String, File> tests = ImmutableMap.of("test", new File(testsDir,
				"test.xspec"));
		xspecRunner.run(tests, reportDir);
		assertThat(reportDir.list().length, equalTo(6));
		assertThat(reportDir.list(), hasItemInArray("xspec-report.css"));
		assertThat(reportDir.list(), hasItemInArray("OUT-test.txt"));
		assertThat(reportDir.list(), hasItemInArray("HTML-test.html"));
		assertThat(reportDir.list(), hasItemInArray("XSPEC-test.xml"));
		assertThat(reportDir.list(), hasItemInArray("TEST-test.xml"));
		assertThat(reportDir.list(), hasItemInArray("index.html"));
	}

	@Test
	public void testErrorReports() {
		Map<String, File> tests = ImmutableMap.of("test", new File(testsDir,
				"error.xspec"));
		xspecRunner.run(tests, reportDir);
		assertThat(reportDir.list().length, equalTo(2));
		assertThat(reportDir.list(), hasItemInArray("OUT-test.txt"));
		assertThat(reportDir.list(), hasItemInArray("index.html"));
	}

	@Test
	public void testCompleteFeatures() {
		Map<String, File> tests = ImmutableMap.of("complete", new File(
				testsDir, "complete.xspec"));
		xspecRunner.run(tests, reportDir);
		assertThat(reportDir.list().length, equalTo(6));
	}

	@Test
	public void testMockCatalog() {
		Map<String, File> tests = ImmutableMap.of("mocking", new File(testsDir,
				"mocking/test.xspec"));
		TestResults results = xspecRunner.run(tests, reportDir);
		assertThat(results.toString(),
				startsWith("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0"));
	}
	
	@Test
	public void testMultiMockCatalog() {
		Map<String, File> tests = ImmutableMap.of("mocking", new File(testsDir,
				"mocking/test.xspec"));
		TestResults results = xspecRunner.run(tests, reportDir);
		assertThat(results.toString(),
				startsWith("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0"));
		tests = ImmutableMap.of("mocking2", new File(testsDir,
				"mocking2/test.xspec"));
		results = xspecRunner.run(tests, reportDir);
		assertThat(results.toString(),
				startsWith("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0"));
	}

	@Test
	public void testNoMockCatalog() {
		Map<String, File> tests = ImmutableMap.of("nomocking", new File(
				testsDir, "nomocking/test.xspec"));
		TestResults results = xspecRunner.run(tests, reportDir);
		assertThat(results.toString(),
				startsWith("Tests run: 0, Failures: 0, Errors: 1, Skipped: 0"));
	}
}
