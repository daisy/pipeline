#include "pch.h"
#include "Connection.h"
#include "Connection.g.cpp"

#include "SpeechResult.g.cpp"

#include <shared_mutex>


using namespace winrt::Windows::Media::SpeechSynthesis;
using namespace winrt::Windows::Storage::Streams;
using namespace winrt::Windows::Foundation;
using UniqueLock = std::unique_lock<std::shared_timed_mutex>;

namespace winrt::onecorenative::implementation
{
	com_array<Windows::Media::SpeechSynthesis::VoiceInformation> Connection::voices()
	{
		auto synth = Windows::Media::SpeechSynthesis::SpeechSynthesizer();
		auto rawList = synth.AllVoices();
		auto voiceList = com_array<Windows::Media::SpeechSynthesis::VoiceInformation>(rawList.begin(), rawList.end());
		return voiceList;
	}

	Windows::Media::SpeechSynthesis::VoiceInformation Connection::defaultVoice()
	{
		auto synth = Windows::Media::SpeechSynthesis::SpeechSynthesizer();
		Windows::Media::SpeechSynthesis::VoiceInformation voice = synth.DefaultVoice();
		return synth.DefaultVoice();
	}

	// Taken from NVDA connector to onecore, apply also to sapi on windows 11 :
    // Using mutex and lock on the synthesis calls to prevent fast fail crash
    std::shared_timed_mutex SPEECH_MUTEX{};
	// setting timeout to 10 seconds as first unlock can be quite long
	std::chrono::duration MAX_WAIT(std::chrono::seconds(10));

	
	onecorenative::SpeechResult Connection::speak(winrt::hstring ssml, winrt::hstring voiceName) 
	{
		try {
			auto synth = Windows::Media::SpeechSynthesis::SpeechSynthesizer();
			if (voiceName != synth.DefaultVoice().DisplayName()) {
				for each (auto voice in synth.AllVoices())
				{
					if (voice.DisplayName() == voiceName) {
						synth.Voice(voice);
						break;
					}
				}
			}
			winrt::Windows::Media::SpeechSynthesis::VoiceInformation selectedVoice = synth.Voice();

			// NOTE : TTS specification of Microsoft requires the xml:lang to be set, else if it will raise an error ...
			// So we need to check if it is defined, and set it if not based on the selected voice language (just in case)
			auto ssmlDocument = winrt::Windows::Data::Xml::Dom::XmlDocument();
			ssmlDocument.LoadXml(ssml);
			auto speakNode = ssmlDocument.ChildNodes().GetAt(0);
			auto langAttribute = speakNode.Attributes().GetNamedItem(L"xml:lang");
			if (langAttribute == NULL) {
				winrt::Windows::Data::Xml::Dom::XmlAttribute newLang = ssmlDocument.CreateAttribute(L"xml:lang");
				newLang.Value(selectedVoice.Language());
				speakNode.Attributes().SetNamedItem(newLang);
			}
			auto fixedSSML = speakNode.GetXml();
			SpeechSynthesisStream stream = { nullptr };
			try {
				// locking with mutex speech synthesis to avoid multithreading crash here
				// Locking only the synthesis call helps reduce overhead
				UniqueLock lock(SPEECH_MUTEX, std::defer_lock);
				bool owned = lock.try_lock();
				if (!owned) {
					owned = lock.try_lock_for(MAX_WAIT);
				}
				if (owned) {
					stream = synth.SynthesizeSsmlToStreamAsync(fixedSSML).get();
					lock.unlock();
				}
				else {
					com_array<hstring> errorMessage = com_array<hstring>((uint32_t)1);
					com_array<int64_t> errorCode = com_array<int64_t>((uint32_t)1);
					errorMessage[0] = hstring(L"Could not unlock speech synthesizer");
					errorCode[0] = -1;
					return winrt::make<implementation::SpeechResult>(
						array_view<uint8_t>(), errorMessage, errorCode, 1
					);
				}
			}
			catch (winrt::hresult_error const& e) {
				com_array<hstring> errorMessage = com_array<hstring>((uint32_t)1);
				com_array<int64_t> errorCode = com_array<int64_t>((uint32_t)1);
				errorMessage[0] = e.message();
				errorCode[0] = e.code();
				return winrt::make<implementation::SpeechResult>(
					array_view<uint8_t>(), errorMessage, errorCode, 1
				);
			}
			const std::uint32_t size = static_cast<std::uint32_t>(stream.Size());
			auto buffer = Buffer(size);
			try {
				stream.ReadAsync(buffer, size, InputStreamOptions::None).get();
				// Data has been read from the speech stream.
				// Pass it to the callback.
			}
			catch (winrt::hresult_error const& e) {
				com_array<hstring> errorMessage = com_array<hstring>((uint32_t)1);
				com_array<int64_t> errorCode = com_array<int64_t>((uint32_t)1);
				errorMessage[0] = e.message();
				errorCode[0] = e.code();
				return winrt::make<implementation::SpeechResult>(
					array_view<uint8_t>(), errorMessage, errorCode, 1
				);
			}
			auto source = Windows::Media::Core::MediaSource::CreateFromStream(stream, stream.ContentType());
			auto playback = Windows::Media::Playback::MediaPlaybackItem(source);
			unsigned int markIndex = 0;

			auto marks = std::vector<Windows::Media::Core::SpeechCue>();
			for (unsigned int index = 0; index < playback.TimedMetadataTracks().Size(); index++) {
				auto timedTrack = playback.TimedMetadataTracks().GetAt(index);
				if (timedTrack.Id() == L"SpeechBookmark") {
					for (auto cue : timedTrack.Cues()) {
						auto realCue = cue.try_as<Windows::Media::Core::SpeechCue>();
						if (markIndex == marks.size()) {
							int newsize = 1 + (3 * static_cast<int>(marks.size())) / 2;
							marks.resize(newsize);
						}
						//bookmarks are not pushed_back to prevent allocating/releasing all over the place
						marks[markIndex] = realCue;
						++markIndex;
					}
				}
			}
			auto names = com_array<hstring>(marks.size());
			for (uint32_t i = 0; i < names.size(); i++)
			{
				names[i] = marks[i].Text();
			}
			auto positions = com_array<int64_t>(marks.size());
			for (uint32_t i = 0; i < positions.size(); i++)
			{
				positions[i] = std::chrono::duration_cast<std::chrono::milliseconds>(marks[i].StartTime()).count();
			}

			return winrt::make<implementation::SpeechResult>(
				com_array<uint8_t>(buffer.data(), buffer.data() + size),
				names,
				positions,
				0
			);

		}
		catch (winrt::hresult_error const& e) {
			com_array<hstring> errorMessage = com_array<hstring>((uint32_t)1);
			com_array<int64_t> errorCode = com_array<int64_t>((uint32_t)1);
			errorMessage[0] = e.message();
			errorCode[0] = e.code();
			return winrt::make<implementation::SpeechResult>(
				array_view<uint8_t>(), errorMessage, errorCode, 1
			);
		}
		catch (std::exception const& e) {
			com_array<hstring> errorMessage = com_array<hstring>((uint32_t)1);
			com_array<int64_t> errorCode = com_array<int64_t>((uint32_t)1);
			
			size_t converted;
			size_t sizeOfMessage = strlen(e.what()) + 1;
			const char* message = e.what();
			wchar_t* messageWide = new wchar_t[sizeOfMessage];
			mbstowcs_s(&converted, messageWide, sizeOfMessage, message, (size_t)sizeOfMessage - 1);
			errorMessage[0] = hstring(messageWide);
			errorCode[0] = -2;
			return winrt::make<implementation::SpeechResult>(
				array_view<uint8_t>(), errorMessage, errorCode, 1
			);
		}
		catch (...) {
			com_array<hstring> errorMessage = com_array<hstring>((uint32_t)1);
			com_array<int64_t> errorCode = com_array<int64_t>((uint32_t)1);
			errorMessage[0] = hstring(L"Unexpected error during speech");
			errorCode[0] = -1;
			return winrt::make<implementation::SpeechResult>(
				array_view<uint8_t>(), errorMessage, errorCode, 1
			);
		}
		
	}

	SpeechResult::SpeechResult(
		array_view<uint8_t const> _speech,
		array_view<hstring const> _marksNames,
		array_view<int64_t const> _marksPositions,
		uint8_t errorFlag
	)
	{
		this->speech = com_array<uint8_t>(_speech.size());
		for (uint32_t i = 0; i < _speech.size(); ++i) {
			this->speech[i] = _speech[i];
		}
		this->marksNames = com_array<hstring>(_marksNames.size());
		for (uint32_t i = 0; i < _marksNames.size(); ++i) {
			this->marksNames[i] = _marksNames[i];
		}
		this->marksPositions = com_array<int64_t>(_marksPositions.size());
		for (uint32_t i = 0; i < _marksPositions.size(); ++i) {
			this->marksPositions[i] = _marksPositions[i];
		}
		this->errorFlag = errorFlag;
	}

	

	uint8_t SpeechResult::getErrorFlag() noexcept
	{
		return this->errorFlag;
	}

	com_array<uint8_t>& SpeechResult::getSpeech() noexcept {
		return this->speech;
	}

	com_array<hstring>& SpeechResult::getMarksNames() noexcept {
		return this->marksNames;
	}

	com_array<int64_t>& SpeechResult::getMarksPositions() noexcept {
		return this->marksPositions;
	}
	void SpeechResult::setErrorFlag(uint8_t _errorFlag) noexcept
	{
		this->errorFlag = errorFlag;
		return;
	}
}
