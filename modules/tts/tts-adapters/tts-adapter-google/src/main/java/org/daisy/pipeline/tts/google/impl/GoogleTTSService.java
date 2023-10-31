package org.daisy.pipeline.tts.google.impl;

import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFormat;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
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

	private static final Property GOOGLE_APIKEY = Properties.getProperty("org.daisy.pipeline.tts.google.apikey",
	                                                                     true,
	                                                                     "API key for Google cloud speech engine",
	                                                                     true,
	                                                                     null);
	private static final Property GOOGLE_SAMPLERATE = Properties.getProperty("org.daisy.pipeline.tts.google.samplerate",
	                                                                         true,
	                                                                         "Audio sample rate of Google cloud voices (in Hz)",
	                                                                         false,
	                                                                         "22050");
	private static final Property GOOGLE_ADDRESS = Properties.getProperty("org.daisy.pipeline.tts.google.address",
	                                                                      false,
	                                                                      "Address of Google cloud speech engine server",
	                                                                      false,
	                                                                      "https://texttospeech.googleapis.com");
	private static final Property GOOGLE_PRIORITY = Properties.getProperty("org.daisy.pipeline.tts.google.priority",
	                                                                       true,
	                                                                       "Priority of Google cloud voices relative to voices of other engines",
	                                                                       false,
	                                                                       "15");

	@Override
	public TTSEngine newEngine(Map<String,String> properties) throws Throwable {
		String serverAddress = GOOGLE_ADDRESS.getValue(properties); // this is a hidden parameter, it is meant to be used in tests only
		String apiKey = GOOGLE_APIKEY.getValue(properties);
		if (apiKey == null)
			throw new SynthesisException("Property not set : " + GOOGLE_APIKEY.getName());
		int sampleRate = getPropertyAsInt(properties, GOOGLE_SAMPLERATE).get();
		int priority = getPropertyAsInt(properties, GOOGLE_PRIORITY).get();
		AudioFormat audioFormat = new AudioFormat((float) sampleRate, 16, 1, true, false);
		return new GoogleRestTTSEngine(this, serverAddress, apiKey, audioFormat, priority);
	}

	@Override
	public String getName() {
		return "google";
	}

	private static Optional<Integer> getPropertyAsInt(Map<String,String> properties, Property prop) throws SynthesisException {
		String str = prop.getValue(properties);
		if (str != null) {
			try {
				return Optional.of(Integer.valueOf(str));
			} catch (NumberFormatException e) {
				throw new SynthesisException(str + " is not a valid a value for property " + prop.getName());
			}
		}
		return Optional.empty();
	}
}
