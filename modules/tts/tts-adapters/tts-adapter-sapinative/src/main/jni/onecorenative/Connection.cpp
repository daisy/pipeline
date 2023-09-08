#include "pch.h"
#include "Connection.h"
#include "Connection.g.cpp"

namespace winrt::onecorenative::implementation
{
	com_array<Windows::Media::SpeechSynthesis::VoiceInformation> Connection::voices()
	{
		auto synth = Windows::Media::SpeechSynthesis::SpeechSynthesizer();
		auto rawList = synth.AllVoices();
		return com_array<Windows::Media::SpeechSynthesis::VoiceInformation>(rawList.begin(), rawList.end());
	}

	Windows::Media::SpeechSynthesis::VoiceInformation Connection::defaultVoice()
	{
		auto synth = Windows::Media::SpeechSynthesis::SpeechSynthesizer();
		return synth.DefaultVoice();
	}

	com_array<uint8_t> Connection::speak(winrt::hstring ssml, winrt::hstring voiceName)
	{
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

		auto stream = synth.SynthesizeSsmlToStreamAsync(fixedSSML).get();
		auto source = Windows::Media::Core::MediaSource::CreateFromStream(stream, stream.ContentType());
		auto playback = Windows::Media::Playback::MediaPlaybackItem(source);
		unsigned int markIndex = 0;

		this->_marks = std::vector<Windows::Media::Core::SpeechCue>();
		for (unsigned int index = 0; index < playback.TimedMetadataTracks().Size(); index++) {
			auto timedTrack = playback.TimedMetadataTracks().GetAt(index);
			if (timedTrack.Id() == L"SpeechBookmark") {
				for (auto cue : timedTrack.Cues()) {
					auto realCue = cue.try_as<Windows::Media::Core::SpeechCue>();
					if (markIndex == this->_marks.size()) {
						int newsize = 1 + (3 * static_cast<int>(this->_marks.size())) / 2;
						this->_marks.resize(newsize);
					}
					//bookmarks are not pushed_back to prevent allocating/releasing all over the place
					this->_marks[markIndex] = realCue;
					++markIndex;
				}
			}
		}

		stream.Seek(0);
		auto reader = Windows::Storage::Streams::DataReader(stream);
		reader.LoadAsync(stream.Size()).get();
		auto buf = reader.ReadBuffer(stream.Size());

		uint8_t* data = buf.data();

		
		return com_array<uint8_t>(data, data + buf.Length());
	}

	com_array<hstring> Connection::marksNames()
	{
		auto names = com_array<hstring>(this->_marks.size());
		for (uint32_t i = 0; i < names.size(); i++)
		{
			names[i] = this->_marks[i].Text();
		}
		return names;
	}

	com_array<int64_t> Connection::marksPositions()
	{
		auto positions = com_array<int64_t>(this->_marks.size());
		for (uint32_t i = 0; i < positions.size(); i++)
		{
			positions[i] = std::chrono::duration_cast<std::chrono::milliseconds>(this->_marks[i].StartTime()).count();
		}
		return positions;
	}
}
