modules/tts/tts-adapter-cereproc/.test modules/tts/tts-adapter-cereproc/.install modules/tts/tts-adapter-cereproc/.install-doc $(TARGET_DIR)/state/modules/tts/tts-adapter-cereproc/modified-since-release_ : \
	modules/tts/tts-adapter-cereproc/src/main/resources/charsubst/character-translation-table_en.xml \
	modules/tts/tts-adapter-cereproc/src/main/resources/charsubst/character-translation-table_sv.xml \
	modules/tts/tts-adapter-cereproc/src/main/resources/charsubst/character-translation-table.xml \
	modules/tts/tts-adapter-cereproc/src/main/resources/regex/cereproc_en.xml \
	modules/tts/tts-adapter-cereproc/src/main/resources/regex/cereproc_sv.xml \
	modules/tts/tts-adapter-cereproc/src/main/resources/transform-ssml.xsl \
	modules/tts/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc/impl/CereProcEngine.java \
	modules/tts/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc/impl/RegexReplace.java \
	modules/tts/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc/impl/CereProcService.java \
	modules/tts/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc/impl/CereprocTTSUtil.java \
	modules/tts/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc/impl/UCharReplacer.java
modules/tts/tts-adapter-cereproc/.test modules/tts/tts-adapter-cereproc/.install-doc : \
	modules/tts/tts-adapter-cereproc/src/test/resources/logback.xml \
	modules/tts/tts-adapter-cereproc/src/test/resources/Clientmock \
	modules/tts/tts-adapter-cereproc/src/test/java/XProcSpecTest.java \
	modules/tts/tts-adapter-cereproc/src/test/java/ignore \
	modules/tts/tts-adapter-cereproc/src/test/java/org/daisy/pipeline/tts/cereproc/impl/CereProcEngineTest.java \
	modules/tts/tts-adapter-cereproc/src/test/java/org/daisy/pipeline/tts/cereproc/impl/CereProcServiceTest.java \
	modules/tts/tts-adapter-cereproc/src/test/java/org/daisy/pipeline/tts/cereproc/impl/CereprocTTSUtilTest.java \
	modules/tts/tts-adapter-cereproc/src/test/xprocspec/test_ssml-to-audio.xprocspec \
	modules/tts/tts-adapter-cereproc/src/test/xprocspec/play-audio-clips.xpl
.make/mk/modules/tts/tts-adapter-cereproc/sources.mk : \
	modules/tts/tts-adapter-cereproc/src \
	modules/tts/tts-adapter-cereproc/src/test \
	modules/tts/tts-adapter-cereproc/src/test/resources \
	modules/tts/tts-adapter-cereproc/src/test/java \
	modules/tts/tts-adapter-cereproc/src/test/java/org \
	modules/tts/tts-adapter-cereproc/src/test/java/org/daisy \
	modules/tts/tts-adapter-cereproc/src/test/java/org/daisy/pipeline \
	modules/tts/tts-adapter-cereproc/src/test/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-cereproc/src/test/java/org/daisy/pipeline/tts/cereproc \
	modules/tts/tts-adapter-cereproc/src/test/java/org/daisy/pipeline/tts/cereproc/impl \
	modules/tts/tts-adapter-cereproc/src/test/xprocspec \
	modules/tts/tts-adapter-cereproc/src/main \
	modules/tts/tts-adapter-cereproc/src/main/resources \
	modules/tts/tts-adapter-cereproc/src/main/resources/charsubst \
	modules/tts/tts-adapter-cereproc/src/main/resources/regex \
	modules/tts/tts-adapter-cereproc/src/main/java \
	modules/tts/tts-adapter-cereproc/src/main/java/org \
	modules/tts/tts-adapter-cereproc/src/main/java/org/daisy \
	modules/tts/tts-adapter-cereproc/src/main/java/org/daisy/pipeline \
	modules/tts/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc \
	modules/tts/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc/impl
