package org.daisy.pipeline.pax.exam;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import static java.util.Collections.sort;
import java.util.HashSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.apache.maven.wagon.providers.http.HttpWagon;
import org.apache.maven.wagon.Wagon;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.AbstractProvisionOption;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.MavenUrlReference.VersionResolver;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.util.PathUtils;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Options {
	
	private static final Logger logger = LoggerFactory.getLogger(Options.class);
	
	private static final File DEFAULT_LOCAL_REPOSITORY = new File(System.getProperty("user.home"), ".m2/repository");
	
	public static SystemPropertyOption logbackConfigFile() {
		return systemProperty("logback.configurationFile").value("file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml");
	}
	
	public static Option calabashConfigFile() {
		File configFile = new File(PathUtils.getBaseDir() + "/src/test/resources/config-calabash.xml");
		return composite(
			configFile.exists()
				? systemProperty("org.daisy.pipeline.xproc.configuration").value(configFile.toString())
				: null,
			systemProperty("com.xmlcalabash.config.user").value("")
		);
	}
	
	public static SystemPackageOption domTraversalPackage() {
		return systemPackage("org.w3c.dom.traversal;uses:=\"org.w3c.dom\";version=\"0.0.0.1\"");
	}
	
	public static MavenBundle felixDeclarativeServices() {
		return mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.scr").version("1.6.2");
	}
	
	public static Option spiflyBundles() {
		return composite(
			mavenBundle().groupId("org.ow2.asm").artifactId("asm-all").version("5.0"),
			mavenBundle().groupId("org.apache.aries").artifactId("org.apache.aries.util").version("1.0.0"),
			mavenBundle().groupId("org.apache.aries.spifly").artifactId("org.apache.aries.spifly.dynamic.bundle").version("1.0.2")
		);
	}
	
	public static MavenBundle logbackClassic() {
		return mavenBundle("ch.qos.logback:logback-classic:1.0.11");
	}
	
	public static MavenBundleOption xprocspec() {
		return mavenBundles(
			mavenBundle("org.daisy.maven:xprocspec-runner:?"),
			mavenBundle("org.daisy.xprocspec:xprocspec:?")
		);
	}
	
	public static MavenBundle xspec() {
		return mavenBundle("org.daisy.maven:xspec-runner:?");
	}
	
	public static UrlProvisionOption thisBundle() {
		File classes = new File(PathUtils.getBaseDir() + "/target/classes");
		Manifest manifest;
		try {
			InputStream stream = new File(classes, "META-INF/MANIFEST.MF").toURI().toURL().openStream();
			try {
				manifest = new Manifest(stream); }
			finally {
				stream.close(); }}
		catch (IOException e) {
			throw new RuntimeException(e); }
		String components = manifest.getMainAttributes().getValue("Service-Component");
		if (components != null)
			for (String component : components.split(","))
				if (!(new File(classes, component)).exists()) {
					Properties dependencies = new Properties();
					try {
						dependencies.load(new FileInputStream(new File(classes, "META-INF/maven/dependencies.properties"))); }
					catch (IOException e) {
						throw new RuntimeException(e); }
					String artifactId = dependencies.getProperty("artifactId");
					String version = dependencies.getProperty("version");
					
					// assuming JAR is named ${artifactId}-${version}.jar
					return bundle("reference:" + new File(PathUtils.getBaseDir() + "/target/" + artifactId + "-" + version + ".jar").toURI()); }
		return bundle("reference:" + classes.toURI());
	}
	
	public static MavenBundle pipelineModule(String artifactId) {
		return mavenBundle().groupId("org.daisy.pipeline.modules").artifactId(artifactId);
	}
	
	public static MavenBundle brailleModule(String artifactId) {
		return mavenBundle().groupId("org.daisy.pipeline.modules.braille").artifactId(artifactId);
	}
	
	public static MavenBundle mavenBundle() {
		return new MavenBundle();
	}
	
	/**
	 * @param artifactCoords must be a string of the form
	 *    <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>. The default <extension> is
	 *    "jar". When <version> is "?", the version as declared in the project is used.
	 */
	public static MavenBundle mavenBundle(String artifactCoords) {
		return new MavenBundle(artifactFromCoords(artifactCoords));
	}
	
	public static interface MavenBundleOption extends Option {
		public MavenBundle[] getBundles();
	}
	
	public static class MavenBundle extends AbstractProvisionOption<MavenBundle> implements MavenBundleOption {
		
		private boolean versionAsInProject = true;
		
		private MavenBundle() {}
		
		private MavenBundle(Artifact artifact) {
			this(artifact, false);
		}
		
		private MavenBundle(Artifact artifact, boolean forceVersionAsInProject) {
			groupId(artifact.getGroupId());
			artifactId(artifact.getArtifactId());
			type(artifact.getExtension());
			classifier(artifact.getClassifier());
			if (!forceVersionAsInProject)
				version(artifact.getVersion());
		}
		
		private String url = null;
		
		public String getURL() {
			if (url == null) {
				MavenArtifactProvisionOption bundle = new MavenArtifactProvisionOption();
				bundle.groupId(groupId);
				bundle.artifactId(artifactId);
				if (type != null)
					bundle.type(type);
				if (classifier != null && !"".equals(classifier))
					bundle.classifier(classifier);
				if (versionAsInProject)
					try {
						version = MavenUtils.asInProject().getVersion(groupId, artifactId); }
					catch (Throwable e) {
						logger.error("Could not find version of " + groupId + ":" + artifactId + " in Maven project");
						throw new RuntimeException("Could not find version of " + groupId + ":" + artifactId + " in Maven project"); }
				bundle.version(version);
				if (startLevel > 0) {
					bundle.startLevel(startLevel);
				}
				// special handling of xprocspec
				if (groupId.equals("org.daisy.xprocspec") && artifactId.equals("xprocspec"))
					url = wrappedBundle(bundle)
						.bundleSymbolicName("org.daisy.xprocspec")
						.bundleVersion(version.replaceAll("-","."))
						.getURL();
				else
					url = bundle.getURL(); }
			return url;
		}
		
		public MavenBundle[] getBundles() {
			return new MavenBundle[]{this};
		}
			
		public MavenBundle itself() {
			return this;
		}
		
		private String groupId = null;
		private String artifactId = null;
		private String type = "jar";
		private String classifier = "";
		private String version = null;
		
		public MavenBundle groupId(String groupId) {
			checkURLResolved();
			this.groupId = groupId;
			return this;
		}
		
		public MavenBundle artifactId(String artifactId) {
			checkURLResolved();
			this.artifactId = artifactId;
			return this;
		}
		
		public MavenBundle type(String type) {
			checkURLResolved();
			this.type = type;
			return this;
		}
		
		public MavenBundle classifier(String classifier) {
			checkURLResolved();
			this.classifier = classifier;
			return this;
		}
		
		public MavenBundle forThisPlatform() {
			return classifier(thisPlatform());
		}
		
		public MavenBundle version(String version) {
			checkURLResolved();
			if (version == null || version.equals("?"))
				versionAsInProject = true;
			else {
				this.version = version;
				versionAsInProject = false; }
			return this;
		}
		
		private int startLevel = -1;
		
		public MavenBundle startLevel(int level) {
			if (level <= 0) {
				throw new IllegalArgumentException("start level must be > 0");
			}
			this.startLevel = level;
			return this;
		}
		
		public MavenBundle versionAsInProject() {
			return version("?");
		}
		
		private Artifact asArtifact() {
			getURL();
			return new DefaultArtifact(groupId, artifactId, classifier, type, version);
		}
		
		private void checkURLResolved() {
			if (url != null)
				throw new RuntimeException();
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("mavenBundle(\"").append(artifactCoords(asArtifact()));
			if (startLevel > 0)
				sb.append("(start@").append(startLevel).append(")");
			sb.append("\")");
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
	
		@Override
		public boolean equals(Object object) {
			if (this == object)
				return true;
			if (object == null)
				return false;
			if (getClass() != object.getClass())
				return false;
			MavenBundle that = (MavenBundle)object;
			return that.toString().equals(toString());
		}
	}
	
	public static MavenBundleOption mavenBundles(final MavenBundleOption... options) {
		final MavenBundle[] bundles; {
			List<MavenBundle> list = new ArrayList<MavenBundle>();
			for (MavenBundleOption o : options)
				if (o != null)
					for (MavenBundle b : o.getBundles())
						list.add(b);
			bundles = list.toArray(new MavenBundle[list.size()]); }
		return new MavenBundleCompositeOption() {
			public MavenBundle[] getBundles() {
				return bundles;
			}
			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder();
				sb.append("compositeMavenBundleOption(");
				int i = 0;
				for (MavenBundleOption b : bundles) {
					if (i > 0) sb.append(",");
					sb.append("\n	").append(b);
					i++; }
				sb.append(")");
				return sb.toString();
			}
		};
	}
	
	public static MavenBundleOption mavenBundles(final String... artifactCoords) {
		MavenBundle[] bundles = new MavenBundle[artifactCoords.length];
		for (int i = 0; i < artifactCoords.length; i++)
			bundles[i] = artifactCoords == null ? null : mavenBundle(artifactCoords[i]);
		return mavenBundles(bundles);
	}
	
	private static abstract class MavenBundleCompositeOption implements MavenBundleOption, CompositeOption {
		public MavenBundle[] getOptions() {
			return getBundles();
		}
	}
	
	public static MavenBundleOption mavenBundlesWithDependencies(MavenBundleOption... options) {
		return new MavenBundlesWithDependencies(options);
	}
	
	private static class MavenBundlesWithDependencies extends MavenBundleCompositeOption {
		
		private final List<MavenBundle> fromBundles;
		
		private MavenBundlesWithDependencies(MavenBundleOption... options) {
			fromBundles = new ArrayList<MavenBundle>();
			for (MavenBundleOption o : options) {
				if (o == null) continue;
				for (MavenBundle b : o.getBundles())
					fromBundles.add(b); }
			logger.info(this.toString());
			StringBuilder sb = new StringBuilder();
			List<String> bundlesAsStrings = new ArrayList<String>();
			for (MavenBundle b : getBundles())
				bundlesAsStrings.add(b.toString());
			sort(bundlesAsStrings);
			sb.append("resolved to: MavenBundle[]{");
			int i = 0;
			for (String b : bundlesAsStrings) {
				if (i > 0) sb.append(",");
				sb.append("\n	").append(b);
				i++; }
			sb.append("}");
			logger.info(sb.toString());
		}
		
		private MavenBundle[] bundles = null;
		
		public MavenBundle[] getBundles() {
			try {
				if (bundles == null) {
					Set<MavenBundle> set = resolveBundles(fromBundles);
					bundles = set.toArray(new MavenBundle[set.size()]); }
				return bundles; }
			catch (RuntimeException e) {
				e.printStackTrace();
				throw e; }
		}
		
		private static Set<MavenBundle> resolveBundles(List<MavenBundle> fromBundles) {
			File settingsFile; {
				// For now don't use "org.ops4j.pax.url.mvn.settings" because Pax Exam itself does not support
				// system properties inside a settings file.
				String prop = System.getProperty("org.daisy.org.ops4j.pax.url.mvn.settings");
				if (prop == null)
					// Also support "org.ops4j.pax.url.mvn.settings"
					// Assume this is set only when no system properties are used in the settings file
					prop = System.getProperty("org.ops4j.pax.url.mvn.settings");
				// Also support environment variables for more flexibility
				if (prop == null)
					prop = System.getenv().get("org.daisy.org.ops4j.pax.url.mvn.settings");
				if (prop == null) {
					prop = System.getenv().get("org.ops4j.pax.url.mvn.settings");
					if (prop != null)
						System.setProperty("org.ops4j.pax.url.mvn.settings", prop); }
				if (prop != null)
					settingsFile = new File(prop);
				else
					settingsFile = new File(System.getProperty("user.home"), ".m2/settings.xml"); }
			File localRepository;
			List<RemoteRepository> repositories; {
				localRepository = null;
				repositories = new Vector<RemoteRepository>();
				boolean centralRedefined = false;
				String localRepositoryProp = System.getProperty("org.ops4j.pax.url.mvn.localRepository");
				if (localRepositoryProp != null)
					localRepository = new File(localRepositoryProp);
				if (settingsFile.exists()) {
					Settings settings; {
						SettingsBuilder b = new DefaultSettingsBuilderFactory().newInstance();
						SettingsBuildingRequest req = new DefaultSettingsBuildingRequest();
						req.setGlobalSettingsFile(settingsFile);
						req.setSystemProperties(System.getProperties());
						try {
							SettingsBuildingResult res = b.build(req);
							settings = res.getEffectiveSettings(); }
						catch (SettingsBuildingException e) {
							throw new RuntimeException(e); }}
					if (localRepository == null)
						if (settings.getLocalRepository() != null)
							localRepository = new File(settings.getLocalRepository());
					for (Profile profile : settings.getProfiles()) {
						String profileName = profile.getId();
						if ((settings.getActiveProfiles() != null && settings.getActiveProfiles().contains(profileName))
						    || (profile.getActivation() != null && profile.getActivation().isActiveByDefault())) {
							for (Repository repo : profile.getRepositories()) {
								RemoteRepository remoteRepo; {
									remoteRepo = new RemoteRepository(repo.getId(), "default", repo.getUrl());
									remoteRepo.setPolicy(false,
										repo.getReleases() != null ?
											new RepositoryPolicy(
												repo.getReleases().isEnabled(),
												repo.getReleases().getUpdatePolicy(),
												repo.getReleases().getChecksumPolicy()) :
											new RepositoryPolicy());
									remoteRepo.setPolicy(true,
										repo.getSnapshots() != null ?
											new RepositoryPolicy(
												repo.getSnapshots().isEnabled(),
												repo.getSnapshots().getUpdatePolicy(),
												repo.getSnapshots().getChecksumPolicy()) :
											new RepositoryPolicy()); }
								if ("central".equals(repo.getId()))
									centralRedefined = true;
								repositories.add(remoteRepo); }}}}
				if (localRepository == null)
					localRepository = DEFAULT_LOCAL_REPOSITORY;
				if (!centralRedefined)
					repositories.add(new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/")); }
			CollectRequest request = new CollectRequest();
			for (MavenBundle bundle : fromBundles) {
				request.addDependency(new Dependency(bundle.asArtifact(), "runtime")); }
			for (RemoteRepository r : repositories)
				request.addRepository(r);
			request.setRequestContext("runtime");
			DefaultServiceLocator locator = new DefaultServiceLocator();
			locator.addService(WagonProvider.class, HttpAndFileWagonProvider.class);
			locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
			RepositorySystem system = locator.getService(RepositorySystem.class);
			DefaultRepositorySystemSession session = new MavenRepositorySystemSession()
				.setLocalRepositoryManager(
					system.newLocalRepositoryManager(
						new LocalRepository(localRepository.getAbsolutePath())))
				.setOffline(false);
			Set<MavenBundle> bundles = new HashSet<MavenBundle>();
			DependencyNode root; {
				try {
					root = system.resolveDependencies(session, new DependencyRequest().setCollectRequest(request)).getRoot(); }
				catch (DependencyResolutionException e) {
					throw new RuntimeException(e); }}
			int startLevel = 4;
			for (DependencyNode n : root.getChildren())
				if (!dependenciesAsBundles(bundles, n, false, fromBundles, null, startLevel++))
					return resolveBundles(fromBundles);
			return bundles;
		}
		
		private static boolean dependenciesAsBundles(Set<MavenBundle> bundles, DependencyNode node, boolean versionAsInProject,
		                                             List<MavenBundle> fromBundles, Artifact parent, int startLevel) {
			Dependency dep = node.getDependency();
			if (dep == null)
				throw new RuntimeException("Coding error");
			Artifact a = dep.getArtifact();
			String groupId = a.getGroupId();
			String artifactId = a.getArtifactId();
			String type = a.getExtension();
			String classifier = a.getClassifier();
			try {
				if (// these should not be runtime dependencies -> fix in POMs
					!(groupId.equals("org.osgi") && (artifactId.equals("org.osgi.compendium") || artifactId.equals("org.osgi.core")))) {
					if ((classifier.equals("linux") || classifier.equals("mac") || classifier.equals("windows"))
					    && !classifier.equals(thisPlatform()));
					else {
						boolean noStart = false;
						if (!(groupId.equals("org.daisy.xprocspec") && artifactId.equals("xprocspec")))
							noStart = validateBundleAndIsFragmentBundle(a.getFile());
						for (MavenBundle b : fromBundles)
							if (b.groupId.equals(groupId)
							    && b.artifactId.equals(artifactId)
							    && b.type.equals(type)
							    && b.classifier.equals(classifier)) {
								if (b.versionAsInProject && !a.getBaseVersion().equals(b.version))
									throw new RuntimeException("Coding error");
								versionAsInProject = b.versionAsInProject;
								if (b.startLevel > 0)
									startLevel = b.startLevel;
								break; }
						if (versionAsInProject) {
							String versionInProject; {
								versionInProject = null;
								try {
									versionInProject = MavenUtils.asInProject().getVersion(groupId, artifactId); }
								catch (RuntimeException e) {
									logger.info("Can not find version of transitive dependency " + groupId + ":" + artifactId
									            + " in project. Assuming it was explicitly excluded, therefore ignoring it.");
									return true; }}
							if (!a.getBaseVersion().equals(versionInProject)) {
								MavenBundle b = new MavenBundle(a, true);
								logger.info("Forcing transitive dependency \"" + artifactCoords(b.asArtifact()) + "\""
								            + " (version as in project) because it would otherwise resolve to version \""
								            + a.getBaseVersion() + "\" (via \"" + artifactCoords(parent) + "\")");
								b.startLevel(startLevel);
								fromBundles.add(b);
								return false; }}
						MavenBundle b = new MavenBundle(a);
						if (noStart)
							b.noStart();
						else
							b.startLevel(startLevel);
						bundles.add(b); }}}
			catch (InvalidBundleException e) {
				logger.info("Ignoring dependency " + groupId + ":" + artifactId + ": not a valid bundle."); }
			for (DependencyNode n : node.getChildren())
				if (!dependenciesAsBundles(bundles, n, versionAsInProject, fromBundles, a, startLevel))
					return false;
			return true;
		}
		
		@Override
			public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("mavenBundlesWithDependencies(");
			int i = 0;
			for (MavenBundle b : fromBundles) {
				if (i > 0) sb.append(",");
				sb.append("\n	").append(b);
				i++; }
			sb.append(")");
			return sb.toString();
		}
		
		// throw exception if bundle is not valid, and return true if it is a fragment bundle
		private static boolean validateBundleAndIsFragmentBundle(File bundle) throws InvalidBundleException {
			JarFile jar = null;
			try {
				jar = new JarFile(bundle, false);
				Manifest manifest = jar.getManifest();
				if (manifest == null)
					throw new RuntimeException("[" + bundle + "] is not a valid bundle: manifest is missing");
				Attributes mainAttrs = manifest.getMainAttributes();
				String bundleSymbolicName = mainAttrs.getValue("Bundle-SymbolicName");
				String bundleName = mainAttrs.getValue("Bundle-Name");
				if (bundleSymbolicName == null && bundleName == null)
					throw new InvalidBundleException("[" + bundle + "] is not a valid bundle: Bundle-SymbolicName and Bundle-Name are missing");
				return (mainAttrs.getValue("Fragment-Host") != null); }
			catch (IOException e) {
				throw new RuntimeException("[" + bundle + "] is not a valid bundle: failed reading jar", e); }
			finally {
				if (jar != null)
					try {
						jar.close(); }
					catch (IOException e) {}}
		}
		
		public static class HttpAndFileWagonProvider implements WagonProvider {
			public Wagon lookup(String roleHint) throws Exception {
				if ("http".equals(roleHint) || "https".equals(roleHint))
					return new HttpWagon();
				else if ("file".equals(roleHint))
					return new FileWagon();
				return null;
			}
			public void release(Wagon wagon) {}
		}
		
		private static class InvalidBundleException extends Exception {
			InvalidBundleException(String message) {
				super(message);
			}
		}
	}
	
	private static Artifact artifactFromCoords(String coords) {
		return new DefaultArtifact(coords);
	}
	
	private static String artifactCoords(Artifact artifact) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String extension = artifact.getExtension();
		String classifier = artifact.getClassifier();
		String version = artifact.getVersion();
		StringBuilder b = new StringBuilder()
			.append(groupId).append(":")
			.append(artifactId).append(":");
		if (!extension.equals("jar") || !classifier.equals("")) {
			b.append(extension).append(":");
			if (!classifier.equals(""))
				b.append(classifier).append(":"); }
		b.append(version);
		return b.toString();
	}
	
	public static String thisPlatform() {
		String name = System.getProperty("os.name").toLowerCase();
		if (name.startsWith("windows"))
			return "windows";
		else if (name.startsWith("mac os x"))
			return "mac";
		else if (name.startsWith("linux"))
			return "linux";
		else
			throw new RuntimeException("Unsupported OS: " + name);
	}
}
