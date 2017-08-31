JEKYLL_SRC_DIR := src
JEKYLL_SRC_FILES_CONTENT := $(shell find $(JEKYLL_SRC_DIR)/{_wiki,_wiki_gui,_wiki_webui} -type f -not -name '_Sidebar.md' -not -name '*.png' )
JEKYLL_SRC_FILES_MUSTACHE := $(shell find $(JEKYLL_SRC_DIR)/_data/_spines/ -type f -name '*.yml')
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

$(JEKYLL_DIR)/_site : %/_site : %/$(meta_file) %/modules $(JEKYLL_FILES)
	mkdir -p $(dir $@)
	cd $(dir $@) && jekyll build --destination $(CURDIR)/$@$(baseurl)/
	make/post_process.rb $< $@$(baseurl) $(CONFIG_FILE)
	touch $@

$(JEKYLL_FILES_CONTENT) : $(JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	eval "$$(echo 'newline="'; echo '"')"; \
	echo "---$${newline}---" | cat - $< >$@

$(JEKYLL_FILES_MUSTACHE) : $(JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/% $(JEKYLL_DIR)/$(meta_file)
	mkdir -p $(dir $@)
	cp $< $@
	make/mustache.rb $@ $(word 2,$^) $(JEKYLL_DIR) $(CONFIG_FILE)

$(JEKYLL_FILES_OTHER) : $(JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	cp $< $@

$(JEKYLL_DIR)/modules : $(MUSTACHE_DIR)/modules
	mkdir -p $(dir $@)
	rm -rf $@
	cp -r $< $@
	eval "$$(echo 'newline="'; echo '"')"; \
	find $@ -name '*.md' -path '**/resources/**' \
	        -exec sh -c "cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}layout: source\\$${newline}---\\$${newline}&/' >{}.tmp && mv {}.tmp {}" \;; \
	find $@ -name '*.md' -exec sh -c "cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}---\\$${newline}&/' >{}.tmp && mv {}.tmp {}" \;; \
	find $@ -type f -exec sh -c "cat {} | sed '1 s/^---$$/&\\$${newline}layout: doc/' >{}.tmp && mv {}.tmp {}" \;; \
	find $@ -type f -exec sh -c "cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}---\\$${newline}&/' >{}.tmp && mv {}.tmp {}" \;

$(JEKYLL_DIR)/$(meta_file) : $(META_JEKYLL_DIR)/_site
	mkdir -p $(dir $@)
	make/make_meta.rb "$<$(baseurl)/**/*.html" $<$(baseurl) $(CONFIG_FILE) >$@

$(META_JEKYLL_DIR)/_site : %/_site : %/$(meta_file) %/modules $(META_JEKYLL_FILES)
	cd $(dir $@) && jekyll build --destination $(CURDIR)/$@$(baseurl)
	touch $@

$(META_JEKYLL_FILES_CONTENT) : $(META_JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	eval "$$(echo 'newline="'; echo '"')"; \
	echo "---$${newline}---" | cat - $< >$@

$(META_JEKYLL_FILES_MUSTACHE) : $(META_JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/% $(META_JEKYLL_DIR)/$(meta_file)
	mkdir -p $(dir $@)
	cp $< $@
	make/mustache.rb $@ $(word 2,$^) $(META_JEKYLL_DIR) $(CONFIG_FILE)

$(META_JEKYLL_FILES_OTHER) : $(META_JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	cp $< $@

$(META_JEKYLL_DIR)/modules : $(MAVEN_DIR)/modules
	mkdir -p $(dir $@)
	rm -rf $@
	cp -r $< $@
	eval "$$(echo 'newline="'; echo '"')"; \
	find $@ -name '*.md' -path '**/resources/**' \
	        -exec sh -c "cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}layout: source\\$${newline}---\\$${newline}&/' >{}.tmp && mv {}.tmp {}" \;; \
	find $@ -name '*.md' -exec sh -c "cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}---\\$${newline}&/' >{}.tmp && mv {}.tmp {}" \;; \
	find $@ -type f -exec sh -c "cat {} | sed '1 s/^---$$/&\\$${newline}layout: doc/' >{}.tmp && mv {}.tmp {}" \;; \
	find $@ -type f -exec sh -c "cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}---\\$${newline}&/' >{}.tmp && mv {}.tmp {}" \;

$(META_JEKYLL_DIR)/$(meta_file) :
	mkdir -p $(dir $@)
	touch $@

$(MUSTACHE_DIR)/modules : $(MAVEN_DIR)/modules $(JEKYLL_DIR)/$(meta_file)
	mkdir -p $(dir $@)
	rm -rf $@
	cp -r $< $@
	make/mustache.rb "$@/**/*" $(word 2,$^) $(MUSTACHE_DIR) $(CONFIG_FILE)

$(MAVEN_DIR)/modules : $(MAVEN_DIR)/pom.xml
	rm -rf $@
	cd $(dir $<) && \
	mvn --quiet $(MVN_OPTS) "process-sources"
	test -e $@

$(MAVEN_DIR)/pom.xml : $(JEKYLL_SRC_DIR)/_data/versions.yml $(JEKYLL_SRC_DIR)/_data/modules.yml
	mkdir -p $(dir $@)
	make/make_pom.rb $^ > $@

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
