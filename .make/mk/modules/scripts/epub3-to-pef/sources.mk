modules/scripts/epub3-to-pef/.test modules/scripts/epub3-to-pef/.install modules/scripts/epub3-to-pef/.install-doc $(TARGET_DIR)/state/modules/scripts/epub3-to-pef/modified-since-release_ : \
	modules/scripts/epub3-to-pef/src/main/resources/css/reset.css \
	modules/scripts/epub3-to-pef/src/main/resources/META-INF/catalog.xml \
	modules/scripts/epub3-to-pef/src/main/resources/xml/xproc/xml-to-pef.store.xpl \
	modules/scripts/epub3-to-pef/src/main/resources/xml/xproc/epub3-to-pef.xpl \
	modules/scripts/epub3-to-pef/src/main/resources/xml/xproc/epub3-to-pef.convert.xpl \
	modules/scripts/epub3-to-pef/src/main/resources/xml/xproc/epub3-to-pef.store.xpl \
	modules/scripts/epub3-to-pef/src/main/resources/xml/xproc/epub3-to-pef.load.xpl \
	modules/scripts/epub3-to-pef/src/main/resources/xml/xproc/library.xpl \
	modules/scripts/epub3-to-pef/src/main/resources/xml/xslt/opf-to-html-head.xsl \
	modules/scripts/epub3-to-pef/src/main/README.md
modules/scripts/epub3-to-pef/.test modules/scripts/epub3-to-pef/.install-doc : \
	modules/scripts/epub3-to-pef/src/test/xspec/catalog.xml \
	modules/scripts/epub3-to-pef/src/test/xspec/mock-functions.xsl \
	modules/scripts/epub3-to-pef/src/test/resources/logback.xml \
	modules/scripts/epub3-to-pef/src/test/resources/preamble.html \
	modules/scripts/epub3-to-pef/src/test/resources/reset.css \
	modules/scripts/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/nav.xhtml \
	modules/scripts/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/p4-padding.css \
	modules/scripts/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/content-2.xhtml \
	modules/scripts/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/p6-padding.css \
	modules/scripts/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/p5-padding.css \
	modules/scripts/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/package.opf \
	modules/scripts/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets/content-1.xhtml \
	modules/scripts/epub3-to-pef/src/test/resources/test_ascii-table_2/nav.xhtml \
	modules/scripts/epub3-to-pef/src/test/resources/test_ascii-table_2/content-2.xhtml \
	modules/scripts/epub3-to-pef/src/test/resources/test_ascii-table_2/package.opf \
	modules/scripts/epub3-to-pef/src/test/resources/test_ascii-table_2/content-1.xhtml \
	modules/scripts/epub3-to-pef/src/test/resources/test_ascii-table_2.brf \
	modules/scripts/epub3-to-pef/src/test/resources/test_ascii-table_1.brf \
	modules/scripts/epub3-to-pef/src/test/resources/simple-epub/nav.xhtml \
	modules/scripts/epub3-to-pef/src/test/resources/simple-epub/content-2.xhtml \
	modules/scripts/epub3-to-pef/src/test/resources/simple-epub/package.opf \
	modules/scripts/epub3-to-pef/src/test/resources/simple-epub/content-1.xhtml \
	modules/scripts/epub3-to-pef/src/test/resources/style.css \
	modules/scripts/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets.override.css \
	modules/scripts/epub3-to-pef/src/test/resources/test_ascii-table_1/nav.xhtml \
	modules/scripts/epub3-to-pef/src/test/resources/test_ascii-table_1/content-2.xhtml \
	modules/scripts/epub3-to-pef/src/test/resources/test_ascii-table_1/package.opf \
	modules/scripts/epub3-to-pef/src/test/resources/test_ascii-table_1/content-1.xhtml \
	modules/scripts/epub3-to-pef/src/test/resources/C00000.epub \
	modules/scripts/epub3-to-pef/src/test/java/XProcSpecTest.java \
	modules/scripts/epub3-to-pef/src/test/xprocspec/test_epub3-to-pef.script.xprocspec \
	modules/scripts/epub3-to-pef/src/test/xprocspec/test_epub3-to-pef.xprocspec \
	modules/scripts/epub3-to-pef/src/test/xprocspec/test_epub3-to-pef.load.xprocspec
modules/scripts/epub3-to-pef/.install-doc : \
	modules/scripts/epub3-to-pef/doc/index.md
.make/mk/modules/scripts/epub3-to-pef/sources.mk : \
	modules/scripts/epub3-to-pef/src \
	modules/scripts/epub3-to-pef/src/test \
	modules/scripts/epub3-to-pef/src/test/xspec \
	modules/scripts/epub3-to-pef/src/test/resources \
	modules/scripts/epub3-to-pef/src/test/resources/test-option_apply-document-specific-stylesheets \
	modules/scripts/epub3-to-pef/src/test/resources/test_ascii-table_2 \
	modules/scripts/epub3-to-pef/src/test/resources/simple-epub \
	modules/scripts/epub3-to-pef/src/test/resources/test_ascii-table_1 \
	modules/scripts/epub3-to-pef/src/test/java \
	modules/scripts/epub3-to-pef/src/test/xprocspec \
	modules/scripts/epub3-to-pef/src/main \
	modules/scripts/epub3-to-pef/src/main/resources \
	modules/scripts/epub3-to-pef/src/main/resources/css \
	modules/scripts/epub3-to-pef/src/main/resources/META-INF \
	modules/scripts/epub3-to-pef/src/main/resources/xml \
	modules/scripts/epub3-to-pef/src/main/resources/xml/xproc \
	modules/scripts/epub3-to-pef/src/main/resources/xml/xslt \
	modules/scripts/epub3-to-pef/doc
