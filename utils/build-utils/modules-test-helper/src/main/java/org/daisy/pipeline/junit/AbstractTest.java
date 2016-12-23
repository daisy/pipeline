package org.daisy.pipeline.junit;

import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import org.daisy.pipeline.pax.exam.Options.MavenBundleOption;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundles;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;

import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public abstract class AbstractTest {
	
	protected String[] testDependencies() {
		return null;
	}
	
	protected String pipelineModule(String module) {
		return "org.daisy.pipeline.modules:" + module + ":?";
	}
	
	@Configuration
	public Option[] config() {
		return _.config(
			logbackConfigFile(),
			mavenBundles(testDependencies()));
	}
	
	// wrapped in class to avoid ClassNotFoundException
	protected static abstract class _ {
		protected static Option[] config(Option systemProperties, MavenBundleOption testDependencies) {
			return options(
				systemProperties,
				domTraversalPackage(),
				felixDeclarativeServices(),
				thisBundle(),
				junitBundles(),
				mavenBundle("org.daisy.pipeline.build:modules-test-helper:?"),
				mavenBundlesWithDependencies(
					testDependencies,
					// logging
					logbackClassic())
			);
		}
	}
}
