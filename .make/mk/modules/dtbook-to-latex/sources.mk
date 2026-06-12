modules/dtbook-to-latex/.test modules/dtbook-to-latex/.install modules/dtbook-to-latex/.install-doc $(TARGET_DIR)/state/modules/dtbook-to-latex/modified-since-release_ : \
	modules/dtbook-to-latex/src/main/resources/META-INF/catalog.xml \
	modules/dtbook-to-latex/src/main/resources/xml/table-utils.xsl \
	modules/dtbook-to-latex/src/main/resources/xml/dtbook-to-latex.script.xpl \
	modules/dtbook-to-latex/src/main/resources/xml/unicode-blocks.xml \
	modules/dtbook-to-latex/src/main/resources/xml/dtbook-to-latex.xsl
modules/dtbook-to-latex/.test modules/dtbook-to-latex/.install-doc : \
	modules/dtbook-to-latex/src/test/resources/logback.xml
.make/mk/modules/dtbook-to-latex/sources.mk : \
	modules/dtbook-to-latex/src \
	modules/dtbook-to-latex/src/test \
	modules/dtbook-to-latex/src/test/resources \
	modules/dtbook-to-latex/src/main \
	modules/dtbook-to-latex/src/main/resources \
	modules/dtbook-to-latex/src/main/resources/META-INF \
	modules/dtbook-to-latex/src/main/resources/xml
