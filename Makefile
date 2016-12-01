POMS := $(shell find * -name pom.xml ! -path '*/target/*')
MAVEN_MODULES := $(patsubst %/pom.xml,%,$(filter-out pom.xml assembly/pom.xml,$(POMS)))
GRADLE_FILES := $(shell find * -name build.gradle -o -name settings.gradle -o -name gradle.properties)

MVN_WORKSPACE := $(CURDIR)/.maven-workspace
MVN_CACHE := $(CURDIR)/.maven-cache

MVN := mvn --settings "$(CURDIR)/settings.xml" -Dworkspace="$(MVN_WORKSPACE)" -Dcache="$(MVN_CACHE)" \
           -Dorg.ops4j.pax.url.mvn.localRepository="$(MVN_WORKSPACE)"
GRADLE := M2_HOME=$(CURDIR)/.gradle-settings libs/dotify/dotify.api/gradlew -Dworkspace="$(MVN_WORKSPACE)" -Dcache="$(MVN_CACHE)"

MVN_LOG := tee -a $(CURDIR)/maven.log | cut -c1-1000 | pcregrep -M "^\[INFO\] -+\n\[INFO\] Building .*\n\[INFO\] -+$$|^\[(ERROR|WARNING)\]"; \
           test $${PIPESTATUS[0]} -eq 0

SHELL := /bin/bash

.PHONY : all
all : compile check dist

.PHONY : dist
dist: dist-zip dist-deb dist-rpm dist-webui-deb dist-webui-rpm

.PHONY : dist-zip
dist-zip : compile
	cd assembly && \
	$(MVN) clean package -Plinux,mac,win | $(MVN_LOG)
	mv assembly/target/*.zip .

.PHONY : dist-deb
dist-deb : compile
	cd assembly && \
	$(MVN) clean package -Pdeb | $(MVN_LOG)
	mv assembly/target/*.deb .

.PHONY : dist-rpm
dist-rpm : compile
	if [ -f /etc/redhat-release ]; then \
		cd assembly && \
		$(MVN) clean package -Prpm | $(MVN_LOG) && \
		mv assembly/target/rpm/pipeline2/RPMS/*/*.rpm .; \
	else \
		echo "Skipping RPM because not running RedHat/CentOS"; \
	fi

.PHONY : dist-webui-deb
dist-webui-deb : compile
	# see webui README for instructions on how to make a signed package for distribution
	cd webui && \
	./activator clean debian:packageBin | $(MVN_LOG)
	mv webui/target/*deb .

.PHONY : dist-webui-rpm
dist-webui-rpm : compile
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
run-webui :
	if [ ! -d webui/dp2webui ]; then cp -r webui/dp2webui-cleandb webui/dp2webui ; fi
	cd webui && \
	./activator run

.PHONY : check
check : assembly/.gradle-test-dependencies assembly/.maven-test-dependencies

.PHONY : compile
compile : assembly/.gradle-install-dependencies assembly/.maven-install-dependencies

.PHONY : $(MAVEN_MODULES)
$(MAVEN_MODULES) : % : compile-%

assembly/target/dev-launcher/bin/pipeline2 : compile
	cd assembly && \
	$(MVN) clean package -Pdev-launcher | $(MVN_LOG)
	rm assembly/target/dev-launcher/etc/*windows*
	if [ "$$(uname)" == Darwin ]; then \
		rm assembly/target/dev-launcher/etc/*linux*; \
	else \
		rm assembly/target/dev-launcher/etc/*mac*; \
	fi

.PHONY : $(addprefix compile-,$(MAVEN_MODULES))
$(addprefix compile-,$(MAVEN_MODULES)) : compile-% : %/.gradle-install-dependencies %/.maven-install-dependencies
	cd $(dir $<) && \
	$(MVN) clean install -DskipTests | $(MVN_LOG) && \
	if [ -e .maven-to-install ]; then \
		mv .maven-to-install .maven-to-test-dependents; \
	fi

.PHONY : $(addprefix check-,$(MAVEN_MODULES))
$(addprefix check-,$(MAVEN_MODULES)) : check-% : %/.gradle-install-dependencies %/.maven-install-dependencies
	cd $(dir $<) && \
	$(MVN) clean test && \
	if [ -e .maven-to-test ]; then \
		rm .maven-to-test; \
	fi

.PHONY : $(addsuffix /.maven-test-dependencies,assembly $(MAVEN_MODULES))
$(addsuffix /.maven-test-dependencies,assembly $(MAVEN_MODULES)) : %/.maven-test-dependencies : %/.maven-dependencies-to-test
	if [ -s $< ]; then \
		cat $< | while read -r line; do \
			echo "--> $$line"; \
		done && \
		modules=$$(cat $< |paste -sd , -) && \
		$(MVN) --projects $$modules test package integration-test \
			org.codehaus.mojo:exec-maven-plugin:1.5.0:exec -Dexec.executable=sh -Dexec.args="-c 'rm .maven-to-test'" \
		| $(MVN_LOG); \
	else \
		echo "All modules have been tested already" >&2; \
	fi

$(addsuffix /.maven-test-dependencies,assembly $(MAVEN_MODULES)) : %/.maven-test-dependencies : %/.maven-install-dependencies

.PHONY : $(addsuffix /.gradle-test-dependencies,assembly $(MAVEN_MODULES))
$(addsuffix /.gradle-test-dependencies,assembly $(MAVEN_MODULES)) : %/.gradle-test-dependencies : %/.gradle-dependencies-to-test
	if [ -s $< ]; then \
		$(GRADLE) test && \
		for module in $$(cat $<); do \
			rm $$module/.gradle-to-test ;\
		done \
	fi

$(addsuffix /.gradle-test-dependencies,assembly $(MAVEN_MODULES)) : %/.gradle-test-dependencies : %/.gradle-install-dependencies

.PHONY : $(addsuffix /.maven-install-dependencies,assembly $(MAVEN_MODULES))
$(addsuffix /.maven-install-dependencies,assembly $(MAVEN_MODULES)) : %/.maven-install-dependencies : %/.maven-dependencies-to-install
	if [ -s $< ]; then \
		cat $< | while read -r line; do \
			echo "--> $$line"; \
		done && \
		modules=$$(cat $< |paste -sd , -) && \
		$(MVN) --projects $$modules clean install -DskipTests -Dinvoker.skip=true \
			org.codehaus.mojo:exec-maven-plugin:1.5.0:exec -Dexec.executable=sh \
				-Dexec.args="-c 'if [ -e .maven-to-install ]; then mv .maven-to-install .maven-to-test-dependents; fi'" \
		| $(MVN_LOG); \
	else \
		echo "All modules are up to date" >&2; \
	fi

$(addsuffix /.maven-install-dependencies,assembly $(MAVEN_MODULES)) : %/.maven-install-dependencies : %/.gradle-install-dependencies
$(addsuffix /.maven-test-dependencies,assembly $(MAVEN_MODULES)) : %/.maven-test-dependencies : %/.gradle-install-dependencies

.PHONY : $(addsuffix /.gradle-install-dependencies,assembly $(MAVEN_MODULES))
$(addsuffix /.gradle-install-dependencies,assembly $(MAVEN_MODULES)) : %/.gradle-install-dependencies : %/.gradle-dependencies-to-install
	if [ -s $< ]; then \
		$(GRADLE) install; \
		for module in $$(cat $<); do \
			rm $$module/.gradle-to-install ;\
		done \
	else \
		echo "All modules are up to date" >&2; \
	fi

# This target should be called from a target that depends on .maven-install-dependencies first
.INTERMEDIATE : $(addsuffix /.maven-dependencies-to-test,assembly $(MAVEN_MODULES))
$(addsuffix /.maven-dependencies-to-test,assembly $(MAVEN_MODULES)) : %/.maven-dependencies-to-test : %/.maven-dependencies-to-test-dependents
	echo "Finding out which modules need to be tested..." >&2
	if [ -s $< ]; then \
		modules=$$(cat $< |paste -sd , -) && \
		$(MVN) --quiet --projects $$modules --also-make-dependents \
			org.codehaus.mojo:exec-maven-plugin:1.5.0:exec -Dexec.executable=sh -Dexec.args="-c 'touch .maven-to-test'"; \
	fi
	for module in $$(cat $$(dirname $@)/.maven-snapshot-dependencies); do \
		rm -rf $$module/.maven-to-test-dependents && \
		if [ -e $$module/.maven-to-test ]; then \
			echo $$module; \
		fi \
	done > $@

# This target should be called from a target that depends on .maven-install-dependencies first
.INTERMEDIATE : $(addsuffix /.maven-dependencies-to-test-dependents,assembly $(MAVEN_MODULES))
$(addsuffix /.maven-dependencies-to-test-dependents,assembly $(MAVEN_MODULES)) : %/.maven-dependencies-to-test-dependents : %/.maven-snapshot-dependencies
	for module in $$(cat $<); do \
		if [ -e $$module/.maven-to-test-dependents ]; then \
			echo $$module; \
		fi \
	done > $@

# Build only the modules that have changed since the last build.
.INTERMEDIATE : $(addsuffix /.maven-dependencies-to-install,assembly $(MAVEN_MODULES))
$(addsuffix /.maven-dependencies-to-install,assembly $(MAVEN_MODULES)) : %/.maven-dependencies-to-install : %/.maven-snapshot-dependencies
	echo "Looking for changes..." >&2
	for module in $$(cat $<); do \
		v=$$(xmllint --xpath "/*/*[local-name()='version']/text()" $$module/pom.xml) && \
		g=$$(xmllint --xpath "/*/*[local-name()='groupId']/text()" $$module/pom.xml 2>/dev/null) || \
		g=$$(xmllint --xpath "/*/*[local-name()='parent']/*[local-name()='groupId']/text()" $$module/pom.xml) && \
		a=$$(xmllint --xpath "/*/*[local-name()='artifactId']/text()" $$module/pom.xml) && \
		dest="$(MVN_WORKSPACE)/$$(echo $$g |tr . /)/$$a/$$v" && \
		if [[ ! -e "$$dest/$$a-$$v.pom" ]] || \
		   [[ ! -e "$$dest/maven-metadata-local.xml" ]] || \
		   [[ -n $$(find $$module/{pom.xml,src} -newer "$$dest/maven-metadata-local.xml" 2>/dev/null) ]] || \
		   [[ -n $$(find $$module -name '*.go' -newer "$$dest/maven-metadata-local.xml" 2>/dev/null) ]]; then \
			touch $$module/.maven-to-{install,test}; \
		fi \
	done
	for module in $$(cat $<); do \
		if [ -e $$module/.maven-to-install ]; then \
			echo $$module; \
		fi \
	done > $@

# From all the Maven modules only include those in the build that are referenced from the
# super aggregator, have a snapshot version, and are listed in the assembly with that same
# version number. If the module is not listed in the assembly at all (also not a different
# version), we assume it is a helper module (parent, BoM, plugin, etc.) so we include it
# in the build as well.
$(addsuffix /.maven-snapshot-dependencies,assembly $(MAVEN_MODULES)) : %/.maven-snapshot-dependencies : %/.maven-effective-pom.xml $(POMS)
	function print_modules_recursively() { \
		local module=$$1 && \
		submodules=($$(xmllint --format --xpath "/*/*[local-name()='modules']/*" $$module/pom.xml 2>/dev/null \
		               | sed -e 's/<module>\([^<]*\)<\/module>/\1 /g')) && \
		if [[ $${#submodules[*]} -gt 0 ]]; then \
			for sub in $${submodules[*]}; do \
				print_modules_recursively $$module/$$sub; \
			done \
		else \
			v=$$(xmllint --xpath "/*/*[local-name()='version']/text()" $$module/pom.xml) && \
			if [[ "$$v" =~ -SNAPSHOT$$ ]]; then \
				g=$$(xmllint --xpath "/*/*[local-name()='groupId']/text()" $$module/pom.xml 2>/dev/null) || \
				g=$$(xmllint --xpath "/*/*[local-name()='parent']/*[local-name()='groupId']/text()" $$module/pom.xml) && \
				a=$$(xmllint --xpath "/*/*[local-name()='artifactId']/text()" $$module/pom.xml) && \
				if v_in_bom=$$(xmllint --xpath "//*[local-name()='dependency'][ \
				                                    *[local-name()='groupId']='$$g' and \
				                                    *[local-name()='artifactId']='$$a' \
				                                ][1]/*[local-name()='version']/text()" $< 2>/dev/null); then \
					if [ $$v_in_bom == $$v ]; then \
						echo $$module; \
					fi \
				else \
					echo $$module; \
				fi \
			fi \
		fi \
	} && \
	print_modules_recursively . >$@

.INTERMEDIATE : $(addsuffix /.gradle-dependencies-to-test,assembly $(MAVEN_MODULES))
$(addsuffix /.gradle-dependencies-to-test,assembly $(MAVEN_MODULES)) : %/.gradle-dependencies-to-test : %/.gradle-dependencies-to-install
	for module in $$(cat $$(dirname $@)/.gradle-snapshot-dependencies); do \
		if [ -e $$module/.maven-to-test ]; then \
			echo $$module; \
		fi \
	done > $@

.INTERMEDIATE : $(addsuffix /.gradle-dependencies-to-install,assembly $(MAVEN_MODULES))
$(addsuffix /.gradle-dependencies-to-install,assembly $(MAVEN_MODULES)) : %/.gradle-dependencies-to-install : %/.gradle-snapshot-dependencies
	echo "Looking for changes..." >&2
	for module in $$(cat $<); do \
		v=$$(cat $$module/gradle.properties | grep '^version' | sed 's/^version=//') && \
		a=$$(basename $$module) && \
		g=$$(cat $$module/build.gradle | grep '^group' | sed "s/^group *= *['\"]\(.*\)['\"]/\1/") && \
		dest="$(MVN_WORKSPACE)/$$(echo $$g |tr . /)/$$a/$$v" && \
		if [[ ! -e "$$dest/$$a-$$v.pom" ]] || \
		   [[ ! -e "$$dest/maven-metadata-local.xml" ]] || \
		   [[ -n $$(find $$module/{build.gradle,gradle.properties,src} -newer "$$dest/maven-metadata-local.xml" 2>/dev/null) ]]; then \
			touch $$module/.gradle-to-{install,test}; \
		fi \
	done
	for module in $$(cat $<); do \
		if [ -e $$module/.gradle-to-install ]; then \
			echo $$module; \
		fi \
	done > $@

$(addsuffix /.gradle-snapshot-dependencies,assembly $(MAVEN_MODULES)) : %/.gradle-snapshot-dependencies : %/.maven-effective-pom.xml $(GRADLE_FILES)
	cat settings.gradle | sed "s/^include  *'\(.*\)'/\1/" | tr : / \
	| while read -r module; do \
		v=$$(cat $$module/gradle.properties | grep '^version' | sed 's/^version=//') && \
		if [[ "$$v" =~ -SNAPSHOT$$ ]]; then \
			a=$$(basename $$module) && \
			g=$$(cat $$module/build.gradle | grep '^group' | sed "s/^group *= *['\"]\(.*\)['\"]/\1/") && \
			if v_in_bom=$$(xmllint --xpath "//*[local-name()='dependency'][ \
			                                    *[local-name()='groupId']='$$g' and \
			                                    *[local-name()='artifactId']='$$a' \
			                                ][1]/*[local-name()='version']/text()" $< 2>/dev/null); then \
				if [ $$v_in_bom == $$v ]; then \
					echo $$module; \
				fi \
			else \
				echo $$module; \
			fi \
		fi \
	done >$@

# The assembly defines which versions of which modules we have to include in the build.
# This target should be called from a target that depends on $(MVN_WORKSPACE) first
$(addsuffix /.maven-effective-pom.xml,assembly $(MAVEN_MODULES)) : %/.maven-effective-pom.xml : %/pom.xml $(POMS)
	poms=($(POMS)) && \
	for pom in $${poms[*]}; do \
		v=$$(xmllint --xpath "/*/*[local-name()='version']/text()" $$pom) && \
		if [[ "$$v" =~ -SNAPSHOT$$ ]]; then \
			g=$$(xmllint --xpath "/*/*[local-name()='groupId']/text()" $$pom 2>/dev/null) || \
			g=$$(xmllint --xpath "/*/*[local-name()='parent']/*[local-name()='groupId']/text()" $$pom) && \
			a=$$(xmllint --xpath "/*/*[local-name()='artifactId']/text()" $$pom) && \
			dest="$(MVN_WORKSPACE)/$$(echo $$g |tr . /)/$$a/$$v/$$a-$$v.pom" && \
			mkdir -p $$(dirname $$dest) && \
			cp $$pom $$dest; \
		fi \
	done
	cd $(dir $<) && $(MVN) --quiet help:effective-pom -Doutput=$(CURDIR)/$@

$(addsuffix /.gradle-install-dependencies,assembly $(MAVEN_MODULES)) : .gradle-settings/conf/settings.xml
$(addsuffix /.gradle-test-dependencies,assembly $(MAVEN_MODULES)) : .gradle-settings/conf/settings.xml

.gradle-settings/conf/settings.xml : settings.xml
	mkdir -p $(dir $@)
	cp $< $@

$(addsuffix /.maven-snapshot-dependencies,assembly $(MAVEN_MODULES)) : .modules-init
$(addsuffix /.gradle-snapshot-dependencies,assembly $(MAVEN_MODULES)) : .modules-init
$(addsuffix /.maven-effective-pom.xml,assembly $(MAVEN_MODULES)) : .modules-init

.SECONDARY : .modules-init
.modules-init :
	echo "Recomputing modules to include in the build..." >&2

$(addsuffix /.gradle-install-dependencies,assembly $(MAVEN_MODULES)) : .gradle-init
$(addsuffix /.gradle-dependencies-to-install,assembly $(MAVEN_MODULES)) : .gradle-init
$(addsuffix /.gradle-dependencies-to-test,assembly $(MAVEN_MODULES)) : .gradle-init

.SECONDARY : .gradle-init
.gradle-init :
	echo "╔════════╗" >&2
	echo "║ GRADLE ║" >&2
	echo "╚════════╝" >&2

$(addsuffix /.maven-install-dependencies,assembly $(MAVEN_MODULES)) : .maven-init
$(addsuffix /.maven-dependencies-to-install,assembly $(MAVEN_MODULES)) : .maven-init
$(addsuffix /.maven-dependencies-to-test,assembly $(MAVEN_MODULES)) : .maven-init

.SECONDARY : .maven-init
.maven-init :
	echo "╔═══════╗" >&2
	echo "║ MAVEN ║" >&2
	echo "╚═══════╝" >&2
	rm -f maven.log

$(addsuffix /.gradle-test-dependencies,assembly $(MAVEN_MODULES)) : $(MVN_WORKSPACE)
$(addsuffix /.gradle-install-dependencies,assembly $(MAVEN_MODULES)) : $(MVN_WORKSPACE)
$(addsuffix /.maven-test-dependencies,assembly $(MAVEN_MODULES)) : $(MVN_WORKSPACE)
$(addsuffix /.maven-install-dependencies,assembly $(MAVEN_MODULES)) : $(MVN_WORKSPACE)

$(MVN_WORKSPACE) :
	mkdir -p $(MVN_CACHE)
	cp -r $(MVN_CACHE) $@

.PHONY : cache
cache :
	if [ -e $(MVN_WORKSPACE) ]; then \
		echo "Caching repository..." >&2 && \
		rm -rf $(MVN_CACHE) && \
		rsync -mr --exclude "*-SNAPSHOT" --exclude "maven-metadata-*.xml" $(MVN_WORKSPACE)/ $(MVN_CACHE); \
	fi

.PHONY : clean
clean : cache
	rm -rf $(MVN_WORKSPACE)
	rm -f maven.log
	rm -f *.zip *.deb *.rpm
	rm -rf webui/dp2webui
	find * -name .maven-to-install -exec rm -r "{}" \;
	find * -name .maven-to-test -exec rm -r "{}" \;
	find * -name .maven-to-test-dependents -exec rm -r "{}" \;
	find * -name .maven-snapshot-dependencies -exec rm -r "{}" \;
	find * -name .maven-effective-pom.xml -exec rm -r "{}" \;
	find * -name .maven-dependencies-to-install -exec rm -r "{}" \;
	find * -name .maven-dependencies-to-test -exec rm -r "{}" \;
	find * -name .maven-dependencies-to-test-dependents -exec rm -r "{}" \;
	find * -name .gradle-dependencies-to-install -exec rm -r "{}" \;
	find * -name .gradle-dependencies-to-test -exec rm -r "{}" \;

.PHONY : gradle-clean
gradle-clean :
	$(GRADLE) clean

.PHONY : help
help :
	echo "make all:"                                                                                       >&2
	echo "	Incrementally compile and test code and package into a ZIP for each platform, a DEB and a RPM" >&2
	echo "make compile:"                                                                                   >&2
	echo "	Incrementally compile code"                                                                    >&2
	echo "make check:"                                                                                     >&2
	echo "	Incrementally compile and test code"                                                           >&2
	echo "make dist:"                                                                                      >&2
	echo "	Incrementally compile code and package into a ZIP for each platform, a DEB and a RPM"          >&2
	echo "make dist-zip:"                                                                                  >&2
	echo "	Incrementally compile code and package into a ZIP for each platform"                           >&2
	echo "make dist-deb:"                                                                                  >&2
	echo "	Incrementally compile code and package into a DEB"                                             >&2
	echo "make dist-rpm:"                                                                                  >&2
	echo "	Incrementally compile code and package into a RPM"                                             >&2
	echo "make dist-webui-deb:"                                                                            >&2
	echo "	Compile Web UI and package into a DEB"                                                         >&2
	echo "make dist-webui-rpm:"                                                                            >&2
	echo "	Compile Web UI and package into a RPM"                                                         >&2
	echo "make run:"                                                                                       >&2
	echo "	Incrementally compile code and run locally"                                                    >&2
	echo "make run-gui:"                                                                                   >&2
	echo "	Incrementally compile code and run GUI locally"                                                >&2
	echo "make run-webui:"                                                                                 >&2
	echo "	Compile and run web UI locally"                                                                >&2

ifndef VERBOSE
.SILENT:
endif

# FIXME: why do I need to do this?
.PRECIOUS: $(addsuffix /.maven-effective-pom.xml,assembly $(MAVEN_MODULES))
.PRECIOUS: $(addsuffix /.maven-snapshot-dependencies,assembly $(MAVEN_MODULES))
.PRECIOUS: $(addsuffix /.gradle-snapshot-dependencies,assembly $(MAVEN_MODULES))
