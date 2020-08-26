package org.daisy.pipeline.tts.qfrency.impl;

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
	name = "qfrency-tts-service",
	service = { TTSService.class }
)
public class QfrencyService extends AbstractTTSService {

	@Activate
	protected void loadSSMLadapter() {
		super.loadSSMLadapter("/transform-ssml.xsl", QfrencyService.class);
	}
	
	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		// settings
		String prop = "org.daisy.pipeline.tts.qfrency.path";
		String qfrencyPath = params.get(prop);
		if (qfrencyPath == null) {
			Optional<String> epath = BinaryFinder.find("synth");
			if (!epath.isPresent()) {
				throw new SynthesisException(
				        "Cannot find qfrency's binary and system property " + prop
				                + " is not set.");
			}
			qfrencyPath = epath.get();
		}
		String address = params.get("org.daisy.pipeline.tts.qfrency.address");
		if (address==null)
			address="localhost";
		String priority = params.get("org.daisy.pipeline.tts.qfrency.priority");
		int intPriority = 2;
		if (priority != null) {
			try {
				intPriority = Integer.valueOf(priority);
			} catch (NumberFormatException e) {

			}
		}

		return new QfrencyEngine(this, qfrencyPath, address, intPriority);
	}

	@Override
	public String getName() {
		return "qfrency_cli";
	}

	@Override
	public String getVersion() {
		return "cli";
	}
}
