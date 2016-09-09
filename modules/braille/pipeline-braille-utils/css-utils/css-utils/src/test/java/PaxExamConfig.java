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
import static org.daisy.pipeline.pax.exam.Options.xspec;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;

public class PaxExamConfig {
	
	public static boolean onWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
	
	public static Option config() {
		return composite(
			logbackConfigFile(),
			domTraversalPackage(),
			felixDeclarativeServices(),
			systemPackage("javax.xml.stream;version=\"1.0.1\""),
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
				mavenBundle("org.daisy.maven:xproc-engine-daisy-pipeline:?"),
				// xspec
				xspec(),
				mavenBundle("org.apache.servicemix.bundles:org.apache.servicemix.bundles.xmlresolver:?"),
				mavenBundle("org.daisy.pipeline:saxon-adapter:?")),
			bundle("reference:file:" + PathUtils.getBaseDir() + "/target/test-classes/css-module/")
			);
	}
}

