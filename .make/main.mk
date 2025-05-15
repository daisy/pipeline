ROOT_DIR          := $(CURDIR)
MY_DIR            := $(shell $(call bash, dirname $(lastword $(MAKEFILE_LIST))))
TARGET_DIR        ?= $(MY_DIR)/target
GRADLE_FILES      := $(shell $(call bash, find * -name build.gradle -o -name settings.gradle -o -name gradle.properties))
GRADLE_MODULES    := $(patsubst %/build.gradle,%,$(filter %/build.gradle,$(GRADLE_FILES)))
MODULES            = $(MAVEN_MODULES) $(GRADLE_MODULES)
GITREPOS          := $(shell $(call bash, find * -name .gitrepo -exec dirname {} \;))
MVN               := $(ROOT_DIR)/$(MY_DIR)/mvn
MVN_LOG           := cat>>$(ROOT_DIR)/maven.log
GRADLE            := M2_HOME=$(ROOT_DIR)/$(TARGET_DIR)/.gradle-settings $(ROOT_DIR)/$(MY_DIR)/gradle.sh $(MVN_PROPERTIES)
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
IMPORTS := build.mvn
STATIC_IMPORTS := build.core.*

export ROOT_DIR MY_DIR TARGET_DIR MVN MVN_SETTINGS MVN_PROPERTIES MVN_LOG MVN_RELEASE_CACHE_REPO GRADLE MAKE SHELL IMPORTS STATIC_IMPORTS CLASSPATH
# MAKECMDGOALS used in gradle-release.sh and mvn-release.sh
export MAKECMDGOALS
# MAKEFLAGS exported by default

rwildcard = $(shell $(call bash, [ -d $1 ] && find $1 -type f -name '$2' | sed 's/ /\\ /g'))
# alternative, but does not support spaces in file names:
#rwildcard = $(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

update-target-if-changed = $@.tmp && ((! [ -e $@ ] || ! diff -q $@ $@.tmp >/dev/null) && mv $@.tmp $@ || rm $@.tmp ) || (rm $@.tmp && false)

.PHONY : $(TARGET_DIR)/properties
$(TARGET_DIR)/properties :
	$(call bash, echo $(call quote-for-bash,$(MVN_PROPERTIES)) >$(update-target-if-changed))

$(TARGET_DIR)/effective-settings.xml : $(MVN_SETTINGS) $(TARGET_DIR)/properties
	/* the test is there because $(TARGET_DIR)/properties is always executed (but not always updated) */ \
	/* cd into random directory in order to force Maven "stub" project */ \
	$(call bash, \
		if ! [ -e $@ ] || [[ -n $$(find $^ -newer $@ 2>/dev/null) ]]; then \
			cd $(TARGET_DIR) && \
			$(MVN) org.apache.maven.plugins:maven-help-plugin:2.2:effective-settings -Doutput=$(ROOT_DIR)/$@; \
		fi \
	)

.SECONDARY : poms parents aggregators
poms : pom.xml
parents :
aggregators :
include $(TARGET_DIR)/maven.mk

$(TARGET_DIR)/maven.mk : $(TARGET_DIR)/maven-modules $(TARGET_DIR)/maven-aggregators $(TARGET_DIR)/effective-settings.xml
	$(call bash, \
		echo "MVN_LOCAL_REPOSITORY := $$(xmllint --xpath "/*/*[local-name()='localRepository']/text()" $(word 3,$^))" >$@ && \
		echo "MAVEN_AGGREGATORS := \$$(shell \$$(call bash, cat $(word 2,$^) 2>/dev/null))" >>$@ && \
		echo "MAVEN_MODULES := \$$(shell \$$(call bash, cat $< 2>/dev/null))" >>$@ && \
		echo "export MVN_LOCAL_REPOSITORY" >>$@ && \
		cat $(word 2,$^) | while read -r module; do \
			echo "aggregators : $$module/pom.xml"; \
		done >>$@ && \
		cat $< | while read -r module; do \
			echo "poms : $$module/pom.xml"; \
		done >>$@ && \
		cat $< | while read -r module; do \
			pom=$$module/pom.xml; \
			if xmllint --xpath "/*/*[local-name()='parent']" $$pom >/dev/null 2>/dev/null; then \
				v=$$(xmllint --xpath "/*/*[local-name()='parent']/*[local-name()='version']/text()" $$pom) && \
				if [[ $$v == *-SNAPSHOT ]]; then \
					g=$$(xmllint --xpath "/*/*[local-name()='parent']/*[local-name()='groupId']/text()" $$pom) && \
					a=$$(xmllint --xpath "/*/*[local-name()='parent']/*[local-name()='artifactId']/text()" $$pom) && \
					echo "\$$(MVN_LOCAL_REPOSITORY)/$$(echo $$g |tr . /)/$$a/$$v/$$a-$$v.pom"; \
				fi \
			fi \
		done \
		| sort \
		| uniq \
		| sed 's/^/parents : /' \
		>>$@ \
	)

$(TARGET_DIR)/maven-aggregators : aggregators poms
	$(call bash, \
		mkdir -p $(dir $@) && \
		function print_aggregators_recursively() { \
			local module=$$1 && \
			submodules=($$(xmllint --format --xpath "/*/*[local-name()='modules']/*" $$module/pom.xml 2>/dev/null \
			               | sed -e 's/<module>\([^<]*\)<\/module>/\1 /g')) && \
			if [[ $${#submodules[*]} -gt 0 ]]; then \
				echo $$module && \
				for sub in $${submodules[*]}; do \
					if [ $$module == "." ]; then \
						print_aggregators_recursively $$sub; \
					else \
						print_aggregators_recursively $$module/$$sub; \
					fi \
				done \
			fi \
		} && \
		print_aggregators_recursively . >$(update-target-if-changed) \
	)

$(TARGET_DIR)/maven-modules : aggregators poms
	$(call bash, \
		mkdir -p $(dir $@) && \
		function print_modules_recursively() { \
			local module=$$1 && \
			submodules=($$(xmllint --format --xpath "/*/*[local-name()='modules']/*" $$module/pom.xml 2>/dev/null \
			               | sed -e 's/<module>\([^<]*\)<\/module>/\1 /g')) && \
			if [[ $${#submodules[*]} -gt 0 ]]; then \
				for sub in $${submodules[*]}; do \
					if [ $$module == "." ]; then \
						print_modules_recursively $$sub; \
					else \
						print_modules_recursively $$module/$$sub; \
					fi \
				done \
			else \
				echo $$module; \
			fi \
		} && \
		print_modules_recursively . >$(update-target-if-changed) \
	)

SAXON = $(MVN_LOCAL_REPOSITORY)/net/sf/saxon/Saxon-HE/9.4/Saxon-HE-9.4.jar
export SAXON

ifneq ($(OS), WINDOWS)
export JAVA_REPL_PORT := $(shell --spawn-repl-server)
endif

$(SAXON) : | .maven-init
	/* cd into random directory in order to force Maven "stub" project */ \
	$(call bash, \
		cd $(TARGET_DIR) && \
		$(MVN) org.apache.maven.plugins:maven-dependency-plugin:3.0.0:get -Dartifact=net.sf.saxon:Saxon-HE:9.4:jar \
	)

# the purpose of the test is for making "make -B" not affect this rule (to speed thing up)
# MAVEN_MODULES computed here because maven.mk may not be up to date yet
# FIXME: the mvn command below depends on the settings.xml.in file which is not inside this directory
$(TARGET_DIR)/effective-pom.xml : $(TARGET_DIR)/maven-modules poms | $(SAXON) $(MVN_SETTINGS)
	$(call bash, \
		MAVEN_MODULES=$$(cat $< 2>/dev/null) && \
		poms=($$(for m in $$MAVEN_MODULES; do echo "$$m/pom.xml"; done)) && \
		if ! [ -e $@ ] || [[ -n $$(find pom.xml $${poms[*]} -newer $@ 2>/dev/null) ]]; then \
			rm -rf $(TARGET_DIR)/poms && \
			for pom in $${poms[*]}; do \
				v=$$(xmllint --xpath "/*/*[local-name()='version']/text()" $$pom) && \
				g=$$(xmllint --xpath "/*/*[local-name()='groupId']/text()" $$pom 2>/dev/null) || \
				g=$$(xmllint --xpath "/*/*[local-name()='parent']/*[local-name()='groupId']/text()" $$pom) && \
				a=$$(xmllint --xpath "/*/*[local-name()='artifactId']/text()" $$pom) && \
				dest="$(TARGET_DIR)/poms/$$(echo $$g |tr . /)/$$a/$$v/$$a-$$v.pom" && \
				mkdir -p $$(dirname $$dest) && \
				cp $$pom $$dest && \
				if ! [[ $$v == *-SNAPSHOT ]]; then \
					v="$$v-SNAPSHOT" && \
					dest="$(TARGET_DIR)/poms/$$(echo $$g |tr . /)/$$a/$$v/$$a-$$v.pom" && \
					mkdir -p $$(dirname $$dest) && \
					java -cp $(SAXON) net.sf.saxon.Transform \
					     -s:$$pom \
					     -xsl:$(MY_DIR)/mvn-set-version.xsl \
					     VERSION="$$v" \
					     >$$dest; \
				fi; \
			done && \
			if mvn --batch-mode --settings "$(ROOT_DIR)/settings.xml.in" \
			       -Dworkspace="$(TARGET_DIR)/poms" \
			       -Dcache=".maven-cache" \
			       --projects $$(printf "%s\n" $$MAVEN_MODULES |paste -sd , -) \
			       -Prun-script-webserver -Ddocumentation \
			       org.apache.maven.plugins:maven-help-plugin:2.2:effective-pom -Doutput=$(ROOT_DIR)/$@ >$(ROOT_DIR)/maven.log; \
			then true; \
			else \
				rv=$$? && \
				echo "Failed to compute Maven dependencies." >&2 && \
				exit $$rv; \
			fi \
		else \
			touch $@; \
		fi \
	)

$(TARGET_DIR)/gradle-pom.xml : $(GRADLE_FILES)
	$(call bash, $(MY_DIR)/make-gradle-pom.sh $(GRADLE_MODULES) > $@)

.SECONDARY : .maven-init .gradle-init

.maven-init :

.gradle-init : | .maven-init $(TARGET_DIR)/.gradle-settings/conf/settings.xml
$(TARGET_DIR)/.gradle-settings/conf/settings.xml : $(MVN_SETTINGS)
	$(call bash, \
		mkdir -p $(dir $@) && \
		cp $< $@ \
	)

# MAVEN_MODULES computed here because maven.mk may not be up to date yet
.SECONDARY : .maven-deps.mk
.maven-deps.mk : $(TARGET_DIR)/maven-modules $(TARGET_DIR)/effective-pom.xml $(TARGET_DIR)/gradle-pom.xml | $(SAXON)
	$(call bash, \
		MAVEN_MODULES=$$(cat $< 2>/dev/null) && \
		rm -f $$(for m in $$MAVEN_MODULES; do echo "$(TARGET_DIR)/mk/$$m/.deps.mk"; done) && \
		if ! java -cp $(SAXON) net.sf.saxon.Transform \
		          -s:$(word 2,$^) \
		          -xsl:$(MY_DIR)/make-maven-deps.mk.xsl \
		          MY_DIR="$(MY_DIR)" \
		          ROOT_DIR="$(ROOT_DIR)" \
		          GRADLE_POM="$(word 3,$^)" \
		          MODULE="." \
		          SRC_DIRS="$$(for m in $$MAVEN_MODULES; do test -e $$m/src && echo $$m/src; done )" \
		          MAIN_DIRS="$$(for m in $$MAVEN_MODULES; do test -e $$m/src/main && echo $$m/src/main; done )" \
		          DOC_DIRS="$$(for m in $$MAVEN_MODULES; do test -e $$m/doc && echo $$m/doc; done )" \
		          INDEX_FILES="$$(for m in $$MAVEN_MODULES; do test -e $$m/index.md && echo $$m/index.md; done )" \
		          RELEASE_DIRS="$$(for x in $(GITREPOS); do [ -e $$x/bom/pom.xml ] || [ -e $$x/bom/pom.xml ] && echo $$x; done )" \
		          OUTPUT_BASEDIR="$(TARGET_DIR)/mk" \
		          OUTPUT_FILENAME=".deps.mk" \
		          VERBOSE="$$([[ -n $${VERBOSE+x} ]] && echo true || echo false)" \
		          >/dev/null \
		; then \
			rm -f $$(for m in $$MAVEN_MODULES; do echo "$(TARGET_DIR)/mk/$$m/.deps.mk"; done) && \
			exit 1; \
		fi \
	)

ifneq ($(MAKECMDGOALS), clean)
$(addsuffix /.deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(MAVEN_MODULES))) : .maven-deps.mk
	$(call bash, \
		if ! test -e $@; then \
			mkdir -p $(dir $@) && \
			echo "\$$(error $@ could not be generated)" >$@; \
		fi && \
		touch $@ \
	)
endif

ifneq ($(MAKECMDGOALS), clean)
$(addsuffix /.deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(GRADLE_MODULES))) : $(GRADLE_FILES)
	$(call bash, \
		mkdir -p $(dir $@) && \
		module=$$(dirname $@) && \
		module=$${module#$(TARGET_DIR)/mk/} && \
		if ! $(MY_DIR)/make-gradle-deps.mk.sh $$module >$@; then \
			echo "\$$(error $@ could not be generated)" >$@; \
		fi \
	)
endif

PHONY : $(addprefix eclipse-,$(MODULES))
$(addprefix eclipse-,$(MODULES)) : eclipse-% : %/.project

# mvn-eclipse.sh requires parent poms to be installed because it uses `mvn --projects ...`
$(addsuffix /.project,$(MODULES)) : parents

$(addsuffix /.project,$(MODULES)) : .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.m2e.core.prefs
.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.m2e.core.prefs : | $(TARGET_DIR)/effective-settings.xml .group-eval
	$(EVAL) $(call bash, $(MY_DIR)/eclipse-init.sh)

# FIXME: specifying "--debug" option breaks this code
# - passing "MAKEFLAGS=" does not fix it for some reason, and also has unwanted side effects
# - passing "--debug=no" to the sub-make would be a solution but not all versions of make support it
.SECONDARY : .group-eval
.group-eval :
ifndef SKIP_GROUP_EVAL_TARGET
	$(call bash, \
		set -o pipefail; \
		if commands=$$( \
			$(MAKE) --no-print-directory -n EVAL="// xxx" SKIP_GROUP_EVAL_TARGET=true $(MAKECMDGOALS) >$(TARGET_DIR)/commands && \
			cat $(TARGET_DIR)/commands \
			| perl -e '$$take = 1; \
			           while (<>) { \
			             chomp; \
			             if ($$_ eq "//") {} \
			             elsif ($$_ =~ /^\/\/ xxx (.+)/) { if ($$take) { print "$$1\n" } else { exit 1 }}\
			             else { $$take = 0 }}' && \
			rm $(TARGET_DIR)/commands \
		); then \
			if [ -n "$$commands" ]; then \
				while true; do \
					$(foreach V,$(sort $(filter-out MFLAGS MAKELEVEL _,$(.VARIABLES))),$(if $(filter environment%,$(origin $V)), \
						echo $(call quote-for-bash,$V=$($V));)) \
					break; \
				done >$(TARGET_DIR)/env && \
				echo "$$commands" | $(SHELL) 'groupEval();' && \
				rm $(TARGET_DIR)/env; \
			fi \
		else \
			rv=$$? && \
			echo "Error in build script. Contact maintainer." >&2 && \
			exit $$rv; \
		fi \
	)
endif

# so that initialization is not part of the commands to be evaluated
.group-eval : | .maven-init .gradle-init

.PHONY : clean
clean : clean-eclipse
	$(call bash, \
		rm -rf $(TARGET_DIR) && \
		rm -f maven.log && \
		find * -name .last-tested -exec rm -r "{}" \; \
	)

.PHONY : clean-eclipse
clean-eclipse :
	$(call bash, rm -rf .metadata)

ifeq ($(MAKECMDGOALS), clean)
include $(shell $(call bash, for f in $(addsuffix /.deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(MODULES) $(MAVEN_AGGREGATORS))); do \
                             test -e $$f && echo $$f; done))
else ifeq ($(MAKECMDGOALS), clean-eclipse)
include $(shell $$(call bash, for f in $(addsuffix /.deps.mk,$(addprefix $(TARGET_DIR)/mk/,$(MODULES) $(MAVEN_AGGREGATORS))); do \
                              test -e $$f && echo $$f; done))
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
