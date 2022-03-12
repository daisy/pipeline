import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.util.PathUtils;

public class XSpecAndXProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			"org.daisy.bindings:jhyphen:?",
			brailleModule("braille-common"),
		};
	}
	
	@Override @Configuration
	public Option[] config() {
		return options(
			thisBundle(thisPlatform()),
			composite(super.config()));
	}
	
	private static UrlProvisionOption thisBundle(String classifier) {
		File classes = new File(PathUtils.getBaseDir() + "/target/classes");
		Properties dependencies = new Properties(); {
			try {
				dependencies.load(new FileInputStream(new File(classes, "META-INF/maven/dependencies.properties"))); }
			catch (IOException e) {
				throw new RuntimeException(e); }
		}
		String artifactId = dependencies.getProperty("artifactId");
		String version = dependencies.getProperty("version");
		// assuming JAR is named ${artifactId}-${version}.jar
		return bundle("reference:" +
		              new File(PathUtils.getBaseDir() + "/target/" + artifactId + "-" + version + "-" + classifier + ".jar").toURI());
	}
}
