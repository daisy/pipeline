ROOT_DIR          := $(CURDIR)
MY_DIR            := $(patsubst %/,%,$(dir $(lastword $(MAKEFILE_LIST))))
TARGET_DIR        ?= $(MY_DIR)/target
GRADLE_FILES      := $(shell for (File f : glob("**/{build.gradle,settings.gradle,gradle.properties}")) println(f.toString().replace('\\', '/'));)
GRADLE_MODULES    := $(filter $(patsubst %/build.gradle,%,$(filter %/build.gradle,$(GRADLE_FILES))), \
                              $(patsubst %/gradle.properties,%,$(filter %/gradle.properties,$(GRADLE_FILES))))
MODULES            = $(MAVEN_MODULES) $(GRADLE_MODULES)
MVN_LOG           := redirectTo("$(ROOT_DIR)/maven.log");
M2_HOME           := $(ROOT_DIR)/$(TARGET_DIR)/gradle-settings
EVAL              := //

CLASSPATH := $(shell                                                                              \
    File javaDir = new File("$(MY_DIR)/java");                                                    \
    File classPath = new File("$(TARGET_DIR)/classes/" + getJavaVersion());                       \
    mkdirs(classPath);                                                                            \
    List<File> javaFiles = glob(javaDir.getPath().replace("\\", "/") +  "/{*.java,**/*.java}");   \
    if (javaFiles.stream().anyMatch(                                                              \
        javaFile -> {                                                                             \
            File classFile = new File(javaFile.getPath().replace(".java", ".class"));             \
            return (!classFile.exists()                                                           \
                    || javaFile.lastModified() > classFile.lastModified()); })) {                 \
        String javac = getOS() == OS.WINDOWS ? "javac.exe" : "javac";                             \
        String JAVA_HOME = System.getenv("JAVA_HOME");                                            \
        if (JAVA_HOME != null) {                                                                  \
            File f = new File(new File(new File(JAVA_HOME), "bin"), javac);                       \
            if (f.exists())                                                                       \
                javac = f.getAbsolutePath(); }                                                    \
        List<String> cmd = new ArrayList<>();                                                     \
        cmd.add(javac);                                                                           \
        cmd.add("-cp");                                                                           \
        cmd.add(classPath.getPath() + File.pathSeparator + "$(MY_DIR)/lib"                        \
                                    + File.pathSeparator + "$(MY_DIR)/lib/Saxon-HE-9.8.0-8.jar"); \
        cmd.add("-d");                                                                            \
        cmd.add(classPath.getPath());                                                             \
        for (File f : javaFiles) cmd.add(f.getPath());                                            \
        exitOnError(captureOutput(err::println, cmd)); }                                          \
    println(classPath.getPath().replace('\\', '/'));                                              )
CLASSPATH += $(MY_DIR)/lib/Saxon-HE-9.8.0-8.jar
IMPORTS := build.mvn build.gradle build.mvn.Coords
STATIC_IMPORTS := build.core.*
# for use in mvn-release.sh
SAXON = $(MY_DIR)/lib/Saxon-HE-9.8.0-8.jar

export ROOT_DIR MY_DIR TARGET_DIR MVN_SETTINGS MVN_PROPERTIES MVN_LOG MVN_RELEASE_CACHE_REPO M2_HOME MAKE SHELL IMPORTS STATIC_IMPORTS CLASSPATH SAXON
# MAKECMDGOALS used in gradle-release.sh and mvn-release.sh
export MAKECMDGOALS
# MAKEFLAGS exported by default

# REPL server can be spawned after the latest environment variable export
ifneq ($(OS), WINDOWS)
export JAVA_REPL_PORT := $(shell --spawn-repl-server)
endif

eval-java = $(ROOT_DIR)/$(SHELL) $(call quote-for-bash,$1)

rwildcard = $(shell if (new File("$1").isDirectory()) glob("$1**/$2").forEach(x -> println(x.getPath().replace('\\', '/').replace(" ", "\\ ")));)
# this alternative does not support spaces in file names
#rwildcard = $(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

update-target-if-changed = $@.tmp && ((! [ -e $@ ] || ! diff -q $@ $@.tmp >/dev/null) && mv $@.tmp $@ || rm $@.tmp ) || (rm $@.tmp && false)

# always execute this recipe (but only update the file it actually changes)
.PHONY : $(TARGET_DIR)/state/properties
$(TARGET_DIR)/state/properties :
	mkdirs("$(dir $@)"); \
	try (OutputStream s = updateFileIfChanged("$@")) { new PrintStream(s).println($(call quote-for-java,$(MVN_PROPERTIES))); }

# this recipe is always executed because the properties recipe is always executed,
# so explicitly check that properties has been updated
$(TARGET_DIR)/effective-settings.xml : $(MVN_SETTINGS) $(TARGET_DIR)/state/properties
	if (isOutOfDate("$@", "$^".trim().split("\\s+"))) \
		/* cd into random directory in order to force Maven "stub" project */ \
		mvn(new File("$(TARGET_DIR)"), \
		    "org.apache.maven.plugins:maven-help-plugin:2.2:effective-settings", "-Doutput=$(ROOT_DIR)/$@");

# if maven.mk does not exist yet or is out of date, make will restart after the file has been created/changed
# (see https://www.gnu.org/software/make/manual/html_node/Remaking-Makefiles.html)
include $(TARGET_DIR)/maven.mk

# effective-pom.xml is updated when any of the prerequisites of `poms` (defined in maven.mk) change
.SECONDARY : poms
poms :

# this recipe is executed only when prerequisites have changed
# note that most prerequisites are defined in maven.mk itself
$(TARGET_DIR)/maven.mk : $(TARGET_DIR)/effective-settings.xml
	try (PrintStream s = new PrintStream(new FileOutputStream("$@"))) { \
		String localRepo = xpath(new File("$<"), "/*/*[local-name()='localRepository']/text()") \
		                   .replace('\\', '/') \
		                   .replace("$(ROOT_DIR)/", ""); \
		s.println("MVN_LOCAL_REPOSITORY := " + localRepo); \
		s.println("export MVN_LOCAL_REPOSITORY"); \
		traverseModules( \
			new File("."), \
			(m, isAggregator) -> { \
				String module = m.toString().replace('\\', '/'); \
				s.println("MAVEN_" + (isAggregator ? "AGGREGATORS" : "MODULES") + " += " + module); \
				if ("bom".equals(m.getName())) { \
					String optimized = "$$(TARGET_DIR)/optimized/" + module + "/pom.xml"; \
					s.println("poms : " + optimized); \
					/* assuming there is an aggregator pom one level up */ \
					try { \
						traverseModules( \
							m.getParentFile(), \
							(mm, aggr) -> { \
								if (!aggr && !"bom".equals(mm.getName())) { \
									/* assume that all modules are in the BOM */ \
									s.println(optimized + " : $$(TARGET_DIR)/state/" + mm.toString().replace('\\', '/') \
									          + "/modified-since-release"); }}); \
					} catch (javax.xml.xpath.XPathExpressionException e) { \
						throw new RuntimeException(e); \
					} \
				} else if (isAggregator) \
					s.println("poms : $$(TARGET_DIR)/optimized/" + module + "/pom.xml"); \
				else \
					s.println("poms : " + module + "/pom.xml"); \
				/* maven.mk should not be updated when optimized POMs change, only when the originals change */ \
				s.println("$$(TARGET_DIR)/maven.mk : " + module + "/pom.xml"); }); \
	}

$(TARGET_DIR)/optimized/%/pom.xml : %/pom.xml
	mkdirs("$(dir $@)"); \
	optimizePom(new File("$<"), new File("$@"));

# updateFileIfChanged() causes a rule to be executed over and over as long as the target is not
# updated, so do the real work in modified-since-release_ rules
$(TARGET_DIR)/state/%/modified-since-release : $(TARGET_DIR)/state/%/modified-since-release_
	try (OutputStream s = updateFileIfChanged("$@")) { \
		new PrintStream(s).print(slurp(new File("$<"))); }

# the rule below is needed for when deps.mk have not been generated yet
# the rule does not really do anything because it is intended to be overridden
# by setting the modified state to "patch" (rather than "null") we ensure the
# deps.mk rules are executed in the right order, and avoid an extra iteration
# note that because this is an implicit rule, the target files will be deleted
# (see https://www.gnu.org/software/make/manual/html_node/Chained-Rules.html)
# the files created by the corresponding rules from deps.mk are not deleted
$(TARGET_DIR)/state/%/modified-since-release_ : %/pom.xml
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		new PrintStream(s).print(ModificationType.PATCH.toString()); }

# this recipe is executed only when prerequisites have changed
# the purpose of the isOutOfDate() is for making "make -B" not affect this rule (to speed thing up)
$(TARGET_DIR)/effective-pom.xml : export JAVA_REPL_PORT =
# FIXME: main.mk is not supposed to be aware of settings.xml.in
$(TARGET_DIR)/effective-pom.xml : export MVN_SETTINGS = settings.xml.in
$(TARGET_DIR)/effective-pom.xml : poms | $(MVN_SETTINGS)
	File optimizedDir = new File("$(TARGET_DIR)/optimized"); \
	List<String> modules = new ArrayList<>(); { \
		traverseModules( \
			optimizedDir, \
			(m, isAggregator) -> { \
				if (!isAggregator) \
					modules.add(m.toString().replace('\\', '/').substring(optimizedDir.toString().length() + 1)); }); } \
	File[] poms = new File[modules.size()]; \
	int i = 0; \
	for (String module : modules) \
		poms[i++] = new File(new File(optimizedDir, module), "pom.xml"); \
	if (isOutOfDate(new File("$@"), poms)) { \
		File tmpRepo = new File("$(TARGET_DIR)/tmp-repo"); \
		rm(tmpRepo); \
		try { \
			for (File pom : poms) { \
				mvn.Coords gav = getMavenCoords(pom); \
				String g = gav.g; \
				String a = gav.a; \
				String v = gav.v; \
				File dest = new File(tmpRepo, String.format("%s/%s/%s/%s-%s.pom", g.replace(".", "/"), a, v, a, v)); \
				mkdirs(dest.getParentFile()); \
				cp(pom, dest); \
				if (!v.endsWith("-SNAPSHOT")) \
					exitOnError( \
						xslt( \
							pom, \
							new FileOutputStream(dest), \
							new File("$(MY_DIR)/mvn-set-version.xsl"), \
							"version", v + "-SNAPSHOT")); \
			} \
			try { \
				mvn("-Dworkspace=" + tmpRepo.toString(), \
				    "-Dcache=.maven-cache", \
				    "-f", optimizedDir.toString(), \
				    "--projects", String.join(",", modules), \
				    "-Prun-script-webserver", "-Ddocumentation", \
				    "org.apache.maven.plugins:maven-help-plugin:2.2:effective-pom", "-Doutput=$(ROOT_DIR)/$@"); \
			} catch (SystemExit e) { \
				err.println("Failed to compute Maven dependencies."); \
				throw e; \
			} \
		} finally { \
			rm(tmpRepo); \
		} \
	} else { \
		touch("$@"); \
	}

$(TARGET_DIR)/gradle-pom.xml : $(GRADLE_FILES)
	try (PrintStream s = new PrintStream(new FileOutputStream("$@"))) { \
		s.println("<projects xmlns=\"http://maven.apache.org/POM/4.0.0\">"); \
		for (String module : "$(GRADLE_MODULES)".trim().split("\\s+")) { \
			Coords gav = getMavenCoords(new File(module)); \
			s.println("  <project>"); \
			s.println("    <groupId>" + gav.g + "</groupId>"); \
			s.println("    <artifactId>" + gav.a + "</artifactId>"); \
			s.println("    <version>" + gav.v + "</version>"); \
			s.println("  </project>"); \
		} \
		s.println("</projects>"); \
	}

.SECONDARY : .maven-init .gradle-init

.maven-init :

.gradle-init : | .maven-init $(TARGET_DIR)/gradle-settings/conf/settings.xml
$(TARGET_DIR)/gradle-settings/conf/settings.xml : $(MVN_SETTINGS)
	mkdirs("$(dir $@)"); \
	rm("$@"); \
	cp("$<", "$@");

# this recipe is executed when any poms (including optimized ones) change (causing effective-pom.xml
# to be regenerated)
# note that it may need to be run multiple times to get everything right, because boms are optimized
# based on compile dependency information
.SECONDARY : .maven-deps.mk
.maven-deps.mk : $(TARGET_DIR)/effective-pom.xml $(TARGET_DIR)/gradle-pom.xml
	computeMavenDeps(new File("$<"), \
	                 new File("$(word 2,$^)"), \
	                 new File("$(TARGET_DIR)/mk"), \
	                 "deps.mk");

ifneq ($(MAKECMDGOALS), clean)

$(addsuffix /deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(MAVEN_MODULES) $(MAVEN_AGGREGATORS))) : .maven-deps.mk
	if (!new File("$@").exists()) { \
		mkdirs("$(dir $@)"); \
		try (PrintStream s = new PrintStream(new FileOutputStream("$@"))) { \
			s.println("$$(error $@ could not be generated)"); \
		} \
	} \
	touch("$@");

$(addsuffix /sources.mk,$(addprefix $(TARGET_DIR)/mk/,$(MAVEN_MODULES))) : $(TARGET_DIR)/mk/%/sources.mk : %/pom.xml
	rm("$@"); \
	mkdirs("$(dir $@)"); \
	touch("$@"); \
	try (PrintStream s = new PrintStream(new FileOutputStream("$@"))) { \
		File pomFile = new File("$<"); \
		File module = pomFile.getParentFile(); \
		File srcDir = new File(module, "src"); \
		File docDir = new File(module, "doc"); \
		File indexFile = new File(module, "index.md"); \
		if (srcDir.isDirectory() || docDir.isDirectory() || indexFile.isFile()) { \
			boolean isSnapshot = getMavenCoords(pomFile).v.endsWith("-SNAPSHOT"); \
			List<String> sourceDirs = new ArrayList<>(); \
			List<String> mainSourceFiles = new ArrayList<>(); \
			List<String> otherSourceFiles = new ArrayList<>(); \
			if (srcDir.isDirectory()) { \
				String mainDir = new File(srcDir, "main").getPath() + "/"; \
				Files.walk(srcDir.toPath()).forEach( \
					f -> { \
						String path = f.toString().replace(" ", "\\ "); \
						if (Files.isDirectory(f)) \
							sourceDirs.add(path); \
						else if (isSnapshot && path.startsWith(mainDir)) \
							mainSourceFiles.add(path); \
						else \
							otherSourceFiles.add(path); \
					} \
				); \
			} \
			if (!isSnapshot) { \
				if (otherSourceFiles.size() > 0) { \
					s.print(String.format("%s/.test :", module)); \
					for (String p : otherSourceFiles) s.print(" \\\n\t" + p.replace("$$", "$$$$")); \
					s.println(); \
				} \
			} else { \
				if (mainSourceFiles.size() > 0 || otherSourceFiles.size() > 0 || docDir.isDirectory() || indexFile.isFile()) { \
					String packaging = xpath(pomFile, "string(/*/*[local-name()='packaging'])"); \
					boolean isJar = "".equals(packaging) || "bundle".equals(packaging) || "maven-plugin".equals(packaging); \
					if (mainSourceFiles.size() > 0) { \
						String targets = String.format("%s/.test %s/.install", module, module); \
						if (isJar) \
							targets += String.format(" %s/.install-doc", module); \
						targets += String.format(" $$(TARGET_DIR)/state/%s/modified-since-release_", module); \
						s.print(targets + " :"); \
						for (String p : mainSourceFiles) s.print(" \\\n\t" + p.replace("$$", "$$$$")); \
						s.println(); \
					} \
					if (otherSourceFiles.size() > 0) { \
						String targets = String.format("%s/.test", module); \
						if (isJar) \
							targets += String.format(" %s/.install-doc", module); \
						s.print(targets + " :"); \
						for (String p : otherSourceFiles) s.print(" \\\n\t" + p.replace("$$", "$$$$")); \
						s.println(); \
					} \
					if (isJar && (docDir.isDirectory() || indexFile.isFile())) { \
						List<String> docFiles = new ArrayList<>(); \
						if (docDir.isDirectory()) { \
							Files.walk(docDir.toPath()).forEach( \
								f -> { \
									String path = f.toString().replace(" ", "\\ "); \
									if (Files.isDirectory(f)) \
										sourceDirs.add(path); \
									else \
										docFiles.add(path); \
								} \
							); \
						} \
						if (indexFile.isFile()) \
							docFiles.add(indexFile.getPath()); \
						if (docFiles.size() > 0) { \
							s.print(String.format("%s/.install-doc :", module)); \
							for (String p : docFiles) s.print(" \\\n\t" + p.replace("$$", "$$$$")); \
							s.println(); \
						} \
					} \
				} \
			} \
			if (sourceDirs.size() > 0) { \
				s.print("$@ :"); \
				for (String p : sourceDirs) s.print(" \\\n\t" + p.replace("$$", "$$$$")); \
				s.println(); \
			} \
		} \
	}

$(addsuffix /deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(GRADLE_MODULES))) : $(GRADLE_FILES)
	computeGradleDeps("$(patsubst $(TARGET_DIR)/mk/%/deps.mk,%,$@)", \
	                  new File("$(TARGET_DIR)/mk"), \
	                  "deps.mk");

$(addsuffix /sources.mk,$(addprefix $(TARGET_DIR)/mk/,$(GRADLE_MODULES))) : $(GRADLE_FILES)
	rm("$@"); \
	mkdirs("$(dir $@)"); \
	touch("$@"); \
	try (PrintStream s = new PrintStream(new FileOutputStream("$@"))) { \
		File module = new File("$(patsubst $(TARGET_DIR)/mk/%/sources.mk,%,$@)"); \
		File srcDir = new File(module, "src"); \
		File testDir = new File(module, "test"); \
		File itDir = new File(module, "integrationtest"); \
		if (srcDir.isDirectory() || testDir.isDirectory() || itDir.isDirectory()) { \
			boolean isSnapshot = getMavenCoords(module).v.endsWith("-SNAPSHOT"); \
			List<String> sourceDirs = new ArrayList<>(); \
			List<String> mainSourceFiles = new ArrayList<>(); \
			List<String> otherSourceFiles = new ArrayList<>(); \
			if (isSnapshot && srcDir.isDirectory()) \
				Files.walk(srcDir.toPath()).forEach( \
					f -> { \
						String path = f.toString().replace(" ", "\\ "); \
						if (Files.isDirectory(f)) \
							sourceDirs.add(path); \
						else \
							mainSourceFiles.add(path); \
					} \
				); \
			if (testDir.isDirectory()) \
				Files.walk(testDir.toPath()).forEach( \
					f -> { \
						String path = f.toString().replace(" ", "\\ "); \
						if (Files.isDirectory(f)) \
							sourceDirs.add(path); \
						else \
							otherSourceFiles.add(path); \
					} \
				); \
			if (itDir.isDirectory()) \
				Files.walk(itDir.toPath()).forEach( \
					f -> { \
						String path = f.toString().replace(" ", "\\ "); \
						if (Files.isDirectory(f)) \
							sourceDirs.add(path); \
						else \
							otherSourceFiles.add(path); \
					} \
				); \
			if (mainSourceFiles.size() > 0) { \
				s.print(String.format("%s/.test %s/.install :", module, module)); \
				for (String p : mainSourceFiles) s.print(" \\\n\t" + p.replace("$$", "$$$$")); \
				s.println(); \
			} \
			if (otherSourceFiles.size() > 0) { \
				s.print(String.format("%s/.test :", module, module)); \
				for (String p : otherSourceFiles) s.print(" \\\n\t" + p.replace("$$", "$$$$")); \
				s.println(); \
			} \
			if (sourceDirs.size() > 0) { \
				s.print("$@ :"); \
				for (String p : sourceDirs) s.print(" \\\n\t" + p.replace("$$", "$$$$")); \
				s.println(); \
			} \
		} \
	}

endif

# FIXME: specifying "--debug" option breaks this code
# - passing "MAKEFLAGS=" does not fix it for some reason, and also has unwanted side effects
# - passing "--debug=no" to the sub-make would be a solution but not all versions of make support it
.SECONDARY : .group-eval
ifneq ($(OS), WINDOWS)
.group-eval : export JAVA_REPL_PORT =
endif
.group-eval :
ifndef SKIP_GROUP_EVAL_TARGET
	List<String> commands = new ArrayList<>(); \
	try { \
		exitOnError( \
			captureOutput(commands::add, \
			              cons("$(MAKE)", "--no-print-directory", "-n", \
			                              "EVAL=// xxx", "SKIP_GROUP_EVAL_TARGET=true", \
			                              "$(MAKECMDGOALS)".trim().split("\\s+")))); \
		boolean take = true; \
		for (ListIterator<String> i = commands.listIterator(); i.hasNext();) { \
			String cmd = i.next(); \
			if ("//".equals(cmd)) i.remove(); \
			else if (cmd.startsWith("// xxx ")) \
				if (take) i.set(cmd.substring(7)); \
				else exit(1); \
			else { \
				i.remove(); \
				take = false; \
			} \
		} \
	} catch (SystemExit ex) { \
		err.println("Error in build script. Contact maintainer."); \
		throw ex; \
	} \
	if (commands.size() > 0) { \
		File env = new File("$(TARGET_DIR)/env"); \
		try { \
			try (PrintStream s = new PrintStream(new FileOutputStream(env))) { \
				$(foreach V,$(sort $(filter-out MFLAGS MAKELEVEL _,$(.VARIABLES))),$(if $(filter environment%,$(origin $V)), \
					s.println($(call quote-for-java,$V=$($V)));)) \
			} \
			groupEval(commands.iterator()); \
		} finally { \
			rm(env); \
		} \
	}
endif

# so that initialization is not part of the commands to be evaluated
.group-eval : | .maven-init .gradle-init

.PHONY : clean
clean :
	rm("$(TARGET_DIR)"); \
	rm("maven.log");

ifneq (,$(filter clean,$(MAKECMDGOALS)))
include $(shell for (String f : "$(addsuffix /deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(MODULES) $(MAVEN_AGGREGATORS)))".trim().split("\\s+")) \
                    if (new File(f).exists()) println(f); \
                for (String f : "$(addsuffix /sources.mk,$(addprefix $(TARGET_DIR)/mk/,$(MODULES)))".trim().split("\\s+")) \
                    if (new File(f).exists()) println(f);)
else
-include $(addsuffix /deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(MODULES) $(MAVEN_AGGREGATORS)))
-include $(addsuffix /sources.mk,$(addprefix $(TARGET_DIR)/mk/,$(MODULES)))
endif

ifdef SKIP_GROUP_EVAL_TARGET
.SILENT:
else ifndef VERBOSE
.SILENT:
endif

define \n


endef
quote-for-bash = '$(subst ${\n},'"\n"',$(subst \,\\,$(subst ','"'"',$(1))))'
