modules/tts/tts-mocks/.test modules/tts/tts-mocks/.install modules/tts/tts-mocks/.install-doc $(TARGET_DIR)/state/modules/tts/tts-mocks/modified-since-release_ : \
	modules/tts/tts-mocks/src/main/resources/mock-tts/daisy-pipeline.wav \
	modules/tts/tts-mocks/src/main/resources/mock-tts/vicki.wav \
	modules/tts/tts-mocks/src/main/resources/mock-tts/alex.wav \
	modules/tts/tts-mocks/src/main/java/org/daisy/pipeline/tts/mock/impl/MockGoogle.java \
	modules/tts/tts-mocks/src/main/java/org/daisy/pipeline/tts/mock/impl/MockTTS.java
.make/mk/modules/tts/tts-mocks/sources.mk : \
	modules/tts/tts-mocks/src \
	modules/tts/tts-mocks/src/main \
	modules/tts/tts-mocks/src/main/resources \
	modules/tts/tts-mocks/src/main/resources/mock-tts \
	modules/tts/tts-mocks/src/main/java \
	modules/tts/tts-mocks/src/main/java/org \
	modules/tts/tts-mocks/src/main/java/org/daisy \
	modules/tts/tts-mocks/src/main/java/org/daisy/pipeline \
	modules/tts/tts-mocks/src/main/java/org/daisy/pipeline/tts \
	modules/tts/tts-mocks/src/main/java/org/daisy/pipeline/tts/mock \
	modules/tts/tts-mocks/src/main/java/org/daisy/pipeline/tts/mock/impl
