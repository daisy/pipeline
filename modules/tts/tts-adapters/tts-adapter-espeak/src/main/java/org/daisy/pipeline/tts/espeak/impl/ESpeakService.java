package org.daisy.pipeline.tts.espeak.impl;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.daisy.common.shell.BinaryFinder;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "espeak-tts-service",
	service = { TTSService.class }
)
public class ESpeakService implements TTSService {

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		// settings
		File eSpeakFile = null; {
			String prop = "org.daisy.pipeline.tts.espeak.path";
			String val = params.get(prop);
			if (val != null) {
				Optional<File> epath = BinaryFinder.get(val);
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
					"Cannot find eSpeak's binary using system property " + prop);
			}
		}

		int intPriority = 2; {
			String priority = params.get("org.daisy.pipeline.tts.espeak.priority");
			if (priority != null) {
				try {
					intPriority = Integer.valueOf(priority);
				} catch (NumberFormatException e) {
				}
			}
		}

		return new ESpeakEngine(this, eSpeakFile, intPriority);
	}

	@Override
	public String getName() {
		return "espeak";
	}
}
