modules/scripts/epub3-validator/.test modules/scripts/epub3-validator/.install modules/scripts/epub3-validator/.install-doc $(TARGET_DIR)/state/modules/scripts/epub3-validator/modified-since-release_ : \
	modules/scripts/epub3-validator/src/main/resources/META-INF/catalog.xml \
	modules/scripts/epub3-validator/src/main/resources/xml/xproc/epub3-validator.xpl
modules/scripts/epub3-validator/.test modules/scripts/epub3-validator/.install-doc : \
	modules/scripts/epub3-validator/src/test/resources/logback.xml \
	modules/scripts/epub3-validator/src/test/java/XProcSpecTest.java \
	modules/scripts/epub3-validator/src/test/xprocspec/test.xprocspec
modules/scripts/epub3-validator/.install-doc : \
	modules/scripts/epub3-validator/doc/index.md
.make/mk/modules/scripts/epub3-validator/sources.mk : \
	modules/scripts/epub3-validator/src \
	modules/scripts/epub3-validator/src/test \
	modules/scripts/epub3-validator/src/test/resources \
	modules/scripts/epub3-validator/src/test/java \
	modules/scripts/epub3-validator/src/test/xprocspec \
	modules/scripts/epub3-validator/src/main \
	modules/scripts/epub3-validator/src/main/resources \
	modules/scripts/epub3-validator/src/main/resources/META-INF \
	modules/scripts/epub3-validator/src/main/resources/xml \
	modules/scripts/epub3-validator/src/main/resources/xml/xproc \
	modules/scripts/epub3-validator/doc
