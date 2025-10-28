package build;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import static lib.util.*;

import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;

public class core {
	private core() {}

	public static void mvn(String... args) throws IOException, InterruptedException {
		mvn.mvn(null, args);
	}

	public static void mvn(File cd, String... args) throws IOException, InterruptedException {
		mvn.mvn(cd, args);
	}

	public static void gradle(String... args) throws IOException, InterruptedException {
		gradle.gradlew(null, args);
	}

	public static void redirectTo(String file) throws IOException {
		cp(System.in, new FileOutputStream(file));
	}

	public static boolean isOutOfDate(String target, String... prerequisites) {
		File[] prerequisiteFiles = new File[prerequisites.length];
		for (int i = 0; i < prerequisites.length; i++)
			prerequisiteFiles[i] = new File(prerequisites[i]);
		return isOutOfDate(new File(target), prerequisiteFiles);
	}

	public static boolean isOutOfDate(File target, File... prerequisites) {
		if (!target.exists())
			return true;
		for (File f : prerequisites)
			if (f.lastModified() > target.lastModified())
				return true;
		return false;
	}

	public static OutputStream updateFileIfChanged(String file) throws IOException {
		return updateFileIfChanged(new File(file));
	}

	public static OutputStream updateFileIfChanged(File file) throws IOException {
		if (!file.exists()) {
			mkdirs(file.getParentFile());
			return new FileOutputStream(file);
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		return new FilterOutputStream(buffer) {
			@Override
			public void close() throws IOException {
				super.close();
				byte[] newContent = buffer.toByteArray();
				boolean changed = false; {
					try (InputStream oldContent = new FileInputStream(file)) {
						int len = newContent.length;
						byte[] block = new byte[1024];
						int read = 0;
						int totalRead = 0;
						while (true) {
							read = oldContent.read(block);
							if (read < 0) {
								changed = (totalRead != len);
								break;
							}
							if (totalRead + read > len) {
								changed = true;
								break;
							}
							for (int i = 0; i < read; i++)
								if (block[i] != newContent[totalRead + i]) {
									changed = true;
									break;
								}
							if (changed)
								break;
							totalRead += read;
						}
					}
				}
				if (changed)
					try (OutputStream s = new FileOutputStream(file)) {
						s.write(newContent);
					}
			}
		};
	}

	public static void traverseModules(File rootDir, BiConsumer<File,Boolean> collector) throws XPathExpressionException {
		new Consumer<File>() {
			public void accept(File module) {
				try {
					String[] submodules = xpath(
						new File(module, "pom.xml"), "string(/*/*[local-name()='modules'])"
					).trim().split("\\s+");
					if (submodules.length > 0 && !(submodules.length == 1 && submodules[0].equals(""))) {
						collector.accept(module, true);
						for (String m : submodules)
							accept(".".equals(module.toString()) ? new File(m) : new File(module, m));
					} else
						collector.accept(module, false);
				} catch (RuntimeException e) {
					throw e;
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
		}.accept(rootDir);
	}

	public static mvn.Coords getMavenCoords(File dirOrFile) throws IOException, XPathExpressionException {
		File gradleBuildFile = null;
		File pomFile = null;
		File dir; {
			if (dirOrFile.isDirectory()) {
				dir = dirOrFile;
				gradleBuildFile = new File(dir, "build.gradle");
				if (gradleBuildFile.exists())
					;
				else {
					gradleBuildFile = null;
					pomFile = new File(dir, "pom.xml");
					if (!pomFile.exists())
						throw new IllegalArgumentException();
				}
			} else if (dirOrFile.exists()) {
				if (dirOrFile.getName().endsWith(".xml"))
					pomFile = dirOrFile;
				else if (dirOrFile.getName().equals("build.gradle"))
					gradleBuildFile = dirOrFile;
				else
					throw new IllegalArgumentException("No pom.xml or build.gradle in directory: " + dirOrFile);
				dir = dirOrFile.getParentFile();
			} else
				throw new IllegalArgumentException("No such file or directory: " + dirOrFile);
		}
		if (pomFile != null) {
			String v = xpath(pomFile, "/*/*[local-name()='version']/text()");
			String g = xpath(pomFile, "/*/*[local-name()='groupId']/text()");
			if ("".equals(g))
				g = xpath(pomFile, "/*/*[local-name()='parent']/*[local-name()='groupId']/text()");
			String a = xpath(pomFile, "/*/*[local-name()='artifactId']/text()");
			return new mvn.Coords(g, a ,v);
		} else {
			File propertiesFile = new File(dir, "gradle.properties");
			File settingsFile = new File(dir, "settings.gradle");
			String g = egrep("^group", gradleBuildFile).map(x -> x.replaceAll("^.*=\\s*['\"](.*)['\"].*$", "$1"))
			                                           .findFirst().get();
			String a = dir.getName();
			if (settingsFile.exists())
				a = egrep("^rootProject\\.name", settingsFile).map(x -> x.replaceAll("^.*=\\s*['\"](.*)['\"].*$", "$1"))
				                                              .findFirst().orElse(a);
			String v = egrep("^distVersion", propertiesFile).map(x -> x.replaceAll("^.*=\\s*", ""))
			                                                .findFirst().orElse(null);
			if (v == null)
				v = egrep("^version", propertiesFile).map(x -> x.replaceAll("^.*=\\s*", ""))
				                                     .findFirst().get();
			return new mvn.Coords(g, a ,v);
		}
	}

	private static final PrintStream nullOutputStream = new PrintStream(new OutputStream() { public void write(int b) {}});

	public static int xslt(File source, OutputStream destination, File stylesheet, Object... parameters) {
		if (!source.exists())
			throw new IllegalArgumentException("Source file " + source + " does not exist");
		if (!stylesheet.exists())
			throw new IllegalArgumentException("Stylesheet file " + stylesheet + " does not exist");
		try {
			Processor processor = new Processor(false);
			processor.getUnderlyingConfiguration().getDefaultXsltCompilerInfo().setSchemaAware(false);
			XsltCompiler compiler = processor.newXsltCompiler();
			Map<QName,XdmValue> paramMap = new HashMap<>(); {
				String param = null;
				for (Object p : parameters)
					if (param == null)
						if (p == null)
							throw new IllegalArgumentException("parameter key must not be null");
						else if (!(p instanceof String))
							throw new IllegalArgumentException("parameter key must be a string");
						else
							param = (String)p;
					else {
						XdmValue value; {
							if (p == null)
								value = XdmValue.wrap(EmptySequence.getInstance());
							else if (p instanceof String)
								value = new XdmAtomicValue((String)p, ItemType.UNTYPED_ATOMIC);
							else if (p instanceof Boolean)
								value = new XdmAtomicValue((Boolean)p);
							else if (p instanceof Iterable) {
								List<Item> seq = new ArrayList<>();
								for (Object o : (Iterable)p)
									if (o instanceof String)
										seq.add(new StringValue((String)o));
									else
										throw new IllegalArgumentException(
											"parameter values must be strings, booleans or maps");
								value = XdmValue.wrap(new SequenceExtent(seq));
							} else if (p instanceof Map) {
								MapItem mapItem = new HashTrieMap();
								for (Map.Entry e : ((Map<?,?>)p).entrySet()) {
									if (!(e.getKey() instanceof String))
										throw new IllegalArgumentException("map keys must be strings");
									if (!(e.getValue() instanceof String))
										throw new IllegalArgumentException("map values must be strings");
									mapItem = mapItem.addEntry(
										new StringValue((String)e.getKey()),
										new StringValue((String)e.getValue()));
								}
								value = XdmValue.wrap(mapItem);
							} else
								throw new IllegalArgumentException(
									"parameter values must be strings, booleans or maps");
						}
						paramMap.put(QName.fromClarkName(param), value);
						param = null; }
				if (param != null)
					throw new IllegalArgumentException("no value specified for parameter " + param);
			}
			for (Map.Entry<QName,XdmValue> e : paramMap.entrySet())
				compiler.setParameter(e.getKey(), e.getValue());
			XsltExecutable exec = compiler.compile(new StreamSource(stylesheet.toURI().toString()));
			boolean xslt3 = true;
			if (xslt3) {
				Xslt30Transformer transformer = exec.load30();
				transformer.setStylesheetParameters(paramMap);
				XdmValue result = transformer.applyTemplates(new StreamSource(source.toURI().toString()));
				if (destination != null)
					transformer.newSerializer(new PrintStream(destination)).serializeXdmValue(result);
			} else {
				XsltTransformer transformer = exec.load();
				transformer.setSource(new StreamSource(source.toURI().toString()));
				Serializer serializer = processor.newSerializer();
				serializer.setOutputStream(destination != null ? destination : nullOutputStream);
				transformer.setDestination(serializer);
				transformer.transform();
			}
			return 0;
		} catch (Throwable e) {
			e.printStackTrace();
			return 1;
		}
	}

	public enum ModificationType {
		PATCH,
		MINOR,
		MAJOR;
		@Override
		public String toString() {
			return name().toLowerCase();
		}
		public ModificationType and(ModificationType other) {
			if (other == null)
				return this;
			switch (this) {
			case PATCH:
				return other;
			case MINOR:
				return other == PATCH
					? this
					: other;
			case MAJOR:
			default:
				return this;
			}
		}
	}

	// cache `git diff-index commands' (only meaningful in REPL mode)
	private static Map<String,List<String>> diffTreeCache = new HashMap<>();
	private static Map<String,List<String>> parentCommits = new HashMap<>();
	private static Map<String,String> subrepoCommits = new HashMap<>();

	/**
	 * @return a {@code ModificationType}, or {@code null} if there are no modifications
	 */
	public static ModificationType isModifiedSinceLastRelease(File module)
			throws IOException, InterruptedException, XPathExpressionException, SystemExit {

		String pom = new File(module, "pom.xml").toString();
		String mainDir = new File(module, "src/main").toString();
		ModificationType modified = null;
		// check for uncommitted changes
		if (captureOutput(_x -> {}, "git", "diff-index", "--quiet", "HEAD", "--", mainDir) != 0)
			modified = ModificationType.PATCH; // might be a minor or major change, but we don't know yet
		// check for committed changes
		String currentVersion = getMavenCoords(module).v;
		if (!currentVersion.endsWith("-SNAPSHOT"))
			return null;
		else if (currentVersion.endsWith(".0-SNAPSHOT"))
			// if the module got a minor or major version update, assume that there have been changes
			return currentVersion.endsWith(".0.0-SNAPSHOT")
				? ModificationType.MAJOR
				: ModificationType.MINOR;
		String subrepo = null; {
			File f = module;
			while (true) {
				if (new File(f, ".gitrepo").exists()) {
					subrepo = f.toString().replace('\\', '/');
					break; }
				f = f.getParentFile();
				if (f == null)
					break; }}
		LinkedList<String> todo = new LinkedList<>();
		todo.add("HEAD");
		Set<String> done = new HashSet<>();
		Map<String,String> versions = new HashMap<>();
		File tmpPom = File.createTempFile("pom-", ".xml");
		try {
			loop: while (true) {
				if (todo.isEmpty())
					break;
				String commit = todo.poll();
				if (!done.add(commit))
					// we already reached this commit through another branch
					continue;
				for (String parent : getParentCommits(commit)) {
					List<String> changedFiles; {
						if (diffTreeCache.containsKey(parent + " " + commit))
							changedFiles = diffTreeCache.get(parent + " " + commit);
						else {
							changedFiles = new ArrayList<>();
							diffTreeCache.put(parent + " " + commit, changedFiles);
							captureOutput(
								line -> {
									try { changedFiles.add(line.split("\\t")[1].replace('\\', '/')); }
									catch (IndexOutOfBoundsException e) {}},
								"git", "diff-tree", "--exit-code", "-r", parent, commit
							);
						}
					}
					if (!changedFiles.isEmpty()) {
						boolean pomChanged = changedFiles.contains(pom);
						if (pomChanged) {
							String version; {
								version = versions.get(commit);
								if (version == null) {
									if (captureOutput(new PrintStream(new FileOutputStream(tmpPom))::println,
									                  "git", "show", commit + ":" + pom) != 0)
										exit(1);
									version = getMavenCoords(tmpPom).v;
									versions.put(commit, version);
								}
							}
							String versionBefore; {
								versionBefore = versions.get(parent);
								if (versionBefore == null) {
									PrintStream restoreErr = System.err;
									System.setErr(nullOutputStream);
									try {
										if (captureOutput(new PrintStream(new FileOutputStream(tmpPom))::println,
										                  "git", "show", parent + ":" + pom) == 0) {
											versionBefore = getMavenCoords(tmpPom).v;
											versions.put(parent, versionBefore);
										} else
											// file did not exist yet before this commit
											versionBefore = "";
									} finally {
										System.setErr(restoreErr);
									}
								}
							}
							if (!versionBefore.equals(version)) {
								if (subrepo != null && changedFiles.contains(subrepo + "/.gitrepo")) {
									// Check if the version update happened before or after any changes to src/main.
									if (changedFiles.stream().anyMatch(startsWith(mainDir + "/"))) {
										String subrepoCommit; {
											if (subrepoCommits.containsKey(commit))
												subrepoCommit = subrepoCommits.get(commit);
											else {
												Matcher m = Pattern
													.compile("(?m)^subrepo:\n +subdir: *\"([^\"]*)\"\n +merged: *\"([0-9a-fA-F]{7,})\"")
													.matcher(getCommitMessage(commit));
												if (m.find() && m.group(1).equals(subrepo)) {
													subrepoCommit = m.group(2);
													if (captureOutput(_x -> {}, "git", "rev-parse", "-q", "--verify", subrepoCommit) != 0)
														subrepoCommit = null;
												} else
													subrepoCommit = null;
												subrepoCommits.put(commit, subrepoCommit);
											}
										}
										if (subrepoCommit != null) {
											pom = pom.substring(subrepo.length() + 1);
											mainDir = mainDir.substring(subrepo.length() + 1);
											subrepo = null;
											todo.clear(); // this should already be the case
											todo.add(subrepoCommit);
											continue loop;
										}
										// If the subrepo commits haven't been fetched, or something else is
										// wrong, fall back to assuming that the changes happened after the
										// version update.
										modified = ModificationType.PATCH; // note that we will miss out on any minor/major info
									}
								}
								// We either found the non-snapshot, or we know there must be a non-snapshot
								// following this version (and preceding the current version). In both cases
								// we need to keep looking for changes, but not in this branch.
								continue;
							}
						}
						boolean srcChanged = changedFiles.stream().anyMatch(startsWith(mainDir + "/"));
						if (pomChanged || srcChanged) {
							Matcher m = Pattern
								.compile("(?m)^(Minor|Major)-Change: *([^\\s]+) *$")
								.matcher(getCommitMessage(commit));
							if (m.find()) {
								String prf = module.toString().replace('\\', '/');
								if (subrepo != null)
									prf = prf.substring(subrepo.length() + 1);
								do {
									String path = m.group(2).replace('\\', '/');
									if (path.equals(prf) || path.startsWith(prf + "/"))
										if ("Major".equals(m.group(1)))
											return ModificationType.MAJOR;
										else
											modified = ModificationType.MINOR;
								} while (m.find());
							}
							if (srcChanged) // ignore changes to pom (unless minor/major change indicated in commit message)
								modified = ModificationType.PATCH.and(modified);
						}
					}
					// keep looking for changes on this branch
					todo.add(parent);
				}
			}
		} finally {
			tmpPom.delete();
		}
		return modified;
	}

	private static Predicate<String> startsWith(String prefix) {
		return x -> x.startsWith(prefix);
	}

	private static String getCommitMessage(String commit) throws IOException, InterruptedException, SystemExit {
		try (ByteArrayOutputStream commitMessage = new ByteArrayOutputStream()) {
			if (captureOutput(new PrintStream(commitMessage)::println,
			                  "git", "log", "--format=%B", "-n", "1", commit) != 0)
				exit(1);
			return commitMessage.toString();
		}
	}

	private static List<String> getParentCommits(String commit) throws IOException, InterruptedException, SystemExit {
		if (parentCommits.containsKey(commit))
			return parentCommits.get(commit);
		else {
			List<String> list = new ArrayList<>();
			parentCommits.put(commit, list);
			if (captureOutput(line -> list.addAll(Arrays.asList(line.trim().split("\\s+"))), "git", "log", "--pretty=%P", "-n", "1", commit) != 0)
				exit(1);
			if (list.size() == 1 && "".equals(list.get(0)))
				list.clear();
			return list;
		}
	}

	/**
	 * @param source is a POM file that is either a BOM (name of parent directory is "bom"), or an
	 *               aggregator (contains {@code modules} element).
	 */
	public static void optimizePom(File source, File dest) throws IOException, InterruptedException, XPathExpressionException, SystemExit {
		String MY_DIR = System.getenv("MY_DIR");
		String TARGET_DIR = System.getenv("TARGET_DIR");
		List<mvn.Coords> unchangedSinceRelease = new ArrayList<>();
		List<String> optimizedModules = new ArrayList<>();
		if (source.getParentFile() != null && "bom".equals(source.getParentFile().getName()))
			traverseModules(
				// assuming there is an aggregator pom one level up
				source.getParentFile().getParentFile(),
				(module, isAggregator) -> {
					if (!isAggregator) {
						// this file is expected to exist
						File modifiedState = new File(TARGET_DIR + "/state/" + module + "/modified-since-release");
						try {
							if (modifiedState.exists() && "null".equalsIgnoreCase(slurp(modifiedState).trim()))
								unchangedSinceRelease.add(getMavenCoords(module));
						} catch (IOException|XPathExpressionException e) {}
					}});
		else {
			// assuming all bom and aggregator modules are optimized
			String[] modules = xpath(
				source, "string(/*/*[local-name()='modules'])"
			).trim().split("\\s+");
			if (modules.length > 0 && !(modules.length == 1 && modules[0].equals("")))
				for (String m : modules)
					if (m.equals("bom")
					    || !xpath(new File(new File(source.getParentFile(), m), "pom.xml"),
					              "string(/*/*[local-name()='modules'])").trim().equals(""))
						optimizedModules.add(m); }
		if (
			xslt(
				source,
				new FileOutputStream(dest),
				new File(MY_DIR + "/mvn-optimize-pom.xsl"),
				"unchanged-deps", unchangedSinceRelease.stream()
				                                       .map(gav -> gav.g + ":" + gav.a)
				                                       .collect(Collectors.toList()),
				"optimized-modules", optimizedModules,
				"relative-path-to-original-dir", relativize(source.getParentFile() != null
				                                                ? source.getParentFile()
				                                                : new File("."),
				                                            dest)
			) != 0) {
			rm(dest);
			exit(1);
		}
	}

	private static String relativize(File file, File base) throws IOException {
		String filePath = file.getCanonicalPath().replace('\\', '/');
		String basePath = base.getCanonicalPath().replace('\\', '/');
		if (base.isDirectory()) basePath += "/.";
		String[] filePathSegments = filePath.split("/", -1);
		String[] basePathSegments = basePath.split("/", -1);
		int i = 0, j = 0;
		while (i < filePathSegments.length && j < basePathSegments.length - 1) {
			if (basePathSegments[i].equals(filePathSegments[j])) {
				i++;
				j++; }
			else
				break; }
		String relativizedPath = "";
		while (j < basePathSegments.length - 1) {
			relativizedPath += "../";
			j++; }
		while (i < filePathSegments.length) {
			relativizedPath += filePathSegments[i] + "/";
			i++; }
		relativizedPath = relativizedPath.substring(0, relativizedPath.length() - 1);
		return relativizedPath;
	}

	public static void computeMavenDeps(File effectivePom, File gradlePom, File outputBaseDir, String outputFileName)
			throws IOException, InterruptedException, XPathExpressionException, SystemExit {

		String MY_DIR = System.getenv("MY_DIR");
		Map<String,String> modules = new HashMap<>(); {
			traverseModules(
				new File("."),
				(module, isAggregator) -> {
					if (!isAggregator)
						try {
							mvn.Coords gav = getMavenCoords(module);
							modules.put(gav.g + ":" + gav.a,
							            module.toString().replace('\\', '/')); }
						catch (IOException|XPathExpressionException e) {
							throw new RuntimeException(e); }}); }
		for (String m : modules.values())
			rm(new File(new File(outputBaseDir, m), outputFileName));
		if (
			xslt(
				effectivePom,
				null,
				new File(MY_DIR + "/make-maven-deps.mk.xsl"),
				"root-dir", System.getenv("ROOT_DIR"),
				"gradle-pom", gradlePom.getPath().replace('\\', '/'),
				"module", ".",
				"release-dirs", glob("[!.]**/.gitrepo").stream()
				                                       .map(f -> f.getParentFile())
				                                       .filter(f -> new File(f, "bom/pom.xml").exists())
				                                       .map(f -> f.toString().replace('\\', '/'))
				                                       .collect(Collectors.toList()),
				"source-modules", modules,
				"output-basedir", outputBaseDir.getPath().replace('\\', '/'),
				"output-filename", outputFileName,
				"verbose", System.getenv("VERBOSE") != null
			) != 0) {
			for (String m : modules.values())
				rm(new File(new File(outputBaseDir, m), outputFileName));
		};
	}

	public static void computeGradleDeps(String module, File outputBaseDir, String outputFileName) throws IOException, XPathExpressionException {
		File outputFile = new File(new File(outputBaseDir, module), outputFileName);
		mkdirs(outputFile.getParentFile());
		try {
			mvn.Coords gav = getMavenCoords(new File(module));
			String g = gav.g;
			String a = gav.a;
			String v = gav.v;
			String m = module;
			try (PrintStream s = new PrintStream(new FileOutputStream(outputFile))) {
				s.println(m + "/VERSION := " + v);
				s.println();
				s.println("$(TARGET_DIR)/state/" + m + "/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval");
				s.println("	+$(EVAL) touch(\"$@\");");
				s.println();
				s.println(".SECONDARY : " + m + "/.test");
				s.println(m + "/.test : | .gradle-init .group-eval");
				s.println("	+$(EVAL) gradle.test(\"" + m + "\");");
				s.println();
				s.println(m + "/.test : %/.test : "
				          + "%/build.gradle %/gradle.properties %/.dependencies");
				if (v.endsWith("-SNAPSHOT")) {
					s.println();
					s.println(String.format("$(MVN_LOCAL_REPOSITORY)/%s/%s/%s/%s-%s.jar : %s/.install.jar",
					                        g.replace(".", "/"), a, v, a, v, m));
					s.println("	+$(EVAL) exit(new File(\"$@\").exists());");
					s.println("	+$(EVAL) touch(\"$@\");");
					s.println();
					s.println(".SECONDARY : " + m + "/.install.jar");
					s.println(m + "/.install.jar : %/.install.jar : %/.install");
					s.println();
					s.println(".SECONDARY : " + m + "/.install");
					s.println(m + "/.install : | .gradle-init .group-eval");
					s.println("	+$(EVAL) gradle.install(\"" + m + "\");");
					s.println();
					s.println(m + "/.install : %/.install : "
					          + "%/build.gradle %/gradle.properties %/.dependencies");
					s.println();
					s.println(".SECONDARY : " + m + "/.dependencies");
					s.println(m + "/.dependencies :");
				}
				s.println();
				s.println(String.format("$(MVN_LOCAL_REPOSITORY)/%s/%s/%s/%s-%s.jar : %s/.release",
				                        g.replace(".", "/"), a, v.replace("-SNAPSHOT", ""), a, v.replace("-SNAPSHOT", ""), m));
				s.println();
				s.println(".SECONDARY : " + m + "/.release");
				if (v.endsWith("-SNAPSHOT")) {
					s.println(m + "/.release : | .gradle-init .group-eval");
					s.println("	+$(EVAL) gradle.release(\"" + m + "\");");
				} else {
					// already released, but empty rule is needed because jar might not be in .maven-workspace yet
					s.println(m + "/.release :");
				}
				if (v.endsWith("-SNAPSHOT")) {
					s.println();
					// FIXME: gradle eclipse does not link up projects
					// FIXME: gradle eclipse does not take into account localRepository from gradle-settings/conf/settings.xml
					// when creating .classpath (but it does need the dependencies to be installed in .maven-workspace)
					s.println(m + "/.project : " + m + "/build.gradle " + m + "/gradle.properties " + m + "/.dependencies .group-eval");
					s.println("	+$(EVAL) gradle.eclipse(\"" + m + "\");");
					s.println();
					s.println("clean-eclipse : " + m + "/.clean-eclipse");
					s.println(".PHONY : " + m + "/.clean-eclipse");
					s.println(m + "/.clean-eclipse :");
					s.println("	$(call bash, \\");
					s.println("		if ! git ls-files --error-unmatch " + m + "/.project >/dev/null 2>/dev/null; then \\");
					s.println("			rm -rf $(addprefix " + m + "/,.project .classpath); \\");
					s.println("		else \\");
					s.println("			git checkout HEAD -- $(addprefix " + m + "/,.project .classpath); \\");
					s.println("		fi \\");
					s.println("	)");
				}
			}
		} catch (RuntimeException|IOException e) {
			rm(outputFile);
			try (PrintStream s = new PrintStream(new FileOutputStream(outputFile))) {
				s.println("$(error $@ could not be generated)");
			}
			throw e;
		}
	}

	public static void groupEval() throws Exception {
		groupEval(System.in);
	}

	public static void groupEval(InputStream commands) throws Exception {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(commands))) {
			groupEval(
				new Iterator<String>() {
					boolean nextComputed = false;
					String next = null;
					public boolean hasNext() {
						if (!nextComputed) {
							try {
								next = in.readLine();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							if (next != null)
								next = next.trim();
							nextComputed = true;
						}
						return next != null;
					}
					public String next() throws NoSuchElementException {
						if (!hasNext())
							throw new NoSuchElementException();
						nextComputed = false;
						return next;
					}
				}
			);
		}
	}

	public static void groupEval(Iterator<String> commands) throws Exception {
		String EVAL_JAVA = System.getenv("SHELL");
		String ANSI_YELLOW_BOLD = "\033[1;33m";
		String ANSI_RED_BOLD = "\033[1;31m";
		String ANSI_RESET = "\u001B[0m";
		LinkedList<Object> groupedCommands = new LinkedList<>(); {
			while (commands.hasNext()) {
				String line = commands.next();
				Pattern methodCall = Pattern.compile(
					"^(mvn\\.releaseModulesInDir\\([^)]+\\))\\s*\\.apply\\(\\s*((?:[^\\s\"]|\"(\\\\\"|[^\"])*\")+\\s*(,\\s*(?:[^\\s\"]|\"(\\\\\"|[^\"])*\")+\\s*)*)\\)\\s*;$");
				Matcher m = methodCall.matcher(line);
				if (m.matches()) {
					String func = m.group(1);
					String arguments = m.group(2).trim();
					CombinableCommand cmd = new CombinableCommand(func, arguments);
					CombinableCommand appendTo = (CombinableCommand)
						groupedCommands.stream()
						               .filter(x -> x instanceof CombinableCommand && ((CombinableCommand)x).isCombinableWith(cmd))
						               .findFirst()
						               .orElse(null);
					if (appendTo != null)
						appendTo.append(cmd);
					else
						groupedCommands.add(cmd);
				} else
					groupedCommands.add(line);
			}
		}
		println("-------------- " + ANSI_YELLOW_BOLD + "Build order" + ANSI_RESET + ": -------------");
		for (Object cmd : groupedCommands)
			println(cmd);
		println("-------------- " + ANSI_YELLOW_BOLD + "Environment" + ANSI_RESET + ": -------------");
		Map<String,String> oldEnv = new HashMap<>(); {
			File envFile = new File(System.getenv("TARGET_DIR") + "/env");
			try (BufferedReader f = new BufferedReader(new FileReader(envFile))) {
				String line;
				while ((line = f.readLine()) != null) {
					if ("".equals(line)) {
					} else if (line.contains("=")) {
						int i = line.indexOf("=");
						oldEnv.put(line.substring(0, i), line.substring(i + 1));
					} else {
						System.err.println("Unexpected line: " + line);
						exit(1);
					}
				}
			} catch (IOException e) {
				System.err.println("Could not open " + envFile);
				exit(1);
			}
		}
		Map<String,String> env = System.getenv();
		for (String k : new TreeSet<>(env.keySet())) {
			if (!"_".equals(k)) {
				String v = env.get(k);
				if (!v.equals(oldEnv.get(k)))
					println("export " + k + "=\"" + v + "\"");
			}
		}
		println("-----------------------------------------");
		for (Object cmd : groupedCommands) {
			System.out.println("--> " + ANSI_YELLOW_BOLD + cmd + ANSI_RESET);
			int rv = captureOutput(System.out::println, EVAL_JAVA, cmd.toString());
			if (rv != 0) {
				System.err.print(ANSI_RED_BOLD);
				System.err.println("\ncommand exited with value " + rv);
				System.err.print(ANSI_RESET);
				exit(rv);
			}
		}
	}

	private static class CombinableCommand {

		final String func;
		private String[] args;

		CombinableCommand(String func, String... args) {
			this.func = func;
			this.args = args;
		}

		boolean isCombinableWith(Object o) {
			return o instanceof CombinableCommand && func.equals(((CombinableCommand)o).func);
		}

		void append(CombinableCommand o) {
			if (!isCombinableWith(o))
			    throw new IllegalArgumentException();
			String[] combinedArgs = new String[args.length + o.args.length];
			System.arraycopy(args, 0, combinedArgs, 0, args.length);
			System.arraycopy(o.args, 0, combinedArgs, args.length, o.args.length) ;
			args = combinedArgs;
		}

		@Override
		public String toString() {
			return String.format("%s.apply(%s);", func, args.length > 1
				? (" \\\n   " + String.join(", \\\n   ", args))
				: String.join(", ", args));
		}
	}
}
