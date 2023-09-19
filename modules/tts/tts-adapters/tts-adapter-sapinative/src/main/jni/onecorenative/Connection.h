#pragma once

#include "Connection.g.h"
#include "SpeechResult.g.h"

namespace winrt::onecorenative::implementation
{
    struct Connection : ConnectionT<Connection>
    {
        Connection() = default;
        
        static com_array<Windows::Media::SpeechSynthesis::VoiceInformation> voices();
        static Windows::Media::SpeechSynthesis::VoiceInformation defaultVoice();
        static onecorenative::SpeechResult speak(winrt::hstring ssml, winrt::hstring voiceName);
    private:
        std::vector<Windows::Media::Core::SpeechCue> _marks = std::vector<Windows::Media::Core::SpeechCue>();
    };

    struct SpeechResult : SpeechResultT<SpeechResult> {
        SpeechResult(
            array_view<uint8_t const> speech,
            array_view<hstring const> marksNames,
            array_view<int64_t const> marksPositions,
            uint8_t errorFlag
        );

        uint8_t getErrorFlag() noexcept;
        com_array<uint8_t>& getSpeech() noexcept;
        com_array<hstring>& getMarksNames() noexcept;
        com_array<int64_t>& getMarksPositions() noexcept;

        void setErrorFlag(uint8_t _errorFlag) noexcept;

    private:
        com_array<uint8_t> speech;
        com_array<hstring> marksNames;
        com_array<int64_t> marksPositions;
        uint8_t errorFlag = 0;
    };
} 

namespace winrt::onecorenative::factory_implementation
{
    struct Connection : ConnectionT<Connection, implementation::Connection>
    {
    };

    struct SpeechResult : SpeechResultT<SpeechResult, implementation::SpeechResult>
    {
    };
}
