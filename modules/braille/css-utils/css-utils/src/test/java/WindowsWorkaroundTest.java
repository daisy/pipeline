import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.daisy.maven.xproc.xprocspec.XProcSpecRunner;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

public class WindowsWorkaroundTest extends PaxExamConfig {
	
	@Override @Configuration
	public Option[] config() {
		return options(
			composite(super.config()),
			systemProperty("file.encoding").value("UTF8")
		);
	}
	
	@Override @Test
	public void runXProcSpec() throws Exception {
		if (!PaxExamConfig.onWindows) return;
		File baseDir = new File(PathUtils.getBaseDir());
		Map<String,File> tests = new HashMap<String,File>();
		tests.put("test_inline_test_encoding_windows_workaround",
		          new File(baseDir, "src/test/xprocspec/test_inline/test_encoding_windows_workaround.xprocspec"));
		boolean success = xprocspecRunner.run(tests,
		                                      new File(baseDir, "target/xprocspec-reports/windows"),
		                                      new File(baseDir, "target/surefire-reports"),
		                                      new File(baseDir, "target/xprocspec"),
		                                      null,
		                                      new XProcSpecRunner.Reporter.DefaultReporter());
		assertTrue("XProcSpec tests should run with success", success);
	}
	
	@Override @Test
	public void runXSpec() throws Exception {
	}
}
