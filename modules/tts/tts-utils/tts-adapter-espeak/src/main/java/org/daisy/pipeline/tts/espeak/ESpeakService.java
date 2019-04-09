package org.daisy.pipeline.tts.espeak;

import java.util.Map;
import java.util.Optional;

import org.daisy.common.shell.BinaryFinder;
import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;

@Component(
	name = "espeak-tts-service",
	service = { TTSService.class }
)
public class ESpeakService extends AbstractTTSService {
	
	@Activate
	protected void loadSSMLadapter() {
		super.loadSSMLadapter("/transform-ssml.xsl", ESpeakService.class);
	}

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		// settings
		String eSpeakPath = null;
		String prop = "org.daisy.pipeline.tts.espeak.path";
		eSpeakPath = params.get(prop);
		if (eSpeakPath == null) {
			Optional<String> epath = BinaryFinder.find("espeak");
			if (!epath.isPresent()) {
				throw new SynthesisException(
				        "Cannot find eSpeak's binary using system property " + prop);
			}
			eSpeakPath = epath.get();
		}

		String priority = params.get("org.daisy.pipeline.tts.espeak.priority");
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
