modules/tts-mocks/.test modules/tts-mocks/.install modules/tts-mocks/.install-doc $(TARGET_DIR)/state/modules/tts-mocks/modified-since-release_ : \
	modules/tts-mocks/src/main/resources/mock-tts/daisy-pipeline.wav \
	modules/tts-mocks/src/main/resources/mock-tts/vicki.wav \
	modules/tts-mocks/src/main/resources/mock-tts/alex.wav \
	modules/tts-mocks/src/main/java/org/daisy/pipeline/tts/mock/impl/MockGoogle.java \
	modules/tts-mocks/src/main/java/org/daisy/pipeline/tts/mock/impl/MockTTS.java
.make/mk/modules/tts-mocks/sources.mk : \
	modules/tts-mocks/src \
	modules/tts-mocks/src/main \
	modules/tts-mocks/src/main/resources \
	modules/tts-mocks/src/main/resources/mock-tts \
	modules/tts-mocks/src/main/java \
	modules/tts-mocks/src/main/java/org \
	modules/tts-mocks/src/main/java/org/daisy \
	modules/tts-mocks/src/main/java/org/daisy/pipeline \
	modules/tts-mocks/src/main/java/org/daisy/pipeline/tts \
	modules/tts-mocks/src/main/java/org/daisy/pipeline/tts/mock \
	modules/tts-mocks/src/main/java/org/daisy/pipeline/tts/mock/impl
