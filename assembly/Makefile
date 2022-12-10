ifneq ($(firstword $(sort $(MAKE_VERSION) 3.82)), 3.82)
$(error "GNU Make 3.82 is required to run this script")
endif

ifeq ($(OS),Windows_NT)
SHELL := make\\eval-java.exe
else
SHELL := make/eval-java
endif
.SHELLFLAGS :=

JAVA_VERSION := $(shell println(getJavaVersion());)

ifeq ($(JAVA_VERSION),)
# probably because java not found or exited with a UnsupportedClassVersionError
$(error "Java 8 is required to run this script")
else ifeq ($(shell println($(JAVA_VERSION) >= 8);), false)
$(error "Java 8 is required to run this script")
endif

OS := $(shell println(getOS());)

ifeq ($(OS), WINDOWS)
MVN ?= mvn.cmd
else
MVN ?= mvn
endif

DOCKER := docker
--classifier ?=
CLASSIFIER := $(shell println("$(--classifier)".replaceAll("^.+$$", "-$$0"));)

.PHONY : default
ifeq ($(OS), WINDOWS)
default : exe
else ifeq ($(OS), MACOSX)
default : dmg
else ifeq ($(OS), REDHAT)
default : rpm
else
default : zip-linux
endif

.PHONY : help
help :
	@err.println(                                                                 \
		"make [default]:"                                                + "\n" + \
		"    Builds the default package for the current platform");               \
	if (getOS() == OS.MACOSX)                                                     \
		err.println(                                                              \
			"make dmg:"                                                  + "\n" + \
			"    Builds a DMG image (Mac OS disk image)");                        \
	err.println(                                                                  \
		"make exe:"                                                      + "\n" + \
		"    Builds a EXE (Windows installer)"                           + "\n" + \
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

.PHONY : dmg exe deb rpm zip-linux zip-mac zip-win zip-minimal deb-cli rpm-cli

dmg         : $(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER).dmg
exe         : $(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER).exe
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
$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER).dmg         \
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

$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER).exe                  : mvn -Pcopy-artifacts \
                                                                                    -Pgenerate-release-descriptor \
                                                                                    -Punpack-cli-win \
                                                                                    -Punpack-updater-win \
                                                                                    -Punpack-updater-gui-win \
                                                                                    -Passemble-win-dir \
                                                                                    -Ppackage-exe
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
ifeq (--without-jre,$(filter --without-jre --with-jre,$(MAKECMDGOALS)))
target/assembly-$(assembly/VERSION)-mac.zip                                   : mvn -Passemble-mac-zip
ifndef DUMP_PROFILES
	exit(new File("$@").exists());
endif
else
target/assembly-$(assembly/VERSION)-mac.zip                                   : mvn -Pbuild-jre-mac
# -Passemble-mac-zip run separately because -Pbuild-jre-mac also run separately
ifndef DUMP_PROFILES
	exec("$(MVN)", "package", "-Passemble-mac-zip");
	exit(new File("$@").exists());
endif
endif # --without-jre
target/assembly-$(assembly/VERSION)-win.zip                                   : mvn -Pcopy-artifacts \
                                                                                    -Pgenerate-release-descriptor \
                                                                                    -Punpack-cli-win \
                                                                                    -Punpack-updater-win \
                                                                                    -Punpack-updater-gui-win
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
	exec("$(MVN)", "package", "-Passemble-win-zip");
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
ifeq ($(OS), MACOSX)
app_version := $(shell println("$(assembly/VERSION)".replaceAll("-.*$$", ""));)
target/assembly-$(assembly/VERSION).dmg                                        : mvn -Pcopy-artifacts \
                                                                                     -Pgenerate-release-descriptor \
                                                                                     -Punpack-updater-mac \
                                                                                     -Passemble-mac-app-dir \
                                                                                     -Pbuild-jre-mac
ifndef DUMP_PROFILES
	rm("target/jpackage");                                                                               \
	String[] guiJar = new File("target/assembly-$(assembly/VERSION)-mac-app/daisy-pipeline/system/gui")  \
	                  .list((dir, name) -> name.matches("org\\.daisy\\.pipeline\\.gui-.*\\.jar"));       \
	exitOnError(guiJar != null && guiJar.length == 1);                                                   \
	exec("src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.3_7/jdk-17.0.3+7/Contents/Home/bin/jpackage", \
	     "--dest", "target/jpackage",                                                                    \
	     "--type", "dmg",                                                                                \
	     "--app-version", "$(app_version)",                                                              \
	     "--description", "A tool for automated production of accessible digital publication",           \
	     "--name", "DAISY Pipeline 2",                                                                   \
	     "--vendor", "DAISY Consortium",                                                                 \
	     "--icon", "src/main/mac/pipeline.icns",                                                         \
	     "--mac-package-identifier", "org.daisy.pipeline2",                                              \
	     "--runtime-image", "target/maven-jlink/classifiers/jre-mac",                                    \
	     "--input", "target/assembly-$(assembly/VERSION)-mac-app/daisy-pipeline",                        \
	     "--main-jar", "system/gui/" + guiJar[0],                                                        \
	     "--main-class", "org.daisy.pipeline.gui.GUIService",                                            \
	     "--java-options", "--add-opens=java.base/java.lang=ALL-UNNAMED "                              + \
	                       "-Dorg.daisy.pipeline.home=$$APPDIR "                                       + \
	                       "-Dorg.daisy.pipeline.data=$$APPDIR/data "                                  + \
	                       "-Dorg.daisy.pipeline.logdir=$$APPDIR/log "                                 + \
	                       "-Dorg.daisy.pipeline.properties=$$APPDIR/etc/pipeline.properties "         + \
	                       "-Dlogback.configurationFile=$$APPDIR/etc/logback.xml");
	exec("mv", "target/jpackage/DAISY Pipeline 2-$(app_version).dmg", "$@");
endif
else
target/assembly-$(assembly/VERSION).dmg :
	@err.println("Can not build DMG because not running MacOS"); \
	exit(1);
endif # eq ($(OS), MACOSX)
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
# --without-webservice, --without-gui, --without-cli, --without-updater and --with-simple-api were also specified.
dir-word-addin                                                                 : assembly/SOURCES
dir-word-addin                                                                 : mvn -Pwithout-osgi \
                                                                                     -Pwithout-persistence \
                                                                                     -Pwithout-webservice \
                                                                                     -Pwithout-gui \
                                                                                     -Pwithout-cli \
                                                                                     -Pwithout-updater \
                                                                                     -Pwith-simple-api \
                                                                                     -Pcopy-artifacts \
                                                                                     -Pbuild-jre-win32 \
                                                                                     -Pbuild-jre-win64
ifndef DUMP_PROFILES
	exec("$(MVN)", "install", "-Passemble-win-dir");
endif

ifneq ($(OS), WINDOWS)

.PHONY : docker
# Note that when `docker' is enabled together with other targets, it is as if --without-gui and --without-osgi were also specified.
docker : mvn -Pwithout-gui -Pwithout-osgi \
         target/maven-jlink/classifiers/jre-linux \
         target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2
ifndef DUMP_PROFILES
	mkdirs("target/docker");                                                                        \
	exec("cp", "src/main/docker/Dockerfile", "target/docker/Dockerfile");
	exec("cp", "-r", "target/assembly-$(assembly/VERSION)-linux/daisy-pipeline", "target/docker/");
	exec("cp", "-r", "$(word 4,$^)", "target/docker/jre");
	exec(new File("target/docker"),                                                                 \
	     "$(DOCKER)", "build", "-t", "daisyorg/pipeline:latest-snapshot", ".");
endif

.PHONY : dev-launcher
dev-launcher : target/dev-launcher/pipeline2
target/dev-launcher/pipeline2 : pom.xml
ifdef BUILD_JRE_FOR_DEV_LAUNCHER
ifeq ($(OS), MACOSX)
target/dev-launcher/pipeline2 : target/maven-jlink/classifiers/jre-mac target/assembly-$(assembly/VERSION)-mac/daisy-pipeline/bin/pipeline2
else
target/dev-launcher/pipeline2 : target/maven-jlink/classifiers/jre-linux target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2
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

target/maven-jlink/classifiers/jre-mac                                 : mvn -Pbuild-jre-mac
target/maven-jlink/classifiers/jre-linux                               : mvn -Pbuild-jre-linux

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

$(INSTALL_DIR)/assembly-$(assembly/VERSION)$(CLASSIFIER).exe           \
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
	for (File f : new File("make/java/").listFiles())  \
		if (f.getName().matches(".*\\.(java|class)"))  \
			f.delete();

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
#                         copy-gui
#                         copy-javafx-linux
#                         copy-javafx-mac
#                         copy-javafx-win
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
# unpack-updater-gui-win                       unpack-updater-gui-win
# assemble-mac-dir                                                                            assemble-mac-dir
# assemble-mac-app-dir                                                                                              assemble-mac-app-dir
# assemble-linux-dir                                                                          assemble-linux-dir
# assemble-win-dir                                                                            assemble-win-dir
# assemble-mac-zip                                                                                                  assemble-mac-zip
# assemble-linux-zip                                                                                                assemble-linux-zip
# assemble-win-zip                                                                                                  assemble-win-zip
# assemble-minimal-zip                                                                                              assemble-minimal-zip
# package-exe                                                                                 copy-nsis-resources   package-exe
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
	assemble-mac-app-dir        \
	assemble-mac-zip            \
	assemble-win-dir            \
	assemble-win-zip            \
	assemble-minimal-zip        \
	package-deb                 \
	package-deb-cli             \
	package-exe                 \
	package-rpm                 \
	package-rpm-cli             \
	unpack-cli-linux            \
	unpack-cli-mac              \
	unpack-cli-win              \
	unpack-updater-linux        \
	unpack-updater-mac          \
	unpack-updater-win          \
	unpack-updater-gui-win

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

.PHONY : --with-gui --without-gui
--without-gui : -Pwithout-gui
ifneq (--with-gui,$(filter --with-gui,$(MAKECMDGOALS)))
PROFILES += without-gui
else
.PHONY : -Pwithout-gui
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
	@List<String> cmd = new ArrayList<>();                                                                                 \
	cmd.add("$(MVN)");                                                                                                     \
	cmd.add("clean");                                                                                                      \
	cmd.add("install");                                                                                                    \
	cmd.add("-Dclassifier=$(--classifier)");                                                                               \
	cmd.add("-Dclassifier.dash=$(shell println("$(--classifier)".replaceAll("^.+$$", "$$0-"));)");                         \
	exitOnError(                                                                                                           \
		captureOutput(                                                                                                     \
			Arrays.asList("$(MAKE) -s --no-print-directory ECHO=true DUMP_PROFILES=true -- $(MAKECMDGOALS)".split("\\s")), \
			line -> { if (line.startsWith("-P")) cmd.add(line); }));                                                       \
	println(String.join(" ", cmd));                                                                                        \
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
-Pwith-simple-api : require-java-10
.PHONY : require-java-10
require-java-10 :
	@err.println("Java 10 is required to compile SimpleAPI.java"); \
	exit(1);
endif
endif

# profiles that are run separately because need to be run with specific JDKs, and because they should not be installed

.PHONY : -Pbuild-jre-mac -Pbuild-jre-linux -Pbuild-jre-win32 -Pbuild-jre-win64
-Pbuild-jre-mac -Pbuild-jre-linux -Pbuild-jre-win32 -Pbuild-jre-win64 : mvn # to make sure they are run after other profiles

ifeq ($(OS), MACOSX)
-Pbuild-jre-mac                                       : src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.3_7/jdk-17.0.3+7
ifndef DUMP_PROFILES
	rm("target/classes");                                \
	exec(env("JAVA_HOME", "$(CURDIR)/$</Contents/Home"), \
	     "$(MVN)", "package", "$@");
endif
-Pbuild-jre-linux -Pbuild-jre-win32 -Pbuild-jre-win64 : src/main/jre/OpenJDK11U-jdk_x64_mac_hotspot_11.0.13_8/jdk-11.0.13+8
ifndef DUMP_PROFILES
	rm("target/classes");                                \
	exec(env("JAVA_HOME", "$(CURDIR)/$</Contents/Home"), \
	     "$(MVN)", "package", "$@");
endif
else ifeq ($(OS), WINDOWS)
-Pbuild-jre-linux -Pbuild-jre-win32 -Pbuild-jre-win64 : src/main/jre/OpenJDK11U-jdk_x64_windows_hotspot_11.0.13_8/jdk-11.0.13+8
ifndef DUMP_PROFILES
	rm("target/classes");                                \
	exec(env("JAVA_HOME", "$(CURDIR)/$<"),               \
	     "$(MVN)", "package", "$@");
endif
else
-Pbuild-jre-linux -Pbuild-jre-win32 -Pbuild-jre-win64 : src/main/jre/OpenJDK11U-jdk_x64_linux_hotspot_11.0.13_8/jdk-11.0.13+8
ifndef DUMP_PROFILES
	rm("target/classes");                                \
	exec(env("JAVA_HOME", "$(CURDIR)/$<"),               \
	     "$(MVN)", "package", "$@");
endif
endif

# for dependencies to jmods
-Pbuild-jre-mac   : src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.3_7/jdk-17.0.3+7
-Pbuild-jre-linux : src/main/jre/OpenJDK11U-jdk_x64_linux_hotspot_11.0.13_8/jdk-11.0.13+8
-Pbuild-jre-win64 : src/main/jre/OpenJDK11U-jdk_x64_windows_hotspot_11.0.13_8/jdk-11.0.13+8
-Pbuild-jre-win32 : src/main/jre/OpenJDK11U-jdk_x86-32_windows_hotspot_11.0.13_8/jdk-11.0.13+8

# JDKs

src/main/jre/OpenJDK11U-jdk_x64_windows_hotspot_11.0.13_8/jdk-11.0.13+8 \
src/main/jre/OpenJDK11U-jdk_x86-32_windows_hotspot_11.0.13_8/jdk-11.0.13+8 : %/jdk-11.0.13+8 : %.zip
	unzip(new File("$<"), new File("$(dir $@)"));

ifneq ($(OS), WINDOWS)
src/main/jre/OpenJDK11U-jdk_x64_linux_hotspot_11.0.13_8/jdk-11.0.13+8 : %/jdk-11.0.13+8 : | %.tar.gz
	mkdirs("$(dir $@)");                                                                                       \
	exec("tar", "-zxvf", "src/main/jre/OpenJDK11U-jdk_x64_linux_hotspot_11.0.13_8.tar.gz", "-C", "$(dir $@)/");
src/main/jre/OpenJDK11U-jdk_x64_mac_hotspot_11.0.13_8/jdk-11.0.13+8 : %/jdk-11.0.13+8 : | %.tar.gz
	mkdirs("$(dir $@)");                                                                                       \
	exec("tar", "-zxvf", "src/main/jre/OpenJDK11U-jdk_x64_mac_hotspot_11.0.13_8.tar.gz", "-C", "$(dir $@)/");
src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.3_7/jdk-17.0.3+7   : %/jdk-17.0.3+7 : | %.tar.gz
	mkdirs("$(dir $@)");                                                                                       \
	exec("tar", "-zxvf", "src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.3_7.tar.gz", "-C", "$(dir $@)/");
endif

.INTERMEDIATE : src/main/jre/OpenJDK11U-jdk_x64_mac_hotspot_11.0.13_8.tar.gz
.INTERMEDIATE : src/main/jre/OpenJDK11U-jdk_x64_linux_hotspot_11.0.13_8.tar.gz
.INTERMEDIATE : src/main/jre/OpenJDK11U-jdk_x86-32_windows_hotspot_11.0.13_8.zip
.INTERMEDIATE : src/main/jre/OpenJDK11U-jdk_x64_windows_hotspot_11.0.13_8.zip

src/main/jre/OpenJDK11U-jdk_x64_mac_hotspot_11.0.13_8.tar.gz \
src/main/jre/OpenJDK11U-jdk_x64_linux_hotspot_11.0.13_8.tar.gz \
src/main/jre/OpenJDK11U-jdk_x86-32_windows_hotspot_11.0.13_8.zip \
src/main/jre/OpenJDK11U-jdk_x64_windows_hotspot_11.0.13_8.zip :
	mkdirs("$(dir $@)");                                                                                           \
	copy(new URL("https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.13%2B8/$(notdir $@)"), \
	     new File("$@"));
src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.3_7.tar.gz :
	mkdirs("$(dir $@)");                                                                                          \
	copy(new URL("https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.3%2B7/$(notdir $@)"), \
	     new File("$@"));

.PHONY : clean-jdk
clean : clean-jdk
clean-jdk :
	rm("src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.3_7/jdk-17.0.3+7");          \
	rm("src/main/jre/OpenJDK11U-jdk_x64_linux_hotspot_11.0.13_8/jdk-11.0.13+8");      \
	rm("src/main/jre/OpenJDK11U-jdk_x64_windows_hotspot_11.0.13_8/jdk-11.0.13+8");    \
	rm("src/main/jre/OpenJDK11U-jdk_x86-32_windows_hotspot_11.0.13_8/jdk-11.0.13+8");
