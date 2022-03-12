package org.daisy.pipeline.tts.osx.impl;

import java.util.Map;

import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "osx-tts-service",
	service = { TTSService.class }
)
public class OSXSpeechService implements TTSService {
	
	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		// settings
		String prop = "org.daisy.pipeline.tts.osxspeech.path";
		String sayPath = params.get(prop);
		if (sayPath == null) {
			sayPath = "/usr/bin/say";
		}
		String priority = params.get("org.daisy.pipeline.tts.osxspeech.priority");
		int intPriority = 2;
		if (priority != null) {
			try {
				intPriority = Integer.valueOf(priority);
			} catch (NumberFormatException e) {

			}
		}

		//allocate the engine
		return new OSXSpeechEngine(this, sayPath, intPriority);
	}

	@Override
	public String getName() {
		return "osx-speech";
	}
}
