package org.daisy.pipeline.tts.cereproc.impl;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CereProcService extends AbstractTTSService {

	private final static Logger logger = LoggerFactory.getLogger(CereProcService.class);

	protected void loadSSMLadapter() {
		super.loadSSMLadapter("/transform-ssml.xsl", CereProcService.class);
	}

	@Override
	public URL getSSMLxslTransformerURL() {
		return null;
	}

	@Override
	public TTSEngine newEngine(Map<String,String> params) throws Throwable {
		String server; {
			String prop = "org.daisy.pipeline.tts.cereproc.server";
			String val = params.get(prop);
			if (val != null) {
				server = val;
			} else {
				server = "localhost";
				logger.warn(prop + " property not set. Defaulting to " + server);
			}
		}
		File client; {
			String prop = "org.daisy.pipeline.tts.cereproc.client";
			String val = params.get(prop);
			if (val != null) {
				client = new File(val);
			} else {
				client = new File("/usr/bin/cspeechclient");
				logger.warn(prop + " property not set. Defaulting to " + client);
			}
		}
		int priority; {
			String prop = "org.daisy.pipeline.tts.cereproc.priority";
			String val = params.get(prop);
			if (val != null) {
				try {
					priority = Integer.valueOf(val);
				} catch (NumberFormatException e) {
					throw new SynthesisException(prop + ": " + val + "is not a valid number", e);
				}
			} else {
				priority = 15;
			}
		}
		return newEngine(server, client, priority, params);
	}

	protected abstract CereProcEngine newEngine(
		String server, File client, int priority, Map<String,String> params) throws Throwable;

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
		public String getVersion() {
			return "standard";
		}

		@Override
		@Activate
		protected void loadSSMLadapter() {
			super.loadSSMLadapter();
		}

		@Override
		protected CereProcEngine newEngine(
				String server, File client, int priority, Map<String,String> params) throws Throwable {

			int port; {
				String prop = "org.daisy.pipeline.tts.cereproc.port";
				String val = params.get(prop);
				if (val != null) {
					try {
						port = Integer.valueOf(val);
					} catch (NumberFormatException e) {
						throw new SynthesisException(prop + ": " + val + "is not a valid number", e);
					}
				} else {
					throw new SynthesisException(prop + " property not set.");
				}
			}
			return new CereProcEngine(CereProcEngine.Variant.STANDARD,
			                          this,
			                          server,
			                          port,
			                          client,
			                          priority,
			                          super.getSSMLxslTransformerURL());
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
		public String getVersion() {
			return "dnn";
		}

		@Override
		@Activate
		protected void loadSSMLadapter() {
			super.loadSSMLadapter();
		}

		@Override
		protected CereProcEngine newEngine(
				String server, File client, int priority, Map<String,String> params) throws Throwable {

			int port; {
				String prop = "org.daisy.pipeline.tts.cereproc.dnn.port";
				String val = params.get(prop);
				if (val != null) {
					try {
						port = Integer.valueOf(val);
					} catch (NumberFormatException e) {
						throw new SynthesisException(prop + ": " + val + "is not a valid number", e);
					}
				} else {
					throw new SynthesisException(prop + " property not set.");
				}
			}
			{
				String prop = "org.daisy.pipeline.tts.cereproc.dnn.priority";
				String val = params.get(prop);
				if (val != null) {
					try {
						priority = Integer.valueOf(val);
					} catch (NumberFormatException e) {
						throw new SynthesisException(prop + ": " + val + "is not a valid number", e);
					}
				} else {
					priority = 15;
				}
			}
			return new CereProcEngine(CereProcEngine.Variant.DNN,
			                          this,
			                          server,
			                          port,
			                          client,
			                          priority,
			                          super.getSSMLxslTransformerURL());
		}
	}
}
