package org.daisy.pipeline.logging.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * OSGI logging configuration class. Activates the standard java logging bridge for SLF4J.
 */
public class Activator implements BundleActivator {


	private static final Logger mLogger = LoggerFactory
			.getLogger(Activator.class);

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		mLogger.debug("earlyStartup slf4j SLF4JBridgeHandler...");
		java.util.logging.LogManager.getLogManager().reset();
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINEST);
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
	}

}
