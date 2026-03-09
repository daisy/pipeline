modules/scripts-utils/odf-utils/.test modules/scripts-utils/odf-utils/.install modules/scripts-utils/odf-utils/.install-doc $(TARGET_DIR)/state/modules/scripts-utils/odf-utils/modified-since-release_ : \
	modules/scripts-utils/odf-utils/src/main/resources/META-INF/catalog.xml \
	modules/scripts-utils/odf-utils/src/main/resources/xml/embed-images.xpl \
	modules/scripts-utils/odf-utils/src/main/resources/xml/manifest-from-fileset.xsl \
	modules/scripts-utils/odf-utils/src/main/resources/xml/manifest-from-fileset.xpl \
	modules/scripts-utils/odf-utils/src/main/resources/xml/get-file.xpl \
	modules/scripts-utils/odf-utils/src/main/resources/xml/store.xpl \
	modules/scripts-utils/odf-utils/src/main/resources/xml/load.xpl \
	modules/scripts-utils/odf-utils/src/main/resources/xml/manifest-to-fileset.xpl \
	modules/scripts-utils/odf-utils/src/main/resources/xml/manifest-to-fileset.xsl \
	modules/scripts-utils/odf-utils/src/main/resources/xml/separate-mathml.xpl \
	modules/scripts-utils/odf-utils/src/main/resources/xml/library.xpl
modules/scripts-utils/odf-utils/.test modules/scripts-utils/odf-utils/.install-doc : \
	modules/scripts-utils/odf-utils/src/test/resources/logback.xml \
	modules/scripts-utils/odf-utils/src/test/resources/odt/test.odt \
	modules/scripts-utils/odf-utils/src/test/resources/test.odt/settings.xml \
	modules/scripts-utils/odf-utils/src/test/resources/test.odt/Configurations2/accelerator/current.xml \
	modules/scripts-utils/odf-utils/src/test/resources/test.odt/content.xml \
	modules/scripts-utils/odf-utils/src/test/resources/test.odt/meta.xml \
	modules/scripts-utils/odf-utils/src/test/resources/test.odt/styles.xml \
	modules/scripts-utils/odf-utils/src/test/resources/test.odt/manifest.rdf \
	modules/scripts-utils/odf-utils/src/test/resources/test.odt/Thumbnails/thumbnail.png \
	modules/scripts-utils/odf-utils/src/test/java/XProcSpecTest.java \
	modules/scripts-utils/odf-utils/src/test/xprocspec/test_store.xprocspec \
	modules/scripts-utils/odf-utils/src/test/xprocspec/test_load.xprocspec
.make/mk/modules/scripts-utils/odf-utils/sources.mk : \
	modules/scripts-utils/odf-utils/src \
	modules/scripts-utils/odf-utils/src/test \
	modules/scripts-utils/odf-utils/src/test/resources \
	modules/scripts-utils/odf-utils/src/test/resources/odt \
	modules/scripts-utils/odf-utils/src/test/resources/test.odt \
	modules/scripts-utils/odf-utils/src/test/resources/test.odt/Configurations2 \
	modules/scripts-utils/odf-utils/src/test/resources/test.odt/Configurations2/accelerator \
	modules/scripts-utils/odf-utils/src/test/resources/test.odt/Thumbnails \
	modules/scripts-utils/odf-utils/src/test/java \
	modules/scripts-utils/odf-utils/src/test/xprocspec \
	modules/scripts-utils/odf-utils/src/main \
	modules/scripts-utils/odf-utils/src/main/resources \
	modules/scripts-utils/odf-utils/src/main/resources/META-INF \
	modules/scripts-utils/odf-utils/src/main/resources/xml
