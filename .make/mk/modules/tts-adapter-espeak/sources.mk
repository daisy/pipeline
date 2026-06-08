modules/tts-adapter-espeak/.test modules/tts-adapter-espeak/.install modules/tts-adapter-espeak/.install-doc $(TARGET_DIR)/state/modules/tts-adapter-espeak/modified-since-release_ : \
	modules/tts-adapter-espeak/src/main/resources/transform-ssml.xsl \
	modules/tts-adapter-espeak/src/main/java/org/daisy/pipeline/tts/espeak/impl/ESpeakService.java \
	modules/tts-adapter-espeak/src/main/java/org/daisy/pipeline/tts/espeak/impl/ESpeakEngine.java
modules/tts-adapter-espeak/.test modules/tts-adapter-espeak/.install-doc : \
	modules/tts-adapter-espeak/src/test/resources/logback.xml \
	modules/tts-adapter-espeak/src/test/java/XProcSpecTest.java \
	modules/tts-adapter-espeak/src/test/java/org/daisy/pipeline/tts/espeak/impl/EspeakSSMLTest.java \
	modules/tts-adapter-espeak/src/test/java/org/daisy/pipeline/tts/espeak/impl/EspeakTest.java \
	modules/tts-adapter-espeak/src/test/xprocspec/test_ssml-to-audio.xprocspec \
	modules/tts-adapter-espeak/src/test/xprocspec/play-audio-clips.xpl
.make/mk/modules/tts-adapter-espeak/sources.mk : \
	modules/tts-adapter-espeak/src \
	modules/tts-adapter-espeak/src/test \
	modules/tts-adapter-espeak/src/test/resources \
	modules/tts-adapter-espeak/src/test/java \
	modules/tts-adapter-espeak/src/test/java/org \
	modules/tts-adapter-espeak/src/test/java/org/daisy \
	modules/tts-adapter-espeak/src/test/java/org/daisy/pipeline \
	modules/tts-adapter-espeak/src/test/java/org/daisy/pipeline/tts \
	modules/tts-adapter-espeak/src/test/java/org/daisy/pipeline/tts/espeak \
	modules/tts-adapter-espeak/src/test/java/org/daisy/pipeline/tts/espeak/impl \
	modules/tts-adapter-espeak/src/test/xprocspec \
	modules/tts-adapter-espeak/src/main \
	modules/tts-adapter-espeak/src/main/resources \
	modules/tts-adapter-espeak/src/main/java \
	modules/tts-adapter-espeak/src/main/java/org \
	modules/tts-adapter-espeak/src/main/java/org/daisy \
	modules/tts-adapter-espeak/src/main/java/org/daisy/pipeline \
	modules/tts-adapter-espeak/src/main/java/org/daisy/pipeline/tts \
	modules/tts-adapter-espeak/src/main/java/org/daisy/pipeline/tts/espeak \
	modules/tts-adapter-espeak/src/main/java/org/daisy/pipeline/tts/espeak/impl
