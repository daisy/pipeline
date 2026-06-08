modules/pandoc-adapter/.test modules/pandoc-adapter/.install modules/pandoc-adapter/.install-doc $(TARGET_DIR)/state/modules/pandoc-adapter/modified-since-release_ : \
	modules/pandoc-adapter/src/main/resources/META-INF/catalog.xml \
	modules/pandoc-adapter/src/main/resources/lua/detect-image-captions.lua \
	modules/pandoc-adapter/src/main/resources/xml/library.xpl \
	modules/pandoc-adapter/src/main/resources/native.tar.bz2 \
	modules/pandoc-adapter/src/main/java/org/daisy/pipeline/pandoc/calabash/impl/MarkdownToHTML.java \
	modules/pandoc-adapter/src/main/java/org/daisy/pipeline/pandoc/Pandoc.java
modules/pandoc-adapter/.test modules/pandoc-adapter/.install-doc : \
	modules/pandoc-adapter/src/test/resources/logback.xml \
	modules/pandoc-adapter/src/test/resources/image.png \
	modules/pandoc-adapter/src/test/java/XProcSpecTest.java \
	modules/pandoc-adapter/src/test/xprocspec/test_markdown-to-html.xprocspec
.make/mk/modules/pandoc-adapter/sources.mk : \
	modules/pandoc-adapter/src \
	modules/pandoc-adapter/src/test \
	modules/pandoc-adapter/src/test/resources \
	modules/pandoc-adapter/src/test/java \
	modules/pandoc-adapter/src/test/xprocspec \
	modules/pandoc-adapter/src/main \
	modules/pandoc-adapter/src/main/resources \
	modules/pandoc-adapter/src/main/resources/META-INF \
	modules/pandoc-adapter/src/main/resources/lua \
	modules/pandoc-adapter/src/main/resources/xml \
	modules/pandoc-adapter/src/main/java \
	modules/pandoc-adapter/src/main/java/org \
	modules/pandoc-adapter/src/main/java/org/daisy \
	modules/pandoc-adapter/src/main/java/org/daisy/pipeline \
	modules/pandoc-adapter/src/main/java/org/daisy/pipeline/pandoc \
	modules/pandoc-adapter/src/main/java/org/daisy/pipeline/pandoc/calabash \
	modules/pandoc-adapter/src/main/java/org/daisy/pipeline/pandoc/calabash/impl
