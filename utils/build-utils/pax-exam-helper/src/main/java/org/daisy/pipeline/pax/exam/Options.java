package org.daisy.pipeline.pax.exam;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.jar.Manifest;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.util.PathUtils;

public abstract class Options {
	
	private static Map<String,String[]> mavenArtifacts = new HashMap<String,String[]>();
	
	static {
		mavenArtifacts.put("ch.qos.logback.classic",                      new String[]{"ch.qos.logback", "logback-classic", "1.0.11"});
		mavenArtifacts.put("ch.qos.logback.core",                         new String[]{"ch.qos.logback", "logback-core", "1.0.11"});
		mavenArtifacts.put("com.xmlcalabash",                             new String[]{"org.daisy.libs", "com.xmlcalabash"});
		mavenArtifacts.put("com.google.guava",                            new String[]{"com.google.guava", "guava"});
		mavenArtifacts.put("com.thaiopensource.jing",                     new String[]{"org.daisy.libs", "jing"});
		mavenArtifacts.put("javax.persistence",                           new String[]{"org.eclipse.persistence", "javax.persistence"});
		mavenArtifacts.put("jcl.over.slf4j",                              new String[]{"org.slf4j", "jcl-over-slf4j"});
		mavenArtifacts.put("net.sf.saxon.saxon-he",                       new String[]{"org.daisy.libs", "saxon-he"});
		mavenArtifacts.put("org.apache.commons.codec",                    new String[]{"commons-codec", "commons-codec"});
		mavenArtifacts.put("org.apache.httpcomponents.httpclient",        new String[]{"org.apache.httpcomponents", "httpclient-osgi"});
		mavenArtifacts.put("org.apache.httpcomponents.httpcore",          new String[]{"org.apache.httpcomponents", "httpcore-osgi"});
		mavenArtifacts.put("org.apache.servicemix.bundles.xmlresolver",   new String[]{"org.apache.servicemix.bundles", "org.apache.servicemix.bundles.xmlresolver"});
		mavenArtifacts.put("org.daisy.maven.xproc-api",                   new String[]{"org.daisy.maven", "xproc-engine-api"});
		mavenArtifacts.put("org.daisy.maven.xproc-engine-calabash",       new String[]{"org.daisy.maven", "xproc-engine-calabash"});
		mavenArtifacts.put("org.daisy.maven.xproc-engine-daisy-pipeline", new String[]{"org.daisy.maven", "xproc-engine-daisy-pipeline"});
		mavenArtifacts.put("org.daisy.maven.xprocspec-runner",            new String[]{"org.daisy.maven", "xprocspec-runner"});
		mavenArtifacts.put("org.daisy.maven.xspec-runner",                new String[]{"org.daisy.maven", "xspec-runner"});
		mavenArtifacts.put("org.daisy.pipeline.calabash-adapter",         new String[]{"org.daisy.pipeline", "calabash-adapter"});
		mavenArtifacts.put("org.daisy.pipeline.common-utils",             new String[]{"org.daisy.pipeline", "common-utils"});
		mavenArtifacts.put("org.daisy.pipeline.framework-core",           new String[]{"org.daisy.pipeline", "framework-core"});
		mavenArtifacts.put("org.daisy.pipeline.modules-registry",         new String[]{"org.daisy.pipeline", "modules-registry"});
		mavenArtifacts.put("org.daisy.pipeline.saxon-adapter",            new String[]{"org.daisy.pipeline", "saxon-adapter"});
		mavenArtifacts.put("org.daisy.pipeline.woodstox-osgi-adapter",    new String[]{"org.daisy.pipeline", "woodstox-osgi-adapter"});
		mavenArtifacts.put("org.daisy.pipeline.xpath-registry",           new String[]{"org.daisy.pipeline", "xpath-registry"});
		mavenArtifacts.put("org.daisy.pipeline.xmlcatalog",               new String[]{"org.daisy.pipeline", "xmlcatalog"});
		mavenArtifacts.put("org.daisy.pipeline.xproc-api",                new String[]{"org.daisy.pipeline", "xproc-api"});
		mavenArtifacts.put("org.daisy.xprocspec",                         new String[]{"org.daisy.xprocspec", "xprocspec"});
		mavenArtifacts.put("slf4j.api",                                   new String[]{"org.slf4j", "slf4j-api", "1.7.2"});
		mavenArtifacts.put("stax2-api",                                   new String[]{"org.codehaus.woodstox", "stax2-api"});
		mavenArtifacts.put("woodstox-core-lgpl",                          new String[]{"org.codehaus.woodstox", "woodstox-core-lgpl"});
	}
	
	private static List<String> wrappedBundles = Arrays.asList("org.daisy.xprocspec");
	
	private static Option getMavenArtifactForBundleId(String bundleId) {
		String[] artifact = mavenArtifacts.get(bundleId);
		if (artifact == null)
			throw new RuntimeException("Couldn't find a maven artifact for bundle " + bundleId);
		MavenArtifactProvisionOption bundle = mavenBundle().groupId(artifact[0]).artifactId(artifact[1]);
		String version = null;
		if (artifact.length == 3) {
			version = artifact[2];
			bundle = bundle.version(version); }
		if (wrappedBundles.contains(bundleId)) {
			if (version == null) {
				version = MavenUtils.asInProject().getVersion(artifact[0], artifact[1]);
				bundle.version(version); }
			return wrappedBundle(bundle).bundleSymbolicName(bundleId)
			                            .bundleVersion(version.replaceAll("-",".")); }
		else if (version == null)
			return bundle.versionAsInProject();
		else
			return bundle;
	}
	
	private static Map<String,String[]> runtimeDependencies = new HashMap<String,String[]>();
	
	static {
		runtimeDependencies.put("ch.qos.logback.classic",                      new String[]{"ch.qos.logback.core",
		                                                                                    "slf4j.api"});
		runtimeDependencies.put("ch.qos.logback.core",                         new String[]{"slf4j.api"});
		runtimeDependencies.put("com.xmlcalabash",                             new String[]{"net.sf.saxon.saxon-he",
		                                                                                    "org.apache.httpcomponents.httpclient"});
		runtimeDependencies.put("jcl.over.slf4j",                              new String[]{"slf4j.api"});
		runtimeDependencies.put("org.apache.httpcomponents.httpclient",        new String[]{"org.apache.commons.codec",
		                                                                                    "org.apache.httpcomponents.httpcore",
		                                                                                    "jcl.over.slf4j"});
		runtimeDependencies.put("org.daisy.maven.xproc-engine-calabash",       new String[]{"net.sf.saxon.saxon-he",
		                                                                                    "org.daisy.maven.xproc-api",
		                                                                                    "com.xmlcalabash"});
		runtimeDependencies.put("org.daisy.maven.xproc-engine-daisy-pipeline", new String[]{"org.daisy.maven.xproc-api",
		                                                                                    "org.daisy.pipeline.calabash-adapter", // org.daisy.common.xproc.XProcEngine (1..1)
		                                                                                    "org.daisy.pipeline.common-utils",
		                                                                                    "org.daisy.pipeline.xproc-api"});
		runtimeDependencies.put("org.daisy.maven.xprocspec-runner",            new String[]{"com.google.guava",
		                                                                                    "net.sf.saxon.saxon-he",
		                                                                                    "org.daisy.maven.xproc-api",
		                                                                                    "org.daisy.maven.xproc-engine-daisy-pipeline", // org.daisy.maven.xproc.api.XProcEngine (1..1)
		                                                                                    "org.daisy.xprocspec"});
		runtimeDependencies.put("org.daisy.maven.xspec-runner",                new String[]{"com.google.guava",
		                                                                                    "net.sf.saxon.saxon-he",
		                                                                                    "org.apache.servicemix.bundles.xmlresolver",
		                                                                                    "org.daisy.pipeline.saxon-adapter"});
		runtimeDependencies.put("org.daisy.pipeline.calabash-adapter",         new String[]{"com.google.guava",
		                                                                                    "com.xmlcalabash",
		                                                                                    "net.sf.saxon.saxon-he",
		                                                                                    "org.daisy.pipeline.common-utils",
		                                                                                    "org.daisy.pipeline.framework-core",
		                                                                                    "org.daisy.pipeline.modules-registry", // javax.xml.transform.URIResolver (1..1)
		                                                                                    "org.daisy.pipeline.xpath-registry",
		                                                                                    "org.daisy.pipeline.xproc-api"});
		runtimeDependencies.put("org.daisy.pipeline.common-utils",             new String[]{"com.google.guava",
		                                                                                    "com.xmlcalabash",
		                                                                                    "javax.persistence",
		                                                                                    "slf4j.api"});
		runtimeDependencies.put("org.daisy.pipeline.framework-core",           new String[]{"com.google.guava",
		                                                                                    "org.daisy.pipeline.common-utils",
		                                                                                    "org.daisy.pipeline.woodstox-osgi-adapter", // javax.xml.stream.XMLInputFactory (1..1)
		                                                                                    "org.daisy.pipeline.xproc-api",
		                                                                                    "slf4j.api"});
		runtimeDependencies.put("org.daisy.pipeline.modules-registry",         new String[]{"org.daisy.pipeline.xmlcatalog",
		                                                                                    "slf4j.api"});
		runtimeDependencies.put("org.daisy.pipeline.saxon-adapter",            new String[]{"net.sf.saxon.saxon-he",
		                                                                                    "org.daisy.pipeline.modules-registry", // javax.xml.transform.URIResolver (0..1)
		                                                                                    "slf4j.api"});
		runtimeDependencies.put("org.daisy.pipeline.woodstox-osgi-adapter",    new String[]{"woodstox-core-lgpl"});
		runtimeDependencies.put("org.daisy.pipeline.xpath-registry",           new String[]{"net.sf.saxon.saxon-he",
		                                                                                    "slf4j.api"});
		runtimeDependencies.put("org.daisy.pipeline.xmlcatalog",               new String[]{"com.google.guava",
		                                                                                    "org.daisy.pipeline.common-utils",
		                                                                                    "org.daisy.pipeline.woodstox-osgi-adapter", // javax.xml.stream.XMLInputFactory (1..1)
		                                                                                    "slf4j.api"});
		runtimeDependencies.put("org.daisy.pipeline.xproc-api",                new String[]{"com.google.guava",
		                                                                                    "org.daisy.pipeline.common-utils"});
		runtimeDependencies.put("woodstox-core-lgpl",                          new String[]{"stax2-api"});
	}
	
	private static Set<String> getRuntimeDependencies(String bundleId) {
		Set<String> dependencies = new HashSet<String>();
		if (runtimeDependencies.containsKey(bundleId))
			for (String dependency : runtimeDependencies.get(bundleId)) {
				dependencies.add(dependency);
				dependencies.addAll(getRuntimeDependencies(dependency)); }
		return dependencies;
	}
	
	public static Option bundlesAndDependencies(String... bundles) {
		Set<String> bundlesAndDependencies = new HashSet<String>();
		for (String bundle : bundles) {
			bundlesAndDependencies.add(bundle);
			bundlesAndDependencies.addAll(getRuntimeDependencies(bundle)); }
		Option[] options = new Option[bundlesAndDependencies.size()];
		int i = 0;
		for (String id : bundlesAndDependencies)
			options[i++] = getMavenArtifactForBundleId(id);
		return composite(options);
	}
	
	public static SystemPropertyOption logbackConfigFile() {
		return systemProperty("logback.configurationFile").value("file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml");
	}
	
	public static Option calabashConfigFile() {
		return composite(
			systemProperty("org.daisy.pipeline.xproc.configuration").value(PathUtils.getBaseDir() + "/src/test/resources/config-calabash.xml"),
			systemProperty("com.xmlcalabash.config.user").value("")
		);
	}
	
	public static SystemPackageOption domTraversalPackage() {
		return systemPackage("org.w3c.dom.traversal;uses:=\"org.w3c.dom\";version=\"0.0.0.1\"");
	}
	
	public static Option logbackBundles() {
		return bundlesAndDependencies("ch.qos.logback.classic");
	}
	
	public static MavenArtifactProvisionOption felixDeclarativeServices() {
		return mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.scr").version("1.6.2");
	}
	
	public static Option spiflyBundles() {
		return composite(
			mavenBundle().groupId("org.ow2.asm").artifactId("asm-all").version("4.0"),
			mavenBundle().groupId("org.apache.aries").artifactId("org.apache.aries.util").version("1.0.0"),
			mavenBundle().groupId("org.apache.aries.spifly").artifactId("org.apache.aries.spifly.dynamic.bundle").version("1.0.0")
		);
	}
	
	public static MavenArtifactProvisionOption pipelineModule(String artifactId) {
		return mavenBundle().groupId("org.daisy.pipeline.modules").artifactId(artifactId).versionAsInProject();
	}
	
	public static MavenArtifactProvisionOption brailleModule(String artifactId) {
		return mavenBundle().groupId("org.daisy.pipeline.modules.braille").artifactId(artifactId).versionAsInProject();
	}
	
	public static UrlProvisionOption thisBundle() {
		File classes = new File(PathUtils.getBaseDir() + "/target/classes");
		Manifest manifest;
		try {
			manifest = new Manifest(new File(classes, "META-INF/MANIFEST.MF").toURI().toURL().openStream()); }
		catch (IOException e) {
			throw new RuntimeException(e); }
		String components = manifest.getMainAttributes().getValue("Service-Component");
		if (components != null)
			for (String component : components.split(","))
				if (!(new File(classes, component)).exists())
					return bundle("reference:"
					              + (new File(PathUtils.getBaseDir() + "/target/")).listFiles(
					                  new FilenameFilter() {
					                      public boolean accept(File dir, String name) {
					                          return name.endsWith(".jar"); }}
						              )[0].toURI());
		return bundle("reference:" + classes.toURI());
	}
	
	public static MavenArtifactProvisionOption forThisPlatform(MavenArtifactProvisionOption bundle) {
		String name = System.getProperty("os.name").toLowerCase();
		if (name.startsWith("windows"))
			return bundle.classifier("windows");
		else if (name.startsWith("mac os x"))
			return bundle.classifier("mac");
		else if (name.startsWith("linux"))
			return bundle.classifier("linux");
		else
			throw new RuntimeException("Unsupported OS: " + name);
	}
	
	public static Option xprocspecBundles() {
		return bundlesAndDependencies("org.daisy.maven.xprocspec-runner");
	}
	
	public static Option xspecBundles() {
		return bundlesAndDependencies("org.daisy.maven.xspec-runner");
	}
}
