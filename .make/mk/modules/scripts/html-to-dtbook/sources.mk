modules/scripts/html-to-dtbook/.test modules/scripts/html-to-dtbook/.install modules/scripts/html-to-dtbook/.install-doc $(TARGET_DIR)/state/modules/scripts/html-to-dtbook/modified-since-release_ : \
	modules/scripts/html-to-dtbook/src/main/resources/css/dtbook.2005.basic.css \
	modules/scripts/html-to-dtbook/src/main/resources/META-INF/catalog.xml \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xproc/daisy202-html-to-dtbook.xpl \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xproc/extract-svg.xpl \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xproc/epub3-html-to-dtbook.xpl \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xproc/html-to-dtbook.script.xpl \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xproc/html-to-dtbook.xpl \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xproc/library.xpl \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xslt/extract-svg.xsl \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xslt/xhtml2dtbook.xsl \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xslt/epub3-to-dtbook.xsl \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xslt/prepare-html.xsl \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xslt/format-list.xsl
modules/scripts/html-to-dtbook/.test modules/scripts/html-to-dtbook/.install-doc : \
	modules/scripts/html-to-dtbook/src/test/xspec/epub3-to-dtbook.xspec \
	modules/scripts/html-to-dtbook/src/test/resources/logback.xml \
	modules/scripts/html-to-dtbook/src/test/resources/single-html/images/valentin.jpg \
	modules/scripts/html-to-dtbook/src/test/resources/single-html/C00000.xhtml \
	modules/scripts/html-to-dtbook/src/test/resources/DTBook/C00000.xml \
	modules/scripts/html-to-dtbook/src/test/java/XProcSpecTest.java \
	modules/scripts/html-to-dtbook/src/test/xprocspec/html-to-dtbook.xprocspec \
	modules/scripts/html-to-dtbook/src/test/xprocspec/prepare-html.xprocspec \
	modules/scripts/html-to-dtbook/src/test/xprocspec/extract-svg.xprocspec \
	modules/scripts/html-to-dtbook/src/test/xprocspec/html-to-dtbook.script.xprocspec
modules/scripts/html-to-dtbook/.install-doc : \
	modules/scripts/html-to-dtbook/doc/index.md
.make/mk/modules/scripts/html-to-dtbook/sources.mk : \
	modules/scripts/html-to-dtbook/src \
	modules/scripts/html-to-dtbook/src/test \
	modules/scripts/html-to-dtbook/src/test/xspec \
	modules/scripts/html-to-dtbook/src/test/resources \
	modules/scripts/html-to-dtbook/src/test/resources/single-html \
	modules/scripts/html-to-dtbook/src/test/resources/single-html/images \
	modules/scripts/html-to-dtbook/src/test/resources/DTBook \
	modules/scripts/html-to-dtbook/src/test/java \
	modules/scripts/html-to-dtbook/src/test/xprocspec \
	modules/scripts/html-to-dtbook/src/main \
	modules/scripts/html-to-dtbook/src/main/resources \
	modules/scripts/html-to-dtbook/src/main/resources/css \
	modules/scripts/html-to-dtbook/src/main/resources/META-INF \
	modules/scripts/html-to-dtbook/src/main/resources/xml \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xproc \
	modules/scripts/html-to-dtbook/src/main/resources/xml/xslt \
	modules/scripts/html-to-dtbook/doc
