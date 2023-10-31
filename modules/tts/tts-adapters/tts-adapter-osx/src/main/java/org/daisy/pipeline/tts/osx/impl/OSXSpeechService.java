package org.daisy.pipeline.tts.osx.impl;

import java.util.Map;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "osx-tts-service",
	service = { TTSService.class }
)
public class OSXSpeechService implements TTSService {

	private static final Property OSXSPEECH_PATH = Properties.getProperty("org.daisy.pipeline.tts.osxspeech.path",
	                                                                      false,
	                                                                      "Path of macOS' command line program `say`",
	                                                                      false,
	                                                                      "/usr/bin/say");
	private static final Property OSXSPEECH_PRIORITY = Properties.getProperty("org.daisy.pipeline.tts.osxspeech.priority",
	                                                                          true,
	                                                                          "Priority of macOS voices relative to voices of other engines",
	                                                                          false,
	                                                                          "2");

	@Override
	public TTSEngine newEngine(Map<String,String> properties) throws Throwable {
		String sayPath = OSXSPEECH_PATH.getValue(properties);
		int priority; {
			String prop = OSXSPEECH_PRIORITY.getValue(properties);
			try {
				priority = Integer.valueOf(prop);
			} catch (NumberFormatException e) {
				throw new SynthesisException(OSXSPEECH_PRIORITY.getName() + ": " + prop + "is not a valid number", e);
			}
		}
		return new OSXSpeechEngine(this, sayPath, priority);
	}

	@Override
	public String getName() {
		return "osx-speech";
	}
}
