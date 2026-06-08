modules/tts-adapter-cereproc/.test modules/tts-adapter-cereproc/.install modules/tts-adapter-cereproc/.install-doc $(TARGET_DIR)/state/modules/tts-adapter-cereproc/modified-since-release_ : \
	modules/tts-adapter-cereproc/src/main/resources/charsubst/character-translation-table_en.xml \
	modules/tts-adapter-cereproc/src/main/resources/charsubst/character-translation-table_sv.xml \
	modules/tts-adapter-cereproc/src/main/resources/charsubst/character-translation-table.xml \
	modules/tts-adapter-cereproc/src/main/resources/regex/cereproc_en.xml \
	modules/tts-adapter-cereproc/src/main/resources/regex/cereproc_sv.xml \
	modules/tts-adapter-cereproc/src/main/resources/transform-ssml.xsl \
	modules/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc/impl/CereProcEngine.java \
	modules/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc/impl/RegexReplace.java \
	modules/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc/impl/CereProcService.java \
	modules/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc/impl/CereprocTTSUtil.java \
	modules/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc/impl/UCharReplacer.java
modules/tts-adapter-cereproc/.test modules/tts-adapter-cereproc/.install-doc : \
	modules/tts-adapter-cereproc/src/test/resources/logback.xml \
	modules/tts-adapter-cereproc/src/test/resources/Clientmock \
	modules/tts-adapter-cereproc/src/test/java/XProcSpecTest.java \
	modules/tts-adapter-cereproc/src/test/java/ignore \
	modules/tts-adapter-cereproc/src/test/java/org/daisy/pipeline/tts/cereproc/impl/CereProcEngineTest.java \
	modules/tts-adapter-cereproc/src/test/java/org/daisy/pipeline/tts/cereproc/impl/CereProcServiceTest.java \
	modules/tts-adapter-cereproc/src/test/java/org/daisy/pipeline/tts/cereproc/impl/CereprocTTSUtilTest.java \
	modules/tts-adapter-cereproc/src/test/xprocspec/test_ssml-to-audio.xprocspec \
	modules/tts-adapter-cereproc/src/test/xprocspec/play-audio-clips.xpl
.make/mk/modules/tts-adapter-cereproc/sources.mk : \
	modules/tts-adapter-cereproc/src \
	modules/tts-adapter-cereproc/src/test \
	modules/tts-adapter-cereproc/src/test/resources \
	modules/tts-adapter-cereproc/src/test/java \
	modules/tts-adapter-cereproc/src/test/java/org \
	modules/tts-adapter-cereproc/src/test/java/org/daisy \
	modules/tts-adapter-cereproc/src/test/java/org/daisy/pipeline \
	modules/tts-adapter-cereproc/src/test/java/org/daisy/pipeline/tts \
	modules/tts-adapter-cereproc/src/test/java/org/daisy/pipeline/tts/cereproc \
	modules/tts-adapter-cereproc/src/test/java/org/daisy/pipeline/tts/cereproc/impl \
	modules/tts-adapter-cereproc/src/test/xprocspec \
	modules/tts-adapter-cereproc/src/main \
	modules/tts-adapter-cereproc/src/main/resources \
	modules/tts-adapter-cereproc/src/main/resources/charsubst \
	modules/tts-adapter-cereproc/src/main/resources/regex \
	modules/tts-adapter-cereproc/src/main/java \
	modules/tts-adapter-cereproc/src/main/java/org \
	modules/tts-adapter-cereproc/src/main/java/org/daisy \
	modules/tts-adapter-cereproc/src/main/java/org/daisy/pipeline \
	modules/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts \
	modules/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc \
	modules/tts-adapter-cereproc/src/main/java/org/daisy/pipeline/tts/cereproc/impl
