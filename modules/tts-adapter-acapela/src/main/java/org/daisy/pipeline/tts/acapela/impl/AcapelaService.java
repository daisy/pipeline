package org.daisy.pipeline.tts.acapela.impl;

import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFormat;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.spi.ActivationException;
import org.daisy.common.spi.annotations.LoadWith;
import org.daisy.pipeline.tts.RoundRobinLoadBalancer;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import com.sun.jna.Native;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "acapela-tts-service",
	service = { TTSService.class }
)
@LoadWith(JNAClassLoader.class)
public class AcapelaService implements TTSService {

	private static Property ACAPELA_SAMPLERATE = null;
	private static Property ACAPELA_SERVERS = null;
	private static Property ACAPELA_SPEED = null;
	private static Property ACAPELA_THREADS = null;
	private static Property ACAPELA_PRIORITY = null;

	@Activate
	protected void activate() throws RuntimeException {
		if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
			failToActivate("This version of Acapela doesn't run on Windows");
		try {
			Native.loadLibrary(NscubeLibrary.JNA_LIBRARY_NAME, NscubeLibrary.class);
		} catch (Throwable e) {
			failToActivate("Unable to load 'nscube' library", e);
		}
		if (ACAPELA_SAMPLERATE == null)
			ACAPELA_SAMPLERATE = Properties.getProperty("org.daisy.pipeline.tts.acapela.samplerate",
			                                            true,
			                                            "Audio sample rate of Acapela voices (in Hz)",
			                                            false,
			                                            "22050");
		if (ACAPELA_SERVERS == null)
			ACAPELA_SERVERS = Properties.getProperty("org.daisy.pipeline.tts.acapela.servers",
			                                         false,
			                                         "Addresses of Acapela speech engine servers",
			                                         false,
			                                         "localhost:0");
		if (ACAPELA_SPEED == null)
			ACAPELA_SPEED = Properties.getProperty("org.daisy.pipeline.tts.acapela.speed",
			                                       false,
			                                       "Expected ms per word for Acapela speech engine",
			                                       false,
			                                       "300");
		if (ACAPELA_THREADS == null)
			ACAPELA_THREADS = Properties.getProperty("org.daisy.pipeline.tts.acapela.threads.reserved",
			                                         false,
			                                         "Number of reserved threads for Acapela speech engine",
			                                         false,
			                                         "3");
		if (ACAPELA_PRIORITY == null)
			ACAPELA_PRIORITY = Properties.getProperty("org.daisy.pipeline.tts.acapela.priority",
			                                          true,
			                                          "Priority of Acapela voices relative to voices of other speech engines",
			                                          false,
			                                          "15");
	}

	@Override
	public TTSEngine newEngine(Map<String,String> properties) throws Throwable {

		// settings
		int sampleRate = getPropertyAsInt(properties, ACAPELA_SAMPLERATE).get();
		int reserved = getPropertyAsInt(properties, ACAPELA_THREADS).get();
		int speed = getPropertyAsInt(properties, ACAPELA_SPEED).get();
		int priority = getPropertyAsInt(properties, ACAPELA_PRIORITY).get();

		AudioFormat format = new AudioFormat((float) sampleRate, 16, 1, true, false);

		// load balancer
		String serverVal = ACAPELA_SERVERS.getValue(properties);
		RoundRobinLoadBalancer balancer;
		try {
			balancer = new RoundRobinLoadBalancer(serverVal, this);
		} catch (Exception e) {
			throw new SynthesisException("invalid value for property " + ACAPELA_SERVERS.getName());
		}
		return new AcapelaEngine(this, format, balancer, speed, reserved, priority);
	}

	@Override
	public String getName() {
		return "acapela";
	}

	private static Optional<Integer> getPropertyAsInt(Map<String,String> properties, Property prop) throws SynthesisException {
		String str = prop.getValue(properties);
		if (str != null) {
			try {
				return Optional.of(Integer.valueOf(str));
			} catch (NumberFormatException e) {
				throw new SynthesisException(str + " is not a valid a value for property " + prop.getName());
			}
		}
		return Optional.empty();
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
