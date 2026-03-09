modules/scripts/daisy202-validator/.test modules/scripts/daisy202-validator/.install modules/scripts/daisy202-validator/.install-doc $(TARGET_DIR)/state/modules/scripts/daisy202-validator/modified-since-release_ : \
	modules/scripts/daisy202-validator/src/main/resources/META-INF/catalog.xml \
	modules/scripts/daisy202-validator/src/main/resources/xml/xproc/daisy202-validator.xpl \
	modules/scripts/daisy202-validator/src/main/resources/xml/xproc/steps/validate.xpl \
	modules/scripts/daisy202-validator/src/main/resources/xml/xproc/steps/validate.check-heading-hierarchy.xsl \
	modules/scripts/daisy202-validator/src/main/resources/xml/xproc/steps/validate.check-references.xsl \
	modules/scripts/daisy202-validator/src/main/resources/xml/xproc/steps/validate.smil-times-2.xsl \
	modules/scripts/daisy202-validator/src/main/resources/xml/xproc/steps/validate.smil-times-1.xsl \
	modules/scripts/daisy202-validator/src/main/resources/xml/xproc/library.xpl \
	modules/scripts/daisy202-validator/src/main/resources/xml/i18n.xml
modules/scripts/daisy202-validator/.test modules/scripts/daisy202-validator/.install-doc : \
	modules/scripts/daisy202-validator/src/test/resources/logback.xml \
	modules/scripts/daisy202-validator/src/test/java/XProcSpecTest.java \
	modules/scripts/daisy202-validator/src/test/xprocspec/test_daisy202-validator.script.xprocspec \
	modules/scripts/daisy202-validator/src/test/xprocspec/test_validate-ncc.xprocspec \
	modules/scripts/daisy202-validator/src/test/xprocspec/test_daisy202-validator.xprocspec
modules/scripts/daisy202-validator/.install-doc : \
	modules/scripts/daisy202-validator/doc/index.md
.make/mk/modules/scripts/daisy202-validator/sources.mk : \
	modules/scripts/daisy202-validator/src \
	modules/scripts/daisy202-validator/src/test \
	modules/scripts/daisy202-validator/src/test/resources \
	modules/scripts/daisy202-validator/src/test/java \
	modules/scripts/daisy202-validator/src/test/xprocspec \
	modules/scripts/daisy202-validator/src/main \
	modules/scripts/daisy202-validator/src/main/resources \
	modules/scripts/daisy202-validator/src/main/resources/META-INF \
	modules/scripts/daisy202-validator/src/main/resources/xml \
	modules/scripts/daisy202-validator/src/main/resources/xml/xproc \
	modules/scripts/daisy202-validator/src/main/resources/xml/xproc/steps \
	modules/scripts/daisy202-validator/doc
