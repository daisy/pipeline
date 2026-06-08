modules/dtbook-to-pef/.test modules/dtbook-to-pef/.install modules/dtbook-to-pef/.install-doc $(TARGET_DIR)/state/modules/dtbook-to-pef/modified-since-release_ : \
	modules/dtbook-to-pef/src/main/resources/css/lists.xsl \
	modules/dtbook-to-pef/src/main/resources/css/tables.md \
	modules/dtbook-to-pef/src/main/resources/css/tables.xsl \
	modules/dtbook-to-pef/src/main/resources/css/default.scss \
	modules/dtbook-to-pef/src/main/resources/css/dotify.params \
	modules/dtbook-to-pef/src/main/resources/css/generate-toc.xsl \
	modules/dtbook-to-pef/src/main/resources/css/reset.css \
	modules/dtbook-to-pef/src/main/resources/css/medium.params \
	modules/dtbook-to-pef/src/main/resources/css/_legacy.scss \
	modules/dtbook-to-pef/src/main/resources/css/_notes.scss \
	modules/dtbook-to-pef/src/main/resources/css/definition-lists.xsl \
	modules/dtbook-to-pef/src/main/resources/css/_volume-breaking.scss \
	modules/dtbook-to-pef/src/main/resources/css/_tables.scss \
	modules/dtbook-to-pef/src/main/resources/css/_definition-lists.scss \
	modules/dtbook-to-pef/src/main/resources/css/generate-toc.md \
	modules/dtbook-to-pef/src/main/resources/css/volume-breaking.xsl \
	modules/dtbook-to-pef/src/main/resources/css/_generate-toc.scss \
	modules/dtbook-to-pef/src/main/resources/css/definition-lists.md \
	modules/dtbook-to-pef/src/main/resources/css/notes.xsl \
	modules/dtbook-to-pef/src/main/resources/META-INF/catalog.xml \
	modules/dtbook-to-pef/src/main/resources/xml/xproc/dtbook-to-pef.store.xpl \
	modules/dtbook-to-pef/src/main/resources/xml/xproc/xml-to-pef.store.xpl \
	modules/dtbook-to-pef/src/main/resources/xml/xproc/library.xpl \
	modules/dtbook-to-pef/src/main/resources/xml/xproc/dtbook-to-pef.convert.xpl \
	modules/dtbook-to-pef/src/main/resources/xml/xproc/dtbook-to-pef.xpl \
	modules/dtbook-to-pef/src/main/README.md
modules/dtbook-to-pef/.test modules/dtbook-to-pef/.install-doc : \
	modules/dtbook-to-pef/src/test/xspec/leaf-sections.xspec \
	modules/dtbook-to-pef/src/test/resources/logback.xml \
	modules/dtbook-to-pef/src/test/resources/dtbook.2005.basic.css \
	modules/dtbook-to-pef/src/test/resources/reset.css \
	modules/dtbook-to-pef/src/test/resources/test_ascii-table_2.brf \
	modules/dtbook-to-pef/src/test/resources/test_ascii-table_1.brf \
	modules/dtbook-to-pef/src/test/resources/style.css \
	modules/dtbook-to-pef/src/test/resources/valentin.jpg \
	modules/dtbook-to-pef/src/test/resources/hauy_valid.xml \
	modules/dtbook-to-pef/src/test/java/XProcSpecTest.java \
	modules/dtbook-to-pef/src/test/xprocspec/test_dtbook-to-pef.script.xprocspec \
	modules/dtbook-to-pef/src/test/xprocspec/test_dtbook-to-pef.xprocspec \
	modules/dtbook-to-pef/src/test/xprocspec/test_generate-toc.xprocspec
modules/dtbook-to-pef/.install-doc : \
	modules/dtbook-to-pef/doc/index.md
.make/mk/modules/dtbook-to-pef/sources.mk : \
	modules/dtbook-to-pef/src \
	modules/dtbook-to-pef/src/test \
	modules/dtbook-to-pef/src/test/xspec \
	modules/dtbook-to-pef/src/test/resources \
	modules/dtbook-to-pef/src/test/java \
	modules/dtbook-to-pef/src/test/xprocspec \
	modules/dtbook-to-pef/src/main \
	modules/dtbook-to-pef/src/main/resources \
	modules/dtbook-to-pef/src/main/resources/css \
	modules/dtbook-to-pef/src/main/resources/META-INF \
	modules/dtbook-to-pef/src/main/resources/xml \
	modules/dtbook-to-pef/src/main/resources/xml/xproc \
	modules/dtbook-to-pef/doc
