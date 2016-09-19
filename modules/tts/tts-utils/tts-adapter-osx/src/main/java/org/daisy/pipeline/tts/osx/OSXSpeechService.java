package org.daisy.pipeline.tts.osx;

import java.util.Map;

import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.TTSEngine;

public class OSXSpeechService extends AbstractTTSService {

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		// settings
		String prop = "osxspeech.path";
		String sayPath = System.getProperty(prop);
		if (sayPath == null) {
			sayPath = "/usr/bin/say";
		}
		String priority = params.get("osxspeech.priority");
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

	@Override
	public String getVersion() {
		return "cli";
	}
}
