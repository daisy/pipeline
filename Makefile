GRADLE := libs/dotify/dotify.api/gradlew

POMS := $(shell find * -name pom.xml)

.PHONY : all
all : gradle maven

.PHONY : gradle
gradle :
	@$(GRADLE) install
	@echo "" >&2

gradle : gradle_BEFORE
.SECONDARY : gradle_BEFORE
gradle_BEFORE :
	@echo "╔════════╗" >&2
	@echo "║ GRADLE ║" >&2
	@echo "╚════════╝" >&2

.PHONY : maven
maven : .maven-modules-with-changes
	@if [ -s $< ]; then \
		modules=$$(cat $< |paste -sd , -) && \
		mvn --projects $$modules clean install -DskipTests; \
	else \
		echo "All modules are up to date" >&2; \
	fi
	@echo "" >&2

# Run mvn after gradle because there are no Gradle modules that depend on Maven modules.
#maven : gradle

# Build only the modules that have changed since the last build.
.INTERMEDIATE : .maven-modules-with-changes
.maven-modules-with-changes : .maven-modules
	@echo "Looking for changes..." >&2
	@for module in $$(cat $<); do \
		v=$$(xmllint --xpath "/*/*[local-name()='version']/text()" $$module/pom.xml) && \
		g=$$(xmllint --xpath "/*/*[local-name()='groupId']/text()" $$module/pom.xml 2>/dev/null) || \
		g=$$(xmllint --xpath "/*/*[local-name()='parent']/*[local-name()='groupId']/text()" $$module/pom.xml) && \
		a=$$(xmllint --xpath "/*/*[local-name()='artifactId']/text()" $$module/pom.xml) && \
		dest="$$HOME/.m2/repository/$$(echo $$g |tr . /)/$$a/$$v" && \
		if [[ ! -e "$$dest/$$a-$$v.pom" ]] || \
		   [[ -n $$(find $$module/{pom.xml,src} -newer "$$dest/maven-metadata-local.xml" 2>/dev/null) ]]; then \
			echo $$module; \
		fi \
	done > $@

# From all the Maven modules only include those in the build that are referenced from the
# super aggregator, have a snapshot version, and are listed in the assembly with that same
# version number. If the module is not listed in the assembly at all (also not a different
# version), we assume it is a helper module (parent, BoM, plugin, etc.) so we include it
# in the build as well.
.maven-modules : .maven-modules_START
	@mvn --quiet exec:exec -Dexec.executable=touch -Dexec.args=.enabled && \
	rm .enabled && \
	find * -mindepth 1 -name .enabled -cnewer $< | while read -r f; do \
		rm $$f && \
		module=$$(dirname $$f) && \
		if [[ -z $$(find $$module -mindepth 2 -name pom.xml 2>/dev/null) ]]; then \
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
	done > $@

.INTERMEDIATE : .maven-modules_START
.maven-modules_START : bom.xml $(POMS)
	@touch $@

# The assembly defines which versions of which modules we have to include in the build.
bom.xml : assembly/pom.xml $(POMS)
	@cd $(dir $<) && mvn --quiet help:effective-pom -Doutput=$(CURDIR)/$@

.maven-modules bom.xml : .maven-modules_BEFORE
.SECONDARY : .maven-modules_BEFORE
.maven-modules_BEFORE : .gitignore
	@echo "Recomputing modules to include in the build..." >&2

maven .maven-modules-with-changes bom.xml .maven-modules_BEFORE : maven_BEFORE
.SECONDARY : maven_BEFORE
maven_BEFORE :
	@echo "╔═══════╗" >&2
	@echo "║ MAVEN ║" >&2
	@echo "╚═══════╝" >&2

.PHONY : assembly
assembly : all
	cd assembly && mvn clean package -Pdeb

.PHONY : mod-sbs
mod-sbs : all
	cd modules/sbs && mvn clean package -DskipTests
