modules/metadata-utils/.test modules/metadata-utils/.install modules/metadata-utils/.install-doc $(TARGET_DIR)/state/modules/metadata-utils/modified-since-release_ : \
	modules/metadata-utils/src/main/resources/META-INF/catalog.xml \
	modules/metadata-utils/src/main/resources/xml/metadata-utils-library.xpl \
	modules/metadata-utils/src/main/resources/xml/validate-mods.xpl \
	modules/metadata-utils/src/main/resources/xml/schema/mods-3-3.xsd \
	modules/metadata-utils/src/main/resources/xml/schema/xlink.xsd \
	modules/metadata-utils/src/main/resources/xml/schema/xml.xsd
modules/metadata-utils/.test modules/metadata-utils/.install-doc : \
	modules/metadata-utils/src/test/resources/logback.xml \
	modules/metadata-utils/src/test/java/XProcSpecTest.java \
	modules/metadata-utils/src/test/xprocspec/test_validate-mods.xprocspec
.make/mk/modules/metadata-utils/sources.mk : \
	modules/metadata-utils/src \
	modules/metadata-utils/src/test \
	modules/metadata-utils/src/test/resources \
	modules/metadata-utils/src/test/java \
	modules/metadata-utils/src/test/xprocspec \
	modules/metadata-utils/src/main \
	modules/metadata-utils/src/main/resources \
	modules/metadata-utils/src/main/resources/META-INF \
	modules/metadata-utils/src/main/resources/xml \
	modules/metadata-utils/src/main/resources/xml/schema
