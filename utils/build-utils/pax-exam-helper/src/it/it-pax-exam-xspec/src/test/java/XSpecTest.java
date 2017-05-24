import java.io.File;

import javax.inject.Inject;

import org.daisy.maven.xspec.TestResults;
import org.daisy.maven.xspec.XSpecRunner;

import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.xspec;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class XSpecTest {
	
	@Configuration
	public Option[] config() {
		return options(
			logbackConfigFile(),
			felixDeclarativeServices(),
			mavenBundlesWithDependencies(
				logbackClassic(),
				// xspec
				xspec(),
				mavenBundle("org.daisy.pipeline:saxon-adapter:?")),
			junitBundles()
		);
	}
	
	@Inject
	private XSpecRunner xspecRunner;
	
	@Test
	public void runXSpec() throws Exception {
		File baseDir = new File(PathUtils.getBaseDir());
		File testsDir = new File(baseDir, "src/test/xspec");
		File reportsDir = new File(baseDir, "target/surefire-reports");
		reportsDir.mkdirs();
		TestResults result = xspecRunner.run(testsDir, reportsDir);
		assertEquals("Number of failures and errors should be zero", 0L, result.getFailures() + result.getErrors());
	}
}
