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


inline Voice<winrt::hstring>::Map getVoices(winrtConnection& onecore) {
    auto voicesMap = Voice<winrt::hstring>::Map();
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

        voicesMap.insert(std::make_pair(
            std::pair<std::wstring, std::wstring>(voice.vendor, voice.name),
            voice
        ));
    }
    return voicesMap;
}


JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_initialize(JNIEnv* env, jclass) {
    // try to connect to onecore for initialization
    try {
        
        winrtConnection onecore = winrtConnection();
        Voice<winrt::hstring>::Map voices = getVoices(onecore);
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


JNIEXPORT jobjectArray JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_getVoices(JNIEnv* env, jclass) {
    winrtConnection onecore = winrtConnection();
    Voice<winrt::hstring>::Map voices = getVoices(onecore);
    return VoicesMapToPipelineVoicesArray<winrt::hstring>(env, voices, L"onecore");
}

// ---- END CONNECTION HANDLING

// ---- TEXT TO SPEECH

JNIEXPORT jobject JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_speak(JNIEnv* env, jclass, jstring voiceVendor, jstring voiceName, jstring text) {
    
    
    winrtConnection onecore = winrtConnection();
    Voice<winrt::hstring>::Map voices = getVoices(onecore);
    Voice<winrt::hstring>::Map::iterator it = voices.find(
        std::make_pair(
            jstringToWstring(env, voiceVendor), 
            jstringToWstring(env, voiceName)
        )
    );
    if (it == voices.end()) {
        raiseException(env, VOICE_NOT_FOUND, L"Voice not found");
        return NULL;
    }

    // VoiceInformation seems to create an exception, so we use the voice display name for now
    winrt::hstring ssmltext = winrt::hstring(jstringToWstring(env, text));
    winrt::hstring foundVoiceName = it->second.rawVoice;
        
    try {
        winrt::com_array<uint8_t> streamData = onecore.speak(ssmltext, foundVoiceName);
        winrt::com_array<winrt::hstring> marksNames = onecore.marksNames();
        winrt::com_array<int64_t> marksPositions = onecore.marksPositions();
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


JNIEXPORT jint JNICALL Java_org_daisy_pipeline_tts_onecore_Onecore_dispose(JNIEnv*, jclass) {

    return SAPI_OK;
}