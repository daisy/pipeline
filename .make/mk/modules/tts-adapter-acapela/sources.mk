modules/tts-adapter-acapela/.test modules/tts-adapter-acapela/.install modules/tts-adapter-acapela/.install-doc $(TARGET_DIR)/state/modules/tts-adapter-acapela/modified-since-release_ : \
	modules/tts-adapter-acapela/src/main/resources/transform-ssml.xsl \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_EVENT_DATA_TextStarted.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_EVENT_DATA_TextDone.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/AcapelaService.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_SOUND_DATA.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NsCubeLoader.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_EVENT_DATA_WordSynch.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_FINDVOICE_DATA.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/JNAClassLoader.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/AcapelaEngine.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_EVENT_DATA_Bookmark.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_EVENT_DATA_PhoSynch.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_EVENT_DATA_TtsError.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_EXEC_DATA.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_EVENT_DATA.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_EVENT_DATA_MouthPos.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NscubeLibrary.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_EVENT_DATA_BookmarkExt.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_SRVSTATUS_DATA.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_SRVSTATUS_DATAEX.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_EVENT_DATA_PhoSynchExt.java \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl/NSC_SRVINFO_DATA.java
modules/tts-adapter-acapela/.test modules/tts-adapter-acapela/.install-doc : \
	modules/tts-adapter-acapela/src/test/resources/decimal_chars.txt \
	modules/tts-adapter-acapela/src/test/resources/logback.xml \
	modules/tts-adapter-acapela/src/test/java/ignore \
	modules/tts-adapter-acapela/src/test/java/org/daisy/pipeline/tts/acapela/impl/AcapelaTest.java \
	modules/tts-adapter-acapela/src/test/java/org/daisy/pipeline/tts/acapela/impl/AcapelaSSMLTest.java
.make/mk/modules/tts-adapter-acapela/sources.mk : \
	modules/tts-adapter-acapela/src \
	modules/tts-adapter-acapela/src/test \
	modules/tts-adapter-acapela/src/test/resources \
	modules/tts-adapter-acapela/src/test/java \
	modules/tts-adapter-acapela/src/test/java/org \
	modules/tts-adapter-acapela/src/test/java/org/daisy \
	modules/tts-adapter-acapela/src/test/java/org/daisy/pipeline \
	modules/tts-adapter-acapela/src/test/java/org/daisy/pipeline/tts \
	modules/tts-adapter-acapela/src/test/java/org/daisy/pipeline/tts/acapela \
	modules/tts-adapter-acapela/src/test/java/org/daisy/pipeline/tts/acapela/impl \
	modules/tts-adapter-acapela/src/main \
	modules/tts-adapter-acapela/src/main/resources \
	modules/tts-adapter-acapela/src/main/java \
	modules/tts-adapter-acapela/src/main/java/org \
	modules/tts-adapter-acapela/src/main/java/org/daisy \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela \
	modules/tts-adapter-acapela/src/main/java/org/daisy/pipeline/tts/acapela/impl
