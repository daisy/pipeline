POMS := $(shell find * -name 'pom.xml')

.PHONY: all
all : .modules-with-changes .modules-unchecked
	@if [ -s $< ]; then \
		mvn --projects $$(cat $< |paste -sd ',' -) clean install -DskipTests; \
	fi

.PHONY: check
check : .modules-unchecked
	@if [ -s $< ]; then \
		mvn --projects $$(cat $< |paste -sd ',' -) clean install exec:exec -Dexec.executable=rm -Dexec.args=.unchecked; \
	fi

.PHONY: release
release : .modules
	@mvn --projects $$(cat $< |paste -sd ',' -) clean release:clean release:prepare && mvn release:perform	

.modules : maven/bom/pom.xml $(POMS)
	@for pom in $(POMS); do \
		module=$$(dirname $$pom) && \
		if [[ -z $$(find $$module -mindepth 2 -name pom.xml 2>/dev/null) ]]; then \
			v=$$(xmllint --xpath "/*/*[local-name()='version']/text()" $$pom) && \
			if [[ "$$v" =~ -SNAPSHOT$$ ]]; then \
				g=$$(xmllint --xpath "/*/*[local-name()='groupId']/text()" $$pom 2>/dev/null) || \
				g=$$(xmllint --xpath "/*/*[local-name()='parent']/*[local-name()='groupId']/text()" $$pom) && \
				a=$$(xmllint --xpath "/*/*[local-name()='artifactId']/text()" $$pom) && \
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
	done > $@

.INTERMEDIATE: .modules-with-changes
.modules-with-changes : .modules
	@for module in $$(cat $<); do \
		v=$$(xmllint --xpath "/*/*[local-name()='version']/text()" $${module}/pom.xml) && \
		g=$$(xmllint --xpath "/*/*[local-name()='groupId']/text()" $${module}/pom.xml 2>/dev/null) || \
		g=$$(xmllint --xpath "/*/*[local-name()='parent']/*[local-name()='groupId']/text()" $${module}/pom.xml) && \
		a=$$(xmllint --xpath "/*/*[local-name()='artifactId']/text()" $${module}/pom.xml) && \
		dest="$$HOME/.m2/repository/$$(echo $$g |tr . /)/$$a/$$v" && \
		if [[ ! -e "$$dest/$$a-$$v.pom" ]] || \
		   [[ -n $$(find $$module/{pom.xml,src} -newer "$$dest/maven-metadata-local.xml" 2>/dev/null) ]]; then \
			echo $$module; \
		fi \
	done > $@

.INTERMEDIATE: .modules-unchecked
.modules-unchecked : .modules-with-changes
	@if [ -s $< ]; then \
		mvn --quiet --projects $$(cat $< |paste -sd ',' -) --also-make-dependents exec:exec -Dexec.executable=touch -Dexec.args=.unchecked; \
	fi && \
	find * -mindepth 1 -name .unchecked | while read -r f; do \
		dirname $$f; \
	done > $@

.PHONY: release-notes
release-notes :
	test -z "$$(git status --porcelain $@)"
	xsltproc make/generate-release-notes.xsl maven/bom/pom.xml | cat - NEWS.md > NEWS.md.tmp
	mv NEWS.md.tmp NEWS.md
