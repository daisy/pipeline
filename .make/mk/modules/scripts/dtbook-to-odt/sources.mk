modules/scripts/dtbook-to-odt/.test modules/scripts/dtbook-to-odt/.install modules/scripts/dtbook-to-odt/.install-doc $(TARGET_DIR)/state/modules/scripts/dtbook-to-odt/modified-since-release_ : \
	modules/scripts/dtbook-to-odt/src/main/resources/META-INF/catalog.xml \
	modules/scripts/dtbook-to-odt/src/main/resources/xml/utilities.xsl \
	modules/scripts/dtbook-to-odt/src/main/resources/xml/dtbook-to-odt.convert.xpl \
	modules/scripts/dtbook-to-odt/src/main/resources/xml/automatic-styles.xsl \
	modules/scripts/dtbook-to-odt/src/main/resources/xml/content.xsl \
	modules/scripts/dtbook-to-odt/src/main/resources/xml/dtbook-to-odt.xpl \
	modules/scripts/dtbook-to-odt/src/main/resources/xml/meta.xsl \
	modules/scripts/dtbook-to-odt/src/main/resources/xml/styles.xsl \
	modules/scripts/dtbook-to-odt/src/main/resources/xml/library.xpl \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/settings.xml \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/Configurations2/accelerator/current.xml \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/content.xml \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/meta.xml \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/META-INF/manifest.xml \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/styles.xml \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/mimetype \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/manifest.rdf \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/Thumbnails/thumbnail.png
modules/scripts/dtbook-to-odt/.test modules/scripts/dtbook-to-odt/.install-doc : \
	modules/scripts/dtbook-to-odt/src/test/resources/logback.xml \
	modules/scripts/dtbook-to-odt/src/test/java/XProcSpecTest.java \
	modules/scripts/dtbook-to-odt/src/test/xprocspec/orion3.jpg \
	modules/scripts/dtbook-to-odt/src/test/xprocspec/test_content.xprocspec.xsl \
	modules/scripts/dtbook-to-odt/src/test/xprocspec/serialize.xsl \
	modules/scripts/dtbook-to-odt/src/test/xprocspec/test_dtbook-to-odt.xprocspec \
	modules/scripts/dtbook-to-odt/src/test/xprocspec/test_content.xprocspec \
	modules/scripts/dtbook-to-odt/src/test/xprocspec/test_content.xprocspec.css \
	modules/scripts/dtbook-to-odt/src/test/xprocspec/orion62.jpg \
	modules/scripts/dtbook-to-odt/src/test/xprocspec/test_dtbook-to-odt.script.xprocspec \
	modules/scripts/dtbook-to-odt/src/test/xprocspec/generate_template.xprocspec
modules/scripts/dtbook-to-odt/.install-doc : \
	modules/scripts/dtbook-to-odt/doc/index.md \
	modules/scripts/dtbook-to-odt/doc/templates/default.ott.html
.make/mk/modules/scripts/dtbook-to-odt/sources.mk : \
	modules/scripts/dtbook-to-odt/src \
	modules/scripts/dtbook-to-odt/src/test \
	modules/scripts/dtbook-to-odt/src/test/resources \
	modules/scripts/dtbook-to-odt/src/test/java \
	modules/scripts/dtbook-to-odt/src/test/xprocspec \
	modules/scripts/dtbook-to-odt/src/main \
	modules/scripts/dtbook-to-odt/src/main/resources \
	modules/scripts/dtbook-to-odt/src/main/resources/META-INF \
	modules/scripts/dtbook-to-odt/src/main/resources/xml \
	modules/scripts/dtbook-to-odt/src/main/resources/templates \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/Configurations2 \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/Configurations2/accelerator \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/META-INF \
	modules/scripts/dtbook-to-odt/src/main/resources/templates/default.ott/Thumbnails \
	modules/scripts/dtbook-to-odt/doc \
	modules/scripts/dtbook-to-odt/doc/templates
