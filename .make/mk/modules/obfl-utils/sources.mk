modules/obfl-utils/.test modules/obfl-utils/.install modules/obfl-utils/.install-doc $(TARGET_DIR)/state/modules/obfl-utils/modified-since-release_ : \
	modules/obfl-utils/src/main/resources/META-INF/catalog.xml \
	modules/obfl-utils/src/main/resources/xml/compare.xpl \
	modules/obfl-utils/src/main/resources/xml/x-compare.xpl \
	modules/obfl-utils/src/main/resources/xml/library.xpl \
	modules/obfl-utils/src/main/README.md
.make/mk/modules/obfl-utils/sources.mk : \
	modules/obfl-utils/src \
	modules/obfl-utils/src/main \
	modules/obfl-utils/src/main/resources \
	modules/obfl-utils/src/main/resources/META-INF \
	modules/obfl-utils/src/main/resources/xml
