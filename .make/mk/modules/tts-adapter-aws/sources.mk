modules/tts-adapter-aws/.test modules/tts-adapter-aws/.install modules/tts-adapter-aws/.install-doc $(TARGET_DIR)/state/modules/tts-adapter-aws/modified-since-release_ : \
	modules/tts-adapter-aws/src/main/resources/transform-ssml.xsl \
	modules/tts-adapter-aws/src/main/java/org/daisy/pipeline/tts/aws/impl/AWSPollyTTSService.java \
	modules/tts-adapter-aws/src/main/java/org/daisy/pipeline/tts/aws/impl/AWSPollyTTSEngine.java
modules/tts-adapter-aws/.test modules/tts-adapter-aws/.install-doc : \
	modules/tts-adapter-aws/src/test/resources/logback.xml \
	modules/tts-adapter-aws/src/test/java/ignore \
	modules/tts-adapter-aws/src/test/java/org/daisy/pipeline/tts/aws/impl/AWSPollyTTSServiceTest.java
.make/mk/modules/tts-adapter-aws/sources.mk : \
	modules/tts-adapter-aws/src \
	modules/tts-adapter-aws/src/test \
	modules/tts-adapter-aws/src/test/resources \
	modules/tts-adapter-aws/src/test/java \
	modules/tts-adapter-aws/src/test/java/org \
	modules/tts-adapter-aws/src/test/java/org/daisy \
	modules/tts-adapter-aws/src/test/java/org/daisy/pipeline \
	modules/tts-adapter-aws/src/test/java/org/daisy/pipeline/tts \
	modules/tts-adapter-aws/src/test/java/org/daisy/pipeline/tts/aws \
	modules/tts-adapter-aws/src/test/java/org/daisy/pipeline/tts/aws/impl \
	modules/tts-adapter-aws/src/main \
	modules/tts-adapter-aws/src/main/resources \
	modules/tts-adapter-aws/src/main/java \
	modules/tts-adapter-aws/src/main/java/org \
	modules/tts-adapter-aws/src/main/java/org/daisy \
	modules/tts-adapter-aws/src/main/java/org/daisy/pipeline \
	modules/tts-adapter-aws/src/main/java/org/daisy/pipeline/tts \
	modules/tts-adapter-aws/src/main/java/org/daisy/pipeline/tts/aws \
	modules/tts-adapter-aws/src/main/java/org/daisy/pipeline/tts/aws/impl
