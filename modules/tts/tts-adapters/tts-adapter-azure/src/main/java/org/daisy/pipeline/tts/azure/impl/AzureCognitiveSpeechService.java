package org.daisy.pipeline.tts.azure.impl;

import java.util.Map;

import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "azure-tts-service",
	service = { TTSService.class }
)
public class AzureCognitiveSpeechService implements TTSService {

	@Override
	public String getName() {
		return "azure";
	}

	@Override
	public AzureCognitiveSpeechEngine newEngine(Map<String, String> params) throws Throwable {
		String key; {
			String prop = "org.daisy.pipeline.tts.azure.key";
			key = params.get(prop);
			if (key == null)
				throw new SynthesisException("Property not set : " + prop);
		}
		String region; {
			String prop = "org.daisy.pipeline.tts.azure.region";
			region = params.get(prop);
			if (region == null)
				throw new SynthesisException("Property not set : " + prop);
		}
		int priority = getParamAsInt(params, "org.daisy.pipeline.tts.azure.priority", 15);
		int threads = getParamAsInt(params, "org.daisy.pipeline.tts.azure.threads", 2);
		return new AzureCognitiveSpeechEngine(this, key, region, threads, priority);
	}

	private static int getParamAsInt(Map<String,String> params, String prop, int defaultVal) throws SynthesisException {
		String str = params.get(prop);
		if (str != null) {
			try {
				defaultVal = Integer.valueOf(str);
			} catch (NumberFormatException e) {
				throw new SynthesisException(str + " is not a valid a value for property " + prop);
			}
		}
		return defaultVal;
	}
}
