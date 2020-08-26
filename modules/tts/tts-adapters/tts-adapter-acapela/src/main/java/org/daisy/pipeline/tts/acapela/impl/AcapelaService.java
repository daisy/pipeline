package org.daisy.pipeline.tts.acapela.impl;

import java.util.Map;

import javax.sound.sampled.AudioFormat;

import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.RoundRobinLoadBalancer;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import com.sun.jna.Native;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;

@Component(
	name = "acapela-tts-service",
	service = { TTSService.class }
)
public class AcapelaService extends AbstractTTSService {
	
	@Activate
	protected void loadSSMLadapter() {
		super.loadSSMLadapter("/transform-ssml.xsl", AcapelaService.class);
	}
	
	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {

		if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
			throw new UnsupportedOperationException(
			        "This version of Acapela doesn't run on Windows.");
		}

		// settings
		int sampleRate = convertToInt(params, "org.daisy.pipeline.tts.acapela.samplerate", 22050);
		int reserved = convertToInt(params, "org.daisy.pipeline.tts.acapela.threads.reserved", 3);
		int speed = convertToInt(params, "org.daisy.pipeline.tts.acapela.speed", 300);
		int priority = convertToInt(params, "org.daisy.pipeline.tts.acapela.priority", 15);

		AudioFormat format = new AudioFormat((float) sampleRate, 16, 1, true, false);

		// load balancer
		String serversProperty = "org.daisy.pipeline.tts.acapela.servers";
		String serverVal = params.get(serversProperty);
		if (serverVal == null) {
			serverVal = "localhost:0";
		}
		RoundRobinLoadBalancer balancer;
		try {
			balancer = new RoundRobinLoadBalancer(serverVal, this);
		} catch (Exception e) {
			throw new SynthesisException("invalid value for property " + serversProperty);
		}

		//try loading the library where we have a chance to catch errors
		Native.loadLibrary(NscubeLibrary.JNA_LIBRARY_NAME, NscubeLibrary.class);

		return new AcapelaEngine(this, format, balancer, speed, reserved, priority);
	}

	@Override
	public String getName() {
		return "acapela";
	}

	@Override
	public String getVersion() {
		return "jna";
	}

	private static int convertToInt(Map<String, String> params, String prop, int defaultVal)
	        throws SynthesisException {
		String str = params.get(prop);
		if (str != null) {
			try {
				defaultVal = Integer.valueOf(str);
			} catch (NumberFormatException e) {
				throw new SynthesisException(str + " is not a valid a value for property "
				        + prop);
			}
		}
		return defaultVal;
	}

}
