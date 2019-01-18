package org.daisy.pipeline.junit;

import java.io.File;

import javax.inject.Inject;

import org.daisy.maven.xproc.xprocspec.XProcSpecRunner;
import org.daisy.maven.xspec.TestResults;
import org.daisy.maven.xspec.XSpecRunner;

import static org.daisy.pipeline.pax.exam.Options.calabashConfigFile;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundles;
import static org.daisy.pipeline.pax.exam.Options.xprocspec;
import static org.daisy.pipeline.pax.exam.Options.xspec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

public abstract class AbstractXSpecAndXProcSpecTest extends AbstractTest {
	
	@Inject
	protected XSpecRunner xspecRunner;
	
	@Test
	public void runXSpec() throws Exception {
		File baseDir = new File(PathUtils.getBaseDir());
		File testsDir = new File(baseDir, "src/test/xspec");
		if (testsDir.exists()) {
			File reportsDir = new File(baseDir, "target/surefire-reports");
			reportsDir.mkdirs();
			TestResults result = xspecRunner.run(testsDir, reportsDir);
			assertEquals("Number of failures and errors should be zero", 0L, result.getFailures() + result.getErrors());
		}
	}
	
	@Inject
	protected XProcSpecRunner xprocspecRunner;
	
	@Test
	public void runXProcSpec() throws Exception {
		File baseDir = new File(PathUtils.getBaseDir());
		File testsDir = new File(baseDir, "src/test/xprocspec");
		if (testsDir.exists()) {
			boolean success = xprocspecRunner.run(testsDir,
			                                      new File(baseDir, "target/xprocspec-reports"),
			                                      new File(baseDir, "target/surefire-reports"),
			                                      new File(baseDir, "target/xprocspec"),
			                                      new XProcSpecRunner.Reporter.DefaultReporter());
			assertTrue("XProcSpec tests should run with success", success);
		}
	}
	
	@Override @Configuration
	public Option[] config() {
		return _.config(
			composite(
				logbackConfigFile(),
				calabashConfigFile()),
			mavenBundles(
				mavenBundles(testDependencies()),
				// xprocspec
				xprocspec(),
				mavenBundle("org.daisy.pipeline:calabash-adapter:?"),
				mavenBundle("org.daisy.pipeline:framework-volatile:?"),
				mavenBundle("org.daisy.maven:xproc-engine-daisy-pipeline:?"),
				// xspec
				xspec(),
				mavenBundle("org.daisy.pipeline:saxon-adapter:?"))
		);
	}
}
