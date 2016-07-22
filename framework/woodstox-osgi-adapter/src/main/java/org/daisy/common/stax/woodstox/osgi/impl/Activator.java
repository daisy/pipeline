package org.daisy.common.stax.woodstox.osgi.impl;
import javax.xml.stream.XMLInputFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

// TODO: Auto-generated Javadoc
/**
 * Activator for OSGI
 */
public class Activator implements BundleActivator {

	/** The registration. */
	private ServiceRegistration registration;

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		registration = context.registerService(XMLInputFactory.class.getName(),
				new StaxInputFactoryServiceFactory(), null);
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		registration.unregister();
	}

}
