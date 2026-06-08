modules/html-to-epub3/.test modules/html-to-epub3/.install modules/html-to-epub3/.install-doc $(TARGET_DIR)/state/modules/html-to-epub3/modified-since-release_ : \
	modules/html-to-epub3/src/main/resources/META-INF/catalog.xml \
	modules/html-to-epub3/src/main/resources/xml/xproc/html-to-opf-metadata.xpl \
	modules/html-to-epub3/src/main/resources/xml/xproc/html-to-epub3.xpl \
	modules/html-to-epub3/src/main/resources/xml/xproc/html-to-epub3.convert.xpl \
	modules/html-to-epub3/src/main/resources/xml/xproc/library.xpl \
	modules/html-to-epub3/src/main/resources/xml/xslt/html-to-metadata.xsl
modules/html-to-epub3/.test modules/html-to-epub3/.install-doc : \
	modules/html-to-epub3/src/test/resources/logback.xml \
	modules/html-to-epub3/src/test/resources/minimal.xhtml \
	modules/html-to-epub3/src/test/java/XProcSpecTest.java \
	modules/html-to-epub3/src/test/xprocspec/test_html-to-epub3.script.xprocspec \
	modules/html-to-epub3/src/test/xprocspec/test_html-to-epub3.xprocspec
modules/html-to-epub3/.install-doc : \
	modules/html-to-epub3/doc/index.md
.make/mk/modules/html-to-epub3/sources.mk : \
	modules/html-to-epub3/src \
	modules/html-to-epub3/src/test \
	modules/html-to-epub3/src/test/resources \
	modules/html-to-epub3/src/test/java \
	modules/html-to-epub3/src/test/xprocspec \
	modules/html-to-epub3/src/main \
	modules/html-to-epub3/src/main/resources \
	modules/html-to-epub3/src/main/resources/META-INF \
	modules/html-to-epub3/src/main/resources/xml \
	modules/html-to-epub3/src/main/resources/xml/xproc \
	modules/html-to-epub3/src/main/resources/xml/xslt \
	modules/html-to-epub3/doc
