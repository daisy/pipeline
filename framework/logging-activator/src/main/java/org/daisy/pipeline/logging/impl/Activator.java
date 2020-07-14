package org.daisy.pipeline.logging.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * OSGI logging configuration class. Activates the standard java logging bridge for SLF4J.
 */
@Component(
	name = "logging-activator",
	immediate = true
)
public class Activator {
	
	@Activate
	public void start() {
		JulToSlf4jBridgeSetup.setup();
	}
}
