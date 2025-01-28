include .make/enable-java-shell.mk

.PHONY : default
default : help

MVN_WORKSPACE           := .maven-workspace
MVN_CACHE               := .maven-cache

# -----------------------------------
MVN_SETTINGS            := settings.xml
MVN_PROPERTIES          := -Dorg.ops4j.pax.url.mvn.localRepository="$(CURDIR)/$(MVN_WORKSPACE)" \
                           -Dorg.ops4j.pax.url.mvn.settings="$(CURDIR)/settings.xml"
MVN_RELEASE_CACHE_REPO  := $(MVN_CACHE)

ifneq ($(MAKECMDGOALS),)
ifneq ($(MAKECMDGOALS), help)
ifneq ($(MAKECMDGOALS), dump-maven-cmd)
ifneq ($(MAKECMDGOALS), dump-gradle-cmd)
ifneq ($(MAKECMDGOALS), cache)
ifneq ($(MAKECMDGOALS), clean-website)
include .make/main.mk
assembly/BASEDIR := assembly
assembly/SOURCES : assembly/.compile-dependencies
include assembly/deps.mk
-include webui/.deps.mk
else
.SILENT: clean-website
endif
else
.SILENT: cache
endif
else
.SILENT: dump-gradle-cmd
endif
else
.SILENT: dump-maven-cmd
endif
else
.SILENT: help
endif
else
.SILENT: help
endif

USER_HOME := $(shell println(System.getenv("HOME"));)

# instead of passing system properties "workspace" and "cache" we substitute them in the settings.xml file
# this is required for org.ops4j.pax.url.mvn.settings
settings.xml : settings.xml.in
	$(call bash, \
		cat $< | sed -e "s|\$${workspace}|$(CURDIR)/$(MVN_WORKSPACE)|g" \
		             -e "s|\$${cache}|$(CURDIR)/$(MVN_CACHE)|g" \
		             -e "s|\$${user\.home}|$(USER_HOME)|g" \
		             >$@ \
	)

# -----------------------------------

.PHONY : dist-zip-linux
dist-zip-linux : pipeline2-$(assembly/VERSION)_linux.zip

.PHONY : dist-zip-mac
dist-zip-mac : pipeline2-$(assembly/VERSION)_mac.zip

.PHONY : dist-zip-win
dist-zip-win : pipeline2-$(assembly/VERSION)_windows.zip

.PHONY : dist-zip-minimal
dist-zip-minimal : pipeline2-$(assembly/VERSION)_minimal.zip

.PHONY : dist-deb
dist-deb : pipeline2-$(assembly/VERSION)_debian.deb

.PHONY : dist-rpm
dist-rpm : pipeline2-$(assembly/VERSION)_redhat.rpm

.PHONY : dist-docker-image
dist-docker-image : assembly/.compile-dependencies | .maven-init .group-eval
	+$(EVAL) $(call bash, unset MAKECMDGOALS && $(MAKE) -C assembly docker)

# FIXME: $(cli/VERSION) does not always match version in assembly/pom.xml
.PHONY : dist-cli-deb
dist-cli-deb : cli-$(cli/VERSION)-linux_386.deb

.PHONY : dist-webui-zip
dist-webui-zip : assembly/.compile-dependencies
	/* see webui README for instructions on how to make a signed package for distribution */ \
	$(call bash, \
		cd webui && \
		./activator -Dmvn.settings.localRepository="file:$(CURDIR)/$(MVN_WORKSPACE)" clean universal:packageBin | $(MVN_LOG) && \
		mv webui/target/universal/*zip . \
	)

.PHONY : dist-webui-deb
dist-webui-deb : assembly/.compile-dependencies
	/* see webui README for instructions on how to make a signed package for distribution */ \
	$(call bash, \
		cd webui && \
		./activator -Dmvn.settings.localRepository="file:$(CURDIR)/$(MVN_WORKSPACE)" clean debian:packageBin | $(MVN_LOG) && \
		mv webui/target/*deb . \
	)

.PHONY : dist-webui-rpm
dist-webui-rpm : assembly/.compile-dependencies
	/* see webui README for instructions on how to make a signed package for distribution */ \
	$(call bash, \
		cd webui && \
		./activator -Dmvn.settings.localRepository="file:$(CURDIR)/$(MVN_WORKSPACE)" clean rpm:packageBin && \
		mv webui/target/rpm/RPMS/noarch/*.rpm . \
	)

dev_launcher := assembly/target/dev-launcher/pipeline2
ifeq ($(OS), MACOSX)
ifeq ($(shell $(call bash, uname -m)),arm64)
dp2 := cli/build/bin/darwin_arm64/dp2
else
dp2 := cli/build/bin/darwin_amd64/dp2
endif
else
dp2 := cli/build/bin/linux_386/dp2
endif

.PHONY : dp2
dp2 : $(dp2)

.PHONY : run
run : $(dev_launcher)
	$(call bash, $< local)

ifeq ($(MAKECMDGOALS),run-with-osgi)
unexport JAVA_REPL_PORT
endif
.PHONY : run-with-osgi
run-with-osgi : $(dev_launcher)
	$(call bash, $< local osgi shell)

.PHONY : run-cli
run-cli :
	$(call bash, \
		echo "dp2 () { test -e $(dp2) || make $(dp2) && curl http://localhost:8181/ws/alive >/dev/null 2>/dev/null || make $(dev_launcher) && $(dp2) --debug false --starting true --exec_line $(CURDIR)/$(dev_launcher) --ws_timeup 30 \"\$$@\"; }" && \
		echo '# Run this command to configure your shell: ' && \
		echo '# eval $$(make $@)' \
	)

.PHONY : run-webui
run-webui : webui/.compile-dependencies
	$(call bash, \
		if [ ! -d webui/dp2webui ]; then cp -r webui/dp2webui-cleandb webui/dp2webui ; fi && \
		cd webui && \
		./activator -Dmvn.settings.localRepository="file:$(CURDIR)/$(MVN_WORKSPACE)" run \
	)

.PHONY : run-docker
run-docker : dist-docker-image
	$(call bash, \
		docker run \
		       -e PIPELINE2_WS_HOST=0.0.0.0 \
		       -e PIPELINE2_WS_AUTHENTICATION=false \
		       -p 8181:8181 daisyorg/pipeline:latest-snapshot \
	)

.PHONY : run-docker-detached
run-docker-detached : dist-docker-image
	$(call bash, \
		docker run --name pipeline --detach \
		       -e PIPELINE2_WS_HOST=0.0.0.0 \
		       -e PIPELINE2_WS_AUTHENTICATION=false \
		       -p 8181:8181 daisyorg/pipeline:latest-snapshot \
	)

SCRIPTS := $(filter modules/scripts/%,$(MAVEN_MODULES)) \
           modules/scripts-utils/daisy202-utils \
           modules/scripts-utils/daisy3-utils \
           modules/scripts-utils/dtbook-utils

.PHONY : $(addprefix run-,$(SCRIPTS))
$(addprefix run-,$(SCRIPTS)) : run-% : %/.compile-dependencies %/.test-dependencies
	/* not using -f because that causes log file to be saved at the wrong location */ \
	$(call bash, \
		cd $(patsubst run-%,%,$@) && \
		$(MVN) clean test -Prun-script-webserver \
	)

.PHONY : check

.PHONY : check-clientlib/go
check-clientlib/go :
	$(call bash, $(MAKE) -C clientlib/go check)

.PHONY : release
release : assembly/.release

.PHONY : $(addprefix check-,$(MODULES) $(MAVEN_AGGREGATORS))
$(addprefix check-,$(MODULES) $(MAVEN_AGGREGATORS)) : check-% : %/.last-tested

pipeline2-$(assembly/VERSION)_linux.zip \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-linux.zip \
	| .group-eval
	+$(EVAL) $(call bash, cp $< $@)

pipeline2-$(assembly/VERSION)_mac.zip \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-mac.zip \
	| .group-eval
	+$(EVAL) $(call bash, cp $< $@)

pipeline2-$(assembly/VERSION)_windows.zip \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-win.zip \
	| .group-eval
	+$(EVAL) $(call bash, cp $< $@)

pipeline2-$(assembly/VERSION)_minimal.zip \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-minimal.zip \
	| .group-eval
	+$(EVAL) $(call bash, cp $< $@)

pipeline2-$(assembly/VERSION)_debian.deb \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).deb \
	| .group-eval
	+$(EVAL) $(call bash, cp $< $@)

pipeline2-$(assembly/VERSION)_redhat.rpm \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).rpm \
	| .group-eval
	+$(EVAL) $(call bash, cp $< $@)

cli-$(cli/VERSION)-linux_386.deb \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-cli.deb \
	| .group-eval
	+$(EVAL) $(call bash, cp $< $@)

$(dev_launcher) : assembly/.compile-dependencies | .maven-init .group-eval
	+$(EVAL) $(call bash, ./assembly-make.sh dev-launcher -- --without-persistence)

.SECONDARY : assembly/.install.deb
assembly/.install.deb : | .maven-init .group-eval
	+$(EVAL) $(call bash, ./assembly-make.sh deb)

.SECONDARY : assembly/.install.rpm
assembly/.install.rpm : | .maven-init .group-eval
	+$(EVAL) $(call bash, ./assembly-make.sh rpm)

.SECONDARY : assembly/.install-linux.zip
assembly/.install-linux.zip : | .maven-init .group-eval
	+$(EVAL) $(call bash, ./assembly-make.sh zip-linux)

.SECONDARY : assembly/.install-minimal.zip
assembly/.install-minimal.zip : | .maven-init .group-eval
	+$(EVAL) $(call bash, ./assembly-make.sh zip-minimal)

.SECONDARY : assembly/.install-mac.zip
assembly/.install-mac.zip : | .maven-init .group-eval
	+$(EVAL) $(call bash, ./assembly-make.sh zip-mac)

.SECONDARY : assembly/.install-win.zip
assembly/.install-win.zip : | .maven-init .group-eval
	+$(EVAL) $(call bash, ./assembly-make.sh zip-win)

.SECONDARY : assembly/.install-cli.deb
assembly/.install-cli.deb : | .maven-init .group-eval
	+$(EVAL) $(call bash, ./assembly-make.sh deb-cli)

webui/.deps.mk : webui/build.sbt
	$(call bash, \
		if ! bash .make/make-webui-deps.mk.sh >$@; then \
			echo "\$$(error $@ could not be generated)" >$@; \
		fi \
	)

clean : clean-webui-deps
.PHONY : clean-webui-deps
clean-webui-deps :
	$(call bash, rm -f webui/.deps.mk)

cli/build/bin/darwin_amd64/dp2 cli/build/bin/darwin_arm64/dp2 cli/build/bin/linux_386/dp2 : cli/.install

cli/.install : $(call rwildcard,cli/cli/,*.go) $(call rwildcard,cli/cli/,*.go.in) $(call rwildcard,cli/dp2/,*.go) $(call rwildcard,clientlib/go/,*.go)

.SECONDARY : cli/.install-darwin_amd64.zip cli/.install-linux_386.zip cli/.install-windows_386.zip
cli/.install-darwin_amd64.zip cli/.install-linux_386.zip cli/.install-windows_386.zip : cli/.install

.SECONDARY : cli/.install
cli/.install : | .maven-init .group-eval
	+$(EVAL) $(call bash, .make/mvn-install.sh $$(dirname $@))

updater/.install : $(call rwildcard,updater/,*)

.SECONDARY : updater/.install-darwin_amd64.zip updater/.install-linux_386.zip updater/.install-windows_386.zip
updater/.install-darwin_amd64.zip updater/.install-linux_386.zip updater/.install-windows_386.zip : updater/.install

.SECONDARY : libs/jstyleparser/.install-sources.jar
libs/jstyleparser/.install-sources.jar : libs/jstyleparser/.install

modules/scripts/dtbook-to-odt/.install-doc.jar : $(call rwildcard,modules/scripts/dtbook-to-odt/src/test/,*)

.SECONDARY : \
	modules/braille/liblouis-utils/.install-mac.jar \
	modules/braille/liblouis-utils/.install-linux.jar \
	modules/braille/liblouis-utils/.install-windows.jar
modules/braille/liblouis-utils/.install-mac.jar \
modules/braille/liblouis-utils/.install-linux.jar \
modules/braille/liblouis-utils/.install-windows.jar: \
	modules/braille/liblouis-utils/.install

.SECONDARY : \
	modules/braille/libhyphen-utils/.install-mac.jar \
	modules/braille/libhyphen-utils/.install-linux.jar \
	modules/braille/libhyphen-utils/.install-windows.jar
modules/braille/libhyphen-utils/.install-mac.jar \
modules/braille/libhyphen-utils/.install-linux.jar \
modules/braille/libhyphen-utils/.install-windows.jar: \
	modules/braille/libhyphen-utils/.install

# treat celia-hyphenation-tables as a released artifact
.PHONY : $(MVN_LOCAL_REPOSITORY)/fi/celia/celia-hyphenation-tables/1.5.1-SNAPSHOT/celia-hyphenation-tables-1.5.1-SNAPSHOT.jar
.PHONY : $(MVN_LOCAL_REPOSITORY)/fi/celia/celia-hyphenation-tables/1.5.1-SNAPSHOT!!!/celia-hyphenation-tables-1.5.1-SNAPSHOT!!!.jar

.maven-init : | $(MVN_WORKSPACE)
# the purpose of the test is for making "make -B" not affect this rule (to speed thing up)
$(MVN_WORKSPACE) :
	$(call bash, \
		if ! [ -e $(MVN_WORKSPACE) ]; then \
			mkdir -p $(MVN_CACHE) && \
			cp -r $(MVN_CACHE) $@; \
		fi \
	)

.PHONY : cache
cache :
	$(call bash, \
		if [ -e $(MVN_WORKSPACE) ]; then \
			echo "Caching downloaded artifacts..." >&2 && \
			rm -rf $(MVN_CACHE) && \
			rsync -mr --exclude "*-SNAPSHOT" --exclude "maven-metadata-*.xml" $(MVN_WORKSPACE)/ $(MVN_CACHE); \
		fi \
	)

clean : cache clean-workspace clean-old clean-website clean-dist clean-webui

.PHONY : clean-workspace
clean-workspace :
	$(call bash, rm -rf $(MVN_WORKSPACE))

.PHONY : clean-dist
clean-dist :
	$(call bash, rm -f *.zip *.deb *.rpm)

# FIXME: run-webui should not store anything in USER_HOME !!
.PHONY : clean-webui
clean-webui :
	$(call bash, rm -rf webui/target webui/dp2webui $(USER_HOME)/Library/Application\ Support/DAISY\ Pipeline\ 2\ Web\ UI)

# clean files generated by previous versions of this Makefile
.PHONY : clean-old
clean-old :
	$(call bash, \
		rm -f .maven-modules && \
		rm -f .effective-pom.xml && \
		rm -f .gradle-pom.xml && \
		rm -f .maven-build.mk && \
		find . -name .deps.mk -exec rm -r "{}" \; && \
		find . -name .build.mk -exec rm -r "{}" \; && \
		find * -name .maven-to-install -exec rm -r "{}" \; && \
		find * -name .maven-to-test -exec rm -r "{}" \; && \
		find * -name .maven-to-test-dependents -exec rm -r "{}" \; && \
		find * -name .maven-snapshot-dependencies -exec rm -r "{}" \; && \
		find * -name .maven-effective-pom.xml -exec rm -r "{}" \; && \
		find * -name .maven-dependencies-to-install -exec rm -r "{}" \; && \
		find * -name .maven-dependencies-to-test -exec rm -r "{}" \; && \
		find * -name .maven-dependencies-to-test-dependents -exec rm -r "{}" \; && \
		find * -name .gradle-to-test -exec rm -r "{}" \; && \
		find * -name .gradle-snapshot-dependencies -exec rm -r "{}" \; && \
		find * -name .gradle-dependencies-to-install -exec rm -r "{}" \; && \
		find * -name .gradle-dependencies-to-test -exec rm -r "{}" \; \
	)

.PHONY : gradle-clean
gradle-clean :
	$(call bash, $(GRADLE) clean)

TEMP_REPOS := modules/scripts/dtbook-to-daisy3/target/test/local-repo

.PHONY : go-offline
go-offline :
	$(call bash, \
		if [ -e $(MVN_WORKSPACE) ]; then \
			for repo in $(TEMP_REPOS); do \
				if [ -e $$repo ]; then \
					rsync -mr --exclude "*-SNAPSHOT" --exclude "maven-metadata-*.xml" $$repo/ $(MVN_WORKSPACE); \
				fi \
			done \
		fi \
	)

.PHONY : checked
checked :
	$(call bash, touch $(addsuffix /.last-tested,$(MODULES)))

poms : website/target/maven/pom.xml
website/target/maven/pom.xml : $(addprefix website/src/_data/,modules.yml api.yml versions.yml)
	$(call bash, \
		if which bundle >/dev/null || ! [ -f $@ ]; then \
			$(MAKE) -C website target/maven/pom.xml; \
		fi \
	)

.PHONY : website
website :
	$(call bash, $(MAKE) -C website)

.PHONY : serve-website publish-website clean-website
serve-website publish-website clean-website :
	$(call bash, \
		target=$@ && \
		$(MAKE) -C website $${target%-website} \
	)

# this dependency is also defined in website/Makefile, but we need to repeat it here to enable the transitive dependency below
website serve-website publish-website : | $(addprefix website/target/maven/,javadoc doc sources xprocdoc)

$(addprefix website/target/maven/,javadoc doc sources xprocdoc) : website/target/maven/.compile-dependencies
	$(call bash, \
		rm -rf $@ && \
		target=$@ && \
		$(MAKE) -C website $${target#website/} \
	)

.PHONY : dump-maven-cmd
dump-maven-cmd :
	$(call bash, \
		echo "mvn () { $(shell $(call bash, dirname "$$(which mvn)"))/mvn --settings \"$(CURDIR)/$(MVN_SETTINGS)\" $(MVN_PROPERTIES) \"\$$@\"; }" && \
		echo '# Run this command to configure your shell: ' && \
		echo '# eval $$(make $@)' \
	)

.PHONY : dump-gradle-cmd
dump-gradle-cmd :
	$(call bash, echo M2_HOME=$(CURDIR)/$(TARGET_DIR)/.gradle-settings $(GRADLE) $(MVN_PROPERTIES))

.PHONY : help
help :
	$(call bash, \
		echo "make help:"                                                                                               >&2 && \
		echo "	Print list of commands"                                                                                 >&2 && \
		echo "make check:"                                                                                              >&2 && \
		echo "	Incrementally compile and test code"                                                                    >&2 && \
		echo "make dist-deb:"                                                                                           >&2 && \
		echo "	Incrementally compile code and package into a DEB"                                                      >&2 && \
		echo "make dist-rpm:"                                                                                           >&2 && \
		echo "	Incrementally compile code and package into a RPM"                                                      >&2 && \
		echo "make dist-zip-linux:"                                                                                     >&2 && \
		echo "	Incrementally compile code and package into a ZIP for Linux"                                            >&2 && \
		echo "make dist-zip-mac:"                                                                                       >&2 && \
		echo "	Incrementally compile code and package into a ZIP for MacOS"                                            >&2 && \
		echo "make dist-zip-win:"                                                                                       >&2 && \
		echo "	Incrementally compile code and package into a ZIP for Windows"                                          >&2 && \
		echo "make dist-docker-image:"                                                                                  >&2 && \
		echo "	Incrementally compile code and package into a Docker image"                                             >&2 && \
		echo "make dist-cli-deb:"                                                                                       >&2 && \
		echo "	Compile CLI and package into a DEB"                                                                     >&2 && \
		echo "make dist-webui-deb:"                                                                                     >&2 && \
		echo "	Compile Web UI and package into a DEB"                                                                  >&2 && \
		echo "make dist-webui-rpm:"                                                                                     >&2 && \
		echo "	Compile Web UI and package into a RPM"                                                                  >&2 && \
		echo "make run:"                                                                                                >&2 && \
		echo "	Incrementally compile code and run a server locally"                                                    >&2 && \
		echo "make run-webui:"                                                                                          >&2 && \
		echo "	Compile and run web UI locally"                                                                         >&2 && \
		echo "make run-cli:"                                                                                            >&2 && \
		echo "	Get the command for compiling and running CLI locally"                                                  >&2 && \
		echo "make run-docker:"                                                                                         >&2 && \
		echo "	Incrementally compile code and run a server inside a Docker container"                                  >&2 && \
		echo "make run-modules/scripts/[SCRIPT]:"                                                                       >&2 && \
		echo "	Incrementally compile code and run a server with a single script"                                       >&2 && \
		echo "make website:"                                                                                            >&2 && \
		echo "	Build the website"                                                                                      >&2 && \
		echo "make dump-maven-cmd:"                                                                                     >&2 && \
		echo '	Get the Maven command used. To configure your shell: eval $$(make dump-maven-cmd)'                      >&2 \
	)
