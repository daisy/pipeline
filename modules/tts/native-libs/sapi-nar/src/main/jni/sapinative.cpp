#include "org_daisy_pipeline_tts_sapinative_SAPILib.h"

#include <map>
#include <string> //must come first because it uses variables called "__in" overridden after
#include <vector>

//The sapi.h copied from the original Windows SDK (MinGW does not provide it) cannot be
//compiled with MinGW because of those macros that are not defined in the MinGW headers.
#ifdef MINGW
# define __in_opt
# define __out_opt
# define __inout_opt
# define __deref_out
# define __deref_opt_out
# define __in
# define __in_z
# define __out
# define __out_ecount(x)
# define __out_ecount_z(x)
# define __out_ecount_opt(x)
# define __in_ecount_opt(x)
# define __in_ecount_part(x,y)
# define __out_ecount_part(x,y)
# define CROSS_PLATFORM_SPFEI(X) ((1LL << X) |(1LL << SPEI_RESERVED1) | (1LL << SPEI_RESERVED2))
#else
# define CROSS_PLATFORM_SPFEI(X) SPFEI(X)
#endif

#include <windows.h>
#include <sapi.h>
#include <sperror.h>

#include "jni_helper.h"
#include "queue_stream.h"

#if  _SAPI_VER <= 0x051
	#define CLIENT_SPEAK_FLAGS (SVSFIsXML | SVSFlagsAsync)
#else
	#define CLIENT_SPEAK_FLAGS (SVSFParseSsml | SVSFIsXML | SVSFlagsAsync)
#endif

#define MAX_SENTENCE_SIZE (1024*512)
#define MAX_VOICE_NAME_SIZE 128

//(utf16 vendor, utf16 name) -> Token
typedef std::map<std::pair<std::wstring, std::wstring>, ISpObjectToken*> VoiceMap;

///////// GLOBAL VARIABLES ////////////
VoiceMap gAllVoices;
WAVEFORMATEX gWaveFormat;
///////////////////////////////////////


struct Connection{
	wchar_t						sentence[MAX_SENTENCE_SIZE/sizeof(wchar_t)];
	ISpVoice*					spVoice;
	ISpStream*					spStream;
	WinQueueStream            	qStream;
	int 						currentBookmarkIndex;             
	std::vector<std::wstring>	bookmarkNames;
	std::vector<jlong>			bookmarkPositions;
	//=> The bookmarks info are in separate lists because it's easier
	//to send them to Java later
	
	Connection():spVoice(0),spStream(0){}
};


struct VoiceNameAccessor{
	static jstring get(const VoiceMap::const_iterator& it, JNIEnv* env){
		//wcslen() returns the number of UTF-16 characters
		const wchar_t* str = it->first.second.c_str();
		return env->NewString((const jchar*) str, std::wcslen(str));
	}
};

struct VoiceVendorAccessor{
	static jstring get(const VoiceMap::const_iterator& it, JNIEnv* env){
		const wchar_t* str = it->first.first.c_str();
		return env->NewString((const jchar*) str, std::wcslen(str));
	}
};

struct BookmarkNameAccessor{
	static jstring get(const std::vector<std::wstring>::const_iterator& it, JNIEnv* env){
		const wchar_t* str = it->c_str();
		return env->NewString((const jchar*) str, std::wcslen(str));
	}
};

enum {
	SAPINATIVE_OK = 0,
	UNSUPPORTED_FORMAT,
	TOO_LONG_VOICE_NAME,
	TOO_LONG_VOICE_VENDOR,
	TOO_LONG_TEXT,
	UNSUPPORTED_AUDIO_FORMAT,
	VOICE_NOT_FOUND,
	COULD_NOT_SET_VOICE,
	COULD_NOT_SET_EVENT_INTERESTS,
	COULD_NOT_LISTEN_TO_EVENTS,
	COULD_NOT_INIT_COM,
	COULD_NOT_CREATE_CATEGORY,
	COULD_NOT_ENUM_CATEGORY,
	COULD_NOT_COUNT_ENUM,
	COULD_NOT_SPEAK_INVALIDARG,
	COULD_NOT_SPEAK_E_POINTER,
	COULD_NOT_SPEAK_OUTOFMEMORY,
	COULD_NOT_SPEAK_INVALIDFLAGS,
	COULD_NOT_SPEAK_BUSY,
	COULD_NOT_SPEAK_THIS_FORMAT,
	COULD_NOT_SPEAK,
	COULD_NOT_SET_STREAM_FORMAT,
	COULD_NOT_BIND_STREAM
};

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_sapinative_SAPILib_initialize(JNIEnv* env, jclass, jint sampleRate, jint bitsPerSample){
	if (bitsPerSample != 8 && bitsPerSample != 16)
		return UNSUPPORTED_AUDIO_FORMAT;
		
	if (sampleRate != 8000 &&
		sampleRate != 11025 &&
		sampleRate != 16000 &&
		sampleRate != 22050 &&
		sampleRate != 44100 &&
		sampleRate != 48000)
		return UNSUPPORTED_AUDIO_FORMAT;

	//TODO: Check that all of those formats are handled by SAPI no matter the back-end voice.
	//i.e. check that SAPI is able to up/down-sample from any rate to any other rate.
		
	gWaveFormat.wFormatTag = WAVE_FORMAT_PCM;
	gWaveFormat.nChannels = 1;
	gWaveFormat.nSamplesPerSec = sampleRate;
	gWaveFormat.wBitsPerSample = bitsPerSample;
	gWaveFormat.nBlockAlign = (gWaveFormat.nChannels*gWaveFormat.wBitsPerSample)/8;
	gWaveFormat.nAvgBytesPerSec = gWaveFormat.nBlockAlign*gWaveFormat.nSamplesPerSec;
	gWaveFormat.cbSize = 0;

	if(FAILED(CoInitialize(NULL))){
		return COULD_NOT_INIT_COM;
	  }
  
  //get the voice information  
  ISpObjectTokenCategory* category; 
  if(FAILED(CoCreateInstance(CLSID_SpObjectTokenCategory, NULL, CLSCTX_ALL, IID_ISpObjectTokenCategory, (void**)(&category)))){
	CoUninitialize();
	return COULD_NOT_CREATE_CATEGORY;
  }
  category->AddRef();
  category->SetId(SPCAT_VOICES, false);
  
  IEnumSpObjectTokens* cpEnum;
  if(FAILED(category->EnumTokens(NULL, NULL, &cpEnum))){
	category->Release();
	CoUninitialize();
	return COULD_NOT_ENUM_CATEGORY;
  }
  cpEnum->AddRef();
  
  ULONG count = 0;
  if (FAILED(cpEnum->GetCount(&count))){
	cpEnum->Release();
	category->Release();
	CoUninitialize();
	return COULD_NOT_COUNT_ENUM;
  }
  
  wchar_t* vendor; //encoded as UTF-16
  wchar_t* name;
  ISpObjectToken* cpToken;
  ISpDataKey* key;
  for (int i = 0; i < count; ++i){
	if (SUCCEEDED(cpEnum->Item(i, &cpToken))){
		cpToken->AddRef();
		if (SUCCEEDED(cpToken->OpenKey(L"attributes", &key))){
			key->AddRef();
			if (SUCCEEDED(key->GetStringValue(L"vendor", &vendor)) &&
				SUCCEEDED(key->GetStringValue(L"name", &name))){
				gAllVoices.insert(std::make_pair(std::pair<std::wstring, std::wstring>(vendor, name), cpToken));
			}
			else
				cpToken->Release();
			key->Release();
		}
		else
			cpToken->Release();
	}
  }
  
  cpEnum->Release();
  category->Release();
  
  return SAPINATIVE_OK;
}

namespace{
	void release(Connection* conn){
		if (conn->spVoice != 0){
			conn->spVoice->Release();
		}
		if (conn->spStream != 0){
			conn->spStream->Close();
			conn->spStream->Release();
		}
		conn->qStream.dispose();
		
		delete conn;
	}
}

JNIEXPORT jlong JNICALL Java_org_daisy_pipeline_tts_sapinative_SAPILib_openConnection(JNIEnv *, jclass){
	Connection* conn = new Connection();

    if(FAILED(CoCreateInstance(CLSID_SpVoice, NULL, CLSCTX_ALL, IID_ISpVoice, (void**)(&conn->spVoice)))){
		::release(conn);
		return 0;
	}
	conn->spVoice->AddRef();
	
	if(FAILED(CoCreateInstance(CLSID_SpStream, NULL, CLSCTX_ALL, IID_ISpStream, (void**)(&conn->spStream)))){
		::release(conn);
		return 0;
	}
	conn->spStream->AddRef();
	
	if (!conn->qStream.initialize())
		return 0;
	
	if (FAILED(conn->spStream->SetBaseStream(conn->qStream.getBaseStream(), SPDFID_WaveFormatEx, &gWaveFormat)))
		return 0;
		
	if(FAILED(conn->spVoice->SetOutput(conn->spStream, TRUE)))
		return 0;
		
	return reinterpret_cast<jlong>(conn);
}

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_sapinative_SAPILib_closeConnection(JNIEnv *, jclass, jlong connection){
  Connection* conn = reinterpret_cast<Connection*>(connection);
  
  conn->qStream.dispose();
  conn->spStream->Close();
  conn->spStream->Release();
  conn->spVoice->Release();
  delete conn;
  
  return SAPINATIVE_OK;
}

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_sapinative_SAPILib_speak(JNIEnv* env, jclass, jlong connection, jstring voiceVendor, jstring voiceName, jstring text){
	wchar_t c_vendor[MAX_VOICE_NAME_SIZE/sizeof(wchar_t)];
	if (!(convertToUTF16(env, voiceVendor, c_vendor, MAX_VOICE_NAME_SIZE)))
		return TOO_LONG_VOICE_VENDOR;

	wchar_t c_name[MAX_VOICE_NAME_SIZE/sizeof(wchar_t)];
	if (!(convertToUTF16(env, voiceName, c_name, MAX_VOICE_NAME_SIZE)))
		return TOO_LONG_VOICE_NAME;
		
	VoiceMap::iterator it = gAllVoices.find(std::make_pair(c_vendor, c_name));
	if (it == gAllVoices.end())
		return VOICE_NOT_FOUND;
  
	Connection* conn = reinterpret_cast<Connection*>(connection);
	if (!(convertToUTF16(env, text, conn->sentence, MAX_SENTENCE_SIZE)))
		return TOO_LONG_TEXT;
	
	if (FAILED(conn->spVoice->SetVoice(it->second)))
		return COULD_NOT_SET_VOICE;
	
	ULONGLONG ullEventInterest =  CROSS_PLATFORM_SPFEI(SPEI_END_INPUT_STREAM) |
		CROSS_PLATFORM_SPFEI(SPEI_TTS_BOOKMARK) |
		CROSS_PLATFORM_SPFEI(SPEI_VISEME);
		
	if (FAILED(conn->spVoice->SetInterest(ullEventInterest, ullEventInterest)))
		return COULD_NOT_SET_EVENT_INTERESTS;
	
    HANDLE hSpeechNotifyEvent = conn->spVoice->GetNotifyEventHandle();
	if (INVALID_HANDLE_VALUE == hSpeechNotifyEvent)
		return COULD_NOT_LISTEN_TO_EVENTS;
	
	conn->qStream.startWritingPhase();
	HRESULT hr = conn->spVoice->Speak((wchar_t*) conn->sentence, CLIENT_SPEAK_FLAGS, 0);
	if (hr == E_INVALIDARG)
		return COULD_NOT_SPEAK_INVALIDARG;
		
	if (hr == E_POINTER)
		return COULD_NOT_SPEAK_E_POINTER;
	
	if (hr == E_OUTOFMEMORY)
		return COULD_NOT_SPEAK_OUTOFMEMORY;
	
	if (hr == SPERR_INVALID_FLAGS)
		return COULD_NOT_SPEAK_INVALIDFLAGS;
		
	if (hr == SPERR_DEVICE_BUSY)
		return COULD_NOT_SPEAK_BUSY;
	
	if (hr == SPERR_UNSUPPORTED_FORMAT)
		return COULD_NOT_SPEAK_THIS_FORMAT;
	
	if (hr != S_OK){
		return COULD_NOT_SPEAK;
	}

	conn->currentBookmarkIndex = 0;
	jlong duration = 0; //in milliseconds
	bool end = false;
	while (!end){
		conn->spVoice->WaitForNotifyEvent(INFINITE);
		SPEVENT event;
		memset(&event, 0, sizeof(SPEVENT));
        while (!end && S_OK == conn->spVoice->GetEvents(1, &event, NULL)){
			switch (event.eEventId){
				case SPEI_VISEME:
					duration += HIWORD(event.wParam);
					break;
				case SPEI_END_INPUT_STREAM:
					end = true;
					break;
                case SPEI_TTS_BOOKMARK:
					 if (conn->currentBookmarkIndex == conn->bookmarkNames.size()){
						int newsize = 1+(3*conn->bookmarkNames.size())/2;
						conn->bookmarkNames.resize(newsize);
						conn->bookmarkPositions.resize(newsize);
					 }
					 //bookmarks are not pushed_back to prevent allocating/releasing all over the place
					 conn->bookmarkNames[conn->currentBookmarkIndex] = (const wchar_t*)(event.lParam);
					 conn->bookmarkPositions[conn->currentBookmarkIndex] = duration;
					 ++(conn->currentBookmarkIndex);
                     break;
            }
			 memset(&event, 0, sizeof(SPEVENT));
        }
	}
	
	conn->qStream.endWritingPhase();
	
	return SAPINATIVE_OK;
}


	
JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_sapinative_SAPILib_getStreamSize(JNIEnv *, jclass, jlong connection){
	Connection* conn = reinterpret_cast<Connection*>(connection);
	return conn->qStream.in_avail();
}
  
JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_sapinative_SAPILib_readStream(JNIEnv* env, jclass, jlong connection, jbyteArray dest, jint offset){
	Connection* conn = reinterpret_cast<Connection*>(connection);
	
	//the array 'dest' is assumed to be big enough thanks to
	//a previous call to getStreamSize()
	const char* audio;
	int size;
	while ((audio = conn->qStream.nextChunk(&size))){
		env->SetByteArrayRegion(dest, offset, size, (const jbyte*) audio);
		offset += size;
	}
	
	return offset;
 }
  
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_sapinative_SAPILib_getVoiceVendors(JNIEnv* env, jclass){
	return newJavaArray<VoiceVendorAccessor, jstring>(env, gAllVoices.begin(), gAllVoices.size(), "java/lang/String");
}

JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_sapinative_SAPILib_getVoiceNames(JNIEnv* env, jclass){
	return newJavaArray<VoiceNameAccessor, jstring>(env, gAllVoices.begin(), gAllVoices.size(), "java/lang/String");
}

JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_sapinative_SAPILib_getBookmarkNames(JNIEnv* env, jclass, jlong connection){
	Connection* conn = reinterpret_cast<Connection*>(connection);
	return newJavaArray<BookmarkNameAccessor, jstring>(env, conn->bookmarkNames.begin(), conn->currentBookmarkIndex, "java/lang/String");
}


JNIEXPORT jlongArray JNICALL Java_org_daisy_pipeline_tts_sapinative_SAPILib_getBookmarkPositions(JNIEnv* env, jclass, jlong connection){
	Connection* conn = reinterpret_cast<Connection*>(connection);
	
	jlongArray result = env->NewLongArray(conn->currentBookmarkIndex);
	env->SetLongArrayRegion(result, 0, conn->currentBookmarkIndex, &(conn->bookmarkPositions[0]));
	
	return result;
}

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_sapinative_SAPILib_dispose(JNIEnv*, jclass){
	VoiceMap::iterator it = gAllVoices.begin();
	for (; it != gAllVoices.end(); ++it)
		it->second->Release();
  
	CoUninitialize();
	return SAPINATIVE_OK;
}