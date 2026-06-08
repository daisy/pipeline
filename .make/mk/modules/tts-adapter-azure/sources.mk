modules/tts-adapter-azure/.test modules/tts-adapter-azure/.install modules/tts-adapter-azure/.install-doc $(TARGET_DIR)/state/modules/tts-adapter-azure/modified-since-release_ : \
	modules/tts-adapter-azure/src/main/resources/transform-ssml.xsl \
	modules/tts-adapter-azure/src/main/java/org/daisy/pipeline/tts/azure/impl/AzureCognitiveSpeechService.java \
	modules/tts-adapter-azure/src/main/java/org/daisy/pipeline/tts/azure/impl/AzureCognitiveSpeechEngine.java
modules/tts-adapter-azure/.test modules/tts-adapter-azure/.install-doc : \
	modules/tts-adapter-azure/src/test/resources/logback.xml \
	modules/tts-adapter-azure/src/test/java/ignore \
	modules/tts-adapter-azure/src/test/java/org/daisy/pipeline/tts/azure/impl/AzureCognitiveSpeechTest.java
.make/mk/modules/tts-adapter-azure/sources.mk : \
	modules/tts-adapter-azure/src \
	modules/tts-adapter-azure/src/test \
	modules/tts-adapter-azure/src/test/resources \
	modules/tts-adapter-azure/src/test/java \
	modules/tts-adapter-azure/src/test/java/org \
	modules/tts-adapter-azure/src/test/java/org/daisy \
	modules/tts-adapter-azure/src/test/java/org/daisy/pipeline \
	modules/tts-adapter-azure/src/test/java/org/daisy/pipeline/tts \
	modules/tts-adapter-azure/src/test/java/org/daisy/pipeline/tts/azure \
	modules/tts-adapter-azure/src/test/java/org/daisy/pipeline/tts/azure/impl \
	modules/tts-adapter-azure/src/main \
	modules/tts-adapter-azure/src/main/resources \
	modules/tts-adapter-azure/src/main/java \
	modules/tts-adapter-azure/src/main/java/org \
	modules/tts-adapter-azure/src/main/java/org/daisy \
	modules/tts-adapter-azure/src/main/java/org/daisy/pipeline \
	modules/tts-adapter-azure/src/main/java/org/daisy/pipeline/tts \
	modules/tts-adapter-azure/src/main/java/org/daisy/pipeline/tts/azure \
	modules/tts-adapter-azure/src/main/java/org/daisy/pipeline/tts/azure/impl
