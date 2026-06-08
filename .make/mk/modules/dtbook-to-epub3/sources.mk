modules/dtbook-to-epub3/.test modules/dtbook-to-epub3/.install modules/dtbook-to-epub3/.install-doc $(TARGET_DIR)/state/modules/dtbook-to-epub3/modified-since-release_ : \
	modules/dtbook-to-epub3/src/main/resources/META-INF/catalog.xml \
	modules/dtbook-to-epub3/src/main/resources/xml/dtbook-to-opf-metadata.xpl \
	modules/dtbook-to-epub3/src/main/resources/xml/dtbook-to-opf-metadata.xsl \
	modules/dtbook-to-epub3/src/main/resources/xml/convert.xpl \
	modules/dtbook-to-epub3/src/main/resources/xml/source-of-pagination.xsl \
	modules/dtbook-to-epub3/src/main/resources/xml/dtbook-to-epub3.xpl \
	modules/dtbook-to-epub3/src/main/resources/xml/library.xpl
modules/dtbook-to-epub3/.test modules/dtbook-to-epub3/.install-doc : \
	modules/dtbook-to-epub3/src/test/resources/logback.xml \
	modules/dtbook-to-epub3/src/test/resources/dtbook110.xml \
	modules/dtbook-to-epub3/src/test/resources/minimal.xml \
	modules/dtbook-to-epub3/src/test/java/XProcSpecTest.java \
	modules/dtbook-to-epub3/src/test/xprocspec/test_dtbook-to-epub3.xprocspec \
	modules/dtbook-to-epub3/src/test/xprocspec/play-audio.xpl \
	modules/dtbook-to-epub3/src/test/xprocspec/test_dtbook-to-epub3.script.xprocspec
modules/dtbook-to-epub3/.install-doc : \
	modules/dtbook-to-epub3/doc/index.md
.make/mk/modules/dtbook-to-epub3/sources.mk : \
	modules/dtbook-to-epub3/src \
	modules/dtbook-to-epub3/src/test \
	modules/dtbook-to-epub3/src/test/resources \
	modules/dtbook-to-epub3/src/test/java \
	modules/dtbook-to-epub3/src/test/xprocspec \
	modules/dtbook-to-epub3/src/main \
	modules/dtbook-to-epub3/src/main/resources \
	modules/dtbook-to-epub3/src/main/resources/META-INF \
	modules/dtbook-to-epub3/src/main/resources/xml \
	modules/dtbook-to-epub3/doc
