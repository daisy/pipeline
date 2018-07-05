.PHONY : all
all : check dist

MVN_WORKSPACE           := .maven-workspace
MVN_CACHE               := .maven-cache

# -----------------------------------
TARGET_DIR              := .make-target
MVN_SETTINGS            := settings.xml
MVN_PROPERTIES          := -Dworkspace="$(CURDIR)/$(MVN_WORKSPACE)" \
                           -Dcache="$(CURDIR)/$(MVN_CACHE)" \
                           -Dorg.ops4j.pax.url.mvn.localRepository="$(CURDIR)/$(MVN_WORKSPACE)" \
                           -Dorg.daisy.org.ops4j.pax.url.mvn.settings="$(CURDIR)/settings.xml"
MVN_RELEASE_CACHE_REPO  := $(MVN_CACHE)
GRADLE                  := $(CURDIR)/libs/dotify/dotify.api/gradlew

ifneq ($(MAKECMDGOALS), dump-maven-cmd)
ifneq ($(MAKECMDGOALS), dump-gradle-cmd)
ifneq ($(MAKECMDGOALS), clean-website)
include .make/main.mk
endif
endif
endif

# -----------------------------------

.PHONY : dist
dist: dist-dmg dist-exe dist-zip-linux dist-zip-minimal dist-deb dist-rpm dist-webui-deb dist-webui-rpm

.PHONY : dist-dmg
dist-dmg : assembly/.dependencies | .maven-init
	cd assembly && \
	$(MVN) clean package -Pmac | $(MVN_LOG)
	mv assembly/target/*.dmg .

.PHONY : dist-exe
dist-exe : assembly/.dependencies | .maven-init
	cd assembly && \
	$(MVN) clean package -Pwin | $(MVN_LOG)
	mv assembly/target/*.exe .

.PHONY : dist-zip-linux
dist-zip-linux : assembly/.dependencies | .maven-init
	cd assembly && \
	$(MVN) clean package -Plinux | $(MVN_LOG)
	mv assembly/target/*.zip .

.PHONY : dist-zip-minimal
dist-zip-minimal : assembly/.dependencies | .maven-init
	cd assembly && \
	$(MVN) clean package -Pminimal | $(MVN_LOG)
	mv assembly/target/*.zip .

.PHONY : dist-deb
dist-deb : assembly/.dependencies | .maven-init
	cd assembly && \
	$(MVN) clean package -Pdeb | $(MVN_LOG)
	mv assembly/target/*.deb .

.PHONY : dist-rpm
dist-rpm : assembly/.dependencies | .maven-init
	if [ -f /etc/redhat-release ]; then \
		cd assembly && \
		$(MVN) clean package -Prpm | $(MVN_LOG) && \
		mv assembly/target/rpm/pipeline2/RPMS/*/*.rpm .; \
	else \
		echo "Skipping RPM because not running RedHat/CentOS"; \
	fi

.PHONY : dist-docker-image
dist-docker-image : dist-zip-linux
	cd assembly && \
	$(MAKE) docker

.PHONY : dist-webui-deb
dist-webui-deb : assembly/.dependencies
	# see webui README for instructions on how to make a signed package for distribution
	cd webui && \
	./activator clean debian:packageBin | $(MVN_LOG)
	mv webui/target/*deb .

.PHONY : dist-webui-rpm
dist-webui-rpm : assembly/.dependencies
	# see webui README for instructions on how to make a signed package for distribution
	cd webui && \
	./activator clean rpm:packageBin
	mv webui/target/rpm/RPMS/noarch/*.rpm .

.PHONY : run
run : assembly/target/dev-launcher/bin/pipeline2
	$<

.PHONY : run-gui
run-gui : assembly/target/dev-launcher/bin/pipeline2
	$< gui

.PHONY : run-webui
run-webui : # webui/.dependencies
	if [ ! -d webui/dp2webui ]; then cp -r webui/dp2webui-cleandb webui/dp2webui ; fi
	cd webui && \
	./activator run

.PHONY : run-docker
run-docker : dist-docker-image
	cd assembly && \
	docker run --name pipeline --detach \
	       -e PIPELINE2_WS_HOST=0.0.0.0 \
	       -p 8181:8181 daisyorg/pipeline2

.PHONY : check

.PHONY : release
release : assembly/.release

.PHONY : $(addprefix check-,$(MODULES))
$(addprefix check-,$(MODULES)) : check-% : %/.last-tested

assembly/target/dev-launcher/bin/pipeline2 : assembly/pom.xml assembly/.dependencies $(call rwildcard,assembly/src/main/,*) | .maven-init
	cd assembly && \
	$(MVN) clean package -Pdev-launcher | $(MVN_LOG)
	rm assembly/target/dev-launcher/etc/*windows*
	if [ "$$(uname)" == Darwin ]; then \
		rm assembly/target/dev-launcher/etc/*linux*; \
	else \
		rm assembly/target/dev-launcher/etc/*mac*; \
	fi

.SECONDARY : cli/.install.zip
cli/.install.zip : cli/.install

cli/.install : cli/cli/*.go

updater/cli/.install : updater/cli/*.go

.SECONDARY : libs/jstyleparser/.install-sources.jar
libs/jstyleparser/.install-sources.jar : libs/jstyleparser/.install

modules/scripts/dtbook-to-odt/.install-doc.jar : $(call rwildcard,modules/scripts/dtbook-to-odt/src/test/,*)

.SECONDARY : \
	modules/braille/liblouis-utils/liblouis-native/.install-mac.jar \
	modules/braille/liblouis-utils/liblouis-native/.install-linux.jar \
	modules/braille/liblouis-utils/liblouis-native/.install-windows.jar
modules/braille/liblouis-utils/liblouis-native/.install-mac.jar \
modules/braille/liblouis-utils/liblouis-native/.install-linux.jar \
modules/braille/liblouis-utils/liblouis-native/.install-windows.jar: \
	modules/braille/liblouis-utils/liblouis-native/.install

.SECONDARY : .dependencies-init
.dependencies-init :
	echo "Recomputing dependencies between modules..." >&2

$(addsuffix /.deps.mk,$(MODULES)) .maven-deps.mk \
$(SUPER_BUILD_SCRIPT_TARGET_DIR)/effective-pom.xml \
$(SUPER_BUILD_SCRIPT_TARGET_DIR)/maven-modules : | .dependencies-init

.maven-init : | $(MVN_WORKSPACE)
# the purpose of the test is for making "make -B" not affect this rule (to speed thing up)
$(MVN_WORKSPACE) :
	if ! [ -e $(MVN_WORKSPACE) ]; then \
		mkdir -p $(MVN_CACHE) && \
		cp -r $(MVN_CACHE) $@; \
	fi

.PHONY : cache
cache :
	if [ -e $(MVN_WORKSPACE) ]; then \
		echo "Caching downloaded artifacts..." >&2 && \
		rm -rf $(MVN_CACHE) && \
		rsync -mr --exclude "*-SNAPSHOT" --exclude "maven-metadata-*.xml" $(MVN_WORKSPACE)/ $(MVN_CACHE); \
	fi

clean : cache clean-workspace clean-old clean-website clean-dist clean-webui

.PHONY : clean-workspace
clean-workspace :
	rm -rf $(MVN_WORKSPACE)

.PHONY : clean-dist
clean-dist :
	rm -f *.zip *.deb *.rpm
	rm -rf webui/dp2webui

.PHONY : clean-webui
clean-webui :
	rm -f *.zip *.deb *.rpm
	rm -rf webui/dp2webui

# clean files generated by previous versions of this Makefile
.PHONY : clean-old
clean-old :
	rm -f .maven-modules
	rm -f .effective-pom.xml
	rm -f .gradle-pom.xml
	rm -f .maven-build.mk
	find . -name .build.mk -exec rm -r "{}" \;
	find * -name .maven-to-install -exec rm -r "{}" \;
	find * -name .maven-to-test -exec rm -r "{}" \;
	find * -name .maven-to-test-dependents -exec rm -r "{}" \;
	find * -name .maven-snapshot-dependencies -exec rm -r "{}" \;
	find * -name .maven-effective-pom.xml -exec rm -r "{}" \;
	find * -name .maven-dependencies-to-install -exec rm -r "{}" \;
	find * -name .maven-dependencies-to-test -exec rm -r "{}" \;
	find * -name .maven-dependencies-to-test-dependents -exec rm -r "{}" \;
	find * -name .gradle-to-test -exec rm -r "{}" \;
	find * -name .gradle-snapshot-dependencies -exec rm -r "{}" \;
	find * -name .gradle-dependencies-to-install -exec rm -r "{}" \;
	find * -name .gradle-dependencies-to-test -exec rm -r "{}" \;

.PHONY : gradle-clean
gradle-clean :
	$(GRADLE) clean

PHONY : $(addprefix eclipse-,$(MODULES))
$(addprefix eclipse-,$(MODULES)) : eclipse-% : %/.project

TEMP_REPOS := modules/scripts/dtbook-to-daisy3/target/test/local-repo

.PHONY : go-offline
go-offline :
	if [ -e $(MVN_WORKSPACE) ]; then \
		for repo in $(TEMP_REPOS); do \
			if [ -e $$repo ]; then \
				rsync -mr --exclude "*-SNAPSHOT" --exclude "maven-metadata-*.xml" $$repo/ $(MVN_WORKSPACE); \
			fi \
		done \
	fi

.PHONY : checked
checked :
	touch $(addsuffix /.last-tested,$(MODULES))

website/target/maven/pom.xml : $(addprefix website/src/_data/,modules.yml api.yml versions.yml)
	cd website && \
	$(MAKE) target/maven/pom.xml

export MVN_OPTS = --settings '$(CURDIR)/settings.xml' -Dworkspace='$(CURDIR)/$(MVN_WORKSPACE)' -Dcache='$(CURDIR)/$(MVN_CACHE)' -Pstaged-releases

$(addprefix website/target/maven/,javadoc doc sources xprocdoc) : website/target/maven/.deps.mk website/target/maven/.dependencies
	rm -rf $@
	cd website && \
	target=$@ && \
	$(MAKE) $${target#website/}

.PHONY : website
website : | $(addprefix website/target/maven/,javadoc doc sources xprocdoc)
	cd website && \
	make

.PHONY : serve-website
serve-website : | $(addprefix website/target/maven/,javadoc doc sources xprocdoc)
	cd website && \
	$(MAKE) serve

.PHONY : publish-website
publish-website : | $(addprefix website/target/maven/,javadoc doc sources xprocdoc)
	cd website && \
	$(MAKE) publish

.PHONY : clean-website
clean-website :
	cd website && \
	$(MAKE) clean

.PHONY : dump-maven-cmd
dump-maven-cmd :
	echo "mvn () { $(shell dirname "$$(which mvn)")/mvn --settings \"$(CURDIR)/$(MVN_SETTINGS)\" $(MVN_PROPERTIES) \"\$$@\"; }"
	echo '# Run this command to configure your shell: '
	echo '# eval $$(make $@)'

.PHONY : dump-gradle-cmd
dump-gradle-cmd :
	echo M2_HOME=$(CURDIR)/$(SUPER_BUILD_SCRIPT_TARGET_DIR)/.gradle-settings $(GRADLE) $(MVN_PROPERTIES)

.PHONY : help
help :
	echo "make all:"                                                                                                >&2
	echo "	Incrementally compile and test code and package into a DMG, a EXE, a ZIP (for Linux), a DEB and a RPM"  >&2
	echo "make check:"                                                                                              >&2
	echo "	Incrementally compile and test code"                                                                    >&2
	echo "make dist:"                                                                                               >&2
	echo "	Incrementally compile code and package into a DMG, a EXE, a ZIP (for Linux), a DEB and a RPM"           >&2
	echo "make dist-dmg:"                                                                                           >&2
	echo "	Incrementally compile code and package into a DMG"                                                      >&2
	echo "make dist-exe:"                                                                                           >&2
	echo "	Incrementally compile code and package into a EXE"                                                      >&2
	echo "make dist-zip-linux:"                                                                                     >&2
	echo "	Incrementally compile code and package into a ZIP (for Linux)"                                          >&2
	echo "make dist-deb:"                                                                                           >&2
	echo "	Incrementally compile code and package into a DEB"                                                      >&2
	echo "make dist-rpm:"                                                                                           >&2
	echo "	Incrementally compile code and package into a RPM"                                                      >&2
	echo "make dist-webui-deb:"                                                                                     >&2
	echo "	Compile Web UI and package into a DEB"                                                                  >&2
	echo "make dist-webui-rpm:"                                                                                     >&2
	echo "	Compile Web UI and package into a RPM"                                                                  >&2
	echo "make run:"                                                                                                >&2
	echo "	Incrementally compile code and run locally"                                                             >&2
	echo "make run-gui:"                                                                                            >&2
	echo "	Incrementally compile code and run GUI locally"                                                         >&2
	echo "make run-webui:"                                                                                          >&2
	echo "	Compile and run web UI locally"                                                                         >&2
	echo "make website:"                                                                                            >&2
	echo "	Build the website"                                                                                      >&2
	echo "make dump-maven-cmd:"                                                                                     >&2
	echo '	Get the Maven command used. To configure your shell: eval $$(make dump-maven-cmd)'                      >&2

ifndef VERBOSE
.SILENT:
endif
