package org.daisy.maven.xproc.xprocspec;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.ServiceLoader;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import org.junit.Test;

public class PerformanceTest {
	
	private static File logbackXml = new File(new File(XProcSpecRunnerTest.class.getResource("/").getPath()), "logback.xml");
	
	@Test
	public void testFileWithManyScenarios() {
		File testsDir = new File(new File(PerformanceTest.class.getResource("/").getPath()), "xprocspec");
		XProcSpecRunner xprocspecRunner = ServiceLoader.load(XProcSpecRunner.class).iterator().next();
		File reportsDir = Files.createTempDir();
		reportsDir.deleteOnExit();
		File surefireReportsDir = Files.createTempDir();
		surefireReportsDir.deleteOnExit();
		File tempDir = Files.createTempDir();
		tempDir.deleteOnExit();
		System.setProperty("logback.configurationFile", logbackXml.toURI().toASCIIString());
		Map<String,File> tests = ImmutableMap.of(
			"test_very_big", new File(testsDir, "test_very_big.xprocspec"));
		long startTime = System.nanoTime();
		xprocspecRunner.run(tests,
		                    reportsDir,
		                    surefireReportsDir,
		                    tempDir,
		                    null,
		                    new XProcSpecRunner.Reporter.DefaultReporter());
		long timeElapsed = System.nanoTime() - startTime;
		System.out.println("Time elapsed: " + TimeUnit.SECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS) + " seconds");
	}
}
