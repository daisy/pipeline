#include "org_daisy_pipeline_tts_attnative_ATTLib.h"

#include <TTSApi.h>
#include <TTSUnixAPI.h>
#include <iostream>
#include <cstdlib>

struct ErrorHandler:  public CTTSErrorInfoObject{
  TTS_RESULT onErrorMessage(TTS_RESULT error, const char *pszErrorMessage){
    return TTS_OK;
  }
  TTS_RESULT onWarningMessage(const char *pszInformationMessage){
    return TTS_OK;
  }
  TTS_RESULT onTraceMessage(int nTraceLevel, const char *pszTraceMessage){
    return TTS_OK;
  }
};

struct CSDKSink : public CTTSSink{
  jobject	handler;
  JNIEnv*	env;
  jclass	klass;
  jmethodID	onRecvAudioID;
  long		audioLength;
  const void*	pAudioData;

  TTS_RESULT onNotification(CTTSNotification *pNotification) {
    switch (pNotification->Notification()) {
    case TTSNOTIFY_BOOKMARK:
      {
	utf8string utf8bookmark;
	if (pNotification->Bookmark(utf8bookmark) == TTS_OK) {
	  //the bookmark name is assumed to contain only simple UTF8 characters, i.e.
	  //characters for which there is no difference between true UTF8 and modified UTF8
	  jstring name = env->NewStringUTF((const char*) utf8bookmark.c_str());
	  jmethodID mid = env->GetStaticMethodID(klass, "onRecvMark", "(Ljava/lang/Object;Ljava/lang/String;)V");
	  env->CallStaticVoidMethod(klass, mid, handler, name);
	}
      }
      break;
    case TTSNOTIFY_AUDIO:
      // current solution (with callbacks and handlers):
      // [ATT engine data] (NO-COPY)--> [directBuffer] --> | -->  [directBuffer] (COPY)--> [java buffer]
      //                                               C++ | Java
      // Another way would be (without callback):
      // 1. [ATT engine data] (COPY) --> [C++ buffers] for each TTSNOTIFY_AUDIO
      // 2. [java buffer] --> | --> [jbyte*] <--(COPY) [C++ buffers] for each call to read()
      //                 Java | C++
      // See forthcoming SAPI native Adapter
      audioLength = 0;
      if (pNotification->AudioData(&pAudioData, &audioLength) == TTS_OK && (audioLength > 0)){
	jobject directBuffer = env->NewDirectByteBuffer((void*) pAudioData, audioLength);
	env->CallStaticLongMethod(klass, onRecvAudioID, handler, directBuffer, audioLength);
      }
      break;
    default:
      break;
    }

    return TTS_OK;
  }
};


struct Connection{
  ErrorHandler  errHandler; //TODO: make sure the destructor does the same as ErrorHandler::Release()
  CSDKSink*     sink;
  CTTSEngine*   engine;
  int		sampleRate;
};

namespace{
  void release(Connection* conn){
    if (conn->engine != 0){
      conn->engine->Shutdown();
      conn->engine = 0;
    }
    //there likely to be a memory leak here
    //but calling conn->engine->Release() or conn->sink->Release() is too risky

    delete conn->sink;
    delete conn;
  }

  PCUTF8String toModifiedUTF8(JNIEnv* env, jstring text, char* buff, int maxSize){
    //warning: this is not true UTF8 but this function will be used with simple ASCII
    //characters that are encoded the same way with the regular UTF8 and the modified UTF8
    int nativeSize = env->GetStringUTFLength(text);
    if (nativeSize >= maxSize+1){
      buff[0] = '\0';
      buff[1] = '\0';
    }
    else{
      env->GetStringUTFRegion(text, 0, env->GetStringLength(text), buff);
      buff[nativeSize] = '\0';
      buff[nativeSize+1] = '\0';
    }
    return (PCUTF8String) buff;
  }
}

JNIEXPORT jlong JNICALL
Java_org_daisy_pipeline_tts_attnative_ATTLib_openConnection
(JNIEnv *env, jclass klass, jstring host, jint port, jint sampleRate, jint bitsPerSample){

  Connection* conn = new Connection();

  conn->sink = new CSDKSink();
  conn->sink->AddRef();

  struct TTSConfig cfgSync(TTSENGINEMODEL_CLIENTSERVER_CURRENT,
			   TTSENGINE_SYNCHRONOUS, &conn->errHandler);

  char nativeHost[256];
  TTSServerConfig serverCfg(::toModifiedUTF8(env, host, nativeHost, 256), port);
  cfgSync.m_lstServerConfig.push_back(serverCfg);

  TTS_RESULT r = ttsCreateEngine(&conn->engine, cfgSync);
  if (r != TTS_OK){
    //TODO:Error
    conn->engine = 0;
  }
  else
    conn->engine->AddRef();

  if (r == TTS_OK && (r = conn->engine->SetSink(conn->sink)) != TTS_OK) {
    //TODO:Error
  }
  if (r == TTS_OK && (r = conn->engine->Initialize()) != TTS_OK) {
    //TODO:Error
  }

  TTSAudioFormat format, formatReturned;
  format.m_nType = TTSAUDIOTYPE_DEFAULT;
  format.m_nSampleRate = sampleRate;
  format.m_nBits = bitsPerSample;
  //No matter which voice is chosen (e.g. mike16 or mike8), the output
  //will be resampled to match with 'format'.
  if (r == TTS_OK && (r == conn->engine->SetAudioFormat(&format, &formatReturned)) != TTS_OK){
    //TODO:Error According to the documentation: "The application
    //should be aware that the returned audio format may not be
    //exactly what the application desired", but if sampleRate == 8000
    //or sampleRate == 16000, it should always work.
  }

  long notifevents = TTSNOTIFY_AUDIO|TTSNOTIFY_BOOKMARK;
  if (r == TTS_OK && (r = conn->engine->SetNotifications(notifevents, notifevents)) != TTS_OK) {
    //TODO:Error
  }

  if (r != TTS_OK){
    ::release(conn);
    return 0;
  }

  conn->sampleRate = sampleRate;

  return reinterpret_cast<jlong>(conn);
}


JNIEXPORT jint JNICALL
Java_org_daisy_pipeline_tts_attnative_ATTLib_closeConnection
(JNIEnv *, jclass, jlong connection){
  ::release(reinterpret_cast<Connection*>(connection));
  return 0;
}


JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_attnative_ATTLib_speak
(JNIEnv* env, jclass klass, jobject handler, jlong connection, jbyteArray text){

  jboolean isCopy;
  //the byte array already contains the null-terminator and is UTF8 encoded
  PCUTF8String str = (unsigned char*) env->GetByteArrayElements(text, &isCopy);

  Connection* conn = reinterpret_cast<Connection*>(connection);
  conn->sink->env = env;
  conn->sink->klass = klass;
  conn->sink->onRecvAudioID =
    env->GetStaticMethodID(klass, "onRecvAudio", "(Ljava/lang/Object;Ljava/lang/Object;I)V");
  conn->sink->handler = handler;

  TTS_RESULT r = conn->engine->Speak(str, CTTSEngine::sf_ssml);

  env->ReleaseByteArrayElements(text, (jbyte*) str, 0);

  if (r != TTS_OK){
    return 1;
  }
  return 0;
}

JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_attnative_ATTLib_getVoiceNames
(JNIEnv* env, jclass, jlong connection){
  Connection* conn = reinterpret_cast<Connection*>(connection);

  int nVoices = 0;
  jclass stringClass = env->FindClass("java/lang/String");
  jobjectArray stringArray;
  TTS_RESULT result = conn->engine->NumVoices(&nVoices);

  if (result == TTS_OK) {
    int size = 0;
    jstring* names = new jstring[nVoices];
    for (int i = 0; i < nVoices; ++i) {
      TTSVoice voice;
      result = conn->engine->EnumVoice(i, &voice);
      if (result == TTS_OK && voice.m_nSampleRate == conn->sampleRate) {
	//When the voice's sample rate doesn't match with the connection's
	//sample rate, ATT chooses the default voice (e.g. mike8) instead of
	//resampling the one chosen by the user, which is not the expected behaviour.
	//This is why we make sure that the sample rates do match.
	names[size++] = env->NewStringUTF((char*) voice.m_szName);
	//m_szName is a UTF8String*, i.e. unsigned char*
      }
    }
    stringArray = env->NewObjectArray(size, stringClass, 0);
    for (int i = 0; i < size; ++i)
      env->SetObjectArrayElement(stringArray, i, names[i]);
    delete [] names;
  }
  else{
    stringArray = env->NewObjectArray(0, stringClass, 0);
  }

  return stringArray;
}
