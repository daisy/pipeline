import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.daisy.maven.xproc.xprocspec.XProcSpecRunner;

import static org.junit.Assert.assertTrue;

import org.ops4j.pax.exam.util.PathUtils;

public class Test extends PaxExamConfig {
	
	@Override
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
}
