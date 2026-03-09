modules/scripts-utils/epubcheck-adapter/.test modules/scripts-utils/epubcheck-adapter/.install modules/scripts-utils/epubcheck-adapter/.install-doc $(TARGET_DIR)/state/modules/scripts-utils/epubcheck-adapter/modified-since-release_ : \
	modules/scripts-utils/epubcheck-adapter/src/main/resources/META-INF/catalog.xml \
	modules/scripts-utils/epubcheck-adapter/src/main/resources/xml/xproc/library.xpl \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/daisy/pipeline/epub/calabash/impl/EpubCheckProvider.java \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/idpf/epubcheck/util/saxon95/SystemIdFunction.java \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/idpf/epubcheck/util/saxon95/ColumnNumberFunction.java \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/idpf/epubcheck/util/saxon95/LineNumberFunction.java
modules/scripts-utils/epubcheck-adapter/.test modules/scripts-utils/epubcheck-adapter/.install-doc : \
	modules/scripts-utils/epubcheck-adapter/src/test/resources/logback.xml \
	modules/scripts-utils/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/META-INF/container.xml \
	modules/scripts-utils/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/mimetype \
	modules/scripts-utils/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/EPUB/nav.xhtml \
	modules/scripts-utils/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/EPUB/package.opf \
	modules/scripts-utils/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/EPUB/C00000-04-chapter.xhtml \
	modules/scripts-utils/epubcheck-adapter/src/test/java/XProcSpecTest.java \
	modules/scripts-utils/epubcheck-adapter/src/test/xprocspec/test.xprocspec
.make/mk/modules/scripts-utils/epubcheck-adapter/sources.mk : \
	modules/scripts-utils/epubcheck-adapter/src \
	modules/scripts-utils/epubcheck-adapter/src/test \
	modules/scripts-utils/epubcheck-adapter/src/test/resources \
	modules/scripts-utils/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException \
	modules/scripts-utils/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/META-INF \
	modules/scripts-utils/epubcheck-adapter/src/test/resources/regression-MissingFormatArgumentException/EPUB \
	modules/scripts-utils/epubcheck-adapter/src/test/java \
	modules/scripts-utils/epubcheck-adapter/src/test/xprocspec \
	modules/scripts-utils/epubcheck-adapter/src/main \
	modules/scripts-utils/epubcheck-adapter/src/main/resources \
	modules/scripts-utils/epubcheck-adapter/src/main/resources/META-INF \
	modules/scripts-utils/epubcheck-adapter/src/main/resources/xml \
	modules/scripts-utils/epubcheck-adapter/src/main/resources/xml/xproc \
	modules/scripts-utils/epubcheck-adapter/src/main/java \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/daisy \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/daisy/pipeline \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/daisy/pipeline/epub \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/daisy/pipeline/epub/calabash \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/daisy/pipeline/epub/calabash/impl \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/idpf \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/idpf/epubcheck \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/idpf/epubcheck/util \
	modules/scripts-utils/epubcheck-adapter/src/main/java/org/idpf/epubcheck/util/saxon95
