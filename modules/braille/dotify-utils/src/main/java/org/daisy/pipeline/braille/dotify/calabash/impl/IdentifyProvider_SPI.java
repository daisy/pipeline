package org.daisy.pipeline.braille.dotify.calabash.impl;

import java.util.ServiceLoader;

import org.daisy.streamline.api.identity.IdentifierFactory;
import org.daisy.streamline.api.identity.IdentityProvider;

// wrapper class for org.daisy.streamline.api.identity.IdentityProvider that can be instantiated using SPI
public class IdentifyProvider_SPI extends IdentityProvider {
	
	public IdentifyProvider_SPI() {
		super();
		// copied from IdentityProvider.newInstance()
		for (IdentifierFactory factory : ServiceLoader.load(IdentifierFactory.class)) {
			factory.setCreatedWithSPI();
			addFactory(factory);
		}
	}
}
