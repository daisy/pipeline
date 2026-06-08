modules/dtbook-to-odt/.test modules/dtbook-to-odt/.install modules/dtbook-to-odt/.install-doc $(TARGET_DIR)/state/modules/dtbook-to-odt/modified-since-release_ : \
	modules/dtbook-to-odt/src/main/resources/META-INF/catalog.xml \
	modules/dtbook-to-odt/src/main/resources/xml/utilities.xsl \
	modules/dtbook-to-odt/src/main/resources/xml/dtbook-to-odt.convert.xpl \
	modules/dtbook-to-odt/src/main/resources/xml/automatic-styles.xsl \
	modules/dtbook-to-odt/src/main/resources/xml/content.xsl \
	modules/dtbook-to-odt/src/main/resources/xml/dtbook-to-odt.xpl \
	modules/dtbook-to-odt/src/main/resources/xml/meta.xsl \
	modules/dtbook-to-odt/src/main/resources/xml/styles.xsl \
	modules/dtbook-to-odt/src/main/resources/xml/library.xpl \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/settings.xml \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/Configurations2/accelerator/current.xml \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/content.xml \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/meta.xml \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/META-INF/manifest.xml \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/styles.xml \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/mimetype \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/manifest.rdf \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/Thumbnails/thumbnail.png
modules/dtbook-to-odt/.test modules/dtbook-to-odt/.install-doc : \
	modules/dtbook-to-odt/src/test/resources/logback.xml \
	modules/dtbook-to-odt/src/test/java/XProcSpecTest.java \
	modules/dtbook-to-odt/src/test/xprocspec/orion3.jpg \
	modules/dtbook-to-odt/src/test/xprocspec/test_content.xprocspec.xsl \
	modules/dtbook-to-odt/src/test/xprocspec/serialize.xsl \
	modules/dtbook-to-odt/src/test/xprocspec/test_dtbook-to-odt.xprocspec \
	modules/dtbook-to-odt/src/test/xprocspec/test_content.xprocspec \
	modules/dtbook-to-odt/src/test/xprocspec/test_content.xprocspec.css \
	modules/dtbook-to-odt/src/test/xprocspec/orion62.jpg \
	modules/dtbook-to-odt/src/test/xprocspec/test_dtbook-to-odt.script.xprocspec \
	modules/dtbook-to-odt/src/test/xprocspec/generate_template.xprocspec
modules/dtbook-to-odt/.install-doc : \
	modules/dtbook-to-odt/doc/index.md \
	modules/dtbook-to-odt/doc/templates/default.ott.html
.make/mk/modules/dtbook-to-odt/sources.mk : \
	modules/dtbook-to-odt/src \
	modules/dtbook-to-odt/src/test \
	modules/dtbook-to-odt/src/test/resources \
	modules/dtbook-to-odt/src/test/java \
	modules/dtbook-to-odt/src/test/xprocspec \
	modules/dtbook-to-odt/src/main \
	modules/dtbook-to-odt/src/main/resources \
	modules/dtbook-to-odt/src/main/resources/META-INF \
	modules/dtbook-to-odt/src/main/resources/xml \
	modules/dtbook-to-odt/src/main/resources/templates \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/Configurations2 \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/Configurations2/accelerator \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/META-INF \
	modules/dtbook-to-odt/src/main/resources/templates/default.ott/Thumbnails \
	modules/dtbook-to-odt/doc \
	modules/dtbook-to-odt/doc/templates
