modules/zedai-to-html/.test modules/zedai-to-html/.install modules/zedai-to-html/.install-doc $(TARGET_DIR)/state/modules/zedai-to-html/modified-since-release_ : \
	modules/zedai-to-html/src/main/resources/META-INF/catalog.xml \
	modules/zedai-to-html/src/main/resources/xml/xproc/zedai-to-html.convert.xpl \
	modules/zedai-to-html/src/main/resources/xml/xproc/zedai-to-html.xpl \
	modules/zedai-to-html/src/main/resources/xml/xproc/diagram-to-html.xpl \
	modules/zedai-to-html/src/main/resources/xml/xproc/library.xpl \
	modules/zedai-to-html/src/main/resources/xml/xslt/zedai-to-html.xsl \
	modules/zedai-to-html/src/main/resources/xml/xslt/fileset-convert-diagram.xsl \
	modules/zedai-to-html/src/main/resources/xml/xslt/diagram-to-html.xsl \
	modules/zedai-to-html/src/main/resources/xml/xslt/zedai-vocab-utils.xsl \
	modules/zedai-to-html/src/main/resources/xml/rdf/z3986-structure-vocab.xhtml \
	modules/zedai-to-html/src/main/resources/xml/rdf/epub-structure-vocab.xhtml
modules/zedai-to-html/.test modules/zedai-to-html/.install-doc : \
	modules/zedai-to-html/src/test/xspec/zedai-to-html_list.xspec \
	modules/zedai-to-html/src/test/xspec/zedai-to-html_imgdescr.xspec \
	modules/zedai-to-html/src/test/xspec/zedai-to-html_figure.xspec \
	modules/zedai-to-html/src/test/xspec/zedai-to-html_table.xspec \
	modules/zedai-to-html/src/test/xspec/zedai-to-html_dir.xspec \
	modules/zedai-to-html/src/test/xspec/zedai-to-html_alttext.xspec \
	modules/zedai-to-html/src/test/xspec/zedai-to-html.mathml.xspec \
	modules/zedai-to-html/src/test/resources/logback.xml \
	modules/zedai-to-html/src/test/resources/resources/alice.xml \
	modules/zedai-to-html/src/test/java/XProcSpecTest.java \
	modules/zedai-to-html/src/test/xprocspec/test_zedai-to-html.xprocspec \
	modules/zedai-to-html/src/test/xprocspec/test_zedai-to-html.script.xprocspec
modules/zedai-to-html/.install-doc : \
	modules/zedai-to-html/doc/index.md
.make/mk/modules/zedai-to-html/sources.mk : \
	modules/zedai-to-html/src \
	modules/zedai-to-html/src/test \
	modules/zedai-to-html/src/test/xspec \
	modules/zedai-to-html/src/test/resources \
	modules/zedai-to-html/src/test/resources/resources \
	modules/zedai-to-html/src/test/java \
	modules/zedai-to-html/src/test/xprocspec \
	modules/zedai-to-html/src/main \
	modules/zedai-to-html/src/main/resources \
	modules/zedai-to-html/src/main/resources/META-INF \
	modules/zedai-to-html/src/main/resources/xml \
	modules/zedai-to-html/src/main/resources/xml/xproc \
	modules/zedai-to-html/src/main/resources/xml/xslt \
	modules/zedai-to-html/src/main/resources/xml/rdf \
	modules/zedai-to-html/doc
