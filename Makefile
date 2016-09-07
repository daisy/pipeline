JEKYLL_SRC_DIR := src
JEKYLL_SRC_FILES_CONTENT := $(shell find $(JEKYLL_SRC_DIR)/_wiki -type f)
JEKYLL_SRC_FILES_OTHER := $(filter-out $(JEKYLL_SRC_FILES_CONTENT),$(shell find $(JEKYLL_SRC_DIR) -type f))
JEKYLL_DIR := target/jekyll
JEKYLL_FILES_CONTENT := $(patsubst $(JEKYLL_SRC_DIR)/%,$(JEKYLL_DIR)/%,$(JEKYLL_SRC_FILES_CONTENT))
JEKYLL_FILES_OTHER := $(patsubst $(JEKYLL_SRC_DIR)/%,$(JEKYLL_DIR)/%,$(JEKYLL_SRC_FILES_OTHER))
JEKYLL_FILES := $(JEKYLL_FILES_CONTENT) $(JEKYLL_FILES_OTHER)
META_JEKYLL_DIR := target/meta/jekyll
META_JEKYLL_FILES_CONTENT := $(patsubst $(JEKYLL_SRC_DIR)/%,$(META_JEKYLL_DIR)/%,$(JEKYLL_SRC_FILES_CONTENT))
META_JEKYLL_FILES_OTHER := $(patsubst $(JEKYLL_SRC_DIR)/%,$(META_JEKYLL_DIR)/%,$(JEKYLL_SRC_FILES_OTHER))
META_JEKYLL_FILES := $(META_JEKYLL_FILES_CONTENT) $(META_JEKYLL_FILES_OTHER)
MAVEN_DIR := target/maven
MUSTACHE_DIR := target/mustache
CONFIG_FILE := $(JEKYLL_SRC_DIR)/_config.yml

yaml_get = $(shell eval $$(cat $(1) | grep '^$(2)' | sed -e 's/^$(2) *:/echo /' ))

meta_file := $(call yaml_get,$(CONFIG_FILE),meta_file)

.PHONY : all
all : $(JEKYLL_DIR)/_site

$(JEKYLL_DIR)/_site : %/_site : %/$(meta_file) %/modules $(JEKYLL_FILES) src/css/coderay.css
	mkdir -p $(dir $@)
	cd $(dir $@) && jekyll build
	make/process_links.rb $< $@ $(CONFIG_FILE)
	touch $@

$(JEKYLL_FILES_CONTENT) : $(JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	eval "$$(echo 'newline="'; echo '"')"; \
	echo "---$${newline}---" | cat - $< >$@

$(JEKYLL_FILES_OTHER) : $(JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	cp $< $@

$(JEKYLL_DIR)/modules : $(MUSTACHE_DIR)/modules
	mkdir -p $(dir $@)
	rm -rf $@
	cp -r $< $@
	eval "$$(echo 'newline="'; echo '"')"; \
	find $@ -name '*.md' -exec sh -c "cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}---\\$${newline}&/' >{}.tmp && mv {}.tmp {}" \;; \
	find $@ -type file -exec sh -c "cat {} | sed '1 s/^---$$/&\\$${newline}layout: doc/' >{}.tmp && mv {}.tmp {}" \;; \
	find $@ -type file -exec sh -c "cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}---\\$${newline}&/' >{}.tmp && mv {}.tmp {}" \;

$(JEKYLL_DIR)/$(meta_file) : $(META_JEKYLL_DIR)/_site
	mkdir -p $(dir $@)
	make/make_meta.rb "$</**/*.html" $< $(CONFIG_FILE) >$@

src/css/coderay.css :
	coderay stylesheet > $@

$(META_JEKYLL_DIR)/_site : %/_site : %/$(meta_file) %/modules $(META_JEKYLL_FILES)
	cd $(dir $@) && jekyll build
	touch $@

$(META_JEKYLL_FILES_CONTENT) : $(META_JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	eval "$$(echo 'newline="'; echo '"')"; \
	echo "---$${newline}---" | cat - $< >$@

$(META_JEKYLL_FILES_OTHER) : $(META_JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	cp $< $@

$(META_JEKYLL_DIR)/modules : $(MAVEN_DIR)/modules
	mkdir -p $(dir $@)
	rm -rf $@
	cp -r $< $@
	eval "$$(echo 'newline="'; echo '"')"; \
	find $@ -name '*.md' -exec sh -c "cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}---\\$${newline}&/' >{}.tmp && mv {}.tmp {}" \;; \
	find $@ -type file -exec sh -c "cat {} | sed '1 s/^---$$/&\\$${newline}layout: doc/' >{}.tmp && mv {}.tmp {}" \;; \
	find $@ -type file -exec sh -c "cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}---\\$${newline}&/' >{}.tmp && mv {}.tmp {}" \;

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
	mkdir -p $@
	cd $(dir $<) && mvn --quiet \
	                    "dependency:unpack-dependencies" \
	                    -Dclassifier=doc -DexcludeTransitive -Dmdep.unpack.excludes='META-INF,META-INF/**/*' \
	                    -Dmdep.useRepositoryLayout
	cd $(dir $<)/target/dependency && find . -type d -name '[0-9]*' -prune -exec bash -c \
		'mkdir -p $(CURDIR)/$@/$$(dirname {}); cp -r {}/* $$_' \; || true

$(MAVEN_DIR)/pom.xml : $(JEKYLL_SRC_DIR)/_data/versions.yml $(JEKYLL_SRC_DIR)/_data/modules.yml
	mkdir -p $(dir $@)
	make/make_pom.rb $^ > $@

.PHONY : serve
serve : ws all
	cd $(JEKYLL_DIR) && jekyll serve

.PHONY : publish
publish : all
	make/publish.sh $(JEKYLL_DIR)/_site

.PHONY : clean
clean :
	rm -rf target
