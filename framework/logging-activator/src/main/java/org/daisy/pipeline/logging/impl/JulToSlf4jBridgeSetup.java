package org.daisy.pipeline.logging.impl;

import org.slf4j.bridge.SLF4JBridgeHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JulToSlf4jBridgeSetup {

	private static final Logger mLogger = LoggerFactory
			.getLogger(Activator.class);

	public static void setup() {
		mLogger.debug("earlyStartup slf4j SLF4JBridgeHandler...");
		java.util.logging.LogManager.getLogManager().reset();
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINEST);
	}
}
