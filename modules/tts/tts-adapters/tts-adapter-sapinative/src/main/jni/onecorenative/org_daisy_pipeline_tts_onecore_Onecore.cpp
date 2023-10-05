#include "pch.h"

// OneCore version of the sapinative bridge

#define MAX_SENTENCE_SIZE (1024*512)
#define MAX_VOICE_NAME_SIZE 128


#define AUDIO_CHUNK_SIZE 4096


using namespace winrt::Windows::Media::SpeechSynthesis;

/// <summary>
/// Connection to onecore API
/// </summary>
using winrtConnection = winrt::onecorenative::Connection;

/// <summary>
/// Retrieve voices from an opened connection to onecore API
/// </summary>
/// <param name="onecore"></param>
/// <returns></returns>
inline Voice<winrt::hstring>::List getVoices(winrtConnection& onecore) {
    auto voicesList = Voice<winrt::hstring>::List();
    auto defaultVoice = onecore.defaultVoice();
    for each (auto rawVoice in onecore.voices())
    {
        auto voice = Voice<winrt::hstring>(
            rawVoice.DisplayName(),
            static_cast<std::wstring>(rawVoice.DisplayName()),
            // there is no vendor information provide with VoiceInformation on onecore, 
            // so we use "onecore" for pipeline reporting of voices to select the correct engine
            L"onecore",
            static_cast<std::wstring>(rawVoice.Language()),
            static_cast<std::wstring>(rawVoice.Gender() == VoiceGender::Male ? L"Male" : L"Female"),
            std::wstring(L"")
        );
        if (rawVoice.DisplayName() == defaultVoice.DisplayName()) {
            voicesList.insert(voicesList.begin(), voice);
        }
        else voicesList.insert(voicesList.end(), voice);
    }
    return voicesList;
}

/// <summary>
/// Initialize native method to check if onecore is reachable
/// </summary>
/// <param name="env"></param>
/// <param name=""></param>
/// <returns></returns>
JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_initialize(JNIEnv* env, jclass) {
    // try to connect to onecore for initialization
    try {
        
        winrtConnection onecore = winrtConnection();
        Voice<winrt::hstring>::List voices = getVoices(onecore);
        if (voices.size() == 0) {
            return COULD_NOT_SET_VOICE;
        }
    }
    catch (winrt::hresult_error const& ex)
    {

        winrt::hresult hr = ex.code();
        std::wstring message = std::wstring(ex.message().c_str());
        raiseException(env, hr.value, message);
        return COULD_NOT_INIT_COM;
    }
    return SAPI_OK;
}

/// <summary>
/// Native method to retrieve Onecore voices
/// </summary>
/// <param name="env"></param>
/// <param name=""></param>
/// <returns></returns>
JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_getVoices(JNIEnv* env, jclass) {
    winrtConnection onecore = winrtConnection();
    Voice<winrt::hstring>::List voices = getVoices(onecore);
    return VoicesListToPipelineVoicesArray<winrt::hstring>(env, voices, L"onecore");
}

/// <summary>
/// Speak a sentence with selected voice using Onecore
/// </summary>
/// <param name="env">Java calling environment</param>
/// <param name="">class of the object (not used here)</param>
/// <param name="voiceVendor">Vendor of the voice (sapi or onecore, as onecore does not store vendor)</param>
/// <param name="voiceName">Voice name (i.e. "Microsoft David")</param>
/// <param name="text">the sentence to be spoken</param>
/// 
/// <returns>A NativeSynthesisResult java object returned by <see cref="newSynthesisResult()" /> </returns>
JNIEXPORT jobject JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_speak(JNIEnv* env, jclass, jstring voiceVendor, jstring voiceName, jstring text) {
    
    std::wstring vendor = jstringToWstring(env, voiceVendor);
    std::wstring name = jstringToWstring(env, voiceName);

    winrtConnection onecore = winrtConnection();
    Voice<winrt::hstring>::List voices = getVoices(onecore);
    Voice<winrt::hstring>::List::iterator it = voices.begin();
    while (it != voices.end()
        && (it->vendor.compare(vendor) != 0
            || it->name.compare(name) != 0
            )
    ) {
        ++it;
    }
    if (it == voices.end()) {
        raiseException(env, VOICE_NOT_FOUND, L"Voice not found");
        return NULL;
    }

    // VoiceInformation seems to create an exception, so we use the voice display name for now
    winrt::hstring ssmltext = winrt::hstring(jstringToWstring(env, text));
    winrt::hstring foundVoiceName = it->rawVoice;
        
    try {
        winrt::onecorenative::SpeechResult res = onecore.speak(ssmltext, foundVoiceName);
        winrt::com_array<uint8_t> streamData = res.getSpeech();
        winrt::com_array<winrt::hstring> marksNames = res.getMarksNames();
        winrt::com_array<int64_t> marksPositions = res.getMarksPositions();
        if (res.getErrorFlag() > 0) {
            raiseException(env, marksPositions[0], marksNames[0].c_str());
            return NULL;
        }
        return newSynthesisResult<winrt::com_array<winrt::hstring>, winrt::com_array<winrt::hstring>::iterator> (env, streamData.size(), streamData.data(), marksNames, marksPositions.data());
    }
    catch (winrt::hresult_error const& ex)
    {
            
        winrt::hresult hr = ex.code();
        std::wstring message = std::wstring(ex.message().c_str());
        raiseException(env, hr.value, message);
       
        return NULL;
    }
}

/// <summary>
/// Called when releasing the library.
/// </summary>
/// <param name=""></param>
/// <param name=""></param>
/// <returns></returns>
JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_dispose(JNIEnv*, jclass) {

    return SAPI_OK;
}