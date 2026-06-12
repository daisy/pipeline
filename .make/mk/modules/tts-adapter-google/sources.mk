modules/tts-adapter-google/.test modules/tts-adapter-google/.install modules/tts-adapter-google/.install-doc $(TARGET_DIR)/state/modules/tts-adapter-google/modified-since-release_ : \
	modules/tts-adapter-google/src/main/resources/transform-ssml.xsl \
	modules/tts-adapter-google/src/main/java/org/daisy/pipeline/tts/google/impl/GoogleRestAction.java \
	modules/tts-adapter-google/src/main/java/org/daisy/pipeline/tts/google/impl/GoogleRequestBuilder.java \
	modules/tts-adapter-google/src/main/java/org/daisy/pipeline/tts/google/impl/GoogleTTSService.java \
	modules/tts-adapter-google/src/main/java/org/daisy/pipeline/tts/google/impl/GoogleRestTTSEngine.java
modules/tts-adapter-google/.test modules/tts-adapter-google/.install-doc : \
	modules/tts-adapter-google/src/test/resources/logback.xml \
	modules/tts-adapter-google/src/test/java/org/daisy/pipeline/tts/google/impl/GoogleTTSTest.java
.make/mk/modules/tts-adapter-google/sources.mk : \
	modules/tts-adapter-google/src \
	modules/tts-adapter-google/src/test \
	modules/tts-adapter-google/src/test/resources \
	modules/tts-adapter-google/src/test/java \
	modules/tts-adapter-google/src/test/java/org \
	modules/tts-adapter-google/src/test/java/org/daisy \
	modules/tts-adapter-google/src/test/java/org/daisy/pipeline \
	modules/tts-adapter-google/src/test/java/org/daisy/pipeline/tts \
	modules/tts-adapter-google/src/test/java/org/daisy/pipeline/tts/google \
	modules/tts-adapter-google/src/test/java/org/daisy/pipeline/tts/google/impl \
	modules/tts-adapter-google/src/main \
	modules/tts-adapter-google/src/main/resources \
	modules/tts-adapter-google/src/main/java \
	modules/tts-adapter-google/src/main/java/org \
	modules/tts-adapter-google/src/main/java/org/daisy \
	modules/tts-adapter-google/src/main/java/org/daisy/pipeline \
	modules/tts-adapter-google/src/main/java/org/daisy/pipeline/tts \
	modules/tts-adapter-google/src/main/java/org/daisy/pipeline/tts/google \
	modules/tts-adapter-google/src/main/java/org/daisy/pipeline/tts/google/impl
