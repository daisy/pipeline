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


#pragma region Utilities

/// <summary>
/// Release all stored references and 
/// </summary>
/// <param name="refsStack"></param>
inline void exitCom(std::stack<IUnknown*>& refsStack) {
    // release all com stored references and quit com context
    while (!refsStack.empty()) {
        IUnknown* toRelease = refsStack.top();
        refsStack.pop();
        toRelease->Release();
    }
    CoUninitialize();
}


/// <summary>
/// Get SAPI voices 
/// </summary>
/// <param name="env"></param>
/// <returns></returns>
inline Voice<ISpObjectToken*>::Map getVoices(JNIEnv* env, std::stack<IUnknown*>& currentRefStack) {
    auto voicesMap = Voice<ISpObjectToken*>::Map();

    //get the voice information  
    ISpObjectTokenCategory* category;
    if (FAILED(CoCreateInstance(CLSID_SpObjectTokenCategory, NULL, CLSCTX_ALL, IID_ISpObjectTokenCategory, (void**)(&category)))) {
        CoUninitialize();
        raiseException(env, COULD_NOT_CREATE_CATEGORY, L"Could not create sapi token category");
        return voicesMap;
    }
    category->AddRef();
    category->SetId(SPCAT_VOICES, false);

    IEnumSpObjectTokens* cpEnum;
    if (FAILED(category->EnumTokens(NULL, NULL, &cpEnum))) {
        category->Release();
        exitCom(currentRefStack);
        raiseException(env, COULD_NOT_ENUM_CATEGORY, L"Could not enumerate sapi categories");
        return voicesMap;
    }
    cpEnum->AddRef();

    ULONG count = 0;
    if (FAILED(cpEnum->GetCount(&count))) {
        cpEnum->Release();
        category->Release();
        exitCom(currentRefStack);
        raiseException(env, COULD_NOT_COUNT_ENUM, L"Could not count enumeration");
        return voicesMap;
    }

    wchar_t* vendor; //encoded as UTF-16
    wchar_t* name;
    ISpObjectToken* cpToken; // Kept in COM memory (AddRef without matching release)
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
                        LCID actualCode = static_cast<LCID>(std::wcstoul(langCode, nullptr, 16));
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

                    // 2022-10-04 : replacing original vendor by sapi keyword 
                    // for voice engine identification on the pipeline side (sapi or onecore)
                    // Note that onecore does not provide vendor informations while sapi does
                    // but the use of "sapi" as vendor could lead to errors in voice 
                    // identification if some vendors decided to provided voices with the same
                    // name
                    voicesMap.insert(std::make_pair(
                        std::pair<std::wstring, std::wstring>(L"sapi", name),
                        Voice<ISpObjectToken*>(cpToken,
                            std::wstring(name),
                            std::wstring(L"sapi"),
                            std::wstring(langCode != nullptr ? langCode : L""),
                            std::wstring(gender != nullptr ? gender : L""),
                            std::wstring(age != nullptr ? age : L"")
                        )
                    ));
                    currentRefStack.push(cpToken);
                }
                else cpToken->Release();
                key->Release();
            }
            else cpToken->Release();
        }
    }
    cpEnum->Release();
    category->Release();
    return voicesMap;
}


/// <summary>
/// 
/// </summary>
/// <param name="env"></param>
/// <param name="sampleRate"></param>
/// <param name="bitsPerSample"></param>
/// <returns></returns>
inline WAVEFORMATEX* getWaveFormat(JNIEnv* env, int sampleRate, short bitsPerSample) {
    if ((
            bitsPerSample != 8 &&
            bitsPerSample != 16
        ) || (
            sampleRate != 8000 &&
            sampleRate != 11025 &&
            sampleRate != 16000 &&
            sampleRate != 22050 &&
            sampleRate != 44100 &&
            sampleRate != 48000)
        
        ) {
        raiseException(env, UNSUPPORTED_AUDIO_FORMAT, L"Unsupported audio format provided");
        CoUninitialize();
        return NULL;
    }

    WAVEFORMATEX* waveFormat = new WAVEFORMATEX;
    waveFormat->wFormatTag = WAVE_FORMAT_PCM;
    waveFormat->nChannels = 1;
    waveFormat->nSamplesPerSec = sampleRate;
    waveFormat->wBitsPerSample = bitsPerSample;
    waveFormat->nBlockAlign = (waveFormat->nChannels * waveFormat->wBitsPerSample) / 8;
    waveFormat->nAvgBytesPerSec = waveFormat->nBlockAlign * waveFormat->nSamplesPerSec;
    waveFormat->cbSize = 0;

    return waveFormat;
}

#pragma endregion

/// <summary>
/// Initialize the lib (mainly checks)
/// </summary>
/// <param name="env"></param>
/// <param name=""></param>
/// <param name="sampleRate"></param>
/// <param name="bitsPerSample"></param>
/// <returns></returns>
JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_initialize(JNIEnv* env, jclass, jint sampleRate, jshort bitsPerSample) {
    std::stack<IUnknown*> refsStack = std::stack<IUnknown*>();
    HRESULT hr;
    hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);
    //hr = CoInitializeEx(NULL, COINIT_APARTMENTTHREADED);
    if (hr != S_OK && hr != S_FALSE) {
        raiseException(env, COULD_NOT_INIT_COM, L"Could not init com server");
        return COULD_NOT_INIT_COM;
    }
    
    // Checking if the format provided is correct
    getWaveFormat(env, sampleRate, bitsPerSample);

    // try to get the list of voices, and return an error if cannot be done
    Voice<ISpObjectToken*>::Map voices = getVoices(env, refsStack);
    if (voices.size() == 0) {
        return COULD_NOT_SET_VOICE;
    }
    exitCom(refsStack);
    return SAPI_OK;
}

/// <summary>
/// Getting voices from java
/// </summary>
/// <param name="env"></param>
/// <param name=""></param>
/// <returns></returns>
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_getVoices(JNIEnv* env, jclass) {
    std::stack<IUnknown*> refsStack = std::stack<IUnknown*>();
    HRESULT hr;
    hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);
    if (hr != S_OK && hr != S_FALSE) {
        raiseException(env, COULD_NOT_INIT_COM, L"Could not init com server");
        return NULL;
    }
    Voice<ISpObjectToken*>::Map voices = getVoices(env, refsStack);
    if (voices.size() == 0) {
        exitCom(refsStack);
        raiseException(env, COULD_NOT_SET_VOICE, L"No voice returned by SAPI");
    }

    exitCom(refsStack);
    return VoicesMapToPipelineVoicesArray<ISpObjectToken*>(env, voices, L"sapi");
}


/// <summary>
/// New speak functions with data isolation
/// </summary>
/// <param name="env"></param>
/// <param name=""></param>
/// <param name="voiceVendor"></param>
/// <param name="voiceName"></param>
/// <param name="text"></param>
/// <param name="sampleRate"></param>
/// <param name="bitsPerSample"></param>
/// <returns></returns>
JNIEXPORT jobject JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_speak(JNIEnv* env, jclass, jstring voiceVendor, jstring voiceName, jstring text, jint sampleRate, jshort bitsPerSample) {
    std::stack<IUnknown*> refsStack = std::stack<IUnknown*>();
    HRESULT hr;
    hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);
    //hr = CoInitializeEx(NULL, COINIT_APARTMENTTHREADED);

    if (hr != S_OK && hr != S_FALSE) {
        raiseException(env, COULD_NOT_INIT_COM, L"Could not init com server");
        return NULL;
    }

    std::wstring vendor = jstringToWstring(env, voiceVendor);
    std::wstring name = jstringToWstring(env, voiceName);
    Voice<ISpObjectToken*>::Map voices = getVoices(env, refsStack);
    Voice<ISpObjectToken*>::Map::iterator it = voices.find(
        std::make_pair(vendor, name)
    );
    if (it == voices.end()) {
        exitCom(refsStack);
        raiseException(env, VOICE_NOT_FOUND, L"Voice not found");
        return NULL;
    }

    ISpObjectToken* foundVoice = it->second.rawVoice;

    ISpVoice* talker;
    hr = CoCreateInstance(CLSID_SpVoice, NULL, CLSCTX_ALL, IID_ISpVoice, (void**)(&talker));
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

        exitCom(refsStack);
        raiseIOException(env, (const jchar*)excep.str().c_str(), excep.str().size());
        errorText = NULL;
        return NULL;
    }
    talker->AddRef();
    refsStack.push(talker);

    if (FAILED(talker->SetVoice(foundVoice))) {
        exitCom(refsStack);
        raiseException(env, COULD_NOT_SET_VOICE, L"Could not set voice as talking voice");
        return NULL;
    }

    ULONGLONG ullEventInterest = CROSS_PLATFORM_SPFEI(SPEI_END_INPUT_STREAM) |
        CROSS_PLATFORM_SPFEI(SPEI_TTS_BOOKMARK) |
        CROSS_PLATFORM_SPFEI(SPEI_VISEME);

    if (FAILED(talker->SetInterest(ullEventInterest, ullEventInterest))) {
        exitCom(refsStack);
        raiseException(env, COULD_NOT_SET_EVENT_INTERESTS, L"Could not set event interests");
        return NULL;
    }

    HANDLE hSpeechNotifyEvent = talker->GetNotifyEventHandle();
    if (INVALID_HANDLE_VALUE == hSpeechNotifyEvent) {
        exitCom(refsStack);
        raiseException(env, COULD_NOT_LISTEN_TO_EVENTS, L"Could not listen to SAPI events");
        return NULL;
    }
    ISpStream* speakingStream;
    // Create a new speak stream
    hr = CoCreateInstance(CLSID_SpStream, NULL, CLSCTX_ALL, IID_ISpStream, (void**)(&speakingStream));
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

        exitCom(refsStack);
        raiseException(env, COULD_NOT_INIT_STREAM, L"Could not init SAPI Speaking stream");
        return NULL;
    }
    speakingStream->AddRef();
    refsStack.push(speakingStream);
    // Create a memory stream and bind it to the speak stream
    WinQueueStream dataStream;
    // Create a new memory stream if a wave format has been initialized
    if (!dataStream.initialize()) {
        exitCom(refsStack);
        raiseException(env, COULD_NOT_INIT_STREAM, L"Could not init Memory buffering stream");
        return NULL;
    }
    // initialize creates a ref in COM, so keep the real Istream object in refstack for disposal when exiting
    refsStack.push(dataStream.getBaseStream());
    WAVEFORMATEX* format = getWaveFormat(env, sampleRate, bitsPerSample);
    
    // Bind speak and memory stream
    hr = speakingStream->SetBaseStream(dataStream.getBaseStream(), SPDFID_WaveFormatEx, format);
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
        format = NULL;
        exitCom(refsStack);
        raiseException(env, COULD_NOT_BIND_STREAM, L"Could not bind Memory buffering stream to speaking stream");
        return NULL;
    }

    // Change voice output to target the new stream
    hr = talker->SetOutput(speakingStream, TRUE);
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
        format = NULL;
        exitCom(refsStack);
        raiseException(env, COULD_NOT_BIND_OUTPUT, L"Could not bind outstream to SAPI Voice output");
        return NULL;
    }

    
    std::wstring sentence = jstringToWstring(env, text);
    std::basic_regex<wchar_t> tagSearch(
        L"xml:lang=",
        std::regex_constants::ECMAScript | std::regex_constants::icase
    );
    // SSML text correction
    if (!std::regex_search(sentence, tagSearch)) {
        std::basic_regex<wchar_t> speakTagSearch(
            L"(<speak[^>]*)>",
            std::regex_constants::ECMAScript
        );
        std::wostringstream newTagStream;
        newTagStream << L"$1 xml:lang=\"" << it->second.language << "\">";
        sentence = std::regex_replace(sentence, speakTagSearch, newTagStream.str());

    }

    dataStream.startWritingPhase();
    int 						currentBookmarkIndex = 0;
    std::vector<std::wstring>	bookmarkNames;
    std::vector<jlong>			bookmarkPositions;
    try {
        hr = talker->Speak(sentence.c_str(), CLIENT_SPEAK_FLAGS, 0);
        if (hr == E_INVALIDARG) {
            format = NULL;
            exitCom(refsStack);
            raiseException(env, COULD_NOT_SPEAK_INVALIDARG, L"Could not speak : Invalid arguments reported");
            return NULL;
        }
            

        if (hr == E_POINTER) {
            format = NULL;
            exitCom(refsStack);
            raiseException(env, COULD_NOT_SPEAK_E_POINTER, L"Could not speak : Invalid pointer");
            return NULL;
        }

        if (hr == E_OUTOFMEMORY) {
            format = NULL;
            exitCom(refsStack);
            raiseException(env, COULD_NOT_SPEAK_OUTOFMEMORY, L"Could not speak : Out of memory exception");
            return NULL;
        }

        if (hr == SPERR_INVALID_FLAGS) {
            format = NULL;
            exitCom(refsStack);
            raiseException(env, COULD_NOT_SPEAK_INVALIDFLAGS, L"Could not speak : Invalid sapi flags");
            return NULL;
        }

        if (hr == SPERR_DEVICE_BUSY) {
            format = NULL;
            exitCom(refsStack);
            raiseException(env, COULD_NOT_SPEAK_BUSY, L"Could not speak : Voice is busy");
            return NULL;
        }

        if (hr == SPERR_UNSUPPORTED_FORMAT) {
            format = NULL;
            exitCom(refsStack);
            raiseException(env, COULD_NOT_SPEAK_THIS_FORMAT, L"Could not speak : unsupported text format received");
            return NULL;
        }

        if (hr != S_OK) {
            format = NULL;
            exitCom(refsStack);
            raiseException(env, hr, L"Could not speak : Unknown error code");
            return NULL;
        }
        
        
        jlong duration = 0; //in milliseconds
        bool end = false;
        HRESULT eventFound = S_FALSE;
        do {
            // wait for a possible last event after end
            talker->WaitForNotifyEvent(INFINITE);
            SPEVENT event;
            eventFound = S_FALSE;
            do {
                memset(&event, 0, sizeof(SPEVENT));
                eventFound = talker->GetEvents(1, &event, NULL);
                if (eventFound == S_OK) {
                    switch (event.eEventId) {
                    case SPEI_VISEME:
                        duration += HIWORD(event.wParam);
                        break;
                    case SPEI_END_INPUT_STREAM:
                        end = true;
                        break;
                    case SPEI_TTS_BOOKMARK:
                        if (currentBookmarkIndex == bookmarkNames.size()) {
                            int newsize = 1 + (3 * static_cast<int>(bookmarkNames.size())) / 2;
                            bookmarkNames.resize(newsize);
                            bookmarkPositions.resize(newsize);
                        }
                        //bookmarks are not pushed_back to prevent allocating/releasing all over the place
                        bookmarkNames[currentBookmarkIndex] = (const wchar_t*)(event.lParam);
                        bookmarkPositions[currentBookmarkIndex] = duration;
                        ++(currentBookmarkIndex);
                        break;
                    }
                }
            } while (eventFound == S_OK);
        } while (!end);
    }
    catch (const std::exception& e) {
        std::wostringstream excep;
        excep << L"Exception raised while speaking " << sentence << std::endl << L"With voice " << it->second.name << L" : " << std::endl;
        excep << e.what() << std::endl;
        exitCom(refsStack);
        raiseIOException(env, (const jchar*)excep.str().c_str(), excep.str().size());
        return NULL;
    }
    dataStream.endWritingPhase();

    const int dataSize = dataStream.in_avail();
    uint8_t* fullAudio = new uint8_t[dataStream.in_avail()];
    memset((void*)fullAudio, 0, dataSize);

    const signed char* audio;
    int size;
    int offset = 0;
    while ((audio = dataStream.nextChunk(&size))) {
        errno_t copyRes = memcpy_s((void*)(fullAudio + offset), size * sizeof(signed char), audio, size * sizeof(signed char));
        if (copyRes != 0) {
            format = NULL;
            exitCom(refsStack);
            raiseException(env, copyRes, L"Could not transfer data : an error occured while copying data");
        }
        offset += size;
    }

    format = NULL;
    exitCom(refsStack);
    return newSynthesisResult<std::vector<std::wstring>, std::vector<std::wstring>::iterator>(env, dataSize, fullAudio, bookmarkNames, bookmarkPositions.data());
    
}

JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_SAPI_dispose(JNIEnv*, jclass)
{
    return SAPI_OK;
}


