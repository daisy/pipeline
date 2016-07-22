package org.daisy.common.stax.woodstox.osgi.impl;
import java.util.Properties;

import org.codehaus.stax2.osgi.Stax2InputFactoryProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import com.ctc.wstx.api.ReaderConfig;
import com.ctc.wstx.stax.WstxInputFactory;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating StaxInputFactoryService objects.
 */
public class StaxInputFactoryServiceFactory implements ServiceFactory {


	/* (non-Javadoc)
	 * @see org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration)
	 */
	@Override
	public Object getService(Bundle bundle, ServiceRegistration registration) {
        Properties props = new Properties();
        props.setProperty(Stax2InputFactoryProvider.OSGI_SVC_PROP_IMPL_NAME, ReaderConfig.getImplName());
        props.setProperty(Stax2InputFactoryProvider.OSGI_SVC_PROP_IMPL_VERSION, ReaderConfig.getImplVersion());
        registration.setProperties(props);
		return new  WstxInputFactory();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.ServiceFactory#ungetService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration, java.lang.Object)
	 */
	@Override
	public void ungetService(Bundle bundle, ServiceRegistration registration,
			Object service) {
	}
}
