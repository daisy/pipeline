import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.daisy.maven.xproc.xprocspec.XProcSpecRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class XProcSpecTest {
	
	@Configuration
	public Option[] config() {
		return options(
			Config.config()
		);
	}
	
	@Inject
	private XProcSpecRunner xprocspecRunner;
	
	@Test
	public void runXProcSpec() throws Exception {
		File baseDir = new File(PathUtils.getBaseDir());
		Map<String,File> tests = new HashMap<String,File>();
		for (File file : new File(baseDir, "src/test/xprocspec").listFiles())
			if (file.getName().endsWith(".xprocspec")) {
				if (file.getName().equals("test_encoding_windows_workaround.xprocspec")) continue;
				if (file.getName().contains("windows") && !Config.onWindows) continue;
				if (file.getName().equals("test_encoding.xprocspec") && Config.onWindows) continue;
				tests.put(file.getName().replaceAll("\\.xprocspec$", ""), file); }
		boolean success = xprocspecRunner.run(tests,
		                                      new File(baseDir, "target/xprocspec-reports"),
		                                      new File(baseDir, "target/surefire-reports"),
		                                      new File(baseDir, "target/xprocspec"),
		                                      null,
		                                      new XProcSpecRunner.Reporter.DefaultReporter());
		assertTrue("XProcSpec tests should run with success", success);
	}
}
