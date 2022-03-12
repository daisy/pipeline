package org.daisy.pipeline.tts.google.impl;

import java.util.Map;

import javax.sound.sampled.AudioFormat;

import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Component;

/**
 * OSGI service to instantiate the Google Cloud Text-to-speech engine adapter
 *
 * @author Louis Caille @ braillenet.org
 */
@Component(
	name = "google-tts-service",
	service = { TTSService.class }
)
public class GoogleTTSService implements TTSService {

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		String serverAddress; {
			String prop = "org.daisy.pipeline.tts.google.address"; // this is a hidden parameter, it is meant to be used in tests only
			String val = params.get(prop);
			if (val != null) {
				serverAddress = val;
			} else {
				serverAddress = "https://texttospeech.googleapis.com";
			}
		}
		String apiKey; {
			String prop = "org.daisy.pipeline.tts.google.apikey";
			apiKey = params.get(prop);
			if (apiKey == null)
				throw new SynthesisException("Property not set : " + prop);
		}
		int sampleRate = convertToInt(params, "org.daisy.pipeline.tts.google.samplerate", 22050);
		int priority = convertToInt(params, "org.daisy.pipeline.tts.google.priority", 15);
		AudioFormat audioFormat = new AudioFormat((float) sampleRate, 16, 1, true, false);
		return new GoogleRestTTSEngine(this, serverAddress, apiKey, audioFormat, priority);
	}

	@Override
	public String getName() {
		return "google";
	}

	private static int convertToInt(Map<String, String> params, String prop, int defaultVal)
	        throws SynthesisException {
		String str = params.get(prop);
		if (str != null) {
			try {
				defaultVal = Integer.valueOf(str);
			} catch (NumberFormatException e) {
				throw new SynthesisException(str + " is not a valid a value for property "
				        + prop);
			}
		}
		return defaultVal;
	}
}
