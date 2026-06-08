modules/zip-utils/.test modules/zip-utils/.install modules/zip-utils/.install-doc $(TARGET_DIR)/state/modules/zip-utils/modified-since-release_ : \
	modules/zip-utils/src/main/resources/config-calabash.xml \
	modules/zip-utils/src/main/resources/META-INF/catalog.xml \
	modules/zip-utils/src/main/resources/xml/xproc/zip-library.xpl \
	modules/zip-utils/src/main/java/org/daisy/pipeline/zip/calabash/impl/UnZipProvider.java
modules/zip-utils/.test modules/zip-utils/.install-doc : \
	modules/zip-utils/src/test/resources/logback.xml \
	modules/zip-utils/src/test/java/XProcSpecTest.java \
	modules/zip-utils/src/test/xprocspec/test_zip.xprocspec
.make/mk/modules/zip-utils/sources.mk : \
	modules/zip-utils/src \
	modules/zip-utils/src/test \
	modules/zip-utils/src/test/resources \
	modules/zip-utils/src/test/java \
	modules/zip-utils/src/test/xprocspec \
	modules/zip-utils/src/main \
	modules/zip-utils/src/main/resources \
	modules/zip-utils/src/main/resources/META-INF \
	modules/zip-utils/src/main/resources/xml \
	modules/zip-utils/src/main/resources/xml/xproc \
	modules/zip-utils/src/main/java \
	modules/zip-utils/src/main/java/org \
	modules/zip-utils/src/main/java/org/daisy \
	modules/zip-utils/src/main/java/org/daisy/pipeline \
	modules/zip-utils/src/main/java/org/daisy/pipeline/zip \
	modules/zip-utils/src/main/java/org/daisy/pipeline/zip/calabash \
	modules/zip-utils/src/main/java/org/daisy/pipeline/zip/calabash/impl
