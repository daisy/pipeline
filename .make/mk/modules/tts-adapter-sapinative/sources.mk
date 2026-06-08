modules/tts-adapter-sapinative/.test modules/tts-adapter-sapinative/.install modules/tts-adapter-sapinative/.install-doc $(TARGET_DIR)/state/modules/tts-adapter-sapinative/modified-since-release_ : \
	modules/tts-adapter-sapinative/src/main/resources/x64/sapinative.dll \
	modules/tts-adapter-sapinative/src/main/resources/transform-ssml.xsl \
	modules/tts-adapter-sapinative/src/main/resources/x86/sapinative.dll \
	modules/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapinative/SAPIResult.java \
	modules/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapinative/SAPI.java \
	modules/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapinative/NativeSynthesisResult.java \
	modules/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapi/impl/SAPIEngine.java \
	modules/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapi/impl/SAPIService.java \
	modules/tts-adapter-sapinative/src/main/jni/sapinative/org_daisy_pipeline_tts_sapinative_SAPI.h \
	modules/tts-adapter-sapinative/src/main/jni/sapinative/pch.h \
	modules/tts-adapter-sapinative/src/main/jni/sapinative/dllmain.cpp \
	modules/tts-adapter-sapinative/src/main/jni/sapinative/org_daisy_pipeline_tts_sapinative_SAPIResult.h \
	modules/tts-adapter-sapinative/src/main/jni/sapinative/win_queue_stream.cpp \
	modules/tts-adapter-sapinative/src/main/jni/sapinative/pch.cpp \
	modules/tts-adapter-sapinative/src/main/jni/sapinative/sapinative.vcxproj \
	modules/tts-adapter-sapinative/src/main/jni/sapinative/framework.h \
	modules/tts-adapter-sapinative/src/main/jni/sapinative/org_daisy_pipeline_tts_sapinative_SAPI.cpp \
	modules/tts-adapter-sapinative/src/main/jni/sapinative/queue_stream.h \
	modules/tts-adapter-sapinative/src/main/jni/jni_helper.h \
	modules/tts-adapter-sapinative/src/main/jni/Voice.hpp
modules/tts-adapter-sapinative/.test modules/tts-adapter-sapinative/.install-doc : \
	modules/tts-adapter-sapinative/src/test/java/ignore \
	modules/tts-adapter-sapinative/src/test/java/org/daisy/pipeline/tts/sapi/impl/SAPIServiceTest.java \
	modules/tts-adapter-sapinative/src/test/java/org/daisy/pipeline/tts/sapi/impl/SAPITest.java \
	modules/tts-adapter-sapinative/src/test/java/org/daisy/pipeline/tts/sapi/impl/SapiSSMLTest.java
.make/mk/modules/tts-adapter-sapinative/sources.mk : \
	modules/tts-adapter-sapinative/src \
	modules/tts-adapter-sapinative/src/test \
	modules/tts-adapter-sapinative/src/test/java \
	modules/tts-adapter-sapinative/src/test/java/org \
	modules/tts-adapter-sapinative/src/test/java/org/daisy \
	modules/tts-adapter-sapinative/src/test/java/org/daisy/pipeline \
	modules/tts-adapter-sapinative/src/test/java/org/daisy/pipeline/tts \
	modules/tts-adapter-sapinative/src/test/java/org/daisy/pipeline/tts/sapi \
	modules/tts-adapter-sapinative/src/test/java/org/daisy/pipeline/tts/sapi/impl \
	modules/tts-adapter-sapinative/src/main \
	modules/tts-adapter-sapinative/src/main/resources \
	modules/tts-adapter-sapinative/src/main/resources/x64 \
	modules/tts-adapter-sapinative/src/main/resources/x86 \
	modules/tts-adapter-sapinative/src/main/java \
	modules/tts-adapter-sapinative/src/main/java/org \
	modules/tts-adapter-sapinative/src/main/java/org/daisy \
	modules/tts-adapter-sapinative/src/main/java/org/daisy/pipeline \
	modules/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts \
	modules/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapinative \
	modules/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapi \
	modules/tts-adapter-sapinative/src/main/java/org/daisy/pipeline/tts/sapi/impl \
	modules/tts-adapter-sapinative/src/main/jni \
	modules/tts-adapter-sapinative/src/main/jni/sapinative
