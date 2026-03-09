modules/tts/tts-adapter-azure/.test modules/tts/tts-adapter-azure/.install modules/tts/tts-adapter-azure/.install-doc $(TARGET_DIR)/state/modules/tts/tts-adapter-azure/modified-since-release_ : \
	modules/tts/tts-adapter-azure/src/main/resources/transform-ssml.xsl \
	modules/tts/tts-adapter-azure/src/main/java/org/daisy/pipeline/tts/azure/impl/AzureCognitiveSpeechService.java \
	modules/tts/tts-adapter-azure/src/main/java/org/daisy/pipeline/tts/azure/impl/AzureCognitiveSpeechEngine.java
modules/tts/tts-adapter-azure/.test modules/tts/tts-adapter-azure/.install-doc : \
	modules/tts/tts-adapter-azure/src/test/resources/logback.xml \
	modules/tts/tts-adapter-azure/src/test/java/ignore \
	modules/tts/tts-adapter-azure/src/test/java/org/daisy/pipeline/tts/azure/impl/AzureCognitiveSpeechTest.java
.make/mk/modules/tts/tts-adapter-azure/sources.mk : \
	modules/tts/tts-adapter-azure/src \
	modules/tts/tts-adapter-azure/src/test \
	modules/tts/tts-adapter-azure/src/test/resources \
	modules/tts/tts-adapter-azure/src/test/java \
	modules/tts/tts-adapter-azure/src/test/java/org \
	modules/tts/tts-adapter-azure/src/test/java/org/daisy \
	modules/tts/tts-adapter-azure/src/test/java/org/daisy/pipeline \
	modules/tts/tts-adapter-azure/src/test/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-azure/src/test/java/org/daisy/pipeline/tts/azure \
	modules/tts/tts-adapter-azure/src/test/java/org/daisy/pipeline/tts/azure/impl \
	modules/tts/tts-adapter-azure/src/main \
	modules/tts/tts-adapter-azure/src/main/resources \
	modules/tts/tts-adapter-azure/src/main/java \
	modules/tts/tts-adapter-azure/src/main/java/org \
	modules/tts/tts-adapter-azure/src/main/java/org/daisy \
	modules/tts/tts-adapter-azure/src/main/java/org/daisy/pipeline \
	modules/tts/tts-adapter-azure/src/main/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-azure/src/main/java/org/daisy/pipeline/tts/azure \
	modules/tts/tts-adapter-azure/src/main/java/org/daisy/pipeline/tts/azure/impl
