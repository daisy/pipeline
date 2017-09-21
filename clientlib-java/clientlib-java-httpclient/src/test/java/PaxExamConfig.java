import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.io.FileUtils;

import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;

import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public abstract class PaxExamConfig {
	
	static final File BASEDIR = new File(PathUtils.getBaseDir());
	static final File PIPELINE_BASE = new File(BASEDIR, "target/tmp/server");
	static final File PIPELINE_DATA = new File(PIPELINE_BASE, "data");
	
	static {
		try {
			FileUtils.deleteDirectory(PIPELINE_BASE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	abstract boolean isLocalFs();
	
	@Configuration
	public Option[] config() throws MalformedURLException {
		return options(
			systemProperty("org.daisy.pipeline.ws.localfs").value(String.valueOf(isLocalFs())),
			systemProperty("org.daisy.pipeline.ws.authentication").value("false"),
			systemProperty("org.daisy.pipeline.iobase").value(new File(PIPELINE_DATA, "jobs").getAbsolutePath()),
			systemProperty("org.daisy.pipeline.version").value("1.10"),
			domTraversalPackage(),
			logbackConfigFile(),
			felixDeclarativeServices(),
			junitBundles(),
			systemPackage("com.sun.org.apache.xml.internal.resolver"),
			systemPackage("com.sun.org.apache.xml.internal.resolver.tools"),
			mavenBundlesWithDependencies(
				logbackClassic(),
				mavenBundle("commons-io:commons-io:?"),
				// pipeline webservice
				mavenBundle("org.daisy.pipeline:webservice:?"),
				mavenBundle("org.daisy.pipeline:calabash-adapter:?"), // org.daisy.common.xproc.XProcEngine
				mavenBundle("org.daisy.pipeline:modules-registry:?"), // javax.xml.transform.URIResolver
				mavenBundle("org.daisy.pipeline:framework-volatile:?"), // org.daisy.pipeline.job.JobStorage
				mavenBundle("org.daisy.pipeline:woodstox-osgi-adapter:?"), // javax.xml.stream.XMLInputFactory
				mavenBundle("org.daisy.pipeline:framework-core:?"), // org.daisy.pipeline.datatypes.DatatypeRegistry
				// FIXME: these belong in the framework as runtime dependencies
				mavenBundle("org.daisy.libs:jing:?")
				),
			// example script (incl. datatypes)
			bundle("reference:" + new File(BASEDIR, "target/test-classes/example_script/").toURL().toString()),
			// the client must technically not be run in OSGi, however there
			// is no other way to keep the webservice running while the test
			// is executed
			wrappedBundle(
				bundle(new File(BASEDIR, "target/clientlib-java-httpclient-"+MavenUtils.asInProject().getVersion("org.daisy.pipeline", "clientlib-java-httpclient")+".jar").toURL().toString()))
				.bundleSymbolicName("org.daisy.pipeline.clientlib-java-httpclient")
				.bundleVersion(MavenUtils.asInProject().getVersion("org.daisy.pipeline", "clientlib-java-httpclient").replaceAll("-",".")),
			wrappedBundle(
				mavenBundle("org.daisy.pipeline:clientlib-java:?"))
				.bundleSymbolicName("org.daisy.pipeline.clientlib-java")
				.bundleVersion(MavenUtils.asInProject().getVersion("org.daisy.pipeline", "clientlib-java")),
			mavenBundle("org.apache.httpcomponents:httpcore-osgi:?"),
			mavenBundle("org.apache.httpcomponents:httpclient-osgi:?")
		);
	}
}
