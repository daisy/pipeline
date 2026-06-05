package org.daisy.pipeline.tts.espeak.impl;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.shell.BinaryFinder;
import org.daisy.common.spi.ActivationException;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Activate;
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
	private static Property ESPEAK_PRIORITY = null;

	private File eSpeakFile;

	@Activate
	protected void activate() throws RuntimeException {
		eSpeakFile = null; {
			String prop = ESPEAK_PATH.getValue();
			if (prop != null) {
				Optional<File> epath = BinaryFinder.get(prop);
				if (epath.isPresent())
					eSpeakFile = epath.get();
			}
			if (eSpeakFile == null) {
				Optional<String> epath = BinaryFinder.find("espeak");
				if (epath.isPresent())
					eSpeakFile = new File(epath.get());
			}
		}
		if (eSpeakFile == null)
			failToActivate("Cannot find eSpeak's binary using system property " + ESPEAK_PATH.getName());
		if (ESPEAK_PRIORITY == null)
			ESPEAK_PRIORITY = Properties.getProperty("org.daisy.pipeline.tts.espeak.priority",
	 		                                         true,
	 		                                         "Priority of eSpeak voices relative to voices of other speech engines",
	 		                                         false,
	 		                                         "2");
	}

	@Override
	public TTSEngine newEngine(Map<String,String> properties) throws Throwable {
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

	@Override
	public String getDisplayName() {
		return "eSpeak";
	}

	private static void failToActivate(String message) throws RuntimeException {
		failToActivate(message, null);
	}

    private static void failToActivate(String message, Throwable cause) throws RuntimeException {
		try {
			SPIHelper.failToActivate(message, cause);
		} catch (NoClassDefFoundError e) {
			// we are probably in OSGi context
			throw new RuntimeException(message, cause);
		}
	}

	// static nested class in order to delay class loading
	private static class SPIHelper {
		private SPIHelper() {}
		public static void failToActivate(String message, Throwable cause) throws ActivationException {
			throw new ActivationException(message, cause);
		}
	}
}
