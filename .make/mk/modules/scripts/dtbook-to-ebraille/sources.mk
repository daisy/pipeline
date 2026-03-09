modules/scripts/dtbook-to-ebraille/.test modules/scripts/dtbook-to-ebraille/.install modules/scripts/dtbook-to-ebraille/.install-doc $(TARGET_DIR)/state/modules/scripts/dtbook-to-ebraille/modified-since-release_ : \
	modules/scripts/dtbook-to-ebraille/src/main/resources/META-INF/catalog.xml \
	modules/scripts/dtbook-to-ebraille/src/main/resources/xml/get-used-braille-codes.xsl \
	modules/scripts/dtbook-to-ebraille/src/main/resources/xml/ebraille-metadata.xsl \
	modules/scripts/dtbook-to-ebraille/src/main/resources/xml/resource-map.xsl \
	modules/scripts/dtbook-to-ebraille/src/main/resources/xml/dtbook-to-ebraille.xpl \
	modules/scripts/dtbook-to-ebraille/src/main/resources/xml/process-primary-entry-page.xsl \
	modules/scripts/dtbook-to-ebraille/src/main/resources/xml/process-html.xsl \
	modules/scripts/dtbook-to-ebraille/src/main/resources/xml/dtbook-to-ebraille.script.xpl \
	modules/scripts/dtbook-to-ebraille/src/main/java/org/daisy/pipeline/dtbook_to_ebraille/calabash/impl/DTBookInsertSyncPointsStep.java
modules/scripts/dtbook-to-ebraille/.test modules/scripts/dtbook-to-ebraille/.install-doc : \
	modules/scripts/dtbook-to-ebraille/src/test/resources/logback.xml \
	modules/scripts/dtbook-to-ebraille/src/test/java/XProcSpecTest.java
modules/scripts/dtbook-to-ebraille/.install-doc : \
	modules/scripts/dtbook-to-ebraille/doc/index.md
.make/mk/modules/scripts/dtbook-to-ebraille/sources.mk : \
	modules/scripts/dtbook-to-ebraille/src \
	modules/scripts/dtbook-to-ebraille/src/test \
	modules/scripts/dtbook-to-ebraille/src/test/resources \
	modules/scripts/dtbook-to-ebraille/src/test/java \
	modules/scripts/dtbook-to-ebraille/src/main \
	modules/scripts/dtbook-to-ebraille/src/main/resources \
	modules/scripts/dtbook-to-ebraille/src/main/resources/META-INF \
	modules/scripts/dtbook-to-ebraille/src/main/resources/xml \
	modules/scripts/dtbook-to-ebraille/src/main/java \
	modules/scripts/dtbook-to-ebraille/src/main/java/org \
	modules/scripts/dtbook-to-ebraille/src/main/java/org/daisy \
	modules/scripts/dtbook-to-ebraille/src/main/java/org/daisy/pipeline \
	modules/scripts/dtbook-to-ebraille/src/main/java/org/daisy/pipeline/dtbook_to_ebraille \
	modules/scripts/dtbook-to-ebraille/src/main/java/org/daisy/pipeline/dtbook_to_ebraille/calabash \
	modules/scripts/dtbook-to-ebraille/src/main/java/org/daisy/pipeline/dtbook_to_ebraille/calabash/impl \
	modules/scripts/dtbook-to-ebraille/doc
