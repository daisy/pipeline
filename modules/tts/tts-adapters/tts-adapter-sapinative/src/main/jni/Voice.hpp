#pragma once

#include "pch.h"

/// <summary>
/// Voice template to be used for sapi and onecore calls.<br/>
/// This class also expose a Map subclass to manage Voice dictionnary using a (name,vendor) paire as reference key.
/// </summary>
/// <typeparam name="TokenPointerType">Type of token used by the API to select a voice for speaking action</typeparam>
template<typename TokenPointerType>
class Voice {
public:
    /// <summary>
    /// Voice map using name and vendor as reference key
    /// </summary>
    using Map = std::map<std::pair<std::wstring, std::wstring>, Voice>;

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
    /// Pointer to the voice original data structure or identifier, the should be use to select it in the corresponding API
    /// </summary>
    TokenPointerType voicePointer;

    Voice<TokenPointerType>(
        TokenPointerType voicePointer,
        std::wstring name,
        std::wstring vendor,
        std::wstring language = L"",
        std::wstring gender = L"",
        std::wstring age = L""
    ) {
        this->voicePointer = voicePointer;
        this->name = name;
        this->vendor = vendor;
        this->language = language;
        this->gender = gender;
        this->age = age;
    }

};


