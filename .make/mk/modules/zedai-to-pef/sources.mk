modules/zedai-to-pef/.test modules/zedai-to-pef/.install modules/zedai-to-pef/.install-doc $(TARGET_DIR)/state/modules/zedai-to-pef/modified-since-release_ : \
	modules/zedai-to-pef/src/main/resources/css/reset.css \
	modules/zedai-to-pef/src/main/resources/css/bana.css \
	modules/zedai-to-pef/src/main/resources/css/default.css \
	modules/zedai-to-pef/src/main/resources/css/ueb.css \
	modules/zedai-to-pef/src/main/resources/META-INF/catalog.xml \
	modules/zedai-to-pef/src/main/resources/xml/xml-to-pef.store.xpl \
	modules/zedai-to-pef/src/main/resources/xml/zedai-to-pef.convert.xpl \
	modules/zedai-to-pef/src/main/resources/xml/zedai-to-pef.xpl \
	modules/zedai-to-pef/src/main/resources/xml/library.xpl \
	modules/zedai-to-pef/src/main/README.md
modules/zedai-to-pef/.test modules/zedai-to-pef/.install-doc : \
	modules/zedai-to-pef/src/test/resources/logback.xml \
	modules/zedai-to-pef/src/test/resources/alice.xml \
	modules/zedai-to-pef/src/test/resources/alice.css \
	modules/zedai-to-pef/src/test/java/ZedaiToPefTest.java \
	modules/zedai-to-pef/src/test/xprocspec/test_zedai-to-pef.script.xprocspec \
	modules/zedai-to-pef/src/test/xprocspec/test_zedai-to-pef.xprocspec
modules/zedai-to-pef/.install-doc : \
	modules/zedai-to-pef/doc/index.md
.make/mk/modules/zedai-to-pef/sources.mk : \
	modules/zedai-to-pef/src \
	modules/zedai-to-pef/src/test \
	modules/zedai-to-pef/src/test/resources \
	modules/zedai-to-pef/src/test/java \
	modules/zedai-to-pef/src/test/xprocspec \
	modules/zedai-to-pef/src/main \
	modules/zedai-to-pef/src/main/resources \
	modules/zedai-to-pef/src/main/resources/css \
	modules/zedai-to-pef/src/main/resources/META-INF \
	modules/zedai-to-pef/src/main/resources/xml \
	modules/zedai-to-pef/doc
