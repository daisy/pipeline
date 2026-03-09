modules/tts/tts-adapter-qfrency/.test modules/tts/tts-adapter-qfrency/.install modules/tts/tts-adapter-qfrency/.install-doc $(TARGET_DIR)/state/modules/tts/tts-adapter-qfrency/modified-since-release_ : \
	modules/tts/tts-adapter-qfrency/src/main/resources/transform-ssml.xsl \
	modules/tts/tts-adapter-qfrency/src/main/java/org/daisy/pipeline/tts/qfrency/impl/QfrencyEngine.java \
	modules/tts/tts-adapter-qfrency/src/main/java/org/daisy/pipeline/tts/qfrency/impl/QfrencyService.java
modules/tts/tts-adapter-qfrency/.test modules/tts/tts-adapter-qfrency/.install-doc : \
	modules/tts/tts-adapter-qfrency/src/test/java/ignore \
	modules/tts/tts-adapter-qfrency/src/test/java/org/daisy/pipeline/tts/qfrency/impl/QfrencyTest.java
.make/mk/modules/tts/tts-adapter-qfrency/sources.mk : \
	modules/tts/tts-adapter-qfrency/src \
	modules/tts/tts-adapter-qfrency/src/test \
	modules/tts/tts-adapter-qfrency/src/test/java \
	modules/tts/tts-adapter-qfrency/src/test/java/org \
	modules/tts/tts-adapter-qfrency/src/test/java/org/daisy \
	modules/tts/tts-adapter-qfrency/src/test/java/org/daisy/pipeline \
	modules/tts/tts-adapter-qfrency/src/test/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-qfrency/src/test/java/org/daisy/pipeline/tts/qfrency \
	modules/tts/tts-adapter-qfrency/src/test/java/org/daisy/pipeline/tts/qfrency/impl \
	modules/tts/tts-adapter-qfrency/src/main \
	modules/tts/tts-adapter-qfrency/src/main/resources \
	modules/tts/tts-adapter-qfrency/src/main/java \
	modules/tts/tts-adapter-qfrency/src/main/java/org \
	modules/tts/tts-adapter-qfrency/src/main/java/org/daisy \
	modules/tts/tts-adapter-qfrency/src/main/java/org/daisy/pipeline \
	modules/tts/tts-adapter-qfrency/src/main/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-qfrency/src/main/java/org/daisy/pipeline/tts/qfrency \
	modules/tts/tts-adapter-qfrency/src/main/java/org/daisy/pipeline/tts/qfrency/impl
