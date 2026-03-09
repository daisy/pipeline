modules/tts/tts-adapter-espeak/.test modules/tts/tts-adapter-espeak/.install modules/tts/tts-adapter-espeak/.install-doc $(TARGET_DIR)/state/modules/tts/tts-adapter-espeak/modified-since-release_ : \
	modules/tts/tts-adapter-espeak/src/main/resources/transform-ssml.xsl \
	modules/tts/tts-adapter-espeak/src/main/java/org/daisy/pipeline/tts/espeak/impl/ESpeakService.java \
	modules/tts/tts-adapter-espeak/src/main/java/org/daisy/pipeline/tts/espeak/impl/ESpeakEngine.java
modules/tts/tts-adapter-espeak/.test modules/tts/tts-adapter-espeak/.install-doc : \
	modules/tts/tts-adapter-espeak/src/test/resources/logback.xml \
	modules/tts/tts-adapter-espeak/src/test/java/XProcSpecTest.java \
	modules/tts/tts-adapter-espeak/src/test/java/org/daisy/pipeline/tts/espeak/impl/EspeakSSMLTest.java \
	modules/tts/tts-adapter-espeak/src/test/java/org/daisy/pipeline/tts/espeak/impl/EspeakTest.java \
	modules/tts/tts-adapter-espeak/src/test/xprocspec/test_ssml-to-audio.xprocspec \
	modules/tts/tts-adapter-espeak/src/test/xprocspec/play-audio-clips.xpl
.make/mk/modules/tts/tts-adapter-espeak/sources.mk : \
	modules/tts/tts-adapter-espeak/src \
	modules/tts/tts-adapter-espeak/src/test \
	modules/tts/tts-adapter-espeak/src/test/resources \
	modules/tts/tts-adapter-espeak/src/test/java \
	modules/tts/tts-adapter-espeak/src/test/java/org \
	modules/tts/tts-adapter-espeak/src/test/java/org/daisy \
	modules/tts/tts-adapter-espeak/src/test/java/org/daisy/pipeline \
	modules/tts/tts-adapter-espeak/src/test/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-espeak/src/test/java/org/daisy/pipeline/tts/espeak \
	modules/tts/tts-adapter-espeak/src/test/java/org/daisy/pipeline/tts/espeak/impl \
	modules/tts/tts-adapter-espeak/src/test/xprocspec \
	modules/tts/tts-adapter-espeak/src/main \
	modules/tts/tts-adapter-espeak/src/main/resources \
	modules/tts/tts-adapter-espeak/src/main/java \
	modules/tts/tts-adapter-espeak/src/main/java/org \
	modules/tts/tts-adapter-espeak/src/main/java/org/daisy \
	modules/tts/tts-adapter-espeak/src/main/java/org/daisy/pipeline \
	modules/tts/tts-adapter-espeak/src/main/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-espeak/src/main/java/org/daisy/pipeline/tts/espeak \
	modules/tts/tts-adapter-espeak/src/main/java/org/daisy/pipeline/tts/espeak/impl
