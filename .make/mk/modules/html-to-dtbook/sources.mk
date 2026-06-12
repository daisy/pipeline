modules/html-to-dtbook/.test modules/html-to-dtbook/.install modules/html-to-dtbook/.install-doc $(TARGET_DIR)/state/modules/html-to-dtbook/modified-since-release_ : \
	modules/html-to-dtbook/src/main/resources/css/dtbook.2005.basic.css \
	modules/html-to-dtbook/src/main/resources/META-INF/catalog.xml \
	modules/html-to-dtbook/src/main/resources/xml/xproc/daisy202-html-to-dtbook.xpl \
	modules/html-to-dtbook/src/main/resources/xml/xproc/extract-svg.xpl \
	modules/html-to-dtbook/src/main/resources/xml/xproc/epub3-html-to-dtbook.xpl \
	modules/html-to-dtbook/src/main/resources/xml/xproc/html-to-dtbook.script.xpl \
	modules/html-to-dtbook/src/main/resources/xml/xproc/html-to-dtbook.xpl \
	modules/html-to-dtbook/src/main/resources/xml/xproc/library.xpl \
	modules/html-to-dtbook/src/main/resources/xml/xslt/extract-svg.xsl \
	modules/html-to-dtbook/src/main/resources/xml/xslt/xhtml2dtbook.xsl \
	modules/html-to-dtbook/src/main/resources/xml/xslt/epub3-to-dtbook.xsl \
	modules/html-to-dtbook/src/main/resources/xml/xslt/prepare-html.xsl \
	modules/html-to-dtbook/src/main/resources/xml/xslt/format-list.xsl
modules/html-to-dtbook/.test modules/html-to-dtbook/.install-doc : \
	modules/html-to-dtbook/src/test/xspec/epub3-to-dtbook.xspec \
	modules/html-to-dtbook/src/test/resources/logback.xml \
	modules/html-to-dtbook/src/test/resources/single-html/images/valentin.jpg \
	modules/html-to-dtbook/src/test/resources/single-html/C00000.xhtml \
	modules/html-to-dtbook/src/test/resources/DTBook/C00000.xml \
	modules/html-to-dtbook/src/test/java/XProcSpecTest.java \
	modules/html-to-dtbook/src/test/xprocspec/html-to-dtbook.xprocspec \
	modules/html-to-dtbook/src/test/xprocspec/prepare-html.xprocspec \
	modules/html-to-dtbook/src/test/xprocspec/extract-svg.xprocspec \
	modules/html-to-dtbook/src/test/xprocspec/html-to-dtbook.script.xprocspec
modules/html-to-dtbook/.install-doc : \
	modules/html-to-dtbook/doc/index.md
.make/mk/modules/html-to-dtbook/sources.mk : \
	modules/html-to-dtbook/src \
	modules/html-to-dtbook/src/test \
	modules/html-to-dtbook/src/test/xspec \
	modules/html-to-dtbook/src/test/resources \
	modules/html-to-dtbook/src/test/resources/single-html \
	modules/html-to-dtbook/src/test/resources/single-html/images \
	modules/html-to-dtbook/src/test/resources/DTBook \
	modules/html-to-dtbook/src/test/java \
	modules/html-to-dtbook/src/test/xprocspec \
	modules/html-to-dtbook/src/main \
	modules/html-to-dtbook/src/main/resources \
	modules/html-to-dtbook/src/main/resources/css \
	modules/html-to-dtbook/src/main/resources/META-INF \
	modules/html-to-dtbook/src/main/resources/xml \
	modules/html-to-dtbook/src/main/resources/xml/xproc \
	modules/html-to-dtbook/src/main/resources/xml/xslt \
	modules/html-to-dtbook/doc
