#include "pch.h"

// OneCore version of the sapinative bridge

#define MAX_SENTENCE_SIZE (1024*512)
#define MAX_VOICE_NAME_SIZE 128


#define AUDIO_CHUNK_SIZE 4096


using namespace winrt::Windows::Media::SpeechSynthesis;
// --- CONNECTION HANDLING

using OneCoreVoice = Voice<winrt::hstring>;
using winrtConnection = winrt::onecorenative::Connection;
struct Connection {
    wchar_t sentence[MAX_SENTENCE_SIZE / sizeof(wchar_t)];
    winrtConnection onecore;
    winrt::com_array<uint8_t> streamData;
    winrt::com_array<winrt::hstring> marksNames;
    winrt::com_array<int64_t> marksPositions;
    Connection() : onecore()
    {
        memset(sentence, 0, MAX_SENTENCE_SIZE / sizeof(wchar_t));
    }


};
using ConnectionsRegistry = std::vector<jlong>;


///////// GLOBAL VARIABLES ////////////
OneCoreVoice::Map* gAllVoices = NULL;
ConnectionsRegistry* openedConnection = NULL;
///////////////////////////////////////

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_initialize(JNIEnv* env, jclass) {
#if _DEBUG
    std::wcout << "Initializing Onecore" << std::endl;
#endif
    gAllVoices = new OneCoreVoice::Map();
    winrtConnection temp = winrtConnection();
    for each (auto rawVoice in temp.voices())
    {
        auto voice = OneCoreVoice(
            rawVoice.DisplayName(),
            static_cast<std::wstring>(rawVoice.DisplayName()),
            // there is no vendor information provide with VoiceInformation on onecore, 
            // so we use "onecore" for pipeline reporting of voices to select the correct engine
            L"onecore", 
            static_cast<std::wstring>(rawVoice.Language()),
            static_cast<std::wstring>(rawVoice.Gender() == VoiceGender::Male ? L"Male" : L"Female"),
            std::wstring(L"")
        );
        
        gAllVoices->insert(std::make_pair(
            std::pair<std::wstring, std::wstring>(voice.vendor, voice.name),
            voice
        ));
    }
    openedConnection = new ConnectionsRegistry();
    return SAPI_OK;
}


JNIEXPORT jlong JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_openConnection(JNIEnv*, jclass) {
    Connection* conn = new Connection();
    jlong connectionPtr = reinterpret_cast<jlong>(conn);
    openedConnection->push_back(connectionPtr);
    return connectionPtr;
}

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_closeConnection(JNIEnv*, jclass, jlong connection) {
#if _DEBUG
    std::wcout << "Closing onecore connection " << connection << std::endl;
#endif
    Connection* conn = reinterpret_cast<Connection*>(connection);
    if (conn != NULL) {
        delete conn;
        // Remove from opened connection registry
        for (ConnectionsRegistry::iterator it = openedConnection->begin(); it != openedConnection->end(); ++it) {
            if ((*it) == connection) {
                openedConnection->erase(it);
                break;
            }
        }
    }
    return SAPI_OK;
}


JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_dispose(JNIEnv*, jclass) {
#if _DEBUG
    std::wcout << "Disposing of Onecore" << std::endl;
#endif
    // Close remaining connections
    if (openedConnection != NULL) {

        for (ConnectionsRegistry::iterator it = openedConnection->begin(); it != openedConnection->end(); ++it) {
#if _DEBUG
            std::wcout << "- Cleaning onecore connection " << *it << std::endl;
#endif
            Connection* conn = reinterpret_cast<Connection*>(*it);
            delete conn;
        }
        delete openedConnection;
    }
    delete gAllVoices;
    return SAPI_OK;
}

// ---- END CONNECTION HANDLING

// ---- TEXT TO SPEECH

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_speak(JNIEnv* env, jclass, jlong connection, jstring voiceVendor, jstring voiceName, jstring text) {
    wchar_t c_vendor[MAX_VOICE_NAME_SIZE / sizeof(wchar_t)];
    if (!(convertToUTF16(env, voiceVendor, c_vendor, MAX_VOICE_NAME_SIZE)))
        return TOO_LONG_VOICE_VENDOR;

    wchar_t c_name[MAX_VOICE_NAME_SIZE / sizeof(wchar_t)];
    if (!(convertToUTF16(env, voiceName, c_name, MAX_VOICE_NAME_SIZE)))
        return TOO_LONG_VOICE_NAME;

    OneCoreVoice::Map::iterator it;
    if (gAllVoices != NULL) {
        it = gAllVoices->find(std::make_pair(c_vendor, c_name));
        if (it == gAllVoices->end()) return VOICE_NOT_FOUND;
    } else {
        return VOICE_NOT_FOUND;
    }

    Connection* conn = reinterpret_cast<Connection*>(connection);
    if (!(convertToUTF16(env, text, conn->sentence, MAX_SENTENCE_SIZE)))
        return TOO_LONG_TEXT;

#if _DEBUG
    std::wcout << it->second.name << " speaking " << conn->sentence << std::endl;
#endif
    // VoiceInformation seems to create an exception, so we use the voice display name for now
    winrt::hstring ssmltext = winrt::hstring(conn->sentence);
    winrt::hstring foundVoiceName = it->second.rawVoice;
        
    try {   
        conn->streamData = conn->onecore.speak(ssmltext, foundVoiceName);
        conn->marksNames = conn->onecore.marksNames();
        conn->marksPositions = conn->onecore.marksPositions();
    }
    catch (winrt::hresult_error const& ex)
    {
            
        winrt::hresult hr = ex.code();
        std::wstring message = std::wstring(ex.message().c_str());
        std::wstring sentence = std::wstring(conn->sentence);
        std::wostringstream excep;
        excep << L"Error code (0x" << std::hex << hr.value << L") raised when trying to speak with OneCore SAPI" << std::endl;
        excep << message << std::endl;
        // Use exception instead of return result to get error code in java
        raiseIOException(env, (const jchar*)excep.str().c_str(), excep.str().size());
        return COULD_NOT_SPEAK;
        
    }
        
    return SAPI_OK;
}

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_getStreamSize(JNIEnv*, jclass, jlong connection) {
    Connection* conn = reinterpret_cast<Connection*>(connection);
    if (conn != NULL) {
        return static_cast<jint>(conn->streamData.size());
    } 
    
    return 0;
}

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_readStream(JNIEnv* env, jclass, jlong connection, jbyteArray dest, jint offset) {
    Connection* conn = reinterpret_cast<Connection*>(connection);
    if (conn != NULL) {
        if (conn->streamData.size() > 0) {
            env->SetByteArrayRegion(dest, offset, conn->streamData.size(), (const jbyte*)(conn->streamData.data()));
            offset += conn->streamData.size();
        }
    }
    
    return 0;
}


// ---- END TEXT TO SPEECH

// ---- VOICE

struct VoiceVendorToJString {
    static jstring convert(const OneCoreVoice::Map::const_iterator& it, JNIEnv* env) {
        const wchar_t* str = it->second.vendor.c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_getVoiceVendors(JNIEnv* env, jclass) {
#if _DEBUG
    std::wcout << "Getting voice vendors" << std::endl;
#endif
    if (gAllVoices != NULL) {
        return newJavaArray<OneCoreVoice::Map::iterator, VoiceVendorToJString>(
            env,
            gAllVoices->begin(),
            gAllVoices->size(),
            "java/lang/String"
        );
    }
    else return emptyJavaArray(env, "java/lang/String");
}


struct VoiceNameToJString {
    static jstring convert(const OneCoreVoice::Map::const_iterator& it, JNIEnv* env) {
        const wchar_t* str = it->second.name.c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_getVoiceNames(JNIEnv* env, jclass) {
#if _DEBUG
    std::wcout << "Getting voice names" << std::endl;
#endif
    if (gAllVoices != NULL) {
        return newJavaArray<OneCoreVoice::Map::iterator, VoiceNameToJString>(
            env,
            gAllVoices->begin(),
            gAllVoices->size(),
            "java/lang/String"
        );
    }
    else return emptyJavaArray(env, "java/lang/String");
}


struct VoiceLocaleToJString {
    static jstring convert(const OneCoreVoice::Map::const_iterator& it, JNIEnv* env) {
        const wchar_t* str = it->second.language.c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_getVoiceLocales(JNIEnv* env, jclass) {
#if _DEBUG
    std::wcout << "Getting voice locales" << std::endl;
#endif
    if (gAllVoices != NULL) {
        return newJavaArray<OneCoreVoice::Map::iterator, VoiceLocaleToJString>(
            env,
            gAllVoices->begin(),
            gAllVoices->size(),
            "java/lang/String"
        );
    }
    else return emptyJavaArray(env, "java/lang/String");
}


struct VoiceGenderToJString {
    static jstring convert(const OneCoreVoice::Map::const_iterator& it, JNIEnv* env) {
        const wchar_t* str = it->second.gender.c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_getVoiceGenders(JNIEnv* env, jclass)
{
#if _DEBUG
    std::wcout << "Getting voice genders" << std::endl;
#endif
    if (gAllVoices != NULL) {
        return newJavaArray<OneCoreVoice::Map::iterator, VoiceGenderToJString>(
            env,
            gAllVoices->begin(),
            gAllVoices->size(),
            "java/lang/String"
        );
    }
    else return emptyJavaArray(env, "java/lang/String");
}

struct VoiceAgeToJString {
    static jstring convert(const OneCoreVoice::Map::const_iterator& it, JNIEnv* env) {
        const wchar_t* str = it->second.age.c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_getVoiceAges(JNIEnv* env, jclass)
{
#if _DEBUG
    std::wcout << "Getting voice ages" << std::endl;
#endif
    if (gAllVoices != NULL) {
        return newJavaArray<OneCoreVoice::Map::iterator, VoiceAgeToJString>(
            env,
            gAllVoices->begin(),
            gAllVoices->size(),
            "java/lang/String"
        );
    }
    else return emptyJavaArray(env, "java/lang/String");
     
}

// ---- END VOICE

// ---- BOOKMARKS
struct BookMarkNamesToJString {
    static jstring convert(const winrt::com_array<winrt::hstring>::iterator& it, JNIEnv* env) {
        const wchar_t* str = it->c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_getBookmarkNames(JNIEnv* env, jclass, jlong connection) {
    Connection* conn = reinterpret_cast<Connection*>(connection);
    auto names = conn->onecore.marksNames();
    return newJavaArray<winrt::com_array<winrt::hstring>::iterator, BookMarkNamesToJString>(
        env,
        names.begin(),
        names.size(),
        "java/lang/String"
    );
}


JNIEXPORT jlongArray JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_getBookmarkPositions(JNIEnv* env, jclass, jlong connection) {
    Connection* conn = reinterpret_cast<Connection*>(connection);
    auto positions = conn->onecore.marksPositions();

    jlongArray result = env->NewLongArray(positions.size());
    if (positions.size() > 0) {
        env->SetLongArrayRegion(result, 0, positions.size(), positions.data());
    }
    return result;
}

// ---- END BOOKMARKS