modules/common/zip-utils/.test modules/common/zip-utils/.install modules/common/zip-utils/.install-doc $(TARGET_DIR)/state/modules/common/zip-utils/modified-since-release_ : \
	modules/common/zip-utils/src/main/resources/config-calabash.xml \
	modules/common/zip-utils/src/main/resources/META-INF/catalog.xml \
	modules/common/zip-utils/src/main/resources/xml/xproc/zip-library.xpl \
	modules/common/zip-utils/src/main/java/org/daisy/pipeline/zip/calabash/impl/UnZipProvider.java
modules/common/zip-utils/.test modules/common/zip-utils/.install-doc : \
	modules/common/zip-utils/src/test/resources/logback.xml \
	modules/common/zip-utils/src/test/java/XProcSpecTest.java \
	modules/common/zip-utils/src/test/xprocspec/test_zip.xprocspec
.make/mk/modules/common/zip-utils/sources.mk : \
	modules/common/zip-utils/src \
	modules/common/zip-utils/src/test \
	modules/common/zip-utils/src/test/resources \
	modules/common/zip-utils/src/test/java \
	modules/common/zip-utils/src/test/xprocspec \
	modules/common/zip-utils/src/main \
	modules/common/zip-utils/src/main/resources \
	modules/common/zip-utils/src/main/resources/META-INF \
	modules/common/zip-utils/src/main/resources/xml \
	modules/common/zip-utils/src/main/resources/xml/xproc \
	modules/common/zip-utils/src/main/java \
	modules/common/zip-utils/src/main/java/org \
	modules/common/zip-utils/src/main/java/org/daisy \
	modules/common/zip-utils/src/main/java/org/daisy/pipeline \
	modules/common/zip-utils/src/main/java/org/daisy/pipeline/zip \
	modules/common/zip-utils/src/main/java/org/daisy/pipeline/zip/calabash \
	modules/common/zip-utils/src/main/java/org/daisy/pipeline/zip/calabash/impl
