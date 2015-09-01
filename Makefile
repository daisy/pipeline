SRC_DIR := src
SRC_FILES := $(shell find $(SRC_DIR) -type f)
TARGET_DIR := target/jekyll
TARGET_FILES := $(patsubst $(SRC_DIR)/%,$(TARGET_DIR)/%,$(SRC_FILES))

site_base := $(shell eval $$(cat $(SRC_DIR)/_config.yml | grep '^site_base' | sed -e 's/^site_base *:/echo /' ))
meta_file := $(shell eval $$(cat $(SRC_DIR)/_config.yml | grep '^meta_file' | sed -e 's/^meta_file *:/echo /' ))

.PHONY : all $(TARGET_DIR)/$(meta_file) serve publish clean

all : $(TARGET_FILES) $(TARGET_DIR)/$(meta_file)
	cd $(TARGET_DIR) && jekyll build

$(TARGET_DIR)/$(meta_file) : $(TARGET_FILES)
	echo '' > $@
	cd $(TARGET_DIR) && jekyll build
	./make_meta.rb "$(TARGET_DIR)/_site/**/*.html" | sed -e 's|$(TARGET_DIR)/_site/|$(site_base)/|g' > $@

$(TARGET_FILES) : $(TARGET_DIR)/% : $(SRC_DIR)/%
	mkdir -p $(dir $@)
	cp $< $@

serve : all
	cd $(TARGET_DIR) && jekyll serve

publish : all
	./publish.sh $(TARGET_DIR)/_site

clean :
	rm -rf target
