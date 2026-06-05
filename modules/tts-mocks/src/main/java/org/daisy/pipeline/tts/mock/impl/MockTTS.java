package org.daisy.pipeline.tts.mock.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.LanguageRange;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "mock-tts",
	service = { TTSService.class }
)
public class MockTTS implements TTSService {
	
	final static Logger logger = LoggerFactory.getLogger(MockTTS.class);
	final static URL alexWaveOut = URLs.getResourceFromJAR("/mock-tts/alex.wav", MockTTS.class);
	final static URL vickiWaveOut = URLs.getResourceFromJAR("/mock-tts/vicki.wav", MockTTS.class);
	final static URL daisyPipelineWaveOut = URLs.getResourceFromJAR("/mock-tts/daisy-pipeline.wav", MockTTS.class);
	
	@Override
	public TTSEngine newEngine(Map<String,String> params) throws Throwable {
		return new TTSEngine(MockTTS.this) {
			
			@Override
			public SynthesisResult synthesize(XdmNode sentence, Voice voice, TTSResource threadResources)
					throws SynthesisException, InterruptedException {
				logger.debug("Synthesizing sentence: " + sentence);
				try {
					return new SynthesisResult(
						createAudioStream(
							(voice.getName().equals("alex")
								? MockTTS.alexWaveOut
								: voice.getName().equals("vicki")
									? MockTTS.vickiWaveOut
									: MockTTS.daisyPipelineWaveOut).openStream())); }
				catch (Exception e) {
					throw new SynthesisException(e); }
			}
			
			@Override
			public Collection<Voice> getAvailableVoices() throws SynthesisException, InterruptedException {
				List<Voice> voices = new ArrayList<Voice>();
				voices.add(new Voice(getProvider().getName(), "alex", new LanguageRange("en"), Gender.MALE_ADULT));
				voices.add(new Voice(getProvider().getName(), "vicki", new LanguageRange("en"), Gender.FEMALE_ADULT));
				voices.add(new Voice(getProvider().getName(), "foo", new LanguageRange("*"), Gender.ANY));
				return voices;
			}
			
			@Override
			public int getOverallPriority() {
				return 2;
			}
		};
	}
	
	@Override
	public String getName() {
		return "mock-tts";
	}
}
