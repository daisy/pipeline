MVN ?= mvn
DOCKER := docker
SHELL  := /bin/bash

.PHONY : default
ifeq ($(shell uname), Darwin)
default : dmg
else
default : zip-linux
endif

.PHONY : help
help :
	echo "make [default]:"                                                      >&2
	echo "	Builds the default package for the current platform"                >&2
	echo "make dmg:"                                                            >&2
	echo "	Builds a DMG image (Mac OS disk image)"                             >&2
	echo "make exe:"                                                            >&2
	echo "	Builds a EXE (Windows installer)"                                   >&2
	echo "make deb:"                                                            >&2
	echo "	Builds a DEB (Debian package)"                                      >&2
	echo "make rpm:"                                                            >&2
	echo "	Builds a RPM (RedHat package)"                                      >&2
	echo "make zip-linux:"                                                      >&2
	echo "	Builds a ZIP for Linux"                                             >&2
	echo "make all:"                                                            >&2
	echo "	Builds a DMG, a EXE, a DEB, a RPM and a ZIP for Linux"              >&2
	echo "make zip-mac:"                                                        >&2
	echo "	Builds a ZIP for Mac OS"                                            >&2
	echo "make zip-win:"                                                        >&2
	echo "	Builds a ZIP for Windows"                                           >&2
	echo "make zips:"                                                           >&2
	echo "	Builds a ZIP for each platform"                                     >&2
	echo "make zip-minimal:"                                                    >&2
	echo "	Builds a minimal ZIP that will complete itself upon first update"   >&2
	echo "make docker:"                                                         >&2
	echo "	Builds a Docker image"                                              >&2
	echo "make check|check-docker:"                                             >&2
	echo "	Tests the Docker image"                                             >&2

assembly/VERSION     := $(shell xmllint --xpath "/*/*[local-name()='version']/text()" pom.xml)
assembly/BASEDIR     := .
MVN_LOCAL_REPOSITORY ?= $(HOME)/.m2/repository

include deps.mk

.PHONY : all
all : dmg exe deb rpm zip-linux

.PHONY : zips
zips : zip-mac zip-linux zip-win

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
                                                                                                                       -Passemble-mac-zip
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-win.zip     : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Punpack-cli-win \
                                                                                                                       -Punpack-updater-win \
                                                                                                                       -Punpack-updater-gui-win \
                                                                                                                       -Passemble-win-zip
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
ifeq ($(shell uname), Darwin)
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).dmg         : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Punpack-cli-mac \
                                                                                                                       -Punpack-updater-mac \
                                                                                                                       -Passemble-mac-dir \
                                                                                                                       -Ppackage-mac-app
	# we run package-dmg in a subsequent mvn call to avoid execution
	# order issues when the package-mac-app and package-dmg profiles
	# are activated together
	$(MVN) install -Ppackage-dmg
	test -e $@
else
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).dmg         :
	@echo "Can not build DMG because not running MacOS" >&2
	exit 1
endif
ifeq ($(shell test -f /etc/redhat-release; echo $$?), 0)
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-linux.rpm   : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Passemble-linux-dir \
                                                                                                                       -Ppackage-rpm
	test -e $@
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-cli.rpm     : mvn -Pcopy-artifacts \
                                                                                                                       -Pgenerate-release-descriptor \
                                                                                                                       -Punpack-cli-linux \
                                                                                                                       -Ppackage-rpm-cli
	test -e $@
else
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-linux.rpm \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-cli.rpm :
	@echo "Can not build RPM because not running RedHat/CentOS" >&2
	exit 1
endif

.PHONY : docker
docker : target/maven-jlink/classifiers/jre-linux target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2
	mkdir target/docker
	cp Dockerfile.without_builder target/docker/Dockerfile
	cp -r target/assembly-$(assembly/VERSION)-linux/daisy-pipeline target/docker/
	cp -r $< target/docker/jre
	cd target/docker && \
	$(DOCKER) build -t daisyorg/pipeline-assembly .

src/main/jre/OpenJDK11-jdk_x64_linux_hotspot_11_28/jdk-11+28 : src/main/jre/OpenJDK11-jdk_x64_linux_hotspot_11_28.tar.gz
	mkdir -p $@
	tar -zxvf $< -C $(dir $@)/

src/main/docker/OpenJDK11-jdk_x64_linux_hotspot_11_28.tar.gz :
	mkdir -p $(dir $@)
	curl -L -o $@ "https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11%2B28/$(notdir $@)"

.PHONY : dev-launcher
dev-launcher : target/dev-launcher/pipeline2
target/dev-launcher/pipeline2 : pom.xml
ifeq ($(shell uname), Darwin)
target/dev-launcher/pipeline2 : target/maven-jlink/classifiers/jre target/assembly-$(assembly/VERSION)-mac/daisy-pipeline/bin/pipeline2
else
target/dev-launcher/pipeline2 : target/maven-jlink/classifiers/jre target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2
endif
	mkdir -p $(dir $@)
	echo "#!/usr/bin/env bash"                  >$@
	echo "JAVA_HOME=$(CURDIR)/$(word 1,$^) \\" >>$@
	echo "$(CURDIR)/$(word 2,$^)"              >>$@
	chmod +x $@

target/maven-jlink/classifiers/jre                                     : mvn -Pbuild-jre
target/maven-jlink/classifiers/jre-linux                               : src/main/jre/OpenJDK11-jdk_x64_linux_hotspot_11_28/jdk-11+28 \
                                                                         mvn -Pbuild-jre-linux

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

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).exe \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).deb \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-linux.zip \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-mac.zip \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-win.zip \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-minimal.zip \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-cli.deb \
target/assembly-$(assembly/VERSION)-mac/daisy-pipeline/bin/pipeline2 \
target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2 :
	test -e $@

.PHONY : check
check : check-docker

.PHONY : check-docker
check-docker :
	bash src/test/resources/test-docker-image.sh

.PHONY : --without-persistence
--without-persistence : -Pwithout-persistence

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
# unpack-cli-mac                               unpack-cli-mac
# unpack-cli-linux                             unpack-cli-linux
# unpack-cli-win                               unpack-cli-win
# unpack-updater-mac                           unpack-updater-mac
# unpack-updater-linux                         unpack-updater-linux
# unpack-updater-win                           unpack-updater-win
# unpack-updater-gui-win                       unpack-updater-gui-win
# assemble-mac-dir                                                                            assemble-mac-dir
# assemble-linux-dir                                                                          assemble-linux-dir
# assemble-win-dir                                                                            assemble-win-dir
# assemble-mac-zip                                                                                                  assemble-mac-zip
# assemble-linux-zip                                                                                                assemble-linux-zip
# assemble-win-zip                                                                                                  assemble-win-zip
# assemble-minimal-zip                                                                                              assemble-minimal-zip
# package-mac-app                                                                                                   javapackager
# package-dmg                                                                                 install-node
#                                                                                             install-appdmg        package-appdmg
#                                                                                             parse-version         attach-dmg
# package-exe                                                                                 copy-nsis-resources   package-exe
# package-deb                                                          filter-deb-resources                         package-deb
# package-deb-cli                                                                                                   package-deb-cli
# package-rpm                                                                                                       package-rpm
# package-rpm-cli                                                                                                   package-rpm-cli

PROFILES :=                     \
	copy-artifacts              \
	generate-release-descriptor \
	build-jre                   \
	build-jre-linux             \
	assemble-linux-dir          \
	assemble-linux-zip          \
	assemble-mac-dir            \
	assemble-mac-zip            \
	assemble-win-dir            \
	assemble-win-zip            \
	assemble-minimal-zip        \
	package-deb                 \
	package-deb-cli             \
	package-mac-app             \
	package-dmg                 \
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
	without-persistence

.PHONY : mvn
mvn :
ifndef DUMP_PROFILES
	set -o pipefail; \
	$(MVN) clean install $(shell $(MAKE) -qs DUMP_PROFILES=true -- $(MAKECMDGOALS))
endif

.PHONY : $(addprefix -P,$(PROFILES))
ifdef DUMP_PROFILES
$(addprefix -P,$(PROFILES)) :
	+echo $@
endif
