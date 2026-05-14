#pragma once

#include <string>
#include <map>

/// <summary>
/// Voice template to be used for sapi and onecore calls.<br/>
/// This class also expose a Map subclass to manage Voice dictionnary using a (name,vendor) paire as reference key.
/// </summary>
/// <typeparam name="RawVoiceType">Type of token used by the API to select a voice for speaking action</typeparam>
template<typename RawVoiceType>
class Voice {
public:
    /// <summary>
    /// Voice ordered list, that should start with the default voice
    /// </summary>
    using List = std::list<Voice>;

    /// <summary>
    /// Voice Name
    /// </summary>
    std::wstring name;

    /// <summary>
    /// Voice vendor
    /// </summary>
    std::wstring vendor;

    /// <summary>
    /// Voice language (or locale code)
    /// </summary>
    std::wstring language = L"";

    /// <summary>
    /// Voice biological gender (Male, Female or empty for unspecified)
    /// </summary>
    std::wstring gender = L"";

    /// <summary>
    /// Voice age (usually child, adult or elderly)
    /// </summary>
    std::wstring age = L"";

    /// <summary>
    /// Voice original data structure or identifier, 
    /// that should be use to select the voice to use it in the corresponding API
    /// </summary>
    const RawVoiceType rawVoice;

    Voice<RawVoiceType>(
        const RawVoiceType& rawVoice,
        std::wstring name,
        std::wstring vendor,
        std::wstring language = L"",
        std::wstring gender = L"",
        std::wstring age = L""
    ) : rawVoice(rawVoice),
        name(name),
        vendor(vendor),
        language(language),
        gender(gender),
        age(age)
    { }

};


