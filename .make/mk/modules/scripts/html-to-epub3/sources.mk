modules/scripts/html-to-epub3/.test modules/scripts/html-to-epub3/.install modules/scripts/html-to-epub3/.install-doc $(TARGET_DIR)/state/modules/scripts/html-to-epub3/modified-since-release_ : \
	modules/scripts/html-to-epub3/src/main/resources/META-INF/catalog.xml \
	modules/scripts/html-to-epub3/src/main/resources/xml/xproc/html-to-opf-metadata.xpl \
	modules/scripts/html-to-epub3/src/main/resources/xml/xproc/html-to-epub3.xpl \
	modules/scripts/html-to-epub3/src/main/resources/xml/xproc/html-to-epub3.convert.xpl \
	modules/scripts/html-to-epub3/src/main/resources/xml/xproc/library.xpl \
	modules/scripts/html-to-epub3/src/main/resources/xml/xslt/html-to-metadata.xsl
modules/scripts/html-to-epub3/.test modules/scripts/html-to-epub3/.install-doc : \
	modules/scripts/html-to-epub3/src/test/resources/logback.xml \
	modules/scripts/html-to-epub3/src/test/resources/minimal.xhtml \
	modules/scripts/html-to-epub3/src/test/java/XProcSpecTest.java \
	modules/scripts/html-to-epub3/src/test/xprocspec/test_html-to-epub3.script.xprocspec \
	modules/scripts/html-to-epub3/src/test/xprocspec/test_html-to-epub3.xprocspec
modules/scripts/html-to-epub3/.install-doc : \
	modules/scripts/html-to-epub3/doc/index.md
.make/mk/modules/scripts/html-to-epub3/sources.mk : \
	modules/scripts/html-to-epub3/src \
	modules/scripts/html-to-epub3/src/test \
	modules/scripts/html-to-epub3/src/test/resources \
	modules/scripts/html-to-epub3/src/test/java \
	modules/scripts/html-to-epub3/src/test/xprocspec \
	modules/scripts/html-to-epub3/src/main \
	modules/scripts/html-to-epub3/src/main/resources \
	modules/scripts/html-to-epub3/src/main/resources/META-INF \
	modules/scripts/html-to-epub3/src/main/resources/xml \
	modules/scripts/html-to-epub3/src/main/resources/xml/xproc \
	modules/scripts/html-to-epub3/src/main/resources/xml/xslt \
	modules/scripts/html-to-epub3/doc
