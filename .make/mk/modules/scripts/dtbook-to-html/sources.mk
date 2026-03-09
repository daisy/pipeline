modules/scripts/dtbook-to-html/.test modules/scripts/dtbook-to-html/.install modules/scripts/dtbook-to-html/.install-doc $(TARGET_DIR)/state/modules/scripts/dtbook-to-html/modified-since-release_ : \
	modules/scripts/dtbook-to-html/src/main/resources/META-INF/catalog.xml \
	modules/scripts/dtbook-to-html/src/main/resources/xml/dtbook-to-html.xpl \
	modules/scripts/dtbook-to-html/src/main/resources/xml/convert.xpl \
	modules/scripts/dtbook-to-html/src/main/resources/xml/library.xpl
modules/scripts/dtbook-to-html/.test modules/scripts/dtbook-to-html/.install-doc : \
	modules/scripts/dtbook-to-html/src/test/resources/logback.xml \
	modules/scripts/dtbook-to-html/src/test/resources/minimal.xml \
	modules/scripts/dtbook-to-html/src/test/java/XProcSpecTest.java \
	modules/scripts/dtbook-to-html/src/test/xprocspec/test_dtbook-to-html.xprocspec \
	modules/scripts/dtbook-to-html/src/test/xprocspec/test_dtbook-to-html.script.xprocspec
modules/scripts/dtbook-to-html/.install-doc : \
	modules/scripts/dtbook-to-html/doc/index.md
.make/mk/modules/scripts/dtbook-to-html/sources.mk : \
	modules/scripts/dtbook-to-html/src \
	modules/scripts/dtbook-to-html/src/test \
	modules/scripts/dtbook-to-html/src/test/resources \
	modules/scripts/dtbook-to-html/src/test/java \
	modules/scripts/dtbook-to-html/src/test/xprocspec \
	modules/scripts/dtbook-to-html/src/main \
	modules/scripts/dtbook-to-html/src/main/resources \
	modules/scripts/dtbook-to-html/src/main/resources/META-INF \
	modules/scripts/dtbook-to-html/src/main/resources/xml \
	modules/scripts/dtbook-to-html/doc
