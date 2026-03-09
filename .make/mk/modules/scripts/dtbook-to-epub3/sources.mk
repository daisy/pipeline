modules/scripts/dtbook-to-epub3/.test modules/scripts/dtbook-to-epub3/.install modules/scripts/dtbook-to-epub3/.install-doc $(TARGET_DIR)/state/modules/scripts/dtbook-to-epub3/modified-since-release_ : \
	modules/scripts/dtbook-to-epub3/src/main/resources/META-INF/catalog.xml \
	modules/scripts/dtbook-to-epub3/src/main/resources/xml/dtbook-to-opf-metadata.xpl \
	modules/scripts/dtbook-to-epub3/src/main/resources/xml/dtbook-to-opf-metadata.xsl \
	modules/scripts/dtbook-to-epub3/src/main/resources/xml/convert.xpl \
	modules/scripts/dtbook-to-epub3/src/main/resources/xml/source-of-pagination.xsl \
	modules/scripts/dtbook-to-epub3/src/main/resources/xml/dtbook-to-epub3.xpl \
	modules/scripts/dtbook-to-epub3/src/main/resources/xml/library.xpl
modules/scripts/dtbook-to-epub3/.test modules/scripts/dtbook-to-epub3/.install-doc : \
	modules/scripts/dtbook-to-epub3/src/test/resources/logback.xml \
	modules/scripts/dtbook-to-epub3/src/test/resources/dtbook110.xml \
	modules/scripts/dtbook-to-epub3/src/test/resources/minimal.xml \
	modules/scripts/dtbook-to-epub3/src/test/java/XProcSpecTest.java \
	modules/scripts/dtbook-to-epub3/src/test/xprocspec/test_dtbook-to-epub3.xprocspec \
	modules/scripts/dtbook-to-epub3/src/test/xprocspec/play-audio.xpl \
	modules/scripts/dtbook-to-epub3/src/test/xprocspec/test_dtbook-to-epub3.script.xprocspec
modules/scripts/dtbook-to-epub3/.install-doc : \
	modules/scripts/dtbook-to-epub3/doc/index.md
.make/mk/modules/scripts/dtbook-to-epub3/sources.mk : \
	modules/scripts/dtbook-to-epub3/src \
	modules/scripts/dtbook-to-epub3/src/test \
	modules/scripts/dtbook-to-epub3/src/test/resources \
	modules/scripts/dtbook-to-epub3/src/test/java \
	modules/scripts/dtbook-to-epub3/src/test/xprocspec \
	modules/scripts/dtbook-to-epub3/src/main \
	modules/scripts/dtbook-to-epub3/src/main/resources \
	modules/scripts/dtbook-to-epub3/src/main/resources/META-INF \
	modules/scripts/dtbook-to-epub3/src/main/resources/xml \
	modules/scripts/dtbook-to-epub3/doc
