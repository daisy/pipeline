#pragma once

#include "Connection.g.h"

namespace winrt::onecorenative::implementation
{
    struct Connection : ConnectionT<Connection>
    {
        Connection() = default;
        
        static com_array<Windows::Media::SpeechSynthesis::VoiceInformation> voices();
        com_array<uint8_t> speak(winrt::hstring ssml, winrt::hstring voiceName);
        com_array<hstring> marksNames();
        com_array<int64_t> marksPositions();
    private:
        std::vector<Windows::Media::Core::SpeechCue> _marks = std::vector<Windows::Media::Core::SpeechCue>();
    };
}

namespace winrt::onecorenative::factory_implementation
{
    struct Connection : ConnectionT<Connection, implementation::Connection>
    {
    };
}
