#include "pch.h"

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

#include <iostream>

#if  _SAPI_VER <= 0x051
#define CLIENT_SPEAK_FLAGS (SVSFIsXML | SVSFlagsAsync)
#else
//#define CLIENT_SPEAK_FLAGS (SVSFParseSsml | SVSFIsXML | SVSFlagsAsync) // ParseSsml flag make the speak crash in my tests
#define CLIENT_SPEAK_FLAGS (SVSFIsXML | SVSFlagsAsync)
#endif

#define MAX_SENTENCE_SIZE (1024*512)
#define MAX_VOICE_NAME_SIZE 128


/// <summary>
/// SAPI Voice class from template
/// </summary>
using SapiVoice = Voice<ISpObjectToken*>;



/// <summary>
/// Connection with the pipeline
/// </summary>
struct Connection {
    wchar_t						sentence[MAX_SENTENCE_SIZE / sizeof(wchar_t)];
    ISpVoice*                   spVoice;
    ISpStream*                  spStream;
    WinQueueStream            	qStream;
    int 						currentBookmarkIndex;
    std::vector<std::wstring>	bookmarkNames;
    std::vector<jlong>			bookmarkPositions;
    //=> The bookmarks info are in separate lists because it's easier
    //to send them to Java later

    Connection() : 
        spVoice(NULL),
        spStream(NULL),
        currentBookmarkIndex(-1)
    {
        memset(sentence, 0, MAX_SENTENCE_SIZE / sizeof(wchar_t));
    }

    ~Connection() {
        if (this->spVoice != 0) {
            this->spVoice->Release();
        }
        if (this->spStream != 0) {
            this->spStream->Close();
            this->spStream->Release();
        }
        this->qStream.dispose();
    }
};
using ConnectionsRegistry = std::vector<jlong>;

///////// GLOBAL VARIABLES ////////////
SapiVoice::Map* gAllVoices = NULL;
PWAVEFORMATEX gWaveFormat = NULL;
ConnectionsRegistry* openedConnection = NULL;
///////////////////////////////////////


JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_initialize(JNIEnv* env, jclass, jint sampleRate, jshort bitsPerSample) {
#if _DEBUG
    std::wcout << "Initializing SAPI" << std::endl;
#endif
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
    gWaveFormat = new WAVEFORMATEX;
    gWaveFormat->wFormatTag = WAVE_FORMAT_PCM;
    gWaveFormat->nChannels = 1;
    gWaveFormat->nSamplesPerSec = sampleRate;
    gWaveFormat->wBitsPerSample = bitsPerSample;
    gWaveFormat->nBlockAlign = (gWaveFormat->nChannels * gWaveFormat->wBitsPerSample) / 8;
    gWaveFormat->nAvgBytesPerSec = gWaveFormat->nBlockAlign * gWaveFormat->nSamplesPerSec;
    gWaveFormat->cbSize = 0;
    
    openedConnection = new ConnectionsRegistry();

    HRESULT hr;
    hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);
    //hr = CoInitializeEx(NULL, COINIT_APARTMENTTHREADED);
    
    if (hr != S_OK && hr != S_FALSE) {
        return COULD_NOT_INIT_COM;
    }

    //get the voice information  
    ISpObjectTokenCategory* category;
    if (FAILED(CoCreateInstance(CLSID_SpObjectTokenCategory, NULL, CLSCTX_ALL, IID_ISpObjectTokenCategory, (void**)(&category)))) {
        CoUninitialize();
        return COULD_NOT_CREATE_CATEGORY;
    }
    category->AddRef();
    category->SetId(SPCAT_VOICES, false);

    IEnumSpObjectTokens* cpEnum;
    if (FAILED(category->EnumTokens(NULL, NULL, &cpEnum))) {
        category->Release();
        CoUninitialize();
        return COULD_NOT_ENUM_CATEGORY;
    }
    cpEnum->AddRef();

    ULONG count = 0;
    if (FAILED(cpEnum->GetCount(&count))) {
        cpEnum->Release();
        category->Release();
        CoUninitialize();
        return COULD_NOT_COUNT_ENUM;
    }

    wchar_t* vendor; //encoded as UTF-16
    wchar_t* name;
    ISpObjectToken* cpToken;
    ISpDataKey* key;
    for (unsigned int i = 0; i < count; ++i) {
        if (SUCCEEDED(cpEnum->Item(i, &cpToken))) {
            cpToken->AddRef();
            if (SUCCEEDED(cpToken->OpenKey(L"attributes", &key))) {
                key->AddRef();
                if (SUCCEEDED(key->GetStringValue(L"vendor", &vendor)) &&
                    SUCCEEDED(key->GetStringValue(L"name", &name))) {

                    wchar_t* langCode = nullptr;
                    if (SUCCEEDED(key->GetStringValue(L"language", &langCode))) {
                        LCID actualCode = static_cast<LCID>(std::wcstoul(langCode,nullptr,16));
                        wchar_t buf[19];
                        int ccBuf = GetLocaleInfo(actualCode, LOCALE_SISO639LANGNAME, buf, 9);
                        buf[ccBuf - 1] = '-';
                        ccBuf += GetLocaleInfo(actualCode, LOCALE_SISO3166CTRYNAME, buf + ccBuf, 9);
                        langCode = buf;
                    }

                    wchar_t* gender = nullptr;
                    key->GetStringValue(L"gender", &gender);
                    

                    wchar_t* age = nullptr;
                    key->GetStringValue(L"age", &age);

                    if (gAllVoices == NULL) {
                        gAllVoices = new SapiVoice::Map();
                    }
                    // 2022-10-04 : replacing original vendor by sapi keyword 
                    // for voice engine identification on the pipeline side (sapi or onecore)
                    // Note that onecore does not provide vendor informations while sapi does
                    // but the use of "sapi" as vendor could lead to errors in voice 
                    // identification if some vendors decided to provided voices with the same
                    // name
#if _DEBUG
                    std::wcout << L"Registering voice " << L"sapi" << " : " << name << std::endl;
#endif
                    gAllVoices->insert(std::make_pair(
                        std::pair<std::wstring, std::wstring>(L"sapi", name),
                        Voice<ISpObjectToken*>(cpToken,
                            std::wstring(name),
                            //std::wstring(vendor), 
                            std::wstring(L"sapi"),
                            std::wstring(langCode != nullptr ? langCode : L""),
                            std::wstring(gender != nullptr ? gender : L""),
                            std::wstring(age != nullptr ? age : L"")
                        )
                    ));
                } else cpToken->Release();
                key->Release();
            }
            else cpToken->Release();
        }
    }
#if _DEBUG
    std::wcout << "Done" << std::endl;
#endif
    cpEnum->Release();
    category->Release();

    return SAPI_OK;
}


JNIEXPORT jlong JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_openConnection(JNIEnv* env, jclass) {

    Connection* conn = new Connection();
    HRESULT hr;
    hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);
    //hr = CoInitializeEx(NULL, COINIT_APARTMENTTHREADED);
    if (hr != S_OK && hr != S_FALSE) {
        std::wcout << "SAPI - COM server not initialized for the connection attempt" << std::endl;
        return 0;
    }
    hr = CoCreateInstance(CLSID_SpVoice, NULL, CLSCTX_ALL, IID_ISpVoice, (void**)(&conn->spVoice));
    if (FAILED(hr)) {
        LPTSTR errorText = NULL;
        FormatMessage(
            FORMAT_MESSAGE_FROM_SYSTEM
            | FORMAT_MESSAGE_ALLOCATE_BUFFER
            | FORMAT_MESSAGE_IGNORE_INSERTS,
            NULL,
            hr,
            MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
            (LPTSTR)&errorText,
            0,
            NULL);
        std::wostringstream excep;
        excep << L"Could not create a Voice instance: " << std::endl;
        excep << errorText << std::endl;
        LocalFree(errorText);
        raiseIOException(env, (const jchar*)excep.str().c_str(), excep.str().size());
        errorText = NULL;
        delete conn;
        return 0;
    }
    conn->spVoice->AddRef();
    jlong connectionPtr = reinterpret_cast<jlong>(conn);
#if _DEBUG
    std::wcout << "New connection opened with the pipeline : " << connectionPtr << std::endl;
#endif
    // Add connection ptr on registry
    openedConnection->push_back(connectionPtr);
    return connectionPtr;
}

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_closeConnection(JNIEnv*, jclass, jlong connection) {
#if _DEBUG
    std::wcout << "Closing SAPI connection " << connection << std::endl;
#endif
    {
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
    }
    CoUninitialize();
    return SAPI_OK;
}

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_speak(JNIEnv* env, jclass, jlong connection, jstring voiceVendor, jstring voiceName, jstring text) {

    wchar_t c_vendor[MAX_VOICE_NAME_SIZE / sizeof(wchar_t)];
    if (!(convertToUTF16(env, voiceVendor, c_vendor, MAX_VOICE_NAME_SIZE)))
        return TOO_LONG_VOICE_VENDOR;

    wchar_t c_name[MAX_VOICE_NAME_SIZE / sizeof(wchar_t)];
    if (!(convertToUTF16(env, voiceName, c_name, MAX_VOICE_NAME_SIZE)))
        return TOO_LONG_VOICE_NAME;

    SapiVoice::Map::iterator it;
    if (gAllVoices != NULL) {
#if _DEBUG
        std::wcout << L"Looking for voice " << c_vendor << " : " << c_name << std::endl;
#endif
        it = gAllVoices->find(std::make_pair(c_vendor, c_name));
        if (it == gAllVoices->end()) return VOICE_NOT_FOUND;
    } else {
        return VOICE_NOT_FOUND;
    }

    Connection* conn = reinterpret_cast<Connection*>(connection);
    if (!(convertToUTF16(env, text, conn->sentence, MAX_SENTENCE_SIZE)))
        return TOO_LONG_TEXT;

    if (FAILED(conn->spVoice->SetVoice(it->second.rawVoice)))
        return COULD_NOT_SET_VOICE;

    ULONGLONG ullEventInterest = CROSS_PLATFORM_SPFEI(SPEI_END_INPUT_STREAM) |
        CROSS_PLATFORM_SPFEI(SPEI_TTS_BOOKMARK) |
        CROSS_PLATFORM_SPFEI(SPEI_VISEME);

    if (FAILED(conn->spVoice->SetInterest(ullEventInterest, ullEventInterest)))
        return COULD_NOT_SET_EVENT_INTERESTS;

    HANDLE hSpeechNotifyEvent = conn->spVoice->GetNotifyEventHandle();
    if (INVALID_HANDLE_VALUE == hSpeechNotifyEvent)
        return COULD_NOT_LISTEN_TO_EVENTS;

    // on speaking request
    // Dispose previous stream
    if (conn->spStream != NULL) {
        conn->spStream->Close();
        conn->spStream->Release();
    }
    conn->qStream.dispose();
    
    // Create a new speak stream
    HRESULT hr = CoCreateInstance(CLSID_SpStream, NULL, CLSCTX_ALL, IID_ISpStream, (void**)(&conn->spStream));
    if (FAILED(hr)) {
        LPTSTR errorText = NULL;
        FormatMessage(
            FORMAT_MESSAGE_FROM_SYSTEM
            | FORMAT_MESSAGE_ALLOCATE_BUFFER
            | FORMAT_MESSAGE_IGNORE_INSERTS,
            NULL,
            GetLastError(),
            MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
            (LPTSTR)&errorText,
            0,
            NULL
        );
        std::wcout << errorText << std::endl;
        LocalFree(errorText);
        errorText = NULL;
        //delete conn;
        return COULD_NOT_INIT_STREAM;
    }

    conn->spStream->AddRef();
    // Create a new memory stream if a wave format has been initialized
    if (!conn->qStream.initialize() || gWaveFormat == NULL) {
        return COULD_NOT_INIT_STREAM;
    }
    // Bind speak and memory stream
    hr = conn->spStream->SetBaseStream(conn->qStream.getBaseStream(), SPDFID_WaveFormatEx, gWaveFormat);
    if (FAILED(hr)) {
        LPTSTR errorText = NULL;
        FormatMessage(
            FORMAT_MESSAGE_FROM_HMODULE
            | FORMAT_MESSAGE_ALLOCATE_BUFFER
            | FORMAT_MESSAGE_IGNORE_INSERTS,
            NULL,
            hr,
            MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
            (LPTSTR)&errorText,
            0,
            NULL
        );

        std::wcout << errorText << std::endl;
        LocalFree(errorText);
        errorText = NULL;
        return COULD_NOT_BIND_STREAM;
    }
    // Change voice output to target the new stream
    hr = conn->spVoice->SetOutput(conn->spStream, TRUE);
    if (FAILED(hr)) {
        LPTSTR errorText = NULL;
        FormatMessage(
            FORMAT_MESSAGE_FROM_HMODULE
            | FORMAT_MESSAGE_ALLOCATE_BUFFER
            | FORMAT_MESSAGE_IGNORE_INSERTS,
            NULL,
            hr,
            MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
            (LPTSTR)&errorText,
            0,
            NULL
        );

        std::wcout << errorText << std::endl;
        LocalFree(errorText);
        errorText = NULL;
        return COULD_NOT_BIND_OUTPUT;
    }
    // Start recording
    // Fixing ssml speak tag to add xml:lang
    std::wstring sentence = std::wstring(conn->sentence);
    std::basic_regex<wchar_t> tagSearch(
        L"xml:lang=",
        std::regex_constants::ECMAScript | std::regex_constants::icase
    );
    if (!std::regex_search(sentence, tagSearch)) {
        std::basic_regex<wchar_t> speakTagSearch(
            L"(<speak[^>]*)>",
            std::regex_constants::ECMAScript
        );
        std::wostringstream newTagStream;
        newTagStream << L"$1 xml:lang=\"" << it->second.language << "\">";
        sentence = std::regex_replace(sentence, speakTagSearch, newTagStream.str());

    }
#if _DEBUG
    else {
        std::wcout << L"Text contains an 'xml:lang' attribute" << std::endl;
    }

    std::wcout << L"SAPI " << c_name <<  L" Speaking " << sentence << std::endl;
#endif
    conn->qStream.startWritingPhase();
    try {
        hr = conn->spVoice->Speak(sentence.c_str(), CLIENT_SPEAK_FLAGS, 0);
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

        if (hr != S_OK) {
            std::wostringstream excep;
            excep << L"Unknown error code (0x" << std::hex << hr <<L") raised while speaking " << std::wstring(conn->sentence) << std::endl << L"With voice " << it->second.name << std::endl;
            // Raise exception to also get the error code from SAPI
            raiseIOException(env, (const jchar*)excep.str().c_str(), excep.str().size());
            return COULD_NOT_SPEAK;
        }

        conn->currentBookmarkIndex = 0;
        jlong duration = 0; //in milliseconds
        bool end = false;
        HRESULT eventFound = S_FALSE;
        do {
#if _DEBUG
            std::wcout << "Waiting for an event with " << (end ? "5000 ms" : "no") << " time out" << std::endl;
#endif
            // wait for a possible last event after end
            conn->spVoice->WaitForNotifyEvent(INFINITE);
            SPEVENT event;
            eventFound = S_FALSE;
            do {
                memset(&event, 0, sizeof(SPEVENT));
                eventFound = conn->spVoice->GetEvents(1, &event, NULL);
                if (eventFound == S_OK) {
#if _DEBUG
                    std::wcout << "event found : " << event.eEventId << std::endl;
#endif
                    switch (event.eEventId) {
                    case SPEI_VISEME:
                        duration += HIWORD(event.wParam);
                        break;
                    case SPEI_END_INPUT_STREAM:
                        end = true;
                        break;
                    case SPEI_TTS_BOOKMARK:
                        if (conn->currentBookmarkIndex == conn->bookmarkNames.size()) {
                            int newsize = 1 + (3 * static_cast<int>(conn->bookmarkNames.size())) / 2;
                            conn->bookmarkNames.resize(newsize);
                            conn->bookmarkPositions.resize(newsize);
                        }
                        //bookmarks are not pushed_back to prevent allocating/releasing all over the place
                        conn->bookmarkNames[conn->currentBookmarkIndex] = (const wchar_t*)(event.lParam);
                        conn->bookmarkPositions[conn->currentBookmarkIndex] = duration;
                        ++(conn->currentBookmarkIndex);
#if _DEBUG
                        std::wcout << "found mark " << (const wchar_t*)(event.lParam) << std::endl;
#endif
                        break;
                    }
                }
            } while (eventFound == S_OK);
        } while (!end);
    }
    catch (const std::exception& e) {
        std::wostringstream excep;
        excep << L"Exception raised while speaking " << std::wstring(conn->sentence) << std::endl << L"With voice " << it->second.name << L" : " << std::endl;
        excep << e.what() << std::endl;
        raiseIOException(env, (const jchar*)excep.str().c_str(), excep.str().size());
    }
    conn->qStream.endWritingPhase();
    // end recording
    return SAPI_OK;
}



JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_getStreamSize(JNIEnv*, jclass, jlong connection) {
    Connection* conn = reinterpret_cast<Connection*>(connection);
    return conn->qStream.in_avail();
}

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_readStream(JNIEnv* env, jclass, jlong connection, jbyteArray dest, jint offset) {
    Connection* conn = reinterpret_cast<Connection*>(connection);
    
    //the array 'dest' is assumed to be big enough thanks to
    //a previous call to getStreamSize()
    const signed char* audio;
    int size;
    while ((audio = conn->qStream.nextChunk(&size))) {
        env->SetByteArrayRegion(dest, offset, size, (const jbyte*)audio);
        offset += size;
    }

    return offset;
}


struct VoiceVendorToJString {
    static jstring convert(const SapiVoice::Map::const_iterator& it, JNIEnv* env) {
        const wchar_t* str = it->second.vendor.c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_getVoiceVendors(JNIEnv* env, jclass) {
#if _DEBUG
    std::wcout << "Getting voice vendors" << std::endl;
#endif
    if (gAllVoices != NULL) {
        return newJavaArray<SapiVoice::Map::iterator, VoiceVendorToJString>(
            env,
            gAllVoices->begin(),
            gAllVoices->size(),
            "java/lang/String"
            );
    }
    else return emptyJavaArray(env, "java/lang/String");
}



struct VoiceNameToJString {
    static jstring convert(const SapiVoice::Map::const_iterator& it, JNIEnv* env) {
        const wchar_t* str = it->second.name.c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};

JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_getVoiceNames(JNIEnv* env, jclass) {
#if _DEBUG
    std::wcout << "Getting voice names" << std::endl;
#endif
    if (gAllVoices != NULL) {
        return newJavaArray<SapiVoice::Map::iterator, VoiceNameToJString>(
            env,
            gAllVoices->begin(),
            gAllVoices->size(),
            "java/lang/String"
            );
    }
    else return emptyJavaArray(env, "java/lang/String");
}


struct VoiceLocaleToJString {
    static jstring convert(const SapiVoice::Map::const_iterator& it, JNIEnv* env) {
        const wchar_t* str = it->second.language.c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_getVoiceLocales(JNIEnv* env, jclass) {
#if _DEBUG
    std::wcout << "Getting voice locales" << std::endl;
#endif
    if (gAllVoices != NULL) {
        return newJavaArray<SapiVoice::Map::iterator, VoiceLocaleToJString>(
            env,
            gAllVoices->begin(),
            gAllVoices->size(),
            "java/lang/String"
            );
    }
    else return emptyJavaArray(env, "java/lang/String");
}


struct VoiceGenderToJString {
    static jstring convert(const SapiVoice::Map::const_iterator& it, JNIEnv* env) {
        const wchar_t* str = it->second.gender.c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_getVoiceGenders(JNIEnv* env, jclass)
{
#if _DEBUG
    std::wcout << "Getting voice genders" << std::endl;
#endif
    if (gAllVoices != NULL) {
        return newJavaArray<SapiVoice::Map::iterator, VoiceGenderToJString>(
            env,
            gAllVoices->begin(),
            gAllVoices->size(),
            "java/lang/String"
            );
    }
    else return emptyJavaArray(env, "java/lang/String");
}

struct VoiceAgeToJString {
    static jstring convert(const SapiVoice::Map::const_iterator& it, JNIEnv* env) {
        const wchar_t* str = it->second.age.c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_getVoiceAges(JNIEnv* env, jclass)
{
#if _DEBUG
    std::wcout << "Getting voice ages" << std::endl;
#endif
    if (gAllVoices != NULL) {
        return newJavaArray<SapiVoice::Map::iterator, VoiceAgeToJString>(
            env,
            gAllVoices->begin(),
            gAllVoices->size(),
            "java/lang/String"
            );
    }
    else return emptyJavaArray(env, "java/lang/String");

}


struct BookMarkNamesToJString {
    static jstring convert(const std::vector<std::wstring>::const_iterator& it, JNIEnv* env) {
        const wchar_t* str = it->c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_getBookmarkNames(JNIEnv* env, jclass, jlong connection) {
    Connection* conn = reinterpret_cast<Connection*>(connection);
    return newJavaArray<std::vector<std::wstring>::iterator, BookMarkNamesToJString>(
        env,
        conn->bookmarkNames.begin(),
        (size_t) conn->currentBookmarkIndex,
        "java/lang/String"
    );
}


JNIEXPORT jlongArray JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_getBookmarkPositions(JNIEnv* env, jclass, jlong connection) {
    Connection* conn = reinterpret_cast<Connection*>(connection);

    jlongArray result = env->NewLongArray(conn->currentBookmarkIndex);
    if (conn->bookmarkPositions.size() > 0) {
        env->SetLongArrayRegion(result, 0, conn->currentBookmarkIndex, &(conn->bookmarkPositions[0]));
    }
    return result;
}

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_dispose(JNIEnv*, jclass) {
#if _DEBUG
    std::wcout << "Disposing of sapi" << std::endl;
#endif
    
    {   
        // Close remaining connections
        if (openedConnection != NULL) {
            
            for (ConnectionsRegistry::iterator it = openedConnection->begin(); it != openedConnection->end(); ++it) {
#if _DEBUG
                std::wcout << "- Cleaning sapi connection " << *it << std::endl;
#endif
                Connection* conn = reinterpret_cast<Connection*>(*it);
                delete conn;
            }
            delete openedConnection;
        }
        // Release voices
        if (gAllVoices != NULL) {
            SapiVoice::Map::iterator it = gAllVoices->begin();
            for (; it != gAllVoices->end(); ++it) {
                if (it->second.rawVoice != NULL) {
                    it->second.rawVoice->Release();
                }
            }
            delete gAllVoices;
        }
        // Release the waveformat pointer
        if (gWaveFormat != NULL) {
            delete gWaveFormat;
        }
    }
    
    CoUninitialize();
#if _DEBUG
    std::wcout << "sapi disposed" << std::endl;
#endif
    return SAPI_OK;
}

