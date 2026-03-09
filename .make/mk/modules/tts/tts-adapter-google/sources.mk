modules/tts/tts-adapter-google/.test modules/tts/tts-adapter-google/.install modules/tts/tts-adapter-google/.install-doc $(TARGET_DIR)/state/modules/tts/tts-adapter-google/modified-since-release_ : \
	modules/tts/tts-adapter-google/src/main/resources/transform-ssml.xsl \
	modules/tts/tts-adapter-google/src/main/java/org/daisy/pipeline/tts/google/impl/GoogleRestAction.java \
	modules/tts/tts-adapter-google/src/main/java/org/daisy/pipeline/tts/google/impl/GoogleRequestBuilder.java \
	modules/tts/tts-adapter-google/src/main/java/org/daisy/pipeline/tts/google/impl/GoogleTTSService.java \
	modules/tts/tts-adapter-google/src/main/java/org/daisy/pipeline/tts/google/impl/GoogleRestTTSEngine.java
modules/tts/tts-adapter-google/.test modules/tts/tts-adapter-google/.install-doc : \
	modules/tts/tts-adapter-google/src/test/resources/logback.xml \
	modules/tts/tts-adapter-google/src/test/java/org/daisy/pipeline/tts/google/impl/GoogleTTSTest.java
.make/mk/modules/tts/tts-adapter-google/sources.mk : \
	modules/tts/tts-adapter-google/src \
	modules/tts/tts-adapter-google/src/test \
	modules/tts/tts-adapter-google/src/test/resources \
	modules/tts/tts-adapter-google/src/test/java \
	modules/tts/tts-adapter-google/src/test/java/org \
	modules/tts/tts-adapter-google/src/test/java/org/daisy \
	modules/tts/tts-adapter-google/src/test/java/org/daisy/pipeline \
	modules/tts/tts-adapter-google/src/test/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-google/src/test/java/org/daisy/pipeline/tts/google \
	modules/tts/tts-adapter-google/src/test/java/org/daisy/pipeline/tts/google/impl \
	modules/tts/tts-adapter-google/src/main \
	modules/tts/tts-adapter-google/src/main/resources \
	modules/tts/tts-adapter-google/src/main/java \
	modules/tts/tts-adapter-google/src/main/java/org \
	modules/tts/tts-adapter-google/src/main/java/org/daisy \
	modules/tts/tts-adapter-google/src/main/java/org/daisy/pipeline \
	modules/tts/tts-adapter-google/src/main/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-google/src/main/java/org/daisy/pipeline/tts/google \
	modules/tts/tts-adapter-google/src/main/java/org/daisy/pipeline/tts/google/impl
