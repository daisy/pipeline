modules/epub3-to-html/.test modules/epub3-to-html/.install modules/epub3-to-html/.install-doc $(TARGET_DIR)/state/modules/epub3-to-html/modified-since-release_ : \
	modules/epub3-to-html/src/main/resources/META-INF/catalog.xml \
	modules/epub3-to-html/src/main/resources/xml/xproc/opf-to-html-metadata.xpl \
	modules/epub3-to-html/src/main/resources/xml/xproc/library.xpl \
	modules/epub3-to-html/src/main/resources/xml/xslt/opf-to-html-metadata.xsl
modules/epub3-to-html/.test modules/epub3-to-html/.install-doc : \
	modules/epub3-to-html/src/test/resources/logback.xml \
	modules/epub3-to-html/src/test/resources/package.opf \
	modules/epub3-to-html/src/test/java/XProcSpecTest.java \
	modules/epub3-to-html/src/test/xprocspec/opf-to-html-metadata.xprocspec
.make/mk/modules/epub3-to-html/sources.mk : \
	modules/epub3-to-html/src \
	modules/epub3-to-html/src/test \
	modules/epub3-to-html/src/test/resources \
	modules/epub3-to-html/src/test/java \
	modules/epub3-to-html/src/test/xprocspec \
	modules/epub3-to-html/src/main \
	modules/epub3-to-html/src/main/resources \
	modules/epub3-to-html/src/main/resources/META-INF \
	modules/epub3-to-html/src/main/resources/xml \
	modules/epub3-to-html/src/main/resources/xml/xproc \
	modules/epub3-to-html/src/main/resources/xml/xslt
