modules/braille/obfl-utils/.test modules/braille/obfl-utils/.install modules/braille/obfl-utils/.install-doc $(TARGET_DIR)/state/modules/braille/obfl-utils/modified-since-release_ : \
	modules/braille/obfl-utils/src/main/resources/META-INF/catalog.xml \
	modules/braille/obfl-utils/src/main/resources/xml/compare.xpl \
	modules/braille/obfl-utils/src/main/resources/xml/x-compare.xpl \
	modules/braille/obfl-utils/src/main/resources/xml/library.xpl \
	modules/braille/obfl-utils/src/main/README.md
.make/mk/modules/braille/obfl-utils/sources.mk : \
	modules/braille/obfl-utils/src \
	modules/braille/obfl-utils/src/main \
	modules/braille/obfl-utils/src/main/resources \
	modules/braille/obfl-utils/src/main/resources/META-INF \
	modules/braille/obfl-utils/src/main/resources/xml
