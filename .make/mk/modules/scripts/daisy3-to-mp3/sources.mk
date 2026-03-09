modules/scripts/daisy3-to-mp3/.test modules/scripts/daisy3-to-mp3/.install modules/scripts/daisy3-to-mp3/.install-doc $(TARGET_DIR)/state/modules/scripts/daisy3-to-mp3/modified-since-release_ : \
	modules/scripts/daisy3-to-mp3/src/main/resources/META-INF/catalog.xml \
	modules/scripts/daisy3-to-mp3/src/main/resources/xml/daisy3-to-mp3.xpl \
	modules/scripts/daisy3-to-mp3/src/main/resources/xml/ncx-to-audio-mapping.xsl \
	modules/scripts/daisy3-to-mp3/src/main/resources/xml/daisy3-to-mp3.script.xpl
modules/scripts/daisy3-to-mp3/.test modules/scripts/daisy3-to-mp3/.install-doc : \
	modules/scripts/daisy3-to-mp3/src/test/resources/logback.xml \
	modules/scripts/daisy3-to-mp3/src/test/java/XProcSpecTest.java \
	modules/scripts/daisy3-to-mp3/src/test/xprocspec/test_daisy3-to-mp3.xprocspec
modules/scripts/daisy3-to-mp3/.install-doc : \
	modules/scripts/daisy3-to-mp3/doc/index.md
.make/mk/modules/scripts/daisy3-to-mp3/sources.mk : \
	modules/scripts/daisy3-to-mp3/src \
	modules/scripts/daisy3-to-mp3/src/test \
	modules/scripts/daisy3-to-mp3/src/test/resources \
	modules/scripts/daisy3-to-mp3/src/test/java \
	modules/scripts/daisy3-to-mp3/src/test/xprocspec \
	modules/scripts/daisy3-to-mp3/src/main \
	modules/scripts/daisy3-to-mp3/src/main/resources \
	modules/scripts/daisy3-to-mp3/src/main/resources/META-INF \
	modules/scripts/daisy3-to-mp3/src/main/resources/xml \
	modules/scripts/daisy3-to-mp3/doc
