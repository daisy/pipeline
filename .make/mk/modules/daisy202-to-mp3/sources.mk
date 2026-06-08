modules/daisy202-to-mp3/.test modules/daisy202-to-mp3/.install modules/daisy202-to-mp3/.install-doc $(TARGET_DIR)/state/modules/daisy202-to-mp3/modified-since-release_ : \
	modules/daisy202-to-mp3/src/main/resources/META-INF/catalog.xml \
	modules/daisy202-to-mp3/src/main/resources/xml/daisy202-to-mp3.xpl \
	modules/daisy202-to-mp3/src/main/resources/xml/ncc-to-audio-mapping.xsl \
	modules/daisy202-to-mp3/src/main/resources/xml/daisy202-to-mp3.script.xpl
modules/daisy202-to-mp3/.test modules/daisy202-to-mp3/.install-doc : \
	modules/daisy202-to-mp3/src/test/resources/logback.xml \
	modules/daisy202-to-mp3/src/test/java/XProcSpecTest.java \
	modules/daisy202-to-mp3/src/test/xprocspec/test_daisy202-to-mp3.xprocspec
modules/daisy202-to-mp3/.install-doc : \
	modules/daisy202-to-mp3/doc/index.md
.make/mk/modules/daisy202-to-mp3/sources.mk : \
	modules/daisy202-to-mp3/src \
	modules/daisy202-to-mp3/src/test \
	modules/daisy202-to-mp3/src/test/resources \
	modules/daisy202-to-mp3/src/test/java \
	modules/daisy202-to-mp3/src/test/xprocspec \
	modules/daisy202-to-mp3/src/main \
	modules/daisy202-to-mp3/src/main/resources \
	modules/daisy202-to-mp3/src/main/resources/META-INF \
	modules/daisy202-to-mp3/src/main/resources/xml \
	modules/daisy202-to-mp3/doc
