package org.daisy.pipeline.tts.qfrency.impl;

import java.util.Map;
import java.util.Optional;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.shell.BinaryFinder;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

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
	private static final Property QFRENCY_ADDRESS = Properties.getProperty("org.daisy.pipeline.tts.qfrency.address",
	                                                                       false,
	                                                                       "Address if Qfrency speech engine server",
	                                                                       false,
	                                                                       "localhost");
	private static final Property QFRENCY_PRIORITY = Properties.getProperty("org.daisy.pipeline.tts.qfrency.priority",
	                                                                        true,
	                                                                        "Priority of Qfrency voices relative to voices of other speech engines",
	                                                                        false,
	                                                                        "2");

	@Override
	public TTSEngine newEngine(Map<String,String> properties) throws Throwable {
		// settings
		String qfrencyPath = QFRENCY_PATH.getValue(properties);
		if (qfrencyPath == null) {
			Optional<String> epath = BinaryFinder.find("synth");
			if (!epath.isPresent()) {
				throw new SynthesisException(
					"Cannot find qfrency's binary and system property " + QFRENCY_PATH.getName() + " is not set.");
			}
			qfrencyPath = epath.get();
		}
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
}
