SRC_DIR := src
SRC_FILES := $(shell find $(SRC_DIR) -type f)
TARGET_DIR := target/jekyll
TARGET_FILES := $(patsubst $(SRC_DIR)/%,$(TARGET_DIR)/%,$(SRC_FILES))

yaml_get = $(shell eval $$(cat $(1) | grep '^$(2)' | sed -e 's/^$(2) *:/echo /' ))

site_base := $(call yaml_get,$(SRC_DIR)/_config.yml,site_base)
meta_file := $(call yaml_get,$(SRC_DIR)/_config.yml,meta_file)

.PHONY : all $(TARGET_DIR)/$(meta_file) serve publish clean

all : $(TARGET_FILES) $(TARGET_DIR)/doc $(TARGET_DIR)/$(meta_file)
	cd $(TARGET_DIR) && jekyll build

$(TARGET_DIR)/$(meta_file) : $(TARGET_FILES) $(TARGET_DIR)/doc
	echo '' > $@
	cd $(TARGET_DIR) && jekyll build
	./make_meta.rb "$(TARGET_DIR)/_site/**/*.html" | sed -e 's|$(TARGET_DIR)/_site/|$(site_base)/|g' > $@

$(TARGET_FILES) : $(TARGET_DIR)/% : $(SRC_DIR)/%
	mkdir -p $(dir $@)
	cp $< $@

target/versions.yml : $(SRC_DIR)/_data/versions.yml
	mkdir -p $(dir $@)
	wget -L -O - "https://raw.githubusercontent.com/snaekobbi/system/$(call yaml_get,$<,system)/roles/test-server/vars/versions.yml" > $@

target/pom.xml : target/versions.yml $(SRC_DIR)/_data/doc_modules.yml
	mkdir -p $(dir $@)
	./make_pom.rb $^ > $@

target/md_front :
	mkdir -p $(dir $@)
	echo "---" > $@
	echo "layout: doc" >> $@
	echo "---" >> $@

$(TARGET_DIR)/doc : target/pom.xml target/md_front
	rm -rf $@
	mkdir -p $@
	cd $(dir $<) && mvn --quiet \
	                    "dependency:unpack-dependencies" \
	                    -Dclassifier=sources -DexcludeTransitive -Dmdep.unpack.includes=**/*.md \
	                    -Dmdep.useRepositoryLayout -DoutputDirectory=$(CURDIR)/$@
	find $@ -name '*.md' -exec sh -c 'cat $(word 2,$^) {} >{}.2 && mv {}.2 {}' \;

serve : all
	cd $(TARGET_DIR) && jekyll serve

publish : all
	./publish.sh $(TARGET_DIR)/_site

clean :
	rm -rf target
