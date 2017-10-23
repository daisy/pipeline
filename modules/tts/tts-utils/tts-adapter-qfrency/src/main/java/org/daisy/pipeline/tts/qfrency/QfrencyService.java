package org.daisy.pipeline.tts.qfrency;

import java.util.Map;

import org.daisy.common.shell.BinaryFinder;
import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.TTSEngine;

import com.google.common.base.Optional;

public class QfrencyService extends AbstractTTSService {

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		// settings
		String prop = "qfrency.path";
		String qfrencyPath = System.getProperty(prop);
		if (qfrencyPath == null) {
			Optional<String> epath = BinaryFinder.find("synth");
			if (!epath.isPresent()) {
				throw new SynthesisException(
				        "Cannot find qfrency's binary and system property " + prop
				                + " is not set.");
			}
			qfrencyPath = epath.get();
		}
		String address = System.getProperty("qfrency.address");
		if (address==null)
			address="localhost";
		String priority = params.get("qfrency.priority");
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
