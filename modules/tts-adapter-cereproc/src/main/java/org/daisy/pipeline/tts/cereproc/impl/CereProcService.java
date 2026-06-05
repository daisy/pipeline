package org.daisy.pipeline.tts.cereproc.impl;

import java.io.File;
import java.util.Map;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.spi.ActivationException;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CereProcService implements TTSService {

	private final static Logger logger = LoggerFactory.getLogger(CereProcService.class);

	private final static Property CEREPROC_CLIENT = Properties.getProperty("org.daisy.pipeline.tts.cereproc.client",
	                                                                       false,
	                                                                       "Path of client program for communicating with CereProc server",
	                                                                       false,
	                                                                       "/usr/bin/cspeechclient");
	private static Property CEREPROC_SERVER = null;
	private static Property CEREPROC_PORT = null;
	private static Property CEREPROC_DNN_PORT = null;
	private static Property CEREPROC_PRIORITY = null;
	private static Property CEREPROC_DNN_PRIORITY = null;

	private File client;

	protected void activate() throws RuntimeException {
	    client = new File(CEREPROC_CLIENT.getValue());
		if (!client.exists())
			failToActivate("No CereProc client installed at " + client);
		if (CEREPROC_SERVER == null)
			CEREPROC_SERVER = Properties.getProperty("org.daisy.pipeline.tts.cereproc.server",
			                                         false,
			                                         "Address of CereProc speech engine server",
			                                         false,
			                                         "localhost");
	}

	@Override
	public TTSEngine newEngine(Map<String,String> properties) throws Throwable {
		String server = CEREPROC_SERVER.getValue(properties);
		return newEngine(server, client, properties);
	}

	protected abstract CereProcEngine newEngine(String server, File client, Map<String,String> properties) throws Throwable;

	@Override
	public String getName() {
		return "cereproc";
	}

	@Component(
		name = "cereproc-tts-service",
		service = { TTSService.class }
	)
	public static class CereProcStandardService extends CereProcService {

		@Override
		@Activate
		protected void activate() throws RuntimeException {
			super.activate();
			if (CEREPROC_PORT == null)
				CEREPROC_PORT = Properties.getProperty("org.daisy.pipeline.tts.cereproc.port",
				                                       false,
				                                       "Port of CereProc speech engine server for regular voices",
				                                       false,
				                                       null);
			if (CEREPROC_PRIORITY == null)
				CEREPROC_PRIORITY = Properties.getProperty("org.daisy.pipeline.tts.cereproc.priority",
				                                           true,
				                                           "Priority of CereProc regular voices relative to voices of other speech engines",
				                                           false,
				                                           "15");
		}

		@Override
		protected CereProcEngine newEngine(String server, File client, Map<String,String> properties) throws Throwable {
			int port; {
				String prop = CEREPROC_PORT.getValue(properties);
				if (prop != null) {
					try {
						port = Integer.valueOf(prop);
					} catch (NumberFormatException e) {
						throw new SynthesisException(CEREPROC_PORT.getName() + ": " + prop + "is not a valid number", e);
					}
				} else {
					throw new SynthesisException(CEREPROC_PORT.getName() + " property not set.");
				}
			}
			int priority; {
				String prop = CEREPROC_PRIORITY.getValue(properties);
				try {
					priority = Integer.valueOf(prop);
				} catch (NumberFormatException e) {
					throw new SynthesisException(CEREPROC_PRIORITY.getName() + ": " + prop + "is not a valid number", e);
				}
			}
			return new CereProcEngine(CereProcEngine.Variant.STANDARD,
			                          this,
			                          server,
			                          port,
			                          client,
			                          priority);
		}
	}

	@Component(
		name = "cereproc-dnn-tts-service",
		service = { TTSService.class }
	)
	public static class CereProcDNNService extends CereProcService {

		// The DNN version is given another name because:
		// a) currently engines with the same name will not fall back to each other
		// b) it is not possible to specify the version when selecting voices through configuration (including CSS)
		@Override
		public String getName() {
			return "cereproc-dnn";
		}

		@Override
		@Activate
		protected void activate() throws RuntimeException {
			super.activate();
			if (CEREPROC_DNN_PORT == null)
				CEREPROC_DNN_PORT = Properties.getProperty("org.daisy.pipeline.tts.cereproc.dnn.port",
				                                           false,
				                                           "Port of CereProc speech engine server for DNN voices",
				                                           false,
				                                           null);
			if (CEREPROC_DNN_PRIORITY == null)
				CEREPROC_DNN_PRIORITY = Properties.getProperty("org.daisy.pipeline.tts.cereproc.dnn.priority",
				                                               true,
				                                               "Priority of CereProc DNN voices relative to voices of other speech engines",
				                                               false,
				                                               "15");
		}

		@Override
		protected CereProcEngine newEngine(String server, File client, Map<String,String> properties) throws Throwable {

			int port; {
				String prop = CEREPROC_DNN_PORT.getValue(properties);
				if (prop != null) {
					try {
						port = Integer.valueOf(prop);
					} catch (NumberFormatException e) {
						throw new SynthesisException(CEREPROC_DNN_PORT.getName() + ": " + prop + "is not a valid number", e);
					}
				} else {
					throw new SynthesisException(CEREPROC_DNN_PORT.getName() + " property not set.");
				}
			}
			int priority; {
				String prop = CEREPROC_DNN_PRIORITY.getValue(properties);
				try {
					priority = Integer.valueOf(prop);
				} catch (NumberFormatException e) {
					throw new SynthesisException(CEREPROC_DNN_PRIORITY.getName() + ": " + prop + "is not a valid number", e);
				}
			}
			return new CereProcEngine(CereProcEngine.Variant.DNN,
			                          this,
			                          server,
			                          port,
			                          client,
			                          priority);
		}
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
