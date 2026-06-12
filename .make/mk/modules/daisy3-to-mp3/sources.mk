modules/daisy3-to-mp3/.test modules/daisy3-to-mp3/.install modules/daisy3-to-mp3/.install-doc $(TARGET_DIR)/state/modules/daisy3-to-mp3/modified-since-release_ : \
	modules/daisy3-to-mp3/src/main/resources/META-INF/catalog.xml \
	modules/daisy3-to-mp3/src/main/resources/xml/daisy3-to-mp3.xpl \
	modules/daisy3-to-mp3/src/main/resources/xml/ncx-to-audio-mapping.xsl \
	modules/daisy3-to-mp3/src/main/resources/xml/daisy3-to-mp3.script.xpl
modules/daisy3-to-mp3/.test modules/daisy3-to-mp3/.install-doc : \
	modules/daisy3-to-mp3/src/test/resources/logback.xml \
	modules/daisy3-to-mp3/src/test/java/XProcSpecTest.java \
	modules/daisy3-to-mp3/src/test/xprocspec/test_daisy3-to-mp3.xprocspec
modules/daisy3-to-mp3/.install-doc : \
	modules/daisy3-to-mp3/doc/index.md
.make/mk/modules/daisy3-to-mp3/sources.mk : \
	modules/daisy3-to-mp3/src \
	modules/daisy3-to-mp3/src/test \
	modules/daisy3-to-mp3/src/test/resources \
	modules/daisy3-to-mp3/src/test/java \
	modules/daisy3-to-mp3/src/test/xprocspec \
	modules/daisy3-to-mp3/src/main \
	modules/daisy3-to-mp3/src/main/resources \
	modules/daisy3-to-mp3/src/main/resources/META-INF \
	modules/daisy3-to-mp3/src/main/resources/xml \
	modules/daisy3-to-mp3/doc
