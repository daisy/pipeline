include make/enable-java-shell.mk

ifeq ($(OS), WINDOWS)
MVN ?= mvn.cmd
else
MVN ?= mvn
endif

DOCKER := docker
--classifier ?=
CLASSIFIER := $(shell println("$(--classifier)".replaceAll("^.+$$", "-$$0"));)

.PHONY : help
help :
	@err.println(                                                                 \
		"make help:"                                                     + "\n" + \
		"    Print list of commands");                                            \
	err.println(                                                                  \
		"make deb:"                                                      + "\n" + \
		"    Builds a DEB (Debian package)");                                     \
	if (getOS() == OS.REDHAT)                                                     \
		err.println(                                                              \
			"make rpm:"                                                  + "\n" + \
			"    Builds a RPM (RedHat package)");                                 \
	err.println(                                                                  \
		"make zip-linux:"                                                + "\n" + \
		"    Builds a ZIP for Linux"                                     + "\n" + \
		"make zip-mac:"                                                  + "\n" + \
		"    Builds a ZIP for Mac OS"                                    + "\n" + \
		"make zip-win:"                                                  + "\n" + \
		"    Builds a ZIP for Windows"                                   + "\n" + \
		"make dir-word-addin:"                                           + "\n" + \
		"	Builds a directory to be included in SaveAsDAISY"            + "\n" + \
		"make zip-minimal:"                                              + "\n" + \
		"    Builds a minimal ZIP that will complete itself upon first update");  \
	if (getOS() != OS.WINDOWS)                                                    \
		err.println(                                                              \
			"make docker:"                                               + "\n" + \
			"    Builds a Docker image"                                  + "\n" + \
			"make check-docker:"                                         + "\n" + \
			"    Tests the Docker image"                                 + "\n" + \
			"make dev-launcher:"                                         + "\n" + \
			"    Builds a version that can be run directly on the current platform");

assembly/VERSION             := $(shell println(xpath(new File("pom.xml"), "/*/*[local-name()='version']/text()"));)
assembly/BASEDIR             := .
DEFAULT_MVN_LOCAL_REPOSITORY := $(shell println(System.getProperty("user.home").replace("\\", "/"));)/.m2/repository
MVN_LOCAL_REPOSITORY         ?= $(DEFAULT_MVN_LOCAL_REPOSITORY)
INSTALL_DIR                  := $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)

include deps.mk

.PHONY : deb rpm zip-linux zip-mac zip-win zip-minimal deb-cli rpm-cli

deb         : $(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER).deb
rpm         : $(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER).rpm
zip-linux   : $(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-linux.zip
zip-mac     : $(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-mac.zip
zip-win     : $(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-win.zip
zip-minimal : $(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-minimal.zip
deb-cli     : $(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-cli.deb
rpm-cli     : $(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-cli.rpm

.PHONY : release-descriptor
release-descriptor : target/release-descriptor/releaseDescriptor.xml
target/release-descriptor/releaseDescriptor.xml : mvn -Pgenerate-release-descriptor

# some artifacts are installed through command line
$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-linux.zip   \
$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-mac.zip     \
$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-win.zip     \
$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-minimal.zip : $(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)% : target/assembly-$(assembly/VERSION)%
ifndef DUMP_PROFILES
	exec("$(MVN)", "install:install-file",                                                                               \
	               "-Dfile=$<",                                                                                          \
	               "-DpomFile=pom.xml",                                                                                  \
	               "-Dclassifier=$(patsubst -%,%,$(patsubst assembly-$(assembly/VERSION)%,%,$(basename $(notdir $@))))", \
	               "-Dpackaging=$(patsubst .%,%,$(suffix $@))");
	exit(new File("$@").exists());
endif

$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER).deb                  : mvn -Pcopy-artifacts \
                                                                                    -Pgenerate-release-descriptor \
                                                                                    -Punpack-updater-linux \
                                                                                    -Ppackage-deb
target/assembly-$(assembly/VERSION)-linux.zip                                 : mvn -Pcopy-artifacts \
                                                                                    -Pgenerate-release-descriptor \
                                                                                    -Punpack-cli-linux \
                                                                                    -Punpack-updater-linux \
                                                                                    -Passemble-linux-zip
target/assembly-$(assembly/VERSION)-mac.zip                                   : mvn -Pcopy-artifacts \
                                                                                    -Pgenerate-release-descriptor \
                                                                                    -Punpack-cli-mac \
                                                                                    -Punpack-updater-mac
ifneq (--without-jre,$(filter --without-jre --with-jre,$(MAKECMDGOALS)))
target/assembly-$(assembly/VERSION)-mac.zip                                   : mvn -Pbuild-jre-mac
endif
ifndef DUMP_PROFILES
target/assembly-$(assembly/VERSION)-mac.zip                                   :
ifeq ($(OS), MACOSX)
	List<String> identities = new ArrayList<>();                                                                \
	List<String> identityDescriptions = new ArrayList<>();                                                      \
	Pattern ID_LINE = Pattern.compile("^ *[0-9]+\\) +(?<id>[0-9A-F]+) +\"(?<desc>.+)\" *$$");                   \
	exitOnError(                                                                                                \
	    captureOutput(                                                                                          \
	        line -> {                                                                                           \
	            Matcher m = ID_LINE.matcher(line);                                                              \
	            if (m.matches()) {                                                                              \
	                identities.add(m.group("id"));                                                              \
	                identityDescriptions.add("" + identities.size() + ") " + m.group("desc")); }},              \
	        "security", "find-identity", "-v", "-p", "codesigning"));                                           \
	if (identities.isEmpty())                                                                                   \
	    err.println("No identity found to sign code");                                                          \
	else {                                                                                                      \
	    String id = identities.size() == 1 ? identities.get(0) : null;                                          \
	    File tmpDir = new File("target/codesign-workaround/");                                                  \
	    File jar = new File("target/jars/common/com.microsoft.cognitiveservices.speech.client-sdk-1.27.0.jar"); \
	    File unzipDir = new File(tmpDir, jar.getName().replaceAll(".jar$$", ""));                               \
	    mkdirs(unzipDir);                                                                                       \
	    // FIXME: not using unzip() because it currently does not preserve file permissions                     \
	    //unzip(jar, unzipDir);                                                                                 \
	    exitOnError(captureOutput(err::println, unzipDir, "unzip", jar.getAbsolutePath()));                     \
	    // replace signature of *.extension.kws.ort.dylib files                                                 \
	    for (File f : glob(unzipDir.getPath() + "/ASSETS/osx-*/*.extension.kws.ort.dylib")) {                   \
	        exitOnError(captureOutput(err::println, "codesign", "--remove-signature", f.getPath()));            \
	        if (id == null) {                                                                                   \
	            err.println("Choose identity to sign code (move with arrows and press ENTER):");                \
	            try {                                                                                           \
	                id = identities.get(prompt(identityDescriptions));                                          \
	            } catch (IOException e) {                                                                       \
	                System.exit(1);                                                                             \
	            }                                                                                               \
	        }                                                                                                   \
	        exitOnError(captureOutput(err::println, "codesign", "-s", id, "-v", f.getPath())); }                \
	    // delete META-INF folder with signature files                                                          \
	    rm(new File(unzipDir, "META-INF"));                                                                     \
	    File fixedJar = new File(tmpDir, jar.getName());                                                        \
	    exitOnError(                                                                                            \
	        captureOutput(err::println, "jar", "cvf", fixedJar.getPath(), "-C", unzipDir.getPath(), "."));      \
	    rm(jar);                                                                                                \
	    cp(fixedJar, jar);                                                                                      \
	    rm(tmpDir);                                                                                             \
	}
endif
	exec("$(MVN)", "assembly:single", "-Passemble-mac-zip");
	exit(new File("$@").exists());
endif
target/assembly-$(assembly/VERSION)-win.zip                                   : mvn -Pcopy-artifacts \
                                                                                    -Pgenerate-release-descriptor \
                                                                                    -Punpack-cli-win \
                                                                                    -Punpack-updater-win
ifeq (--without-jre,$(filter --without-jre --with-jre,$(MAKECMDGOALS)))
target/assembly-$(assembly/VERSION)-win.zip                                   : mvn -Passemble-win-zip
ifndef DUMP_PROFILES
	exit(new File("$@").exists());
endif
else
target/assembly-$(assembly/VERSION)-win.zip                                   : mvn -Pbuild-jre-win64
ifeq (--with-jre32,$(filter --with-jre32,$(MAKECMDGOALS)))
target/assembly-$(assembly/VERSION)-win.zip                                   : mvn -Pbuild-jre-win32
endif # --with-jre32
# -Passemble-win-zip run separately because -Pbuild-jre-win64 also run separately
ifndef DUMP_PROFILES
	exec("$(MVN)", "assembly:single", "-Passemble-win-zip");
	exit(new File("$@").exists());
endif
endif # --without-jre
target/assembly-$(assembly/VERSION)-minimal.zip                               : mvn -Pcopy-artifacts \
                                                                                    -Pgenerate-release-descriptor \
                                                                                    -Punpack-updater-mac \
                                                                                    -Punpack-updater-linux \
                                                                                    -Punpack-updater-win \
                                                                                    -Passemble-minimal-zip
$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-cli.deb              : mvn -Pcopy-artifacts \
                                                                                    -Pgenerate-release-descriptor \
                                                                                    -Punpack-cli-linux \
                                                                                    -Ppackage-deb-cli
ifeq ($(OS), REDHAT)
$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER).rpm                   : mvn -Pcopy-artifacts \
                                                                                     -Pgenerate-release-descriptor \
                                                                                     -Passemble-linux-dir \
                                                                                     -Ppackage-rpm
ifndef DUMP_PROFILES
	exit(new File("$@").exists());
endif
$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-cli.rpm               : mvn -Pcopy-artifacts \
                                                                                     -Pgenerate-release-descriptor \
                                                                                     -Punpack-cli-linux \
                                                                                     -Ppackage-rpm-cli
ifndef DUMP_PROFILES
	exit(new File("$@").exists());
endif
else
$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER).rpm \
$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-cli.rpm :
	@err.println("Can not build RPM because not running RedHat/CentOS"); \
	exit(1);
endif # eq ($(OS), REDHAT)

.PHONY : dir-word-addin
# Note that when `dir-word-addin' is enabled together with other targets, it is as if --without-osgi, --without-persistence,
# --without-webservice, --without-cli, --without-updater and --with-simple-api were also specified.
dir-word-addin                                                                 : assembly/SOURCES
dir-word-addin                                                                 : mvn -Pwithout-osgi \
                                                                                     -Pwithout-persistence \
                                                                                     -Pwithout-webservice \
                                                                                     -Pwithout-cli \
                                                                                     -Pwithout-updater \
                                                                                     -Pwith-simple-api \
                                                                                     -Pcopy-artifacts \
                                                                                     -Pbuild-jre-win32 \
                                                                                     -Pbuild-jre-win64
ifndef DUMP_PROFILES
	exec("$(MVN)", "assembly:single", "-Passemble-win-dir");
endif

ifneq ($(OS), WINDOWS)

.PHONY : docker
# Note that when `docker' is enabled together with other targets, it is as if --without-osgi was also specified.
docker : mvn -Pwithout-osgi \
         target/maven-jlink/classifiers/linux \
         target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2
ifndef DUMP_PROFILES
	mkdirs("target/docker");                                                                        \
	exec("cp", "src/main/docker/Dockerfile", "target/docker/Dockerfile");
	exec("cp", "src/main/docker/logback.xml", "target/docker/logback.xml");
	exec("cp", "-r", "target/assembly-$(assembly/VERSION)-linux/daisy-pipeline", "target/docker/");
	exec("cp", "-r", "$(word 3,$^)", "target/docker/jre");
	exec(new File("target/docker"),                                                                 \
	     "$(DOCKER)", "build", "-t", "daisyorg/pipeline:latest-snapshot", ".");
endif

.PHONY : dev-launcher
dev-launcher : target/dev-launcher/pipeline2
target/dev-launcher/pipeline2 : pom.xml
ifdef BUILD_JRE_FOR_DEV_LAUNCHER
ifeq ($(OS), MACOSX)
target/dev-launcher/pipeline2 : target/maven-jlink/classifiers/mac target/assembly-$(assembly/VERSION)-mac/daisy-pipeline/bin/pipeline2
else
target/dev-launcher/pipeline2 : target/maven-jlink/classifiers/linux target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2
endif
ifndef DUMP_PROFILES
	mkdirs("$(dir $@)");                                \
	File f = new File("$@");                            \
	f.delete();                                         \
	write(f, "#!/usr/bin/env bash\n");                  \
	write(f, "JAVA_HOME=$(CURDIR)/$(word 1,$^) \\\n");  \
	write(f, "$(CURDIR)/$(word 2,$^) \"$$@\"\n");       \
	exec("chmod", "+x", "$@");
endif
else
ifeq ($(OS), MACOSX)
target/dev-launcher/pipeline2 : target/assembly-$(assembly/VERSION)-mac/daisy-pipeline/bin/pipeline2
else
target/dev-launcher/pipeline2 : target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2
endif
ifndef DUMP_PROFILES
	mkdirs("$(dir $@)");                                \
	File f = new File("$@");                            \
	f.delete();                                         \
	write(f, "#!/usr/bin/env bash\n");                  \
	write(f, "$(CURDIR)/$< \"$$@\"\n");                 \
	exec("chmod", "+x", "$@");
endif
endif

target/maven-jlink/classifiers/mac                                     : mvn -Pbuild-jre-mac
target/maven-jlink/classifiers/linux                                   : mvn -Pbuild-jre-linux

target/assembly-$(assembly/VERSION)-mac/daisy-pipeline/bin/pipeline2   : mvn -Pcopy-artifacts \
                                                                             -Pgenerate-release-descriptor \
                                                                             -Punpack-cli-mac \
                                                                             -Punpack-updater-mac \
                                                                             -Passemble-mac-dir
target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2 : mvn -Pcopy-artifacts \
                                                                             -Pgenerate-release-descriptor \
                                                                             -Punpack-cli-linux \
                                                                             -Punpack-updater-linux \
                                                                             -Passemble-linux-dir

endif # neq ($(OS), WINDOWS)

$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER).deb           \
$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER)-cli.deb       \
target/assembly-$(assembly/VERSION)-linux.zip             \
target/assembly-$(assembly/VERSION)-minimal.zip           \
target/assembly-$(assembly/VERSION)-mac/daisy-pipeline/bin/pipeline2   \
target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2 :
ifndef DUMP_PROFILES
	exit(new File("$@").exists());
endif

ifneq ($(OS), WINDOWS)
.PHONY : check
check : check-docker

.PHONY : check-docker
check-docker :
	exec("bash", "src/test/resources/test-docker-image.sh");
endif # neq ($(OS), WINDOWS)

.PHONY : clean
clean :
	exec("$(MVN)", "clean");
	rm("make/recipes");

#                         process-sources      generate-resources      process-resources      prepare-package       package
#                         ---------------      ---------------         -----------------      ---------------       -------
# copy-artifacts          copy-felix-launcher
#                         copy-felix-bundles
#                         copy-felix-gogo
#                         copy-framework
#                         copy-framework-osgi
#                         copy-framework-no-osgi
#                         copy-persistence
#                         copy-persistence-osgi
#                         copy-persistence-no-osgi
#                         copy-webservice
#                         copy-modules
#                         copy-modules-osgi
#                         copy-modules-linux
#                         copy-modules-mac
#                         copy-modules-win
# generate-release-descriptor                  generate-effective-pom
#                                              generate-release-descriptor
# build-jre-linux                                                                                                   jlink-linux
# build-jre-win32                                                                                                   jlink-win32
# build-jre-win64                                                                                                   jlink-win64
# build-jre-mac                                                                                                     jlink-mac
# unpack-cli-mac                               unpack-cli-mac
# unpack-cli-linux                             unpack-cli-linux
# unpack-cli-win                               unpack-cli-win
# unpack-updater-mac                           unpack-updater-mac
# unpack-updater-linux                         unpack-updater-linux
# unpack-updater-win                           unpack-updater-win
# assemble-mac-dir                                                                            assemble-mac-dir
# assemble-linux-dir                                                                          assemble-linux-dir
# assemble-win-dir                                                                            assemble-win-dir
# assemble-mac-zip                                                                                                  assemble-mac-zip
# assemble-linux-zip                                                                                                assemble-linux-zip
# assemble-win-zip                                                                                                  assemble-win-zip
# assemble-minimal-zip                                                                                              assemble-minimal-zip
# package-deb                                                          filter-deb-resources                         package-deb
# package-deb-cli                                                                                                   package-deb-cli
# package-rpm                                                                                                       package-rpm
# package-rpm-cli                                                                                                   package-rpm-cli

PROFILES :=                     \
	copy-artifacts              \
	generate-release-descriptor \
	assemble-linux-dir          \
	assemble-linux-zip          \
	assemble-mac-dir            \
	assemble-mac-zip            \
	assemble-win-dir            \
	assemble-win-zip            \
	assemble-minimal-zip        \
	package-deb                 \
	package-deb-cli             \
	package-rpm                 \
	package-rpm-cli             \
	unpack-cli-linux            \
	unpack-cli-mac              \
	unpack-cli-win              \
	unpack-updater-linux        \
	unpack-updater-mac          \
	unpack-updater-win

.PHONY : --with-persistence --without-persistence
--without-persistence : -Pwithout-persistence
ifneq (--with-persistence,$(filter --with-persistence,$(MAKECMDGOALS)))
PROFILES += without-persistence
else
.PHONY : -Pwithout-persistence
endif

.PHONY : --with-osgi --without-osgi
--without-osgi : -Pwithout-osgi
ifneq (--with-osgi,$(filter --with-osgi,$(MAKECMDGOALS)))
PROFILES += without-osgi
else
.PHONY : -Pwithout-osgi
endif

.PHONY : --with-webservice --without-webservice
--without-webservice : -Pwithout-webservice
ifneq (--with-webservice,$(filter --with-webservice,$(MAKECMDGOALS)))
PROFILES += without-webservice
else
.PHONY : -Pwithout-webservice
endif

.PHONY : --with-cli --without-cli
--without-cli : -Pwithout-cli
ifneq (--with-cli,$(filter --with-cli,$(MAKECMDGOALS)))
PROFILES += without-cli
else
.PHONY : -Pwithout-cli
endif

.PHONY : --with-updater --without-updater
--without-updater : -Pwithout-updater
ifneq (--with-updater,$(filter --with-updater,$(MAKECMDGOALS)))
PROFILES += without-updater
else
.PHONY : -Pwithout-updater
endif

.PHONY : --with-simple-api --without-simple-api
--with-simple-api : -Pwith-simple-api
ifneq (--without-simple-api,$(filter --without-simple-api,$(MAKECMDGOALS)))
PROFILES += with-simple-api
else
.PHONY : -Pwith-simple-api
endif

# handled above (zip-mac and zip-win)
.PHONY : --with-jre --without-jre

# handled above (zip-win)
.PHONY : --with-jre32 --without-jre32

.PHONY : mvn
mvn :
ifndef DUMP_PROFILES
	@List<String> cmd = new ArrayList<>();                                                                                   \
	cmd.add("$(MVN)");                                                                                                       \
	cmd.add("clean");                                                                                                        \
	cmd.add("install");                                                                                                      \
	cmd.add("-Dclassifier=$(--classifier)");                                                                                 \
	cmd.add("-Dclassifier.dash=$(shell println("$(--classifier)".replaceAll("^.+$$", "$$0-"));)");                           \
	exitOnError(                                                                                                             \
		captureOutput(                                                                                                       \
			line -> { if (line.startsWith("-P")) cmd.add(line); },                                                           \
			Arrays.asList("$(MAKE) -s --no-print-directory ECHO=true DUMP_PROFILES=true -- $(MAKECMDGOALS)".split("\\s")))); \
	println(String.join(" ", cmd));                                                                                          \
	exec(runInShell(cmd));
endif

.PHONY : $(addprefix -P,$(PROFILES))
ifdef DUMP_PROFILES
$(addprefix -P,$(PROFILES)) :
	@println("$@");
endif

# profiles that require Java 10

ifdef DUMP_PROFILES
ifeq ($(shell println($(JAVA_VERSION) >= 10);), false)
--with-simple-api : require-java-10
.PHONY : require-java-10
require-java-10 :
	@err.println("Java 10 is required to compile SimpleAPI.java"); \
	exit(1);
endif
endif

# profiles that are run separately because need to be run with specific JDKs, because they should not be
# installed, and because they require a pom without dependencies

.PHONY : -Pbuild-jre-mac -Pbuild-jre-linux -Pbuild-jre-win32 -Pbuild-jre-win64
-Pbuild-jre-mac -Pbuild-jre-linux -Pbuild-jre-win32 -Pbuild-jre-win64 : mvn # to make sure they are run after other profiles

ifeq ($(OS), MACOSX)
-Pbuild-jre-linux -Pbuild-jre-win32 -Pbuild-jre-win64 -Pbuild-jre-mac : src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.7_7/jdk-17.0.7+7
ifndef DUMP_PROFILES
	rm("target/classes");                                \
	exec(env("JAVA_HOME", "$(CURDIR)/$</Contents/Home"), \
	     "$(MVN)", "-f", "build-jre.xml", "jlink:jlink", "$@");
endif
else ifeq ($(OS), WINDOWS)
-Pbuild-jre-linux -Pbuild-jre-win32 -Pbuild-jre-win64                 : src/main/jre/OpenJDK17U-jdk_x64_windows_hotspot_17.0.7_7/jdk-17.0.7+7
ifndef DUMP_PROFILES
	rm("target/classes");                                \
	exec(env("JAVA_HOME", "$(CURDIR)/$<"),               \
	     "$(MVN)", "-f", "build-jre.xml", "jlink:jlink", "$@");
endif
else
-Pbuild-jre-linux -Pbuild-jre-win32 -Pbuild-jre-win64                 : src/main/jre/OpenJDK17U-jdk_x64_linux_hotspot_17.0.7_7/jdk-17.0.7+7
ifndef DUMP_PROFILES
	rm("target/classes");                                \
	exec(env("JAVA_HOME", "$(CURDIR)/$<"),               \
	     "$(MVN)", "-f", "build-jre.xml", "jlink:jlink", "$@");
endif
endif

# for dependencies to jmods
-Pbuild-jre-mac   : src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.7_7/jdk-17.0.7+7
-Pbuild-jre-linux : src/main/jre/OpenJDK17U-jdk_x64_linux_hotspot_17.0.7_7/jdk-17.0.7+7
-Pbuild-jre-win64 : src/main/jre/OpenJDK17U-jdk_x64_windows_hotspot_17.0.7_7/jdk-17.0.7+7
-Pbuild-jre-win32 : src/main/jre/OpenJDK17U-jdk_x86-32_windows_hotspot_17.0.7_7/jdk-17.0.7+7

# JDKs

src/main/jre/OpenJDK17U-jdk_x64_windows_hotspot_17.0.7_7/jdk-17.0.7+7 \
src/main/jre/OpenJDK17U-jdk_x86-32_windows_hotspot_17.0.7_7/jdk-17.0.7+7 : %/jdk-17.0.7+7 : %.zip
	mkdirs("$(dir $@)"); \
	unzip(new File("$<"), new File("$(dir $@)"));

ifneq ($(OS), WINDOWS)
src/main/jre/OpenJDK17U-jdk_x64_linux_hotspot_17.0.7_7/jdk-17.0.7+7 : %/jdk-17.0.7+7 : | %.tar.gz
	mkdirs("$(dir $@)"); \
	exec("tar", "-zxvf", "src/main/jre/OpenJDK17U-jdk_x64_linux_hotspot_17.0.7_7.tar.gz", "-C", "$(dir $@)/");
src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.7_7/jdk-17.0.7+7   : %/jdk-17.0.7+7 : | %.tar.gz
	mkdirs("$(dir $@)"); \
	exec("tar", "-zxvf", "src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.7_7.tar.gz", "-C", "$(dir $@)/");
endif

.INTERMEDIATE : src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.7_7.tar.gz
.INTERMEDIATE : src/main/jre/OpenJDK17U-jdk_x64_linux_hotspot_17.0.7_7.tar.gz
.INTERMEDIATE : src/main/jre/OpenJDK17U-jdk_x86-32_windows_hotspot_17.0.7_7.zip
.INTERMEDIATE : src/main/jre/OpenJDK17U-jdk_x64_windows_hotspot_17.0.7_7.zip

src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.7_7.tar.gz \
src/main/jre/OpenJDK17U-jdk_x64_linux_hotspot_17.0.7_7.tar.gz \
src/main/jre/OpenJDK17U-jdk_x86-32_windows_hotspot_17.0.7_7.zip \
src/main/jre/OpenJDK17U-jdk_x64_windows_hotspot_17.0.7_7.zip :
	mkdirs("$(dir $@)");                                                                                          \
	copy(new URL("https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.7%2B7/$(notdir $@)"), \
	     new File("$@"));

.PHONY : clean-jdk
clean : clean-jdk
clean-jdk :
	rm("src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.7_7/jdk-17.0.7+7");        \
	rm("src/main/jre/OpenJDK17U-jdk_x64_linux_hotspot_17.0.7_7/jdk-17.0.7+7");      \
	rm("src/main/jre/OpenJDK17U-jdk_x64_windows_hotspot_17.0.7_7/jdk-17.0.7+7");    \
	rm("src/main/jre/OpenJDK17U-jdk_x86-32_windows_hotspot_17.0.7_7/jdk-17.0.7+7");
