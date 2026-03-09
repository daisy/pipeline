modules/scripts-utils/epub3-to-html/.test modules/scripts-utils/epub3-to-html/.install modules/scripts-utils/epub3-to-html/.install-doc $(TARGET_DIR)/state/modules/scripts-utils/epub3-to-html/modified-since-release_ : \
	modules/scripts-utils/epub3-to-html/src/main/resources/META-INF/catalog.xml \
	modules/scripts-utils/epub3-to-html/src/main/resources/xml/xproc/opf-to-html-metadata.xpl \
	modules/scripts-utils/epub3-to-html/src/main/resources/xml/xproc/library.xpl \
	modules/scripts-utils/epub3-to-html/src/main/resources/xml/xslt/opf-to-html-metadata.xsl
modules/scripts-utils/epub3-to-html/.test modules/scripts-utils/epub3-to-html/.install-doc : \
	modules/scripts-utils/epub3-to-html/src/test/resources/logback.xml \
	modules/scripts-utils/epub3-to-html/src/test/resources/package.opf \
	modules/scripts-utils/epub3-to-html/src/test/java/XProcSpecTest.java \
	modules/scripts-utils/epub3-to-html/src/test/xprocspec/opf-to-html-metadata.xprocspec
.make/mk/modules/scripts-utils/epub3-to-html/sources.mk : \
	modules/scripts-utils/epub3-to-html/src \
	modules/scripts-utils/epub3-to-html/src/test \
	modules/scripts-utils/epub3-to-html/src/test/resources \
	modules/scripts-utils/epub3-to-html/src/test/java \
	modules/scripts-utils/epub3-to-html/src/test/xprocspec \
	modules/scripts-utils/epub3-to-html/src/main \
	modules/scripts-utils/epub3-to-html/src/main/resources \
	modules/scripts-utils/epub3-to-html/src/main/resources/META-INF \
	modules/scripts-utils/epub3-to-html/src/main/resources/xml \
	modules/scripts-utils/epub3-to-html/src/main/resources/xml/xproc \
	modules/scripts-utils/epub3-to-html/src/main/resources/xml/xslt
