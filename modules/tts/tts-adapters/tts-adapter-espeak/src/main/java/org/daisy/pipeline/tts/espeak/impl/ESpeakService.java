package org.daisy.pipeline.tts.espeak.impl;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.shell.BinaryFinder;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "espeak-tts-service",
	service = { TTSService.class }
)
public class ESpeakService implements TTSService {

	private static final Property ESPEAK_PATH = Properties.getProperty("org.daisy.pipeline.tts.espeak.path",
	                                                                   false,
	                                                                   "Path of eSpeak executable",
	                                                                   false,
	                                                                   null);
	private static final Property ESPEAK_PRIORITY = Properties.getProperty("org.daisy.pipeline.tts.espeak.priority",
	                                                                       true,
	                                                                       "Priority of eSpeak voices relative to voices of other speech engines",
	                                                                       false,
	                                                                       "2");

	@Override
	public TTSEngine newEngine(Map<String,String> properties) throws Throwable {
		// settings
		File eSpeakFile = null; {
			String prop = ESPEAK_PATH.getValue(properties);
			if (prop != null) {
				Optional<File> epath = BinaryFinder.get(prop);
				if (epath.isPresent()) {
					eSpeakFile = epath.get();
				}
			}
			if (eSpeakFile == null) {
				Optional<String> epath = BinaryFinder.find("espeak");
				if (epath.isPresent()) {
					eSpeakFile = new File(epath.get());
				}
			}
			if (eSpeakFile == null) {
				throw new SynthesisException(
					"Cannot find eSpeak's binary using system property " + ESPEAK_PATH.getName());
			}
		}
		int priority; {
			String prop = ESPEAK_PRIORITY.getValue(properties);
			try {
				priority = Integer.valueOf(prop);
			} catch (NumberFormatException e) {
				throw new SynthesisException(ESPEAK_PRIORITY.getName() + ": " + prop + "is not a valid number", e);
			}
		}
		return new ESpeakEngine(this, eSpeakFile, priority);
	}

	@Override
	public String getName() {
		return "espeak";
	}
}
