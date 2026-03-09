modules/tts/tts-adapter-aws/.test modules/tts/tts-adapter-aws/.install modules/tts/tts-adapter-aws/.install-doc $(TARGET_DIR)/state/modules/tts/tts-adapter-aws/modified-since-release_ : \
	modules/tts/tts-adapter-aws/src/main/resources/transform-ssml.xsl \
	modules/tts/tts-adapter-aws/src/main/java/org/daisy/pipeline/tts/aws/impl/AWSPollyTTSService.java \
	modules/tts/tts-adapter-aws/src/main/java/org/daisy/pipeline/tts/aws/impl/AWSPollyTTSEngine.java
modules/tts/tts-adapter-aws/.test modules/tts/tts-adapter-aws/.install-doc : \
	modules/tts/tts-adapter-aws/src/test/resources/logback.xml \
	modules/tts/tts-adapter-aws/src/test/java/ignore \
	modules/tts/tts-adapter-aws/src/test/java/org/daisy/pipeline/tts/aws/impl/AWSPollyTTSServiceTest.java
.make/mk/modules/tts/tts-adapter-aws/sources.mk : \
	modules/tts/tts-adapter-aws/src \
	modules/tts/tts-adapter-aws/src/test \
	modules/tts/tts-adapter-aws/src/test/resources \
	modules/tts/tts-adapter-aws/src/test/java \
	modules/tts/tts-adapter-aws/src/test/java/org \
	modules/tts/tts-adapter-aws/src/test/java/org/daisy \
	modules/tts/tts-adapter-aws/src/test/java/org/daisy/pipeline \
	modules/tts/tts-adapter-aws/src/test/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-aws/src/test/java/org/daisy/pipeline/tts/aws \
	modules/tts/tts-adapter-aws/src/test/java/org/daisy/pipeline/tts/aws/impl \
	modules/tts/tts-adapter-aws/src/main \
	modules/tts/tts-adapter-aws/src/main/resources \
	modules/tts/tts-adapter-aws/src/main/java \
	modules/tts/tts-adapter-aws/src/main/java/org \
	modules/tts/tts-adapter-aws/src/main/java/org/daisy \
	modules/tts/tts-adapter-aws/src/main/java/org/daisy/pipeline \
	modules/tts/tts-adapter-aws/src/main/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-aws/src/main/java/org/daisy/pipeline/tts/aws \
	modules/tts/tts-adapter-aws/src/main/java/org/daisy/pipeline/tts/aws/impl
