package org.daisy.pipeline.tts.qfrency.impl;

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
	name = "qfrency-tts-service",
	service = { TTSService.class }
)
public class QfrencyService implements TTSService {

	private static final Property QFRENCY_PATH = Properties.getProperty("org.daisy.pipeline.tts.qfrency.path",
	                                                                    false,
	                                                                    "Path of `synth` client program for communicating with Qfrency server",
	                                                                    false,
	                                                                    null);
	private static Property QFRENCY_ADDRESS = null;
	private static Property QFRENCY_PRIORITY = null;

	private String qfrencyPath;

	@Activate
	protected void activate() throws RuntimeException {
	    qfrencyPath = QFRENCY_PATH.getValue();
		if (qfrencyPath == null) {
			Optional<String> epath = BinaryFinder.find("synth");
			if (epath.isPresent())
				qfrencyPath = epath.get();
			else
				failToActivate("Cannot find qfrency's binary and property '" + QFRENCY_PATH.getName() + "' is not set");
		}
		if (QFRENCY_ADDRESS == null)
			QFRENCY_ADDRESS = Properties.getProperty("org.daisy.pipeline.tts.qfrency.address",
			                                         false,
			                                         "Address if Qfrency speech engine server",
			                                         false,
			                                         "localhost");
		if (QFRENCY_PRIORITY == null)
			QFRENCY_PRIORITY = Properties.getProperty("org.daisy.pipeline.tts.qfrency.priority",
			                                          true,
			                                          "Priority of Qfrency voices relative to voices of other speech engines",
			                                          false,
			                                          "2");
	}

	@Override
	public TTSEngine newEngine(Map<String,String> properties) throws Throwable {
		if (!new File(qfrencyPath).exists())
			throw new SynthesisException("No executable at " + qfrencyPath);
		String address = QFRENCY_ADDRESS.getValue(properties);
		int priority; {
			String prop = QFRENCY_PRIORITY.getValue(properties);
			try {
				priority = Integer.valueOf(prop);
			} catch (NumberFormatException e) {
				throw new SynthesisException(QFRENCY_PRIORITY.getName() + ": " + prop + "is not a valid number", e);
			}
		}
		return new QfrencyEngine(this, qfrencyPath, address, priority);
	}

	@Override
	public String getName() {
		return "qfrency_cli";
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
