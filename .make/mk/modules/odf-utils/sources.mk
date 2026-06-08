modules/odf-utils/.test modules/odf-utils/.install modules/odf-utils/.install-doc $(TARGET_DIR)/state/modules/odf-utils/modified-since-release_ : \
	modules/odf-utils/src/main/resources/META-INF/catalog.xml \
	modules/odf-utils/src/main/resources/xml/embed-images.xpl \
	modules/odf-utils/src/main/resources/xml/manifest-from-fileset.xsl \
	modules/odf-utils/src/main/resources/xml/manifest-from-fileset.xpl \
	modules/odf-utils/src/main/resources/xml/get-file.xpl \
	modules/odf-utils/src/main/resources/xml/store.xpl \
	modules/odf-utils/src/main/resources/xml/load.xpl \
	modules/odf-utils/src/main/resources/xml/manifest-to-fileset.xpl \
	modules/odf-utils/src/main/resources/xml/manifest-to-fileset.xsl \
	modules/odf-utils/src/main/resources/xml/separate-mathml.xpl \
	modules/odf-utils/src/main/resources/xml/library.xpl
modules/odf-utils/.test modules/odf-utils/.install-doc : \
	modules/odf-utils/src/test/resources/logback.xml \
	modules/odf-utils/src/test/resources/odt/test.odt \
	modules/odf-utils/src/test/resources/test.odt/settings.xml \
	modules/odf-utils/src/test/resources/test.odt/Configurations2/accelerator/current.xml \
	modules/odf-utils/src/test/resources/test.odt/content.xml \
	modules/odf-utils/src/test/resources/test.odt/meta.xml \
	modules/odf-utils/src/test/resources/test.odt/styles.xml \
	modules/odf-utils/src/test/resources/test.odt/manifest.rdf \
	modules/odf-utils/src/test/resources/test.odt/Thumbnails/thumbnail.png \
	modules/odf-utils/src/test/java/XProcSpecTest.java \
	modules/odf-utils/src/test/xprocspec/test_store.xprocspec \
	modules/odf-utils/src/test/xprocspec/test_load.xprocspec
.make/mk/modules/odf-utils/sources.mk : \
	modules/odf-utils/src \
	modules/odf-utils/src/test \
	modules/odf-utils/src/test/resources \
	modules/odf-utils/src/test/resources/odt \
	modules/odf-utils/src/test/resources/test.odt \
	modules/odf-utils/src/test/resources/test.odt/Configurations2 \
	modules/odf-utils/src/test/resources/test.odt/Configurations2/accelerator \
	modules/odf-utils/src/test/resources/test.odt/Thumbnails \
	modules/odf-utils/src/test/java \
	modules/odf-utils/src/test/xprocspec \
	modules/odf-utils/src/main \
	modules/odf-utils/src/main/resources \
	modules/odf-utils/src/main/resources/META-INF \
	modules/odf-utils/src/main/resources/xml
