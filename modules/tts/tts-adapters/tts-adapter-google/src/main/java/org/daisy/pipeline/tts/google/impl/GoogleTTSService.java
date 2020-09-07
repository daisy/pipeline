package org.daisy.pipeline.tts.google.impl;

import java.util.Map;

import javax.sound.sampled.AudioFormat;

import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Activate;
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
public class GoogleTTSService extends AbstractTTSService {

	@Activate
	protected void loadSSMLadapter() {
		super.loadSSMLadapter("/transform-ssml.xsl", GoogleTTSService.class);
	}

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		String apiKey = params.get("org.daisy.pipeline.tts.google.apikey");
		int sampleRate = convertToInt(params, "org.daisy.pipeline.tts.google.samplerate", 22050);
		int priority = convertToInt(params, "org.daisy.pipeline.tts.google.priority", 15);
		AudioFormat audioFormat = new AudioFormat((float) sampleRate, 16, 1, true, false);
		return new GoogleRestTTSEngine(this, apiKey, audioFormat, priority);
	}

	@Override
	public String getName() {
		return "google";
	}

	@Override
	public String getVersion() {
		return "rest";
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
