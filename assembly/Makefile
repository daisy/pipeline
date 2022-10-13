ifneq ($(firstword $(sort $(MAKE_VERSION) 3.82)), 3.82)
$(error "GNU Make 3.82 is required to run this script")
endif

ifeq ($(OS),Windows_NT)
SHELL := make\\eval-java.exe
else
SHELL := make/eval-java
endif
.SHELLFLAGS :=

OS := $(shell println(getOS());)

ifeq ($(OS), WINDOWS)
MVN ?= mvn.cmd
else
MVN ?= mvn
endif

DOCKER := docker

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

include deps.mk

.PHONY : dmg exe deb rpm zip-linux zip-mac zip-win zip-minimal deb-cli rpm-cli

dmg         : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).dmg
exe         : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).exe
deb         : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).deb
rpm         : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-linux.rpm
zip-linux   : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-linux.zip
zip-mac     : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-mac.zip
zip-win     : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-win.zip
zip-minimal : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-minimal.zip
deb-cli     : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-cli.deb
rpm-cli     : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-cli.rpm

.PHONY : release-descriptor
release-descriptor : target/release-descriptor/releaseDescriptor.xml
target/release-descriptor/releaseDescriptor.xml : mvn -Pgenerate-release-descriptor

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).exe         : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Punpack-cli-win \
                                                                                                                       -Punpack-updater-win \
                                                                                                                       -Punpack-updater-gui-win \
                                                                                                                       -Passemble-win-dir \
                                                                                                                       -Ppackage-exe
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).deb         : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Punpack-updater-linux \
                                                                                                                       -Ppackage-deb
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-linux.zip   : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Punpack-cli-linux \
                                                                                                                       -Punpack-updater-linux \
                                                                                                                       -Passemble-linux-zip
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-mac.zip     : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Punpack-cli-mac \
                                                                                                                       -Punpack-updater-mac \
                                                                                                                       -Pbuild-jre-mac
ifndef DUMP_PROFILES
	exec("$(MVN)", "install", "-Passemble-mac-zip");
	exit(new File("$@").exists());
endif
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-win.zip     : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Punpack-cli-win \
                                                                                                                       -Punpack-updater-win \
                                                                                                                       -Punpack-updater-gui-win \
                                                                                                                       -Pbuild-jre-win64
ifndef DUMP_PROFILES
	exec("$(MVN)", "install", "-Passemble-win-zip");
	exit(new File("$@").exists());
endif
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-minimal.zip : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Punpack-updater-mac \
                                                                                                                       -Punpack-updater-linux \
                                                                                                                       -Punpack-updater-win \
                                                                                                                       -Passemble-minimal-zip
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-cli.deb     : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Punpack-cli-linux \
                                                                                                                       -Ppackage-deb-cli
ifeq ($(OS), MACOSX)
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).dmg         : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Punpack-cli-mac \
                                                                                                                       -Punpack-updater-mac \
                                                                                                                       -Passemble-mac-app-dir \
                                                                                                                       -Pbuild-jre-mac
ifndef DUMP_PROFILES
	rm("target/jpackage");                                                                               \
	String[] guiJar = new File("target/assembly-$(assembly/VERSION)-mac-app/daisy-pipeline/system/gui")  \
	                  .list((dir, name) -> name.matches("org\\.daisy\\.pipeline\\.gui-.*\\.jar"));       \
	exitOnError(guiJar != null && guiJar.length == 1);                                                   \
	String appVersion = "$(assembly/VERSION)".replaceAll("-.*$$", "");                                   \
	exec("src/main/jre/OpenJDK17U-jdk_x64_mac_hotspot_17.0.3_7/jdk-17.0.3+7/Contents/Home/bin/jpackage", \
	     "--dest", "target/jpackage",                                                                    \
	     "--type", "dmg",                                                                                \
	     "--app-version", appVersion,                                                                    \
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
	                       "-Dorg.daisy.pipeline.mode=gui "                                            + \
	                       "-Dorg.daisy.pipeline.home=$$APPDIR "                                       + \
	                       "-Dorg.daisy.pipeline.data=$$APPDIR/data "                                  + \
	                       "-Dorg.daisy.pipeline.logdir=$$APPDIR/data/log "                            + \
	                       "-Dorg.daisy.pipeline.ws.localfs=true "                                     + \
	                       "-Dorg.daisy.pipeline.ws.authentication=false "                             + \
	                       "-Dorg.daisy.pipeline.properties=$$APPDIR/etc/pipeline.properties "         + \
	                       "-Dlogback.configurationFile=$$APPDIR/etc/config-logback.xml");
	String appVersion = "$(assembly/VERSION)".replaceAll("-.*$$", "");                                   \
	exec("$(MVN)", "install:install-file",                                                               \
	               "-Dfile=target/jpackage/DAISY Pipeline 2-" + appVersion + ".dmg",                     \
	               "-DpomFile=pom.xml",                                                                  \
	               "-Dpackaging=dmg");
	exit(new File("$@").exists());
endif
else
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).dmg         :
	@err.println("Can not build DMG because not running MacOS"); \
	exit(1);
endif # eq ($(OS), MACOSX)
ifeq ($(OS), REDHAT)
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-linux.rpm   : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Passemble-linux-dir \
                                                                                                                       -Ppackage-rpm
ifndef DUMP_PROFILES
	exit(new File("$@").exists());
endif
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-cli.rpm     : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Punpack-cli-linux \
                                                                                                                       -Ppackage-rpm-cli
ifndef DUMP_PROFILES
	exit(new File("$@").exists());
endif
else
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-linux.rpm \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-cli.rpm :
	@err.println("Can not build RPM because not running RedHat/CentOS"); \
	exit(1);
endif # eq ($(OS), REDHAT)

.PHONY : dir-word-addin
dir-word-addin : target/assembly-$(assembly/VERSION)-word-addin
target/assembly-$(assembly/VERSION)-word-addin : assembly/SOURCES
target/assembly-$(assembly/VERSION)-word-addin : mvn -Pwithout-osgi \
                                                     -Pwithout-persistence \
                                                     -Pwithout-webservice \
                                                     -Pwithout-gui \
                                                     -Pwithout-cli \
                                                     -Pwithout-updater \
                                                     -Pcompile-simple-api \
                                                     -Pcopy-artifacts \
                                                     -Pbuild-jre-win32 \
                                                     -Pbuild-jre-win64
ifndef DUMP_PROFILES
	exec("$(MVN)", "install", "-Passemble-word-addin-dir");
endif

ifneq ($(OS), WINDOWS)

.PHONY : docker
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
ifeq ($(OS), MACOSX)
target/dev-launcher/pipeline2 : target/maven-jlink/classifiers/jre target/assembly-$(assembly/VERSION)-mac/daisy-pipeline/bin/pipeline2
else
target/dev-launcher/pipeline2 : target/maven-jlink/classifiers/jre target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2
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

target/maven-jlink/classifiers/jre                                     : mvn -Pbuild-jre
target/maven-jlink/classifiers/jre-linux                               : mvn -Pbuild-jre-linux

target/assembly-$(assembly/VERSION)-mac/daisy-pipeline/bin/pipeline2   : mvn -Pwithout-persistence \
                                                                             -Pcopy-artifacts \
                                                                             -Pcompile-simple-api \
                                                                             -Pgenerate-release-descriptor \
                                                                             -Punpack-cli-mac \
                                                                             -Punpack-updater-mac \
                                                                             -Passemble-mac-dir
target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2 : mvn -Pwithout-persistence \
                                                                             -Pcopy-artifacts \
                                                                             -Pcompile-simple-api \
                                                                             -Pgenerate-release-descriptor \
                                                                             -Punpack-cli-linux \
                                                                             -Punpack-updater-linux \
                                                                             -Passemble-linux-dir

endif # neq ($(OS), WINDOWS)

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).exe \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).deb \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-linux.zip \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-minimal.zip \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-cli.deb \
target/assembly-$(assembly/VERSION)-mac/daisy-pipeline/bin/pipeline2 \
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

.PHONY : --without-persistence
--without-persistence : -Pwithout-persistence

.PHONY : --without-osgi
--without-osgi : -Pwithout-osgi

.PHONY : --without-gui
--without-gui : -Pwithout-gui

.PHONY : --without-webservice
--without-webservice : -Pwithout-webservice

.PHONY : --without-cli
--without-cli : -Pwithout-cli

.PHONY : --without-updater
--without-updater : -Pwithout-updater

clean :
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
#                         copy-volatile
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
# build-jre                                                                                                         jlink
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
# assemble-word-addin-dir                                                                                           assemble-word-addin-dir
# package-exe                                                                                 copy-nsis-resources   package-exe
# package-deb                                                          filter-deb-resources                         package-deb
# package-deb-cli                                                                                                   package-deb-cli
# package-rpm                                                                                                       package-rpm
# package-rpm-cli                                                                                                   package-rpm-cli

PROFILES :=                     \
	copy-artifacts              \
	compile-simple-api          \
	generate-release-descriptor \
	build-jre                   \
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
	unpack-updater-gui-win      \
	without-persistence         \
	without-osgi                \
	without-gui                 \
	without-webservice          \
	without-cli                 \
	without-updater

.PHONY : mvn
mvn :
ifndef DUMP_PROFILES
	@List<String> cmd = new ArrayList<>();                                                                                 \
	cmd.add("$(MVN)");                                                                                                     \
	cmd.add("clean");                                                                                                      \
	cmd.add("install");                                                                                                    \
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

# profiles that are run separately because need to be run with specific JDKs

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
