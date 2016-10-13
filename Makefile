POMS := $(shell find * -name pom.xml ! -path '*/target/*')
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
dist: dist-zip dist-deb

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

.PHONY : run
run : assembly/target/dev-launcher/bin/pipeline2
	$<

.PHONY : run-gui
run-gui : assembly/target/dev-launcher/bin/pipeline2
	$< gui

.PHONY : check
check : gradle-test maven-test

.PHONY : compile
compile : gradle-install maven-install

assembly/target/dev-launcher/bin/pipeline2 : compile
	cd assembly && \
	$(MVN) clean package -Pdev-launcher | $(MVN_LOG)
	rm assembly/target/dev-launcher/etc/*windows*
	if [ "$$(uname)" == Darwin ]; then \
		rm assembly/target/dev-launcher/etc/*linux*; \
	else \
		rm assembly/target/dev-launcher/etc/*mac*; \
	fi

.PHONY: maven-test
maven-test : .maven-modules-test
	if [ -s $< ]; then \
		cat $< | while read -r line; do \
			echo "--> $$line"; \
		done && \
		modules=$$(cat $< |paste -sd , -) && \
		$(MVN) --projects $$modules test package integration-test \
			org.codehaus.mojo:exec-maven-plugin:1.5.0:exec -Dexec.executable=sh -Dexec.args="-c 'rm .maven-test'" \
		| $(MVN_LOG); \
	else \
		echo "All modules have been tested already" >&2; \
	fi

maven-test : maven-install

.PHONY : gradle-test
gradle-test : .gradle-test
	if [ -e $< ]; then \
		$(GRADLE) test && \
		rm $<; \
	fi

gradle-test : gradle-install

.PHONY : maven-install
maven-install : .maven-modules-install 
	if [ -s $< ]; then \
		cat $< | while read -r line; do \
			echo "--> $$line"; \
		done && \
		modules=$$(cat $< |paste -sd , -) && \
		$(MVN) --projects $$modules clean install -DskipTests -Dinvoker.skip=true \
			org.codehaus.mojo:exec-maven-plugin:1.5.0:exec -Dexec.executable=sh \
				-Dexec.args="-c 'if [ -e .maven-install ]; then mv .maven-install .maven-test-dependents; fi'" \
		| $(MVN_LOG); \
	else \
		echo "All modules are up to date" >&2; \
	fi

maven-install maven-test : gradle-install

.PHONY : gradle-install
gradle-install : .gradle-install
	if [ -e $< ]; then \
		$(GRADLE) install && \
		rm $<; \
	else \
		echo "All modules are up to date" >&2; \
	fi

# This target should be called from a target that depends on maven-install first
.INTERMEDIATE : .maven-modules-test
.maven-modules-test : .maven-modules-test-dependents
	echo "Finding out which modules need to be tested..." >&2
	if [ -s $< ]; then \
		modules=$$(cat $< |paste -sd , -) && \
		$(MVN) --quiet --projects $$modules --also-make-dependents \
			org.codehaus.mojo:exec-maven-plugin:1.5.0:exec -Dexec.executable=sh -Dexec.args="-c 'touch .maven-test'"; \
	fi
	for module in $$(cat .maven-modules); do \
		rm -rf $$module/.maven-test-dependents && \
		if [ -e $$module/.maven-test ]; then \
			echo $$module; \
		fi \
	done > $@

# This target should be called from a target that depends on maven-install first
.INTERMEDIATE : .maven-modules-test-dependents
.maven-modules-test-dependents : .maven-modules
	for module in $$(cat $<); do \
		if [ -e $$module/.maven-test-dependents ]; then \
			echo $$module; \
		fi \
	done > $@

# Build only the modules that have changed since the last build.
.INTERMEDIATE : .maven-modules-install
.maven-modules-install : .maven-modules
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
			touch $$module/.maven-{install,test}; \
		fi \
	done
	for module in $$(cat $<); do \
		if [ -e $$module/.maven-install ]; then \
			echo $$module; \
		fi \
	done > $@

# From all the Maven modules only include those in the build that are referenced from the
# super aggregator, have a snapshot version, and are listed in the assembly with that same
# version number. If the module is not listed in the assembly at all (also not a different
# version), we assume it is a helper module (parent, BoM, plugin, etc.) so we include it
# in the build as well.
.maven-modules : bom.xml $(POMS)
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
				                                ][1]/*[local-name()='version']/text()" bom.xml 2>/dev/null); then \
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

.gradle-test : .gradle-install

.gradle-install : .gradle-modules
	echo "Looking for changes..." >&2
	for module in $$(cat $<); do \
		v=$$(cat $$module/gradle.properties | grep '^version' | sed 's/^version=//') && \
		a=$$(basename $$module) && \
		g=$$(cat $$module/build.gradle | grep '^group' | sed "s/^group *= *'\(.*\)'/\1/") && \
		dest="$(MVN_WORKSPACE)/$$(echo $$g |tr . /)/$$a/$$v" && \
		if [[ ! -e "$$dest/$$a-$$v.pom" ]] || \
		   [[ ! -e "$$dest/maven-metadata-local.xml" ]] || \
		   [[ -n $$(find $$module/{build.gradle,gradle.properties,src} -newer "$$dest/maven-metadata-local.xml" 2>/dev/null) ]]; then \
			touch .gradle-{install,test} && \
			break; \
		fi \
	done

.gradle-modules : bom.xml $(GRADLE_FILES)
	cat settings.gradle | sed "s/^include  *'\(.*\)'/\1/" | tr : / \
	| while read -r module; do \
		v=$$(cat $$module/gradle.properties | grep '^version' | sed 's/^version=//') && \
		if [[ "$$v" =~ -SNAPSHOT$$ ]]; then \
			a=$$(basename $$module) && \
			g=$$(cat $$module/build.gradle | grep '^group' | sed "s/^group *= *'\(.*\)'/\1/") && \
			if v_in_bom=$$(xmllint --xpath "//*[local-name()='dependency'][ \
			                                    *[local-name()='groupId']='$$g' and \
			                                    *[local-name()='artifactId']='$$a' \
			                                ][1]/*[local-name()='version']/text()" bom.xml 2>/dev/null); then \
				if [ $$v_in_bom == $$v ]; then \
					echo $$module; \
				fi \
			else \
				echo $$module; \
			fi \
		fi \
	done >$@

# The assembly defines which versions of which modules we have to include in the build.
bom.xml : assembly/pom.xml $(POMS)
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

gradle-install gradle-test : .gradle-settings/conf/settings.xml
.gradle-settings/conf/settings.xml : settings.xml
	mkdir -p $(dir $@)
	cp $< $@

.maven-modules .gradle-modules bom.xml : .modules-init
.SECONDARY : .modules-init
.modules-init :
	echo "Recomputing modules to include in the build..." >&2

gradle-install .gradle-install .gradle-test : .gradle-init
.SECONDARY : .gradle-init
.gradle-init :
	echo "╔════════╗" >&2
	echo "║ GRADLE ║" >&2
	echo "╚════════╝" >&2

maven-install .maven-modules-install .maven-modules-test : .maven-init
.SECONDARY : .maven-init
.maven-init :
	echo "╔═══════╗" >&2
	echo "║ MAVEN ║" >&2
	echo "╚═══════╝" >&2
	rm -f maven.log

.maven-init : gradle-test gradle-install

gradle-test gradle-install maven-test maven-install bom.xml : workspace

.PHONY : workspace
workspace : $(MVN_WORKSPACE)

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
	rm -f .maven-modules .maven-modules-install .maven-modules-test .maven-modules-test-dependents maven.log bom.xml
	rm -f .gradle-install .gradle-test
	rm -f *.zip *.deb
	find * -name .maven-install -exec rm -r "{}" \;
	find * -name .maven-test -exec rm -r "{}" \;
	find * -name .maven-test-dependents -exec rm -r "{}" \;

.PHONY : help
help :
	echo "make all:"                                                                                >&2
	echo "	Incrementally compile and test code and package into a ZIP for each platform and a DEB" >&2
	echo "make compile:"                                                                            >&2
	echo "	Incrementally compile code"                                                             >&2
	echo "make check:"                                                                              >&2
	echo "	Incrementally compile and test code"                                                    >&2
	echo "make dist:"                                                                               >&2
	echo "	Incrementally compile code and package into a ZIP for each platform and a DEB"          >&2
	echo "make dist-zip:"                                                                           >&2
	echo "	Incrementally compile code and package into a ZIP for each platform"                    >&2
	echo "make dist-deb:"                                                                           >&2
	echo "	Incrementally compile code and package into a DEB"                                      >&2
	echo "make run:"                                                                                >&2
	echo "	Incrementally compile code and run locally"                                             >&2
	echo "make run-gui:"                                                                            >&2
	echo "	Incrementally compile code and run GUI locally"                                         >&2

ifndef VERBOSE
.SILENT:
endif
