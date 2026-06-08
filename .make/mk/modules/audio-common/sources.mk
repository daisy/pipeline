modules/audio-common/.test modules/audio-common/.install modules/audio-common/.install-doc $(TARGET_DIR)/state/modules/audio-common/modified-since-release_ : \
	modules/audio-common/src/main/resources/META-INF/catalog.xml \
	modules/audio-common/src/main/resources/xml/audio-rearrange.xpl \
	modules/audio-common/src/main/resources/xml/audio-transcode.xpl \
	modules/audio-common/src/main/resources/xml/audio-transcode.xsl \
	modules/audio-common/src/main/resources/xml/library.xpl \
	modules/audio-common/src/main/resources/xml/library.xsl \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/impl/SystemAudioEncoder.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/impl/JLayerMP3Decoder.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/impl/SystemAudioDecoder.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/AudioEncoder.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/AudioFileTypes.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/calabash/impl/AudioRearrangeStep.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/PCMAudioFormat.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/AudioEncoderService.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/AudioServices.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/AudioDecoder.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/saxon/impl/AudioErrors.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/saxon/impl/TranscodeAudioFileDefinition.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/AudioUtils.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/AudioClip.java \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/AudioDecoderService.java
modules/audio-common/.test modules/audio-common/.install-doc : \
	modules/audio-common/src/test/resources/logback.xml \
	modules/audio-common/src/test/resources/blah.mp3 \
	modules/audio-common/src/test/resources/blah.wav \
	modules/audio-common/src/test/java/XProcSpecTest.java \
	modules/audio-common/src/test/java/ignore \
	modules/audio-common/src/test/java/org/daisy/pipeline/audio/impl/WaveAudioEncoderTest.java \
	modules/audio-common/src/test/java/org/daisy/pipeline/audio/AudioUtilsTest.java \
	modules/audio-common/src/test/xprocspec/test_audio-rearrange.xprocspec \
	modules/audio-common/src/test/xprocspec/test_audio-transcode.xprocspec
.make/mk/modules/audio-common/sources.mk : \
	modules/audio-common/src \
	modules/audio-common/src/test \
	modules/audio-common/src/test/resources \
	modules/audio-common/src/test/java \
	modules/audio-common/src/test/java/org \
	modules/audio-common/src/test/java/org/daisy \
	modules/audio-common/src/test/java/org/daisy/pipeline \
	modules/audio-common/src/test/java/org/daisy/pipeline/audio \
	modules/audio-common/src/test/java/org/daisy/pipeline/audio/impl \
	modules/audio-common/src/test/xprocspec \
	modules/audio-common/src/main \
	modules/audio-common/src/main/resources \
	modules/audio-common/src/main/resources/META-INF \
	modules/audio-common/src/main/resources/xml \
	modules/audio-common/src/main/java \
	modules/audio-common/src/main/java/org \
	modules/audio-common/src/main/java/org/daisy \
	modules/audio-common/src/main/java/org/daisy/pipeline \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/impl \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/calabash \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/calabash/impl \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/saxon \
	modules/audio-common/src/main/java/org/daisy/pipeline/audio/saxon/impl
