modules/dtbook-to-ebraille/.test modules/dtbook-to-ebraille/.install modules/dtbook-to-ebraille/.install-doc $(TARGET_DIR)/state/modules/dtbook-to-ebraille/modified-since-release_ : \
	modules/dtbook-to-ebraille/src/main/resources/META-INF/catalog.xml \
	modules/dtbook-to-ebraille/src/main/resources/xml/get-used-braille-codes.xsl \
	modules/dtbook-to-ebraille/src/main/resources/xml/ebraille-metadata.xsl \
	modules/dtbook-to-ebraille/src/main/resources/xml/resource-map.xsl \
	modules/dtbook-to-ebraille/src/main/resources/xml/dtbook-to-ebraille.xpl \
	modules/dtbook-to-ebraille/src/main/resources/xml/process-primary-entry-page.xsl \
	modules/dtbook-to-ebraille/src/main/resources/xml/process-html.xsl \
	modules/dtbook-to-ebraille/src/main/resources/xml/dtbook-to-ebraille.script.xpl \
	modules/dtbook-to-ebraille/src/main/java/org/daisy/pipeline/dtbook_to_ebraille/calabash/impl/DTBookInsertSyncPointsStep.java
modules/dtbook-to-ebraille/.test modules/dtbook-to-ebraille/.install-doc : \
	modules/dtbook-to-ebraille/src/test/resources/logback.xml \
	modules/dtbook-to-ebraille/src/test/java/XProcSpecTest.java
modules/dtbook-to-ebraille/.install-doc : \
	modules/dtbook-to-ebraille/doc/index.md
.make/mk/modules/dtbook-to-ebraille/sources.mk : \
	modules/dtbook-to-ebraille/src \
	modules/dtbook-to-ebraille/src/test \
	modules/dtbook-to-ebraille/src/test/resources \
	modules/dtbook-to-ebraille/src/test/java \
	modules/dtbook-to-ebraille/src/main \
	modules/dtbook-to-ebraille/src/main/resources \
	modules/dtbook-to-ebraille/src/main/resources/META-INF \
	modules/dtbook-to-ebraille/src/main/resources/xml \
	modules/dtbook-to-ebraille/src/main/java \
	modules/dtbook-to-ebraille/src/main/java/org \
	modules/dtbook-to-ebraille/src/main/java/org/daisy \
	modules/dtbook-to-ebraille/src/main/java/org/daisy/pipeline \
	modules/dtbook-to-ebraille/src/main/java/org/daisy/pipeline/dtbook_to_ebraille \
	modules/dtbook-to-ebraille/src/main/java/org/daisy/pipeline/dtbook_to_ebraille/calabash \
	modules/dtbook-to-ebraille/src/main/java/org/daisy/pipeline/dtbook_to_ebraille/calabash/impl \
	modules/dtbook-to-ebraille/doc
