modules/epubcheck-adapter/.test modules/epubcheck-adapter/.install modules/epubcheck-adapter/.install-doc $(TARGET_DIR)/state/modules/epubcheck-adapter/modified-since-release_ : \
	modules/epubcheck-adapter/src/main/resources/META-INF/catalog.xml \
	modules/epubcheck-adapter/src/main/resources/xml/xproc/library.xpl \
	modules/epubcheck-adapter/src/main/java/org/daisy/pipeline/epub/calabash/impl/EpubCheckProvider.java \
	modules/epubcheck-adapter/src/main/java/org/idpf/epubcheck/util/saxon95/SystemIdFunction.java \
	modules/epubcheck-adapter/src/main/java/org/idpf/epubcheck/util/saxon95/ColumnNumberFunction.java \
	modules/epubcheck-adapter/src/main/java/org/idpf/epubcheck/util/saxon95/LineNumberFunction.java
modules/epubcheck-adapter/.test modules/epubcheck-adapter/.install-doc : \
	modules/epubcheck-adapter/src/test/resources/logback.xml \
	modules/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/META-INF/container.xml \
	modules/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/mimetype \
	modules/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/EPUB/nav.xhtml \
	modules/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/EPUB/package.opf \
	modules/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/EPUB/C00000-04-chapter.xhtml \
	modules/epubcheck-adapter/src/test/java/XProcSpecTest.java \
	modules/epubcheck-adapter/src/test/xprocspec/test.xprocspec
.make/mk/modules/epubcheck-adapter/sources.mk : \
	modules/epubcheck-adapter/src \
	modules/epubcheck-adapter/src/test \
	modules/epubcheck-adapter/src/test/resources \
	modules/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException \
	modules/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/META-INF \
	modules/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/EPUB \
	modules/epubcheck-adapter/src/test/java \
	modules/epubcheck-adapter/src/test/xprocspec \
	modules/epubcheck-adapter/src/main \
	modules/epubcheck-adapter/src/main/resources \
	modules/epubcheck-adapter/src/main/resources/META-INF \
	modules/epubcheck-adapter/src/main/resources/xml \
	modules/epubcheck-adapter/src/main/resources/xml/xproc \
	modules/epubcheck-adapter/src/main/java \
	modules/epubcheck-adapter/src/main/java/org \
	modules/epubcheck-adapter/src/main/java/org/daisy \
	modules/epubcheck-adapter/src/main/java/org/daisy/pipeline \
	modules/epubcheck-adapter/src/main/java/org/daisy/pipeline/epub \
	modules/epubcheck-adapter/src/main/java/org/daisy/pipeline/epub/calabash \
	modules/epubcheck-adapter/src/main/java/org/daisy/pipeline/epub/calabash/impl \
	modules/epubcheck-adapter/src/main/java/org/idpf \
	modules/epubcheck-adapter/src/main/java/org/idpf/epubcheck \
	modules/epubcheck-adapter/src/main/java/org/idpf/epubcheck/util \
	modules/epubcheck-adapter/src/main/java/org/idpf/epubcheck/util/saxon95
