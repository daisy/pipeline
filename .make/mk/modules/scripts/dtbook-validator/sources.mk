modules/scripts/dtbook-validator/.test modules/scripts/dtbook-validator/.install modules/scripts/dtbook-validator/.install-doc $(TARGET_DIR)/state/modules/scripts/dtbook-validator/modified-since-release_ : \
	modules/scripts/dtbook-validator/src/main/resources/META-INF/catalog.xml \
	modules/scripts/dtbook-validator/src/main/resources/xml/dtbook-validator.store.xpl \
	modules/scripts/dtbook-validator/src/main/resources/xml/dtbook-validator.xpl
modules/scripts/dtbook-validator/.test modules/scripts/dtbook-validator/.install-doc : \
	modules/scripts/dtbook-validator/src/test/resources/logback.xml \
	modules/scripts/dtbook-validator/src/test/resources/MathML_Sample2.xml \
	modules/scripts/dtbook-validator/src/test/resources/nativemathml.xml \
	modules/scripts/dtbook-validator/src/test/resources/dtbook_valid_2005-3.xml \
	modules/scripts/dtbook-validator/src/test/resources/dtbook_valid_2005-2.xml \
	modules/scripts/dtbook-validator/src/test/java/XProcSpecTest.java \
	modules/scripts/dtbook-validator/src/test/xprocspec/test_dtbook-validator.script.xprocspec
modules/scripts/dtbook-validator/.install-doc : \
	modules/scripts/dtbook-validator/doc/dev-notes.md \
	modules/scripts/dtbook-validator/doc/index.md
.make/mk/modules/scripts/dtbook-validator/sources.mk : \
	modules/scripts/dtbook-validator/src \
	modules/scripts/dtbook-validator/src/test \
	modules/scripts/dtbook-validator/src/test/resources \
	modules/scripts/dtbook-validator/src/test/java \
	modules/scripts/dtbook-validator/src/test/xprocspec \
	modules/scripts/dtbook-validator/src/main \
	modules/scripts/dtbook-validator/src/main/resources \
	modules/scripts/dtbook-validator/src/main/resources/META-INF \
	modules/scripts/dtbook-validator/src/main/resources/xml \
	modules/scripts/dtbook-validator/doc
