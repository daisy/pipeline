modules/tts/tts-adapter-sapinative/.test modules/tts/tts-adapter-sapinative/.install modules/tts/tts-adapter-sapinative/.install-doc $(TARGET_DIR)/state/modules/tts/tts-adapter-sapinative/modified-since-release_ : \
	modules/tts/tts-adapter-sapinative/src/main/resources/x64/sapinative.dll \
	modules/tts/tts-adapter-sapinative/src/main/resources/transform-ssml.xsl \
	modules/tts/tts-adapter-sapinative/src/main/resources/x86/sapinative.dll \
	modules/tts/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapinative/SAPIResult.java \
	modules/tts/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapinative/SAPI.java \
	modules/tts/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapinative/NativeSynthesisResult.java \
	modules/tts/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapi/impl/SAPIEngine.java \
	modules/tts/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapi/impl/SAPIService.java \
	modules/tts/tts-adapter-sapinative/src/main/jni/sapinative/org_daisy_pipeline_tts_sapinative_SAPI.h \
	modules/tts/tts-adapter-sapinative/src/main/jni/sapinative/pch.h \
	modules/tts/tts-adapter-sapinative/src/main/jni/sapinative/dllmain.cpp \
	modules/tts/tts-adapter-sapinative/src/main/jni/sapinative/org_daisy_pipeline_tts_sapinative_SAPIResult.h \
	modules/tts/tts-adapter-sapinative/src/main/jni/sapinative/win_queue_stream.cpp \
	modules/tts/tts-adapter-sapinative/src/main/jni/sapinative/pch.cpp \
	modules/tts/tts-adapter-sapinative/src/main/jni/sapinative/sapinative.vcxproj \
	modules/tts/tts-adapter-sapinative/src/main/jni/sapinative/framework.h \
	modules/tts/tts-adapter-sapinative/src/main/jni/sapinative/org_daisy_pipeline_tts_sapinative_SAPI.cpp \
	modules/tts/tts-adapter-sapinative/src/main/jni/sapinative/queue_stream.h \
	modules/tts/tts-adapter-sapinative/src/main/jni/jni_helper.h \
	modules/tts/tts-adapter-sapinative/src/main/jni/Voice.hpp
modules/tts/tts-adapter-sapinative/.test modules/tts/tts-adapter-sapinative/.install-doc : \
	modules/tts/tts-adapter-sapinative/src/test/java/ignore \
	modules/tts/tts-adapter-sapinative/src/test/java/org/daisy/pipeline/tts/sapi/impl/SAPIServiceTest.java \
	modules/tts/tts-adapter-sapinative/src/test/java/org/daisy/pipeline/tts/sapi/impl/SAPITest.java \
	modules/tts/tts-adapter-sapinative/src/test/java/org/daisy/pipeline/tts/sapi/impl/SapiSSMLTest.java
.make/mk/modules/tts/tts-adapter-sapinative/sources.mk : \
	modules/tts/tts-adapter-sapinative/src \
	modules/tts/tts-adapter-sapinative/src/test \
	modules/tts/tts-adapter-sapinative/src/test/java \
	modules/tts/tts-adapter-sapinative/src/test/java/org \
	modules/tts/tts-adapter-sapinative/src/test/java/org/daisy \
	modules/tts/tts-adapter-sapinative/src/test/java/org/daisy/pipeline \
	modules/tts/tts-adapter-sapinative/src/test/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-sapinative/src/test/java/org/daisy/pipeline/tts/sapi \
	modules/tts/tts-adapter-sapinative/src/test/java/org/daisy/pipeline/tts/sapi/impl \
	modules/tts/tts-adapter-sapinative/src/main \
	modules/tts/tts-adapter-sapinative/src/main/resources \
	modules/tts/tts-adapter-sapinative/src/main/resources/x64 \
	modules/tts/tts-adapter-sapinative/src/main/resources/x86 \
	modules/tts/tts-adapter-sapinative/src/main/java \
	modules/tts/tts-adapter-sapinative/src/main/java/org \
	modules/tts/tts-adapter-sapinative/src/main/java/org/daisy \
	modules/tts/tts-adapter-sapinative/src/main/java/org/daisy/pipeline \
	modules/tts/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapinative \
	modules/tts/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapi \
	modules/tts/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapi/impl \
	modules/tts/tts-adapter-sapinative/src/main/jni \
	modules/tts/tts-adapter-sapinative/src/main/jni/sapinative
