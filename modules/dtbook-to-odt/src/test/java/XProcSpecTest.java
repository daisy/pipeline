import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.daisy.maven.xproc.xprocspec.XProcSpecRunner;
import org.junit.Assume;
import org.junit.Test;

import org.ops4j.pax.exam.util.PathUtils;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {

	@Override
	public void runXProcSpec() {
		File baseDir = new File(PathUtils.getBaseDir());
		File testsDir = new File(baseDir, XPROCSPEC_TESTS_DIR);
		File dtbookToWordTest = new File(testsDir, "test_dtbook-to-odt-to-docx-to-dtbook.xprocspec");
		Map<String,File> tests = new HashMap<>(); {
			for (File file : xprocspecRunner.listXProcSpecFilesRecursively(testsDir)) {
				// FIXME: add a "filter" argument to XProcRunner.run() instead
				if (file.equals(dtbookToWordTest))
					try {
						LibreOfficeFinder.get();
					} catch (NoSuchElementException e) {
						System.out.println(
							"Skipping " + dtbookToWordTest
							+ ": can not be run because LibreOffice can not be found");
						continue;
					}
				tests.put(
					file.getAbsolutePath().substring(testsDir.getAbsolutePath().length() + 1)
					    .replaceAll("\\.xprocspec$", ""),
					file);
			}
		}
		File reportsDir = new File(baseDir, "target/xprocspec-reports");
		for (int i = 2; reportsDir.exists(); i++)
			reportsDir = new File(baseDir, "target/xprocspec-reports-" + i);
		File surefireDir = new File(baseDir, "target/surefire-reports");
		for (int i = 2; surefireDir.exists(); i++)
			surefireDir = new File(baseDir, "target/surefire-reports-" + i);
		File tmpDir = new File(baseDir, "target/xprocspec");
		for (int i = 2; tmpDir.exists(); i++)
			tmpDir = new File(baseDir, "target/xprocspec-" + i);
		boolean success = xprocspecRunner.run(tests,
		                                      reportsDir,
		                                      surefireDir,
		                                      tmpDir,
		                                      null,
		                                      null,
		                                      new XProcSpecRunner.Reporter.DefaultReporter());
		if (!success)
			throw new AssertionError("There are XProcSpec test failures.");
	}
}
