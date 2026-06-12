modules/epub3-to-pef/.test modules/epub3-to-pef/.install modules/epub3-to-pef/.install-doc $(TARGET_DIR)/state/modules/epub3-to-pef/modified-since-release_ : \
	modules/epub3-to-pef/src/main/resources/css/reset.css \
	modules/epub3-to-pef/src/main/resources/META-INF/catalog.xml \
	modules/epub3-to-pef/src/main/resources/xml/xproc/xml-to-pef.store.xpl \
	modules/epub3-to-pef/src/main/resources/xml/xproc/epub3-to-pef.xpl \
	modules/epub3-to-pef/src/main/resources/xml/xproc/epub3-to-pef.convert.xpl \
	modules/epub3-to-pef/src/main/resources/xml/xproc/epub3-to-pef.store.xpl \
	modules/epub3-to-pef/src/main/resources/xml/xproc/epub3-to-pef.load.xpl \
	modules/epub3-to-pef/src/main/resources/xml/xproc/library.xpl \
	modules/epub3-to-pef/src/main/resources/xml/xslt/opf-to-html-head.xsl \
	modules/epub3-to-pef/src/main/README.md
modules/epub3-to-pef/.test modules/epub3-to-pef/.install-doc : \
	modules/epub3-to-pef/src/test/xspec/catalog.xml \
	modules/epub3-to-pef/src/test/xspec/mock-functions.xsl \
	modules/epub3-to-pef/src/test/resources/logback.xml \
	modules/epub3-to-pef/src/test/resources/preamble.html \
	modules/epub3-to-pef/src/test/resources/reset.css \
	modules/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/nav.xhtml \
	modules/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/p4-padding.css \
	modules/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/content-2.xhtml \
	modules/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/p6-padding.css \
	modules/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/p5-padding.css \
	modules/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/package.opf \
	modules/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/content-1.xhtml \
	modules/epub3-to-pef/src/test/resources/test_ascii-table_2/nav.xhtml \
	modules/epub3-to-pef/src/test/resources/test_ascii-table_2/content-2.xhtml \
	modules/epub3-to-pef/src/test/resources/test_ascii-table_2/package.opf \
	modules/epub3-to-pef/src/test/resources/test_ascii-table_2/content-1.xhtml \
	modules/epub3-to-pef/src/test/resources/test_ascii-table_2.brf \
	modules/epub3-to-pef/src/test/resources/test_ascii-table_1.brf \
	modules/epub3-to-pef/src/test/resources/simple-epub/nav.xhtml \
	modules/epub3-to-pef/src/test/resources/simple-epub/content-2.xhtml \
	modules/epub3-to-pef/src/test/resources/simple-epub/package.opf \
	modules/epub3-to-pef/src/test/resources/simple-epub/content-1.xhtml \
	modules/epub3-to-pef/src/test/resources/style.css \
	modules/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets.override.css \
	modules/epub3-to-pef/src/test/resources/test_ascii-table_1/nav.xhtml \
	modules/epub3-to-pef/src/test/resources/test_ascii-table_1/content-2.xhtml \
	modules/epub3-to-pef/src/test/resources/test_ascii-table_1/package.opf \
	modules/epub3-to-pef/src/test/resources/test_ascii-table_1/content-1.xhtml \
	modules/epub3-to-pef/src/test/resources/C00000.epub \
	modules/epub3-to-pef/src/test/java/XProcSpecTest.java \
	modules/epub3-to-pef/src/test/xprocspec/test_epub3-to-pef.script.xprocspec \
	modules/epub3-to-pef/src/test/xprocspec/test_epub3-to-pef.xprocspec \
	modules/epub3-to-pef/src/test/xprocspec/test_epub3-to-pef.load.xprocspec
modules/epub3-to-pef/.install-doc : \
	modules/epub3-to-pef/doc/index.md
.make/mk/modules/epub3-to-pef/sources.mk : \
	modules/epub3-to-pef/src \
	modules/epub3-to-pef/src/test \
	modules/epub3-to-pef/src/test/xspec \
	modules/epub3-to-pef/src/test/resources \
	modules/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets \
	modules/epub3-to-pef/src/test/resources/test_ascii-table_2 \
	modules/epub3-to-pef/src/test/resources/simple-epub \
	modules/epub3-to-pef/src/test/resources/test_ascii-table_1 \
	modules/epub3-to-pef/src/test/java \
	modules/epub3-to-pef/src/test/xprocspec \
	modules/epub3-to-pef/src/main \
	modules/epub3-to-pef/src/main/resources \
	modules/epub3-to-pef/src/main/resources/css \
	modules/epub3-to-pef/src/main/resources/META-INF \
	modules/epub3-to-pef/src/main/resources/xml \
	modules/epub3-to-pef/src/main/resources/xml/xproc \
	modules/epub3-to-pef/src/main/resources/xml/xslt \
	modules/epub3-to-pef/doc
