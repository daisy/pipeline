modules/tts-adapter-qfrency/.test modules/tts-adapter-qfrency/.install modules/tts-adapter-qfrency/.install-doc $(TARGET_DIR)/state/modules/tts-adapter-qfrency/modified-since-release_ : \
	modules/tts-adapter-qfrency/src/main/resources/transform-ssml.xsl \
	modules/tts-adapter-qfrency/src/main/java/org/daisy/pipeline/tts/qfrency/impl/QfrencyEngine.java \
	modules/tts-adapter-qfrency/src/main/java/org/daisy/pipeline/tts/qfrency/impl/QfrencyService.java
modules/tts-adapter-qfrency/.test modules/tts-adapter-qfrency/.install-doc : \
	modules/tts-adapter-qfrency/src/test/java/ignore \
	modules/tts-adapter-qfrency/src/test/java/org/daisy/pipeline/tts/qfrency/impl/QfrencyTest.java
.make/mk/modules/tts-adapter-qfrency/sources.mk : \
	modules/tts-adapter-qfrency/src \
	modules/tts-adapter-qfrency/src/test \
	modules/tts-adapter-qfrency/src/test/java \
	modules/tts-adapter-qfrency/src/test/java/org \
	modules/tts-adapter-qfrency/src/test/java/org/daisy \
	modules/tts-adapter-qfrency/src/test/java/org/daisy/pipeline \
	modules/tts-adapter-qfrency/src/test/java/org/daisy/pipeline/tts \
	modules/tts-adapter-qfrency/src/test/java/org/daisy/pipeline/tts/qfrency \
	modules/tts-adapter-qfrency/src/test/java/org/daisy/pipeline/tts/qfrency/impl \
	modules/tts-adapter-qfrency/src/main \
	modules/tts-adapter-qfrency/src/main/resources \
	modules/tts-adapter-qfrency/src/main/java \
	modules/tts-adapter-qfrency/src/main/java/org \
	modules/tts-adapter-qfrency/src/main/java/org/daisy \
	modules/tts-adapter-qfrency/src/main/java/org/daisy/pipeline \
	modules/tts-adapter-qfrency/src/main/java/org/daisy/pipeline/tts \
	modules/tts-adapter-qfrency/src/main/java/org/daisy/pipeline/tts/qfrency \
	modules/tts-adapter-qfrency/src/main/java/org/daisy/pipeline/tts/qfrency/impl
