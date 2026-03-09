modules/scripts/dtbook-to-pef/.test modules/scripts/dtbook-to-pef/.install modules/scripts/dtbook-to-pef/.install-doc $(TARGET_DIR)/state/modules/scripts/dtbook-to-pef/modified-since-release_ : \
	modules/scripts/dtbook-to-pef/src/main/resources/css/lists.xsl \
	modules/scripts/dtbook-to-pef/src/main/resources/css/tables.md \
	modules/scripts/dtbook-to-pef/src/main/resources/css/tables.xsl \
	modules/scripts/dtbook-to-pef/src/main/resources/css/default.scss \
	modules/scripts/dtbook-to-pef/src/main/resources/css/dotify.params \
	modules/scripts/dtbook-to-pef/src/main/resources/css/generate-toc.xsl \
	modules/scripts/dtbook-to-pef/src/main/resources/css/reset.css \
	modules/scripts/dtbook-to-pef/src/main/resources/css/medium.params \
	modules/scripts/dtbook-to-pef/src/main/resources/css/_legacy.scss \
	modules/scripts/dtbook-to-pef/src/main/resources/css/_notes.scss \
	modules/scripts/dtbook-to-pef/src/main/resources/css/definition-lists.xsl \
	modules/scripts/dtbook-to-pef/src/main/resources/css/_volume-breaking.scss \
	modules/scripts/dtbook-to-pef/src/main/resources/css/_tables.scss \
	modules/scripts/dtbook-to-pef/src/main/resources/css/_definition-lists.scss \
	modules/scripts/dtbook-to-pef/src/main/resources/css/generate-toc.md \
	modules/scripts/dtbook-to-pef/src/main/resources/css/volume-breaking.xsl \
	modules/scripts/dtbook-to-pef/src/main/resources/css/_generate-toc.scss \
	modules/scripts/dtbook-to-pef/src/main/resources/css/definition-lists.md \
	modules/scripts/dtbook-to-pef/src/main/resources/css/notes.xsl \
	modules/scripts/dtbook-to-pef/src/main/resources/META-INF/catalog.xml \
	modules/scripts/dtbook-to-pef/src/main/resources/xml/xproc/dtbook-to-pef.store.xpl \
	modules/scripts/dtbook-to-pef/src/main/resources/xml/xproc/xml-to-pef.store.xpl \
	modules/scripts/dtbook-to-pef/src/main/resources/xml/xproc/library.xpl \
	modules/scripts/dtbook-to-pef/src/main/resources/xml/xproc/dtbook-to-pef.convert.xpl \
	modules/scripts/dtbook-to-pef/src/main/resources/xml/xproc/dtbook-to-pef.xpl \
	modules/scripts/dtbook-to-pef/src/main/README.md
modules/scripts/dtbook-to-pef/.test modules/scripts/dtbook-to-pef/.install-doc : \
	modules/scripts/dtbook-to-pef/src/test/xspec/leaf-sections.xspec \
	modules/scripts/dtbook-to-pef/src/test/resources/logback.xml \
	modules/scripts/dtbook-to-pef/src/test/resources/dtbook.2005.basic.css \
	modules/scripts/dtbook-to-pef/src/test/resources/reset.css \
	modules/scripts/dtbook-to-pef/src/test/resources/test_ascii-table_2.brf \
	modules/scripts/dtbook-to-pef/src/test/resources/test_ascii-table_1.brf \
	modules/scripts/dtbook-to-pef/src/test/resources/style.css \
	modules/scripts/dtbook-to-pef/src/test/resources/valentin.jpg \
	modules/scripts/dtbook-to-pef/src/test/resources/hauy_valid.xml \
	modules/scripts/dtbook-to-pef/src/test/java/XProcSpecTest.java \
	modules/scripts/dtbook-to-pef/src/test/xprocspec/test_dtbook-to-pef.script.xprocspec \
	modules/scripts/dtbook-to-pef/src/test/xprocspec/test_dtbook-to-pef.xprocspec \
	modules/scripts/dtbook-to-pef/src/test/xprocspec/test_generate-toc.xprocspec
modules/scripts/dtbook-to-pef/.install-doc : \
	modules/scripts/dtbook-to-pef/doc/index.md
.make/mk/modules/scripts/dtbook-to-pef/sources.mk : \
	modules/scripts/dtbook-to-pef/src \
	modules/scripts/dtbook-to-pef/src/test \
	modules/scripts/dtbook-to-pef/src/test/xspec \
	modules/scripts/dtbook-to-pef/src/test/resources \
	modules/scripts/dtbook-to-pef/src/test/java \
	modules/scripts/dtbook-to-pef/src/test/xprocspec \
	modules/scripts/dtbook-to-pef/src/main \
	modules/scripts/dtbook-to-pef/src/main/resources \
	modules/scripts/dtbook-to-pef/src/main/resources/css \
	modules/scripts/dtbook-to-pef/src/main/resources/META-INF \
	modules/scripts/dtbook-to-pef/src/main/resources/xml \
	modules/scripts/dtbook-to-pef/src/main/resources/xml/xproc \
	modules/scripts/dtbook-to-pef/doc
