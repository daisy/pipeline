package org.daisy.maven.xproc.xprocspec;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import org.junit.Ignore;
import org.junit.Test;

@Ignore // takes too much time at the moment
public class PerformanceTest {
	
	@Test
	public void testFileWithManyScenarios() {
		File testsDir = new File(new File(PerformanceTest.class.getResource("/").getPath()), "xprocspec");
		XProcSpecRunner xprocspecRunner = new XProcSpecRunner();
		File reportsDir = Files.createTempDir();
		reportsDir.deleteOnExit();
		File surefireReportsDir = Files.createTempDir();
		surefireReportsDir.deleteOnExit();
		File tempDir = Files.createTempDir();
		tempDir.deleteOnExit();
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
