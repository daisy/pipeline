modules/scripts-utils/metadata-utils/.test modules/scripts-utils/metadata-utils/.install modules/scripts-utils/metadata-utils/.install-doc $(TARGET_DIR)/state/modules/scripts-utils/metadata-utils/modified-since-release_ : \
	modules/scripts-utils/metadata-utils/src/main/resources/META-INF/catalog.xml \
	modules/scripts-utils/metadata-utils/src/main/resources/xml/metadata-utils-library.xpl \
	modules/scripts-utils/metadata-utils/src/main/resources/xml/validate-mods.xpl \
	modules/scripts-utils/metadata-utils/src/main/resources/xml/schema/mods-3-3.xsd \
	modules/scripts-utils/metadata-utils/src/main/resources/xml/schema/xlink.xsd \
	modules/scripts-utils/metadata-utils/src/main/resources/xml/schema/xml.xsd
modules/scripts-utils/metadata-utils/.test modules/scripts-utils/metadata-utils/.install-doc : \
	modules/scripts-utils/metadata-utils/src/test/resources/logback.xml \
	modules/scripts-utils/metadata-utils/src/test/java/XProcSpecTest.java \
	modules/scripts-utils/metadata-utils/src/test/xprocspec/test_validate-mods.xprocspec
.make/mk/modules/scripts-utils/metadata-utils/sources.mk : \
	modules/scripts-utils/metadata-utils/src \
	modules/scripts-utils/metadata-utils/src/test \
	modules/scripts-utils/metadata-utils/src/test/resources \
	modules/scripts-utils/metadata-utils/src/test/java \
	modules/scripts-utils/metadata-utils/src/test/xprocspec \
	modules/scripts-utils/metadata-utils/src/main \
	modules/scripts-utils/metadata-utils/src/main/resources \
	modules/scripts-utils/metadata-utils/src/main/resources/META-INF \
	modules/scripts-utils/metadata-utils/src/main/resources/xml \
	modules/scripts-utils/metadata-utils/src/main/resources/xml/schema
