modules/scripts/zedai-to-html/.test modules/scripts/zedai-to-html/.install modules/scripts/zedai-to-html/.install-doc $(TARGET_DIR)/state/modules/scripts/zedai-to-html/modified-since-release_ : \
	modules/scripts/zedai-to-html/src/main/resources/META-INF/catalog.xml \
	modules/scripts/zedai-to-html/src/main/resources/xml/xproc/zedai-to-html.convert.xpl \
	modules/scripts/zedai-to-html/src/main/resources/xml/xproc/zedai-to-html.xpl \
	modules/scripts/zedai-to-html/src/main/resources/xml/xproc/diagram-to-html.xpl \
	modules/scripts/zedai-to-html/src/main/resources/xml/xproc/library.xpl \
	modules/scripts/zedai-to-html/src/main/resources/xml/xslt/zedai-to-html.xsl \
	modules/scripts/zedai-to-html/src/main/resources/xml/xslt/fileset-convert-diagram.xsl \
	modules/scripts/zedai-to-html/src/main/resources/xml/xslt/diagram-to-html.xsl \
	modules/scripts/zedai-to-html/src/main/resources/xml/xslt/zedai-vocab-utils.xsl \
	modules/scripts/zedai-to-html/src/main/resources/xml/rdf/z3986-structure-vocab.xhtml \
	modules/scripts/zedai-to-html/src/main/resources/xml/rdf/epub-structure-vocab.xhtml
modules/scripts/zedai-to-html/.test modules/scripts/zedai-to-html/.install-doc : \
	modules/scripts/zedai-to-html/src/test/xspec/zedai-to-html_list.xspec \
	modules/scripts/zedai-to-html/src/test/xspec/zedai-to-html_imgdescr.xspec \
	modules/scripts/zedai-to-html/src/test/xspec/zedai-to-html_figure.xspec \
	modules/scripts/zedai-to-html/src/test/xspec/zedai-to-html_table.xspec \
	modules/scripts/zedai-to-html/src/test/xspec/zedai-to-html_dir.xspec \
	modules/scripts/zedai-to-html/src/test/xspec/zedai-to-html_alttext.xspec \
	modules/scripts/zedai-to-html/src/test/xspec/zedai-to-html.mathml.xspec \
	modules/scripts/zedai-to-html/src/test/resources/logback.xml \
	modules/scripts/zedai-to-html/src/test/resources/resources/alice.xml \
	modules/scripts/zedai-to-html/src/test/java/XProcSpecTest.java \
	modules/scripts/zedai-to-html/src/test/xprocspec/test_zedai-to-html.xprocspec \
	modules/scripts/zedai-to-html/src/test/xprocspec/test_zedai-to-html.script.xprocspec
modules/scripts/zedai-to-html/.install-doc : \
	modules/scripts/zedai-to-html/doc/index.md
.make/mk/modules/scripts/zedai-to-html/sources.mk : \
	modules/scripts/zedai-to-html/src \
	modules/scripts/zedai-to-html/src/test \
	modules/scripts/zedai-to-html/src/test/xspec \
	modules/scripts/zedai-to-html/src/test/resources \
	modules/scripts/zedai-to-html/src/test/resources/resources \
	modules/scripts/zedai-to-html/src/test/java \
	modules/scripts/zedai-to-html/src/test/xprocspec \
	modules/scripts/zedai-to-html/src/main \
	modules/scripts/zedai-to-html/src/main/resources \
	modules/scripts/zedai-to-html/src/main/resources/META-INF \
	modules/scripts/zedai-to-html/src/main/resources/xml \
	modules/scripts/zedai-to-html/src/main/resources/xml/xproc \
	modules/scripts/zedai-to-html/src/main/resources/xml/xslt \
	modules/scripts/zedai-to-html/src/main/resources/xml/rdf \
	modules/scripts/zedai-to-html/doc
