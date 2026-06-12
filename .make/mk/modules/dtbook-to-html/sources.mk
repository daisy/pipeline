modules/dtbook-to-html/.test modules/dtbook-to-html/.install modules/dtbook-to-html/.install-doc $(TARGET_DIR)/state/modules/dtbook-to-html/modified-since-release_ : \
	modules/dtbook-to-html/src/main/resources/META-INF/catalog.xml \
	modules/dtbook-to-html/src/main/resources/xml/dtbook-to-html.xpl \
	modules/dtbook-to-html/src/main/resources/xml/convert.xpl \
	modules/dtbook-to-html/src/main/resources/xml/library.xpl
modules/dtbook-to-html/.test modules/dtbook-to-html/.install-doc : \
	modules/dtbook-to-html/src/test/resources/logback.xml \
	modules/dtbook-to-html/src/test/resources/minimal.xml \
	modules/dtbook-to-html/src/test/java/XProcSpecTest.java \
	modules/dtbook-to-html/src/test/xprocspec/test_dtbook-to-html.xprocspec \
	modules/dtbook-to-html/src/test/xprocspec/test_dtbook-to-html.script.xprocspec
modules/dtbook-to-html/.install-doc : \
	modules/dtbook-to-html/doc/index.md
.make/mk/modules/dtbook-to-html/sources.mk : \
	modules/dtbook-to-html/src \
	modules/dtbook-to-html/src/test \
	modules/dtbook-to-html/src/test/resources \
	modules/dtbook-to-html/src/test/java \
	modules/dtbook-to-html/src/test/xprocspec \
	modules/dtbook-to-html/src/main \
	modules/dtbook-to-html/src/main/resources \
	modules/dtbook-to-html/src/main/resources/META-INF \
	modules/dtbook-to-html/src/main/resources/xml \
	modules/dtbook-to-html/doc
