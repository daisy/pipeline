package org.daisy.pipeline.tts.azure.impl;

import java.util.Map;
import java.util.Optional;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "azure-tts-service",
	service = { TTSService.class }
)
public class AzureCognitiveSpeechService implements TTSService {

	private static final Property AZURE_KEY = Properties.getProperty("org.daisy.pipeline.tts.azure.key",
	                                                                 true,
	                                                                 "Access key for Azure cognitive speech engine",
	                                                                 true,
	                                                                 null);
	private static final Property AZURE_REGION = Properties.getProperty("org.daisy.pipeline.tts.azure.region",
	                                                                    true,
	                                                                    "Region for Azure cognitive speech engine",
	                                                                    true,
	                                                                    null);
	private static final Property AZURE_THREADS = Properties.getProperty("org.daisy.pipeline.tts.azure.threads",
	                                                                     false,
	                                                                     "Number of reserved threads for Azure cognitive speech engine",
	                                                                     false,
	                                                                     "2");
	private static final Property AZURE_PRIORITY = Properties.getProperty("org.daisy.pipeline.tts.azure.priority",
	                                                                      true,
	                                                                      "Priority of Azure voices relative to voices of other engines",
	                                                                      false,
	                                                                      "15");

	@Override
	public String getName() {
		return "azure";
	}

	@Override
	public AzureCognitiveSpeechEngine newEngine(Map<String,String> properties) throws Throwable {
		String key = AZURE_KEY.getValue(properties);
		if (key == null)
			throw new SynthesisException("Property not set: " + AZURE_KEY.getName());
		String region = AZURE_REGION.getValue(properties);
		if (region == null)
			throw new SynthesisException("Property not set: " + AZURE_REGION.getName());
		int priority = getPropertyAsInt(properties, AZURE_PRIORITY).get();
		int threads = getPropertyAsInt(properties, AZURE_THREADS).get();
		return new AzureCognitiveSpeechEngine(this, key, region, threads, priority);
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
