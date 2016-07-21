import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.calabashConfigFile;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.pipelineModule;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;
import static org.daisy.pipeline.pax.exam.Options.xprocspec;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

public class Config {
	
	public static boolean onWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
	
	public static Option config() {
		return composite(
			logbackConfigFile(),
			domTraversalPackage(),
			felixDeclarativeServices(),
			calabashConfigFile(),
			thisBundle(),
			junitBundles(),
			mavenBundlesWithDependencies(
				brailleModule("common-utils"),
				brailleModule("css-core"),
				mavenBundle("org.daisy.libs:io.bit3.jsass:?"),
				mavenBundle("com.google.guava:guava:?"),
				mavenBundle("org.daisy.libs:com.xmlcalabash:?"),
				mavenBundle("org.daisy.libs:saxon-he:?"),
				mavenBundle("org.daisy.libs:jstyleparser:?"),
				mavenBundle("org.daisy.pipeline:calabash-adapter:?"),
				// logging
				logbackClassic(),
				// xprocspec
				xprocspec(),
				mavenBundle("org.daisy.maven:xproc-engine-daisy-pipeline:?")),
			bundle("reference:file:" + PathUtils.getBaseDir() + "/target/test-classes/css-module/")
			);
	}
}
