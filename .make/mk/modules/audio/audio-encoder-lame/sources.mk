modules/audio/audio-encoder-lame/.test modules/audio/audio-encoder-lame/.install modules/audio/audio-encoder-lame/.install-doc $(TARGET_DIR)/state/modules/audio/audio-encoder-lame/modified-since-release_ : \
	modules/audio/audio-encoder-lame/src/main/resources/macosx/lame \
	modules/audio/audio-encoder-lame/src/main/resources/windows_x86/lame_enc.dll \
	modules/audio/audio-encoder-lame/src/main/resources/windows_x86/lame.exe \
	modules/audio/audio-encoder-lame/src/main/java/org/daisy/pipeline/audio/lame/impl/LameEncoderService.java \
	modules/audio/audio-encoder-lame/src/main/java/org/daisy/pipeline/audio/lame/impl/LameEncoder.java
modules/audio/audio-encoder-lame/.test modules/audio/audio-encoder-lame/.install-doc : \
	modules/audio/audio-encoder-lame/src/test/resources/logback.xml \
	modules/audio/audio-encoder-lame/src/test/resources/blah.wav \
	modules/audio/audio-encoder-lame/src/test/java/org/daisy/pipeline/audio/lame/impl/LameTest.java
.make/mk/modules/audio/audio-encoder-lame/sources.mk : \
	modules/audio/audio-encoder-lame/src \
	modules/audio/audio-encoder-lame/src/test \
	modules/audio/audio-encoder-lame/src/test/resources \
	modules/audio/audio-encoder-lame/src/test/java \
	modules/audio/audio-encoder-lame/src/test/java/org \
	modules/audio/audio-encoder-lame/src/test/java/org/daisy \
	modules/audio/audio-encoder-lame/src/test/java/org/daisy/pipeline \
	modules/audio/audio-encoder-lame/src/test/java/org/daisy/pipeline/audio \
	modules/audio/audio-encoder-lame/src/test/java/org/daisy/pipeline/audio/lame \
	modules/audio/audio-encoder-lame/src/test/java/org/daisy/pipeline/audio/lame/impl \
	modules/audio/audio-encoder-lame/src/main \
	modules/audio/audio-encoder-lame/src/main/resources \
	modules/audio/audio-encoder-lame/src/main/resources/macosx \
	modules/audio/audio-encoder-lame/src/main/resources/windows_x86 \
	modules/audio/audio-encoder-lame/src/main/java \
	modules/audio/audio-encoder-lame/src/main/java/org \
	modules/audio/audio-encoder-lame/src/main/java/org/daisy \
	modules/audio/audio-encoder-lame/src/main/java/org/daisy/pipeline \
	modules/audio/audio-encoder-lame/src/main/java/org/daisy/pipeline/audio \
	modules/audio/audio-encoder-lame/src/main/java/org/daisy/pipeline/audio/lame \
	modules/audio/audio-encoder-lame/src/main/java/org/daisy/pipeline/audio/lame/impl
