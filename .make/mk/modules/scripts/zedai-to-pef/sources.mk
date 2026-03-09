modules/scripts/zedai-to-pef/.test modules/scripts/zedai-to-pef/.install modules/scripts/zedai-to-pef/.install-doc $(TARGET_DIR)/state/modules/scripts/zedai-to-pef/modified-since-release_ : \
	modules/scripts/zedai-to-pef/src/main/resources/css/reset.css \
	modules/scripts/zedai-to-pef/src/main/resources/css/bana.css \
	modules/scripts/zedai-to-pef/src/main/resources/css/default.css \
	modules/scripts/zedai-to-pef/src/main/resources/css/ueb.css \
	modules/scripts/zedai-to-pef/src/main/resources/META-INF/catalog.xml \
	modules/scripts/zedai-to-pef/src/main/resources/xml/xml-to-pef.store.xpl \
	modules/scripts/zedai-to-pef/src/main/resources/xml/zedai-to-pef.convert.xpl \
	modules/scripts/zedai-to-pef/src/main/resources/xml/zedai-to-pef.xpl \
	modules/scripts/zedai-to-pef/src/main/resources/xml/library.xpl \
	modules/scripts/zedai-to-pef/src/main/README.md
modules/scripts/zedai-to-pef/.test modules/scripts/zedai-to-pef/.install-doc : \
	modules/scripts/zedai-to-pef/src/test/resources/logback.xml \
	modules/scripts/zedai-to-pef/src/test/resources/alice.xml \
	modules/scripts/zedai-to-pef/src/test/resources/alice.css \
	modules/scripts/zedai-to-pef/src/test/java/ZedaiToPefTest.java \
	modules/scripts/zedai-to-pef/src/test/xprocspec/test_zedai-to-pef.script.xprocspec \
	modules/scripts/zedai-to-pef/src/test/xprocspec/test_zedai-to-pef.xprocspec
modules/scripts/zedai-to-pef/.install-doc : \
	modules/scripts/zedai-to-pef/doc/index.md
.make/mk/modules/scripts/zedai-to-pef/sources.mk : \
	modules/scripts/zedai-to-pef/src \
	modules/scripts/zedai-to-pef/src/test \
	modules/scripts/zedai-to-pef/src/test/resources \
	modules/scripts/zedai-to-pef/src/test/java \
	modules/scripts/zedai-to-pef/src/test/xprocspec \
	modules/scripts/zedai-to-pef/src/main \
	modules/scripts/zedai-to-pef/src/main/resources \
	modules/scripts/zedai-to-pef/src/main/resources/css \
	modules/scripts/zedai-to-pef/src/main/resources/META-INF \
	modules/scripts/zedai-to-pef/src/main/resources/xml \
	modules/scripts/zedai-to-pef/doc
