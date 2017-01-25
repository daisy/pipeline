import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.daisy.maven.xproc.xprocspec.XProcSpecRunner;
import org.daisy.maven.xspec.TestResults;
import org.daisy.maven.xspec.XSpecRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class Test {
	
	@Configuration
	public Option[] config() {
		return options(
			PaxExamConfig.config()
		);
	}
	
	@Inject
	private XProcSpecRunner xprocspecRunner;
	
	@org.junit.Test
	public void runXProcSpec() throws Exception {
		File baseDir = new File(PathUtils.getBaseDir());
		Map<String,File> tests = new HashMap<String,File>();
		for (File file : new File(baseDir, "src/test/xprocspec").listFiles())
			if (file.getName().endsWith(".xprocspec"))
				tests.put(file.getName().replaceAll("\\.xprocspec$", ""), file);
		for (File file : new File(baseDir, "src/test/xprocspec/test_inline").listFiles())
			if (file.getName().endsWith(".xprocspec")) {
				if (file.getName().equals("test_encoding_windows_workaround.xprocspec")) continue;
				if (file.getName().contains("windows") && !PaxExamConfig.onWindows) continue;
				if (file.getName().equals("test_encoding.xprocspec") && PaxExamConfig.onWindows) continue;
				tests.put("test_inline_" + file.getName().replaceAll("\\.xprocspec$", ""), file); }
		boolean success = xprocspecRunner.run(tests,
		                                      new File(baseDir, "target/xprocspec-reports"),
		                                      new File(baseDir, "target/surefire-reports"),
		                                      new File(baseDir, "target/xprocspec"),
		                                      null,
		                                      new XProcSpecRunner.Reporter.DefaultReporter());
		assertTrue("XProcSpec tests should run with success", success);
	}
	
	@Inject
	private XSpecRunner xspecRunner;
	
	@org.junit.Test
	public void runXSpec() throws Exception {
		File baseDir = new File(PathUtils.getBaseDir());
		File testsDir = new File(baseDir, "src/test/xspec");
		File reportsDir = new File(baseDir, "target/surefire-reports");
		reportsDir.mkdirs();
		TestResults result = xspecRunner.run(testsDir, reportsDir);
		assertEquals("Number of failures and errors should be zero", 0L, result.getFailures() + result.getErrors());
	}
}
