JEKYLL_SRC_DIR := src
SHELL := bash
RUBY := ruby
JEKYLL_SRC_FILES_CONTENT := $(shell find $(JEKYLL_SRC_DIR)/{_wiki,_wiki_gui,_wiki_webui} -type f -not -name '_*' -not -name '*.png' )
JEKYLL_SRC_FILES_MUSTACHE := $(shell find $(JEKYLL_SRC_DIR)/ -type f -name '_Sidebar.md')
JEKYLL_SRC_FILES_OTHER := $(filter-out $(JEKYLL_SRC_FILES_CONTENT) $(JEKYLL_SRC_FILES_MUSTACHE),\
                                       $(shell find $(JEKYLL_SRC_DIR) -type f))
JEKYLL_DIR := target/jekyll
JEKYLL_FILES_CONTENT := $(patsubst $(JEKYLL_SRC_DIR)/%,$(JEKYLL_DIR)/%,$(JEKYLL_SRC_FILES_CONTENT))
JEKYLL_FILES_MUSTACHE := $(patsubst $(JEKYLL_SRC_DIR)/%,$(JEKYLL_DIR)/%,$(JEKYLL_SRC_FILES_MUSTACHE))
JEKYLL_FILES_OTHER := $(patsubst $(JEKYLL_SRC_DIR)/%,$(JEKYLL_DIR)/%,$(JEKYLL_SRC_FILES_OTHER))
JEKYLL_FILES := $(JEKYLL_FILES_CONTENT) $(JEKYLL_FILES_MUSTACHE) $(JEKYLL_FILES_OTHER)
META_JEKYLL_DIR := target/meta/jekyll
META_JEKYLL_FILES_CONTENT := $(patsubst $(JEKYLL_SRC_DIR)/%,$(META_JEKYLL_DIR)/%,$(JEKYLL_SRC_FILES_CONTENT))
META_JEKYLL_FILES_MUSTACHE := $(patsubst $(JEKYLL_SRC_DIR)/%,$(META_JEKYLL_DIR)/%,$(JEKYLL_SRC_FILES_MUSTACHE))
META_JEKYLL_FILES_OTHER := $(patsubst $(JEKYLL_SRC_DIR)/%,$(META_JEKYLL_DIR)/%,$(JEKYLL_SRC_FILES_OTHER))
META_JEKYLL_FILES := $(META_JEKYLL_FILES_CONTENT) $(META_JEKYLL_FILES_MUSTACHE) $(META_JEKYLL_FILES_OTHER)
MAVEN_DIR := target/maven
MUSTACHE_DIR := target/mustache
CONFIG_FILE := $(JEKYLL_SRC_DIR)/_config.yml

yaml_get = $(shell eval $$(cat $(1) | grep '^$(2)' | sed -e 's/^$(2) *:/echo /' ))

meta_file := $(call yaml_get,$(CONFIG_FILE),meta_file)
baseurl := $(call yaml_get,$(CONFIG_FILE),baseurl)

.PHONY : all
all : $(JEKYLL_DIR)/_site

$(JEKYLL_DIR)/_site : %/_site : %/$(meta_file) %/modules %/api $(JEKYLL_FILES)
	mkdir -p $(dir $@)
	cd $(dir $@) && jekyll build --destination $(CURDIR)/$@$(baseurl)/
	if ! make/post_process.rb $< $@$(baseurl) $(JEKYLL_DIR) $(CONFIG_FILE); then \
		rm -rf $@; \
		exit 1; \
	fi
	touch $@

$(JEKYLL_FILES_CONTENT) : $(JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	eval "$$(echo 'newline="'; echo '"')"; \
	echo "---$${newline}---" | cat - $< >$@

$(JEKYLL_FILES_MUSTACHE) : $(JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/% $(JEKYLL_DIR)/$(meta_file)
	mkdir -p $(dir $@)
	cp $< $@
	if ! make/mustache.rb $@ $(word 2,$^) $(JEKYLL_DIR) $(CONFIG_FILE); then \
		rm -f $@; \
		exit 1; \
	fi

$(JEKYLL_FILES_OTHER) : $(JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	cp $< $@

$(JEKYLL_DIR)/modules : $(MUSTACHE_DIR)/modules $(MAVEN_DIR)/sources
	mkdir -p $(dir $@)
	rm -rf $@
	eval "$$(echo 'newline="'; echo '"')"; \
	cd $(CURDIR)/$<; \
	find . -type f -name '*.md' \
	       -exec sh -c "mkdir -p \$$(dirname $(CURDIR)/$@/{}) && \
	                    cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}layout: doc\\$${newline}---\\$${newline}&/' >$(CURDIR)/$@/{}" \;; \
	find . -type f ! -name '*.md' \
	       -exec sh -c "mkdir -p \$$(dirname $(CURDIR)/$@/{}) && \
	                    cp {} $(CURDIR)/$@/{}" \;; \
	cd $(CURDIR)/$(word 2,$^)/org/daisy/pipeline/modules; \
	find . -type f -name '*.md' \
	       -exec sh -c "mkdir -p \$$(dirname $(CURDIR)/$@/{}) && \
	                    cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}layout: source\\$${newline}---\\$${newline}&/' >$(CURDIR)/$@/{}" \;; \
	find . -type f ! -name '*.md' \
	       -exec sh -c "mkdir -p \$$(dirname $(CURDIR)/$@/{}) && \
	                    cp {} $(CURDIR)/$@/{}" \;

$(JEKYLL_DIR)/api : $(MAVEN_DIR)/javadoc $(MAVEN_DIR)/xprocdoc
	rm -rf $@
	mkdir -p $@
	cp -r $</* $@/
	cp -r $(word 2,$^)/* $@/

$(JEKYLL_DIR)/$(meta_file) : $(META_JEKYLL_DIR)/_site
	mkdir -p $(dir $@)
	if ! $(RUBY) make/make_meta.rb "$<$(baseurl)/**/*.html" $<$(baseurl) $(CONFIG_FILE) >$@; then
		rm -f $@; \
		exit 1; \
	fi

$(META_JEKYLL_DIR)/_site : %/_site : %/$(meta_file) %/modules %/api $(META_JEKYLL_FILES)
	cd $(dir $@) && jekyll build --destination $(CURDIR)/$@$(baseurl)
	touch $@

$(META_JEKYLL_FILES_CONTENT) : $(META_JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	eval "$$(echo 'newline="'; echo '"')"; \
	echo "---$${newline}---" | cat - $< >$@

$(META_JEKYLL_FILES_MUSTACHE) : $(META_JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/% $(META_JEKYLL_DIR)/$(meta_file)
	mkdir -p $(dir $@)
	cp $< $@
	if ! make/mustache.rb $@ $(word 2,$^) $(META_JEKYLL_DIR) $(CONFIG_FILE); then \
		rm -f $@; \
		exit 1; \
	fi

$(META_JEKYLL_FILES_OTHER) : $(META_JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	cp $< $@

$(META_JEKYLL_DIR)/api : $(MAVEN_DIR)/xprocdoc
	rm -rf $@
	mkdir -p $(dir $@)
	cp -r $< $@

$(META_JEKYLL_DIR)/modules : $(MAVEN_DIR)/doc $(MAVEN_DIR)/sources
	mkdir -p $(dir $@)
	rm -rf $@
	eval "$$(echo 'newline="'; echo '"')"; \
	cd $(CURDIR)/$</org/daisy/pipeline/modules; \
	find . -type f -name '*.md' \
	       -exec sh -c "mkdir -p \$$(dirname $(CURDIR)/$@/{}) && \
	                    cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}layout: doc\\$${newline}---\\$${newline}&/' >$(CURDIR)/$@/{}" \;; \
	find . -type f ! -name '*.md' \
	       -exec sh -c "mkdir -p \$$(dirname $(CURDIR)/$@/{}) && \
	                    cp {} $(CURDIR)/$@/{}" \;; \
	cd $(CURDIR)/$(word 2,$^)/org/daisy/pipeline/modules; \
	find . -type f -name '*.md' \
	       -exec sh -c "mkdir -p \$$(dirname $(CURDIR)/$@/{}) && \
	                    cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}layout: source\\$${newline}---\\$${newline}&/' >$(CURDIR)/$@/{}" \;; \
	find . -type f ! -name '*.md' \
	       -exec sh -c "mkdir -p \$$(dirname $(CURDIR)/$@/{}) && \
	                    cp {} $(CURDIR)/$@/{}" \;

$(META_JEKYLL_DIR)/$(meta_file) :
	mkdir -p $(dir $@)
	touch $@

$(MUSTACHE_DIR)/modules : $(MAVEN_DIR)/doc $(JEKYLL_DIR)/$(meta_file)
	mkdir -p $(dir $@)
	rm -rf $@
	cp -r $</org/daisy/pipeline/modules $@
	if ! $(RUBY) make/mustache.rb "$@/**/*" $(word 2,$^) $(MUSTACHE_DIR) $(CONFIG_FILE); then \
		rm -rf $@; \
		exit 1; \
	fi

$(MAVEN_DIR)/doc \
$(MAVEN_DIR)/javadoc \
$(MAVEN_DIR)/xprocdoc \
$(MAVEN_DIR)/sources : $(MAVEN_DIR)/pom.xml
	rm -rf $(MAVEN_DIR)/doc
	rm -rf $(MAVEN_DIR)/javadoc
	rm -rf $(MAVEN_DIR)/xprocdoc
	rm -rf $(MAVEN_DIR)/sources
	cd $(MAVEN_DIR) && \
	if ! mvn --quiet $(MVN_OPTS) "process-sources"; then \
		rm -rf $(MAVEN_DIR)/doc; \
		rm -rf $(MAVEN_DIR)/javadoc; \
		exit 1; \
	fi
	cd $(MAVEN_DIR)/doc && \
	find . -type f \( -path '**/src/main/**' -o -path '**/src/test/**' \) \
	       -exec sh -c "mkdir -p \$$(dirname $(CURDIR)/$(MAVEN_DIR)/sources/{}) && \
	                    mv {} $(CURDIR)/$(MAVEN_DIR)/sources/{}" \;

$(MAVEN_DIR)/pom.xml : $(JEKYLL_SRC_DIR)/_data/versions.yml $(JEKYLL_SRC_DIR)/_data/modules.yml $(JEKYLL_SRC_DIR)/_data/api.yml
	mkdir -p $(dir $@)
	if ! $(RUBY) make/make_pom.rb $^ > $@; then \
		rm -f $@ && \
		exit 1; \
	fi

.PHONY : serve
serve : ws all
	ws -d $(CURDIR)/$(JEKYLL_DIR)/_site

.PHONY : ws
ws :
	@if ! which $@ >/dev/null 2>/dev/null; then \
		echo "ws is not installed, install with 'npm install -g local-web-server'" 2>&1; \
		exit 1; \
	fi

.PHONY : publish
publish : all
	make/publish.sh $(JEKYLL_DIR)/_site$(baseurl)

.PHONY : clean
clean :
	rm -rf target
