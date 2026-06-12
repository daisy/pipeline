modules/audio-encoder-lame/.test modules/audio-encoder-lame/.install modules/audio-encoder-lame/.install-doc $(TARGET_DIR)/state/modules/audio-encoder-lame/modified-since-release_ : \
	modules/audio-encoder-lame/src/main/resources/macosx/lame \
	modules/audio-encoder-lame/src/main/resources/windows_x86/lame_enc.dll \
	modules/audio-encoder-lame/src/main/resources/windows_x86/lame.exe \
	modules/audio-encoder-lame/src/main/java/org/daisy/pipeline/audio/lame/impl/LameEncoderService.java \
	modules/audio-encoder-lame/src/main/java/org/daisy/pipeline/audio/lame/impl/LameEncoder.java
modules/audio-encoder-lame/.test modules/audio-encoder-lame/.install-doc : \
	modules/audio-encoder-lame/src/test/resources/logback.xml \
	modules/audio-encoder-lame/src/test/resources/blah.wav \
	modules/audio-encoder-lame/src/test/java/org/daisy/pipeline/audio/lame/impl/LameTest.java
.make/mk/modules/audio-encoder-lame/sources.mk : \
	modules/audio-encoder-lame/src \
	modules/audio-encoder-lame/src/test \
	modules/audio-encoder-lame/src/test/resources \
	modules/audio-encoder-lame/src/test/java \
	modules/audio-encoder-lame/src/test/java/org \
	modules/audio-encoder-lame/src/test/java/org/daisy \
	modules/audio-encoder-lame/src/test/java/org/daisy/pipeline \
	modules/audio-encoder-lame/src/test/java/org/daisy/pipeline/audio \
	modules/audio-encoder-lame/src/test/java/org/daisy/pipeline/audio/lame \
	modules/audio-encoder-lame/src/test/java/org/daisy/pipeline/audio/lame/impl \
	modules/audio-encoder-lame/src/main \
	modules/audio-encoder-lame/src/main/resources \
	modules/audio-encoder-lame/src/main/resources/macosx \
	modules/audio-encoder-lame/src/main/resources/windows_x86 \
	modules/audio-encoder-lame/src/main/java \
	modules/audio-encoder-lame/src/main/java/org \
	modules/audio-encoder-lame/src/main/java/org/daisy \
	modules/audio-encoder-lame/src/main/java/org/daisy/pipeline \
	modules/audio-encoder-lame/src/main/java/org/daisy/pipeline/audio \
	modules/audio-encoder-lame/src/main/java/org/daisy/pipeline/audio/lame \
	modules/audio-encoder-lame/src/main/java/org/daisy/pipeline/audio/lame/impl
