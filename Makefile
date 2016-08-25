JEKYLL_SRC_DIR := src
JEKYLL_SRC_FILES := $(shell find $(JEKYLL_SRC_DIR) -type f)
JEKYLL_DIR := target/jekyll
JEKYLL_FILES := $(patsubst $(JEKYLL_SRC_DIR)/%,$(JEKYLL_DIR)/%,$(JEKYLL_SRC_FILES))
META_JEKYLL_DIR := target/meta/jekyll
META_JEKYLL_FILES := $(patsubst $(JEKYLL_SRC_DIR)/%,$(META_JEKYLL_DIR)/%,$(JEKYLL_SRC_FILES))
MAVEN_DIR := target/maven
MUSTACHE_DIR := target/mustache

yaml_get = $(shell eval $$(cat $(1) | grep '^$(2)' | sed -e 's/^$(2) *:/echo /' ))

site_base := $(call yaml_get,$(JEKYLL_SRC_DIR)/_config.yml,site_base)
meta_file := $(call yaml_get,$(JEKYLL_SRC_DIR)/_config.yml,meta_file)

.PHONY : all
all : $(JEKYLL_DIR)/_site

$(JEKYLL_DIR)/_site : %/_site : %/$(meta_file) %/doc $(JEKYLL_FILES) src/css/coderay.css
	mkdir -p $(dir $@)
	cd $(dir $@) && jekyll build
	make/process_links.rb $< $@ $(site_base)
	touch $@

$(JEKYLL_FILES) : $(JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	cp $< $@

$(JEKYLL_DIR)/doc : $(MUSTACHE_DIR)/doc
	mkdir -p $(dir $@)
	rm -rf $@
	cp -r $< $@
	eval "$$(echo 'newline="'; echo '"')"; \
	find $@ -name '*.md' -exec sh -c "cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}---\\$${newline}&/' >{}.tmp && mv {}.tmp {}" \;; \
	find $@ -type file -exec sh -c "cat {} | sed '1 s/^---$$/&\\$${newline}layout: doc/' >{}.tmp && mv {}.tmp {}" \;; \
	find $@ -type file -exec sh -c "cat {} | sed -E '1 s/^([^-].*)?$$/---\\$${newline}---\\$${newline}&/' >{}.tmp && mv {}.tmp {}" \;

$(JEKYLL_DIR)/$(meta_file) : $(META_JEKYLL_DIR)/_site
	mkdir -p $(dir $@)
	make/make_meta.rb "$</**/*.html" $< $(site_base) >$@

src/css/coderay.css :
	coderay stylesheet > $@

$(META_JEKYLL_DIR)/_site : %/_site : %/$(meta_file) %/doc $(META_JEKYLL_FILES)
	cd $(dir $@) && jekyll build
	touch $@

$(META_JEKYLL_FILES) : $(META_JEKYLL_DIR)/% : $(JEKYLL_SRC_DIR)/%
	mkdir -p $(dir $@)
	cp $< $@

$(META_JEKYLL_DIR)/doc : $(MAVEN_DIR)/doc
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

$(MUSTACHE_DIR)/doc : $(MAVEN_DIR)/doc $(JEKYLL_DIR)/$(meta_file)
	mkdir -p $(dir $@)
	rm -rf $@
	cp -r $< $@
	make/mustache.rb "$@/**/*" $(word 2,$^) $@ $(site_base)/doc 2>/dev/null

$(MAVEN_DIR)/doc : $(MAVEN_DIR)/pom.xml
	rm -rf $@
	mkdir -p $@
	cd $(dir $<) && mvn --quiet \
	                    "dependency:unpack-dependencies" \
	                    -Dclassifier=doc -DexcludeTransitive -Dmdep.unpack.excludes='META-INF,META-INF/**/*' \
	                    -Dmdep.useRepositoryLayout
	cd $(dir $<)/target/dependency && find . -type d -name '[0-9]*' -prune -exec bash -c \
		'mkdir -p $(CURDIR)/$@/$$(dirname {}); cp -r {}/* $$_' \; || true

$(MAVEN_DIR)/pom.xml : target/versions.yml $(JEKYLL_SRC_DIR)/_data/doc_modules.yml
	mkdir -p $(dir $@)
	make/make_pom.rb $^ > $@

target/versions.yml : $(JEKYLL_SRC_DIR)/_data/versions.yml
	mkdir -p $(dir $@)
	wget -L -O - "https://raw.githubusercontent.com/snaekobbi/system/$(call yaml_get,$<,system)/roles/test-server/vars/versions.yml" > $@

.PHONY : serve
serve : ws all
	cd $(JEKYLL_DIR) && jekyll serve

.PHONY : publish
publish : all
	make/publish.sh $(JEKYLL_DIR)/_site

.PHONY : clean
clean :
	rm -rf target
