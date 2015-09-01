SRC_DIR := src
SRC_FILES := $(shell find $(SRC_DIR) -type f)
TARGET_DIR := target/jekyll
TARGET_FILES := $(patsubst $(SRC_DIR)/%,$(TARGET_DIR)/%,$(SRC_FILES))

.PHONY : all serve publish clean

all : $(TARGET_FILES)
	jekyll build -s $(TARGET_DIR) -d $(TARGET_DIR)/_site

$(TARGET_FILES) : $(TARGET_DIR)/% : $(SRC_DIR)/%
	mkdir -p $(dir $@)
	cp $< $@

serve : all
	jekyll serve -s $(TARGET_DIR) -d $(TARGET_DIR)/_site

publish : all
	./publish.sh $(TARGET_DIR)/_site

clean :
	rm -rf target
