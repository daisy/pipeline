package org.daisy.common.logging.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activate the standard java logging bridge for SLF4J.
 */
@Component(
	name = "logging-activator",
	immediate = true
)
public class JulToSlf4jBridgeSetup {

	private static final Logger mLogger = LoggerFactory.getLogger(JulToSlf4jBridgeSetup.class);

	@Activate
	public void setup() {
		mLogger.debug("earlyStartup slf4j SLF4JBridgeHandler...");
		java.util.logging.LogManager.getLogManager().reset();
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINEST);
	}
}
