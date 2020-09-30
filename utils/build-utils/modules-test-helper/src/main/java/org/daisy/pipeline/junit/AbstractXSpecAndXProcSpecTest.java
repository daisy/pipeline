package org.daisy.pipeline.junit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.daisy.maven.xproc.xprocspec.XProcSpecRunner;
import org.daisy.maven.xspec.TestResults;
import org.daisy.maven.xspec.XSpecRunner;

import org.daisy.pipeline.pax.exam.Options;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundles;
import static org.daisy.pipeline.pax.exam.Options.xprocspec;
import static org.daisy.pipeline.pax.exam.Options.xspec;

import org.junit.Test;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

public abstract class AbstractXSpecAndXProcSpecTest extends AbstractTest {
	
	protected String XSPEC_TESTS_DIR = "src/test/xspec";
	protected String XPROCSPEC_TESTS_DIR = "src/test/xprocspec";
	
	@Inject
	protected XSpecRunner xspecRunner;
	
	//@Test
	public void runXSpec() throws Exception {
		File baseDir = new File(PathUtils.getBaseDir());
		File testsDir = new File(baseDir, XSPEC_TESTS_DIR);
		if (testsDir.exists()) {
			File reportsDir = new File(baseDir, "target/surefire-reports");
			for (int i = 2; reportsDir.exists(); i++)
				reportsDir = new File(baseDir, "target/surefire-reports-" + i);
			reportsDir.mkdirs();
			TestResults result = xspecRunner.run(testsDir, reportsDir);
			if (result.getFailures() > 0 || result.getErrors() > 0) {
				System.out.println(result.toDetailedString());
				 throw new AssertionError("There are XSpec test failures.");
			}
		}
	}
	
	@Inject
	protected XProcSpecRunner xprocspecRunner;
	
	//@Test
	public void runXProcSpec() throws Exception {
		File baseDir = new File(PathUtils.getBaseDir());
		File testsDir = new File(baseDir, XPROCSPEC_TESTS_DIR);
		if (testsDir.exists()) {
			File reportsDir = new File(baseDir, "target/xprocspec-reports");
			for (int i = 2; reportsDir.exists(); i++)
				reportsDir = new File(baseDir, "target/xprocspec-reports-" + i);
			File surefireDir = new File(baseDir, "target/surefire-reports");
			for (int i = 2; surefireDir.exists(); i++)
				surefireDir = new File(baseDir, "target/surefire-reports-" + i);
			File tmpDir = new File(baseDir, "target/xprocspec");
			for (int i = 2; tmpDir.exists(); i++)
				tmpDir = new File(baseDir, "target/xprocspec-" + i);
			boolean success = xprocspecRunner.run(testsDir,
			                                      reportsDir,
			                                      surefireDir,
			                                      tmpDir,
			                                      new XProcSpecRunner.Reporter.DefaultReporter());
			if (!success)
				throw new AssertionError("There are XProcSpec test failures.");
		}
	}
	
	@Test
	public void runXSpecAndXProcSpec() throws Exception {
		File baseDir = new File(PathUtils.getBaseDir());
		File xspecDir = new File(baseDir, "src/test/xspec");
		File xprocspecDir = new File(baseDir, "src/test/xprocspec");
		boolean xspecHasFocus = xspecRunner.hasFocus(xspecDir);
		boolean xprocspecHasFocus = xprocspecRunner.hasFocus(xprocspecDir);
		List<AssertionError> errors = new ArrayList<AssertionError>();
		if (xspecDir.exists() && (xspecHasFocus || !xprocspecHasFocus))
			try {
				runXSpec();
			} catch (AssertionError e) {
				errors.add(e);
			}
		if (xprocspecDir.exists() && (xprocspecHasFocus || !xspecHasFocus))
			try {
				runXProcSpec();
			} catch (AssertionError e) {
				errors.add(e);
			}
		for (AssertionError e : errors)
			throw e;
	}
	
	@Override
	protected Properties allSystemProperties() {
		return mergeProperties(
			super.allSystemProperties(),
			calabashConfiguration());
	}
	
	/* ------------- */
	/* For OSGi only */
	/* ------------- */
	
	@Override @Configuration
	public Option[] config() {
		return _.config(
			Options.systemProperties(allSystemProperties()),
			mavenBundles(
				mavenBundles(toStrings(testDependencies())),
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
