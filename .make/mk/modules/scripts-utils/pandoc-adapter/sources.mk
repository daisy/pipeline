modules/scripts-utils/pandoc-adapter/.test modules/scripts-utils/pandoc-adapter/.install modules/scripts-utils/pandoc-adapter/.install-doc $(TARGET_DIR)/state/modules/scripts-utils/pandoc-adapter/modified-since-release_ : \
	modules/scripts-utils/pandoc-adapter/src/main/resources/META-INF/catalog.xml \
	modules/scripts-utils/pandoc-adapter/src/main/resources/lua/detect-image-captions.lua \
	modules/scripts-utils/pandoc-adapter/src/main/resources/xml/library.xpl \
	modules/scripts-utils/pandoc-adapter/src/main/resources/native.tar.bz2 \
	modules/scripts-utils/pandoc-adapter/src/main/java/org/daisy/pipeline/pandoc/calabash/impl/MarkdownToHTML.java \
	modules/scripts-utils/pandoc-adapter/src/main/java/org/daisy/pipeline/pandoc/Pandoc.java
modules/scripts-utils/pandoc-adapter/.test modules/scripts-utils/pandoc-adapter/.install-doc : \
	modules/scripts-utils/pandoc-adapter/src/test/resources/logback.xml \
	modules/scripts-utils/pandoc-adapter/src/test/resources/image.png \
	modules/scripts-utils/pandoc-adapter/src/test/java/XProcSpecTest.java \
	modules/scripts-utils/pandoc-adapter/src/test/xprocspec/test_markdown-to-html.xprocspec
.make/mk/modules/scripts-utils/pandoc-adapter/sources.mk : \
	modules/scripts-utils/pandoc-adapter/src \
	modules/scripts-utils/pandoc-adapter/src/test \
	modules/scripts-utils/pandoc-adapter/src/test/resources \
	modules/scripts-utils/pandoc-adapter/src/test/java \
	modules/scripts-utils/pandoc-adapter/src/test/xprocspec \
	modules/scripts-utils/pandoc-adapter/src/main \
	modules/scripts-utils/pandoc-adapter/src/main/resources \
	modules/scripts-utils/pandoc-adapter/src/main/resources/META-INF \
	modules/scripts-utils/pandoc-adapter/src/main/resources/lua \
	modules/scripts-utils/pandoc-adapter/src/main/resources/xml \
	modules/scripts-utils/pandoc-adapter/src/main/java \
	modules/scripts-utils/pandoc-adapter/src/main/java/org \
	modules/scripts-utils/pandoc-adapter/src/main/java/org/daisy \
	modules/scripts-utils/pandoc-adapter/src/main/java/org/daisy/pipeline \
	modules/scripts-utils/pandoc-adapter/src/main/java/org/daisy/pipeline/pandoc \
	modules/scripts-utils/pandoc-adapter/src/main/java/org/daisy/pipeline/pandoc/calabash \
	modules/scripts-utils/pandoc-adapter/src/main/java/org/daisy/pipeline/pandoc/calabash/impl
