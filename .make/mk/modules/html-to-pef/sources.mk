modules/html-to-pef/.test modules/html-to-pef/.install modules/html-to-pef/.install-doc $(TARGET_DIR)/state/modules/html-to-pef/modified-since-release_ : \
	modules/html-to-pef/src/main/resources/css/lists.xsl \
	modules/html-to-pef/src/main/resources/css/tables.md \
	modules/html-to-pef/src/main/resources/css/tables.xsl \
	modules/html-to-pef/src/main/resources/css/default.scss \
	modules/html-to-pef/src/main/resources/css/dotify.params \
	modules/html-to-pef/src/main/resources/css/generate-toc.xsl \
	modules/html-to-pef/src/main/resources/css/reset.css \
	modules/html-to-pef/src/main/resources/css/medium.params \
	modules/html-to-pef/src/main/resources/css/_legacy.scss \
	modules/html-to-pef/src/main/resources/css/_notes.scss \
	modules/html-to-pef/src/main/resources/css/definition-lists.xsl \
	modules/html-to-pef/src/main/resources/css/_volume-breaking.scss \
	modules/html-to-pef/src/main/resources/css/_tables.scss \
	modules/html-to-pef/src/main/resources/css/_definition-lists.scss \
	modules/html-to-pef/src/main/resources/css/_page-breaking.scss \
	modules/html-to-pef/src/main/resources/css/generate-toc.md \
	modules/html-to-pef/src/main/resources/css/volume-breaking.xsl \
	modules/html-to-pef/src/main/resources/css/_generate-toc.scss \
	modules/html-to-pef/src/main/resources/css/definition-lists.md \
	modules/html-to-pef/src/main/resources/css/notes.xsl \
	modules/html-to-pef/src/main/resources/META-INF/catalog.xml \
	modules/html-to-pef/src/main/resources/xml/xproc/xml-to-pef.store.xpl \
	modules/html-to-pef/src/main/resources/xml/xproc/html-to-pef.store.xpl \
	modules/html-to-pef/src/main/resources/xml/xproc/html-to-pef.convert.xpl \
	modules/html-to-pef/src/main/resources/xml/xproc/html-to-pef.xpl \
	modules/html-to-pef/src/main/resources/xml/xproc/library.xpl \
	modules/html-to-pef/src/main/README.md
modules/html-to-pef/.test modules/html-to-pef/.install-doc : \
	modules/html-to-pef/src/test/xspec/leaf-sections.xspec \
	modules/html-to-pef/src/test/xspec/leaf-sections_2.xspec \
	modules/html-to-pef/src/test/resources/logback.xml \
	modules/html-to-pef/src/test/resources/reset-test.html \
	modules/html-to-pef/src/test/resources/images/alice09a.jpg \
	modules/html-to-pef/src/test/resources/images/alice05a.jpg \
	modules/html-to-pef/src/test/resources/images/alice04a.jpg \
	modules/html-to-pef/src/test/resources/images/alice08a.jpg \
	modules/html-to-pef/src/test/resources/images/alice03a.jpg \
	modules/html-to-pef/src/test/resources/images/alice02a.jpg \
	modules/html-to-pef/src/test/resources/images/alice01a.jpg \
	modules/html-to-pef/src/test/resources/images/alice07a.jpg \
	modules/html-to-pef/src/test/resources/images/alice10a.jpg \
	modules/html-to-pef/src/test/resources/images/alice06a.jpg \
	modules/html-to-pef/src/test/resources/test_ascii-table_2.brf \
	modules/html-to-pef/src/test/resources/test_ascii-table_1.brf \
	modules/html-to-pef/src/test/resources/xinclude-math.xhtml \
	modules/html-to-pef/src/test/resources/alice.xhtml \
	modules/html-to-pef/src/test/resources/external-entity.xhtml \
	modules/html-to-pef/src/test/resources/math.xml \
	modules/html-to-pef/src/test/resources/test_ascii-table_1.xhtml \
	modules/html-to-pef/src/test/resources/test_ascii-table_2.xhtml \
	modules/html-to-pef/src/test/java/XProcSpecTest.java \
	modules/html-to-pef/src/test/xprocspec/test_html-to-pef.script.xprocspec \
	modules/html-to-pef/src/test/xprocspec/test_html-to-pef.xprocspec
modules/html-to-pef/.install-doc : \
	modules/html-to-pef/doc/index.md
.make/mk/modules/html-to-pef/sources.mk : \
	modules/html-to-pef/src \
	modules/html-to-pef/src/test \
	modules/html-to-pef/src/test/xspec \
	modules/html-to-pef/src/test/resources \
	modules/html-to-pef/src/test/resources/images \
	modules/html-to-pef/src/test/java \
	modules/html-to-pef/src/test/xprocspec \
	modules/html-to-pef/src/main \
	modules/html-to-pef/src/main/resources \
	modules/html-to-pef/src/main/resources/css \
	modules/html-to-pef/src/main/resources/META-INF \
	modules/html-to-pef/src/main/resources/xml \
	modules/html-to-pef/src/main/resources/xml/xproc \
	modules/html-to-pef/doc
