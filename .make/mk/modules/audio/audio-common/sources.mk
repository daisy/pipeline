modules/audio/audio-common/.test modules/audio/audio-common/.install modules/audio/audio-common/.install-doc $(TARGET_DIR)/state/modules/audio/audio-common/modified-since-release_ : \
	modules/audio/audio-common/src/main/resources/META-INF/catalog.xml \
	modules/audio/audio-common/src/main/resources/xml/audio-rearrange.xpl \
	modules/audio/audio-common/src/main/resources/xml/audio-transcode.xpl \
	modules/audio/audio-common/src/main/resources/xml/audio-transcode.xsl \
	modules/audio/audio-common/src/main/resources/xml/library.xpl \
	modules/audio/audio-common/src/main/resources/xml/library.xsl \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/impl/SystemAudioEncoder.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/impl/JLayerMP3Decoder.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/impl/SystemAudioDecoder.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/AudioEncoder.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/AudioFileTypes.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/calabash/impl/AudioRearrangeStep.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/PCMAudioFormat.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/AudioEncoderService.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/AudioServices.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/AudioDecoder.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/saxon/impl/AudioErrors.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/saxon/impl/TranscodeAudioFileDefinition.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/AudioUtils.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/AudioClip.java \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/AudioDecoderService.java
modules/audio/audio-common/.test modules/audio/audio-common/.install-doc : \
	modules/audio/audio-common/src/test/resources/logback.xml \
	modules/audio/audio-common/src/test/resources/blah.mp3 \
	modules/audio/audio-common/src/test/resources/blah.wav \
	modules/audio/audio-common/src/test/java/XProcSpecTest.java \
	modules/audio/audio-common/src/test/java/ignore \
	modules/audio/audio-common/src/test/java/org/daisy/pipeline/audio/impl/WaveAudioEncoderTest.java \
	modules/audio/audio-common/src/test/java/org/daisy/pipeline/audio/AudioUtilsTest.java \
	modules/audio/audio-common/src/test/xprocspec/test_audio-rearrange.xprocspec \
	modules/audio/audio-common/src/test/xprocspec/test_audio-transcode.xprocspec
.make/mk/modules/audio/audio-common/sources.mk : \
	modules/audio/audio-common/src \
	modules/audio/audio-common/src/test \
	modules/audio/audio-common/src/test/resources \
	modules/audio/audio-common/src/test/java \
	modules/audio/audio-common/src/test/java/org \
	modules/audio/audio-common/src/test/java/org/daisy \
	modules/audio/audio-common/src/test/java/org/daisy/pipeline \
	modules/audio/audio-common/src/test/java/org/daisy/pipeline/audio \
	modules/audio/audio-common/src/test/java/org/daisy/pipeline/audio/impl \
	modules/audio/audio-common/src/test/xprocspec \
	modules/audio/audio-common/src/main \
	modules/audio/audio-common/src/main/resources \
	modules/audio/audio-common/src/main/resources/META-INF \
	modules/audio/audio-common/src/main/resources/xml \
	modules/audio/audio-common/src/main/java \
	modules/audio/audio-common/src/main/java/org \
	modules/audio/audio-common/src/main/java/org/daisy \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/impl \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/calabash \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/calabash/impl \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/saxon \
	modules/audio/audio-common/src/main/java/org/daisy/pipeline/audio/saxon/impl
