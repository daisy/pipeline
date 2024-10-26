include make/enable-java-shell.mk

PIPELINE1_VERSION := 20250106

dependencies : src/main/resources/pipeline-$(PIPELINE1_VERSION).jar \
               src/main/resources/org.daisy.util-$(PIPELINE1_VERSION).jar

src/main/resources/pipeline-20230619.zip :
	mkdirs("$(dir $@)");                                                                                     \
	copy(new URL("https://github.com/daisy/pipeline1/releases/download/v20230619/$(notdir $@)"), \
	     new File("$@"));

src/main/resources/pipeline-$(PIPELINE1_VERSION).jar : src/main/resources/pipeline-$(PIPELINE1_VERSION).zip
	rm(new File("$@"));                                                                   \
	copy(new URL("jar:file:$(CURDIR)/$<!/$(patsubst %.zip,%,$(notdir $<))/pipeline.jar"), \
	     new File("$@"));

src/main/resources/org.daisy.util-$(PIPELINE1_VERSION).jar : src/main/resources/pipeline-$(PIPELINE1_VERSION).zip
	rm(new File("$@"));                                                                             \
	copy(new URL("jar:file:$(CURDIR)/$<!/$(patsubst %.zip,%,$(notdir $<))/lib/org.daisy.util.jar"), \
	     new File("$@"));
