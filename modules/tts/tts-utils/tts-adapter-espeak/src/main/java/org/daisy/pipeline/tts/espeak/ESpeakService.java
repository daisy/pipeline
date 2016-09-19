package org.daisy.pipeline.tts.espeak;

import java.util.Map;

import org.daisy.common.shell.BinaryFinder;
import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.TTSEngine;

import com.google.common.base.Optional;

public class ESpeakService extends AbstractTTSService {

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		// settings
		String eSpeakPath = null;
		String prop = "espeak.path";
		eSpeakPath = System.getProperty(prop);
		if (eSpeakPath == null) {
			Optional<String> epath = BinaryFinder.find("espeak");
			if (!epath.isPresent()) {
				throw new SynthesisException(
				        "Cannot find eSpeak's binary using system property " + prop);
			}
			eSpeakPath = epath.get();
		}

		String priority = params.get("espeak.priority");
		int intPriority = 2;
		if (priority != null) {
			try {
				intPriority = Integer.valueOf(priority);
			} catch (NumberFormatException e) {

			}
		}

		return new ESpeakEngine(this, eSpeakPath, intPriority);
	}

	@Override
	public String getName() {
		return "espeak";
	}

	@Override
	public String getVersion() {
		return "cli";
	}
}
