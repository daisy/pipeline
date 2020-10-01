package org.daisy.pipeline.tts.cereproc.impl;

import java.io.File;
import java.util.Map;

import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "cereproc-tts-service",
	service = { TTSService.class }
)
public class CereProcService extends AbstractTTSService {

	private final static Logger logger = LoggerFactory.getLogger(CereProcService.class);

	@Activate
	protected void loadSSMLadapter() {
		super.loadSSMLadapter("/transform-ssml.xsl", CereProcService.class);
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
				port = 8989;
				logger.warn(prop + " property not set. Defaulting to " + port);
			}
		}
		File clientDir; {
			String prop = "org.daisy.pipeline.tts.cereproc.client.dir";
			String val = params.get(prop);
			if (val != null) {
				clientDir = new File(val);
			} else {
				clientDir = new File("/opt/cspeech/client");
				logger.warn(prop + " property not set. Defaulting to " + clientDir);
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
		return new CereProcEngine(this, server, port, clientDir, priority);
	}

	@Override
	public String getName() {
		return "cereproc";
	}

	@Override
	public String getVersion() {
		return "jni";
	}
}
