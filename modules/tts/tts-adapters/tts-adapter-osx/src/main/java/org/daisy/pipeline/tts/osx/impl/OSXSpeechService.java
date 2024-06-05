package org.daisy.pipeline.tts.osx.impl;

import java.util.Map;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.spi.ActivationException;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Activate;
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
	private static Property OSXSPEECH_PRIORITY = null;

	private String sayPath;

	@Activate
	protected void activate() throws RuntimeException {
		if (!System.getProperty("os.name").toLowerCase().startsWith("mac os x"))
			failToActivate("osx-speech only works on macOS");
		sayPath = OSXSPEECH_PATH.getValue();
		if (OSXSPEECH_PRIORITY == null)
			OSXSPEECH_PRIORITY = Properties.getProperty("org.daisy.pipeline.tts.osxspeech.priority",
			                                            true,
			                                            "Priority of macOS voices relative to voices of other engines",
			                                            false,
			                                            "2");
	}

	@Override
	public TTSEngine newEngine(Map<String,String> properties) throws Throwable {
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

	@Override
	public String getDisplayName() {
		return "macOS";
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
