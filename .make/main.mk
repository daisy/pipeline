ROOT_DIR          := $(CURDIR)
MY_DIR            := $(patsubst %/,%,$(dir $(lastword $(MAKEFILE_LIST))))
TARGET_DIR        ?= $(MY_DIR)/target
GRADLE_FILES      := $(shell for (File f : glob("**/{build.gradle,settings.gradle,gradle.properties}")) println(f.toString());)
GRADLE_MODULES    := $(filter $(patsubst %/build.gradle,%,$(filter %/build.gradle,$(GRADLE_FILES))), \
                              $(patsubst %/gradle.properties,%,$(filter %/gradle.properties,$(GRADLE_FILES))))
MODULES            = $(MAVEN_MODULES) $(GRADLE_MODULES)
MVN_LOG           := redirectTo("$(ROOT_DIR)/maven.log");
M2_HOME           := $(ROOT_DIR)/$(TARGET_DIR)/.gradle-settings
EVAL              := //

CLASSPATH := $(shell                                                                    \
    File classPath = new File("$(MY_DIR)/java");                                        \
    List<File> javaFiles = glob(classPath.getPath() +  "/{*.java,**/*.java}");          \
    if (javaFiles.stream().anyMatch(                                                    \
        javaFile -> {                                                                   \
            File classFile = new File(javaFile.getPath().replace(".java", ".class"));   \
            return (!classFile.exists()                                                 \
                    || javaFile.lastModified() > classFile.lastModified()); })) {       \
        String javac = getOS() == OS.WINDOWS ? "javac.exe" : "javac";                   \
        String JAVA_HOME = System.getenv("JAVA_HOME");                                  \
        if (JAVA_HOME != null) {                                                        \
            File f = new File(new File(new File(JAVA_HOME), "bin"), javac);             \
            if (f.exists())                                                             \
                javac = f.getAbsolutePath(); }                                          \
        List<String> cmd = new ArrayList<>();                                           \
        cmd.add(javac);                                                                 \
        cmd.add("-cp");                                                                 \
        cmd.add(classPath.getPath() + File.pathSeparator + "$(MY_DIR)/lib");            \
        for (File f : javaFiles) cmd.add(f.getPath());                                  \
        exitOnError(captureOutput(err::println, cmd)); }                                \
    println(classPath.getPath());                                                       )
IMPORTS := build.mvn build.gradle build.mvn.Coords
STATIC_IMPORTS := build.core.*

export ROOT_DIR MY_DIR TARGET_DIR MVN_SETTINGS MVN_PROPERTIES MVN_LOG MVN_RELEASE_CACHE_REPO M2_HOME MAKE SHELL IMPORTS STATIC_IMPORTS CLASSPATH
# MAKECMDGOALS used in gradle-release.sh and mvn-release.sh
export MAKECMDGOALS
# MAKEFLAGS exported by default

eval-java = $(ROOT_DIR)/$(SHELL) $(call quote-for-bash,$1)

rwildcard = $(shell if (new File("$1").isDirectory()) glob("$1**/$2").forEach(x -> println(x.getPath().replace(" ", "\\ ")));)
# this alternative does not support spaces in file names
#rwildcard = $(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

update-target-if-changed = $@.tmp && ((! [ -e $@ ] || ! diff -q $@ $@.tmp >/dev/null) && mv $@.tmp $@ || rm $@.tmp ) || (rm $@.tmp && false)

.PHONY : $(TARGET_DIR)/properties
$(TARGET_DIR)/properties :
	try (OutputStream s = updateFileIfChanged("$@")) { new PrintStream(s).println($(call quote-for-java,$(MVN_PROPERTIES))); }

$(TARGET_DIR)/effective-settings.xml : $(MVN_SETTINGS) $(TARGET_DIR)/properties
	/* the test is there because $(TARGET_DIR)/properties is always executed (but not always updated) */ \
	if (isOutOfDate("$@", "$^".trim().split("\\s+"))) \
		/* cd into random directory in order to force Maven "stub" project */ \
		mvn(new File("$(TARGET_DIR)"), \
		    "org.apache.maven.plugins:maven-help-plugin:2.2:effective-settings", "-Doutput=$(ROOT_DIR)/$@");

.SECONDARY : poms parents aggregators
poms : pom.xml
parents :
aggregators :
include $(TARGET_DIR)/maven.mk

$(TARGET_DIR)/maven.mk : $(TARGET_DIR)/maven-modules $(TARGET_DIR)/maven-aggregators $(TARGET_DIR)/effective-settings.xml
	try (PrintStream s = new PrintStream(new FileOutputStream("$@"))) { \
		String localRepo = xpath(new File("$(word 3,$^)"), "/*/*[local-name()='localRepository']/text()"); \
		s.println("MVN_LOCAL_REPOSITORY := " + localRepo); \
		s.println("MAVEN_AGGREGATORS := $$(shell println(slurp(\"$(word 2,$^)\"));)"); \
		s.println("MAVEN_MODULES := $$(shell println(slurp(\"$<\"));)"); \
		s.println("export MVN_LOCAL_REPOSITORY"); \
		for (String module : slurp("$(word 2,$^)").trim().split("\\s+")) \
			s.println("aggregators : " + module + "/pom.xml"); \
		for (String module : slurp("$<").trim().split("\\s+")) \
			s.println("poms : " + module + "/pom.xml"); \
		Set<String> parents = new HashSet<>(); { \
			for (String module : slurp("$<").trim().split("\\s+")) { \
				File pom = new File(module + "/pom.xml"); \
				if (!"".equals(xpath(pom, "/*/*[local-name()='parent']"))) { \
					String v = xpath(pom, "/*/*[local-name()='parent']/*[local-name()='version']/text()"); \
					if (v.endsWith("-SNAPSHOT")) { \
						String g = xpath(pom, "/*/*[local-name()='parent']/*[local-name()='groupId']/text()"); \
						String a = xpath(pom, "/*/*[local-name()='parent']/*[local-name()='artifactId']/text()"); \
						parents.add(String.format("$$(MVN_LOCAL_REPOSITORY)/%s/%s/%s/%s-%s.pom", g.replace(".", "/"), a, v, a, v)); \
					} \
				} \
			} \
		} \
		for (String p : parents) \
			s.println("parents : " + p); \
	}

$(TARGET_DIR)/maven-aggregators : aggregators poms
	mkdirs("$(dir $@)"); \
	try (PrintStream s = new PrintStream(updateFileIfChanged("$@"))) { \
		new java.util.function.Consumer<File>() { \
			public void accept(File module) { \
				try { \
					String[] submodules = xpath( \
						new File(module, "pom.xml"), "string(/*/*[local-name()='modules'])" \
					).trim().split("\\s+"); \
					if (submodules.length > 0 && !(submodules.length == 1 && submodules[0].equals(""))) { \
						s.println(module.toString()); \
						for (String m : submodules) \
							accept(".".equals(module.toString()) ? new File(m) : new File(module, m)); \
					} \
				} catch (RuntimeException e) { \
					throw e; \
				} catch (Throwable e) { \
					throw new RuntimeException(e); \
				} \
			} \
		}.accept(new File(".")); \
	}

$(TARGET_DIR)/maven-modules : aggregators poms
	mkdirs("$(dir $@)"); \
	try (PrintStream s = new PrintStream(updateFileIfChanged("$@"))) { \
		new java.util.function.Consumer<File>() { \
			public void accept(File module) { \
				try { \
					String[] submodules = xpath( \
						new File(module, "pom.xml"), "string(/*/*[local-name()='modules'])" \
					).trim().split("\\s+"); \
					if (submodules.length > 0 && !(submodules.length == 1 && submodules[0].equals(""))) { \
						for (String m : submodules) \
							accept(".".equals(module.toString()) ? new File(m) : new File(module, m)); \
					} else \
						s.println(module.toString()); \
				} catch (RuntimeException e) { \
					throw e; \
				} catch (Throwable e) { \
					throw new RuntimeException(e); \
				} \
			} \
		}.accept(new File(".")); \
	}

SAXON = $(MVN_LOCAL_REPOSITORY)/net/sf/saxon/Saxon-HE/9.4/Saxon-HE-9.4.jar
export SAXON

ifneq ($(OS), WINDOWS)
export JAVA_REPL_PORT := $(shell --spawn-repl-server)
endif

$(SAXON) : | .maven-init
	/* cd into random directory in order to force Maven "stub" project */ \
	mvn(new File("$(TARGET_DIR)"), \
	    "org.apache.maven.plugins:maven-dependency-plugin:3.0.0:get", "-Dartifact=net.sf.saxon:Saxon-HE:9.4:jar");

# the purpose of the test is for making "make -B" not affect this rule (to speed thing up)
# MAVEN_MODULES computed here because maven.mk may not be up to date yet
# FIXME: the mvn command below depends on the settings.xml.in file which is not inside this directory
$(TARGET_DIR)/effective-pom.xml : $(TARGET_DIR)/maven-modules poms | $(SAXON) $(MVN_SETTINGS)
	String MAVEN_MODULES = slurp("$<"); \
	String[] modules = MAVEN_MODULES.trim().split("\\s+"); \
	File[] poms = new File[modules.length]; \
	for (int i = 0; i < poms.length; i++) \
		poms[i] = new File(modules[i], "pom.xml"); \
	if (isOutOfDate(new File("$@"), poms)) { \
		rm("$(TARGET_DIR)/poms"); \
		for (File pom : poms) { \
			mvn.Coords gav = getMavenCoords(pom); \
			String g = gav.g; \
			String a = gav.a; \
			String v = gav.v; \
			File dest = new File("$(TARGET_DIR)/poms/" + String.format("%s/%s/%s/%s-%s.pom", g.replace(".", "/"), a, v, a, v)); \
			mkdirs(dest.getParentFile()); \
			cp(pom, dest); \
			if (v.endsWith("-SNAPSHOT")) \
				exitOnError( \
					captureOutput( \
						new PrintStream(new FileOutputStream(dest))::println, \
						"java", "-cp", "$(SAXON)", "net.sf.saxon.Transform", \
						        "-s:" + pom, \
						        "-xsl:$(MY_DIR)/mvn-set-version.xsl", \
						        "VERSION=" + v)); \
		} \
		int rv = captureOutput( \
			new PrintStream(new FileOutputStream("$(ROOT_DIR)/maven.log"))::println, \
			"mvn", "--batch-mode", "--settings", "$(ROOT_DIR)/settings.xml.in", \
			       "-Dworkspace=$(TARGET_DIR)/poms", \
			       "-Dcache=.maven-cache", \
			       "--projects", String.join(",", modules), \
			       "-Prun-script-webserver", "-Ddocumentation", \
			       "org.apache.maven.plugins:maven-help-plugin:2.2:effective-pom", "-Doutput=$(ROOT_DIR)/$@"); \
		if (rv != 0) { \
			err.println("Failed to compute Maven dependencies."); \
			exit(rv); \
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

.gradle-init : | .maven-init $(TARGET_DIR)/.gradle-settings/conf/settings.xml
$(TARGET_DIR)/.gradle-settings/conf/settings.xml : $(MVN_SETTINGS)
	mkdirs("$(dir $@)"); \
	rm("$@"); \
	cp("$<", "$@");

# MAVEN_MODULES computed here because maven.mk may not be up to date yet
.SECONDARY : .maven-deps.mk
.maven-deps.mk : $(TARGET_DIR)/maven-modules $(TARGET_DIR)/effective-pom.xml $(TARGET_DIR)/gradle-pom.xml | $(SAXON)
	computeMavenDeps(new File("$<"), \
	                 new File("$(word 2,$^)"), \
	                 new File("$(word 3,$^)"), \
	                 new File("$(TARGET_DIR)/mk"), \
	                 ".deps.mk");

ifneq ($(MAKECMDGOALS), clean)
$(addsuffix /.deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(MAVEN_MODULES))) : .maven-deps.mk
	if (!new File("$@").exists()) { \
		mkdirs("$(dir $@)"); \
		try (PrintStream s = new PrintStream(new FileOutputStream("$@"))) { \
			s.println("$$(error $@ could not be generated)"); \
		} \
	} \
	touch("$@");
endif

ifneq ($(MAKECMDGOALS), clean)
$(addsuffix /.deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(GRADLE_MODULES))) : $(GRADLE_FILES)
	computeGradleDeps("$(patsubst $(TARGET_DIR)/mk/%/.deps.mk,%,$@)", \
	                  new File("$(TARGET_DIR)/mk"), \
	                  ".deps.mk");
endif

ifneq ($(OS), WINDOWS)

PHONY : $(addprefix eclipse-,$(MODULES))
$(addprefix eclipse-,$(MODULES)) : eclipse-% : %/.project

# mvn-eclipse.sh requires parent poms to be installed because it uses `mvn --projects ...`
$(addsuffix /.project,$(MODULES)) : parents

$(addsuffix /.project,$(MODULES)) : .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.m2e.core.prefs
.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.m2e.core.prefs : | $(TARGET_DIR)/effective-settings.xml .group-eval
	$(EVAL) $(call bash, $(MY_DIR)/eclipse-init.sh)

endif

# FIXME: specifying "--debug" option breaks this code
# - passing "MAKEFLAGS=" does not fix it for some reason, and also has unwanted side effects
# - passing "--debug=no" to the sub-make would be a solution but not all versions of make support it
.SECONDARY : .group-eval
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
			else take = false; \
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
clean : clean-eclipse
	rm("$(TARGET_DIR)"); \
	rm("maven.log"); \
	glob("**/.last-tested").forEach(x -> rm(x));

.PHONY : clean-eclipse
clean-eclipse :
	rm(".metadata");

ifeq ($(MAKECMDGOALS), clean)
include $(shell for (String f : "$(addsuffix /.deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(MODULES) $(MAVEN_AGGREGATORS)))".trim().split("\\s+")) \
                    if (new File(f).exists()) println(f);)
else ifeq ($(MAKECMDGOALS), clean-eclipse)
include $(shell for (String f : "$(addsuffix /.deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(MODULES) $(MAVEN_AGGREGATORS)))".trim().split("\\s+")) \
                    if (new File(f).exists()) println(f);)
else
-include $(addsuffix /.deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(MODULES) $(MAVEN_AGGREGATORS)))
endif

ifdef SKIP_GROUP_EVAL_TARGET
.SILENT:
else ifndef VERBOSE
.SILENT:
endif

define \n


endef
quote-for-bash = '$(subst ${\n},'"\n"',$(subst \,\\,$(subst ','"'"',$(1))))'
