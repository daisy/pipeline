package org.daisy.pipeline.junit;

import java.io.File;
import java.util.Properties;

import org.daisy.pipeline.pax.exam.Options;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import org.daisy.pipeline.pax.exam.Options.MavenBundleOption;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundles;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;
import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

@RunWith(TestRunner.class)
public abstract class AbstractTest {
	
	protected Properties systemProperties() {
		return null;
	}
	
	protected Properties calabashConfiguration() {
		File baseDir = new File(PathUtils.getBaseDir());
		Properties p = new Properties();
		p.setProperty("com.xmlcalabash.config.user", "");
		File file = new File(baseDir, "/src/test/resources/config-calabash.xml");
		if (file.exists())
			p.setProperty("org.daisy.pipeline.xproc.configuration", file.getAbsolutePath());
		return p;
	}
	
	protected Properties logbackConfiguration() {
		File baseDir = new File(PathUtils.getBaseDir());
		File file = new File(baseDir, "/src/test/resources/logback.xml");
		if (!file.exists())
			return null;
		Properties p = new Properties();
		p.setProperty("logback.configurationFile", file.toURI().toString());
		return p;
	}
	
	protected Properties allSystemProperties() {
		return mergeProperties(
			systemProperties(),
			logbackConfiguration());
	}
	
	protected Properties mergeProperties(Properties... properties) {
		Properties merged = new Properties();
		for (Properties props : properties)
			if (props != null)
				for (String key : props.stringPropertyNames())
					merged.setProperty(key, props.getProperty(key));
		return merged;
	}
	
	@OSGiLessConfiguration
	public void osgiLessConfiguration() {
		Properties props = allSystemProperties();
		for (String key : props.stringPropertyNames())
			System.setProperty(key, props.getProperty(key));
	}
	
	/* ------------- */
	/* For OSGi only */
	/* ------------- */
	
	protected CharSequence[] testDependencies() {
		return new String[]{};
	}
	
	protected String pipelineModule(String module) {
		return "org.daisy.pipeline.modules:" + module + ":?";
	}
	
	protected String brailleModule(String module) {
		return "org.daisy.pipeline.modules.braille:" + module + ":?";
	}
	
	@Configuration
	public Option[] config() {
		return _.config(
			Options.systemProperties(allSystemProperties()),
			mavenBundles(toStrings(testDependencies())));
	}
	
	// wrapped in class to avoid ClassNotFoundException
	protected static abstract class _ {
		protected static Option[] config(Option systemProperties, MavenBundleOption testDependencies) {
			try {
				return options(
					systemProperties,
					domTraversalPackage(),
					felixDeclarativeServices(),
					thisBundle(),
					junitBundles(),
					systemPackage("javax.xml.stream;version=\"1.0.1\""),
					systemPackage("com.sun.org.apache.xml.internal.resolver"),
					systemPackage("com.sun.org.apache.xml.internal.resolver.tools"),
					systemPackage("javax.xml.bind"),
					mavenBundle("org.daisy.pipeline.build:modules-test-helper:?"),
					mavenBundlesWithDependencies(
						testDependencies,
						// logging
						logbackClassic(),
						mavenBundle("org.slf4j:jcl-over-slf4j:1.7.2")) // required by httpclient (FIXME: add to runtime dependencies of calabash)
				);
			} catch (RuntimeException e) {
				e.printStackTrace();
				throw e;
			}
		}
	}
	
	protected static String[] toStrings(Object[] array) {
		String[] strings = new String[array.length];
		for (int i = 0; i < array.length; i++)
			strings[i] = array[i].toString();
		return strings;
	}
	
	protected static class MavenBundle extends ForwardingCharSequence {
		
		private final String groupId;
		private final String artifactId;
		private String classifier = "";
		
		private MavenBundle(String groupId, String artifactId) {
			this.groupId = groupId;
			this.artifactId = artifactId;
		}
		
		private MavenBundle classifier(String classifier) {
			this.classifier = classifier;
			return this;
		}
		
		public MavenBundle forThisPlatform() {
			return classifier(thisPlatform());
		}
		
		public String toString() {
			StringBuilder b = new StringBuilder()
				.append(groupId).append(":")
				.append(artifactId).append(":");
			if (!classifier.equals("")) {
				b.append("jar").append(":").append(classifier).append(":"); }
			b.append("?");
			return b.toString();
		}
	}
	
	protected static abstract class ForwardingCharSequence implements CharSequence {
		
		public abstract String toString();
		
		private CharSequence delegate;
		
		private final CharSequence delegate() {
			if (delegate == null)
				delegate = toString();
			return delegate;
		}
		
		public final char charAt(int index) {
			return delegate().charAt(index);
		}
		
		public final int length() {
			return delegate().length();
		}
		
		public final CharSequence subSequence(int start, int end) {
			return delegate().subSequence(start, end);
		}
	}
}
