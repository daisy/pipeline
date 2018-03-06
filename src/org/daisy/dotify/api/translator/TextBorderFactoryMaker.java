package org.daisy.dotify.api.translator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Provides a text border factory maker. This class will look for
 * implementations of the TextBorderFactory interface using the
 * services API. It will return the first implementation that matches the
 * requested specification.
 * 
 * <p>
 * This class can be overridden by extending it and adding a reference to the
 * new implementation to the services API. This class will then choose the new
 * implementation when a new instance is requested.
 * </p>
 * 
 * @author Joel Håkansson
 * 
 */
@Component
public class TextBorderFactoryMaker implements TextBorderFactoryMakerService {
	private final List<TextBorderFactoryService> factories;
	private final Logger logger;

	/**
	 * Creates a new text border factory maker. 
	 */
	public TextBorderFactoryMaker() {
		logger = Logger.getLogger(TextBorderFactoryMaker.class.getCanonicalName());
		factories = new CopyOnWriteArrayList<>();
	}

	/**
	 * <p>
	 * Creates a new TextBorderFactoryMaker and populates it using the SPI (java
	 * service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the TextBorderFactoryMakerService
	 * interface.
	 * </p>
	 * 
	 * @return returns a new marker processor factory maker.
	 */
	public static TextBorderFactoryMakerService newInstance() {
		TextBorderFactoryMaker ret = new TextBorderFactoryMaker();
		{
			Iterator<TextBorderFactoryService> i = ServiceLoader.load(TextBorderFactoryService.class).iterator();
			while (i.hasNext()) {
				TextBorderFactoryService f = i.next();
				f.setCreatedWithSPI();
				ret.addFactory(f);
			}
		}
		return ret;
	}
	
	/**
	 * Adds a factory (intended for use by the OSGi framework)
	 * @param factory the factory to add
	 */
	@Reference(cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC)
	public void addFactory(TextBorderFactoryService factory) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Adding factory: " + factory);
		}
		factories.add(factory);
	}

	/**
	 * Removes a factory (intended for use by the OSGi framework)
	 * @param factory the factory to remove
	 */
	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(TextBorderFactoryService factory) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Removing factory: " + factory);
		}
		// this is to avoid adding items to the cache that were removed while
		// iterating
		factories.remove(factory);
	}

	@Override
	public TextBorderStyle newTextBorderStyle(Map<String, Object> features) throws TextBorderConfigurationException {
		// TODO: this could be optimized
		for (TextBorderFactoryService s : factories) {
			TextBorderFactory h = s.newFactory();
			for (String key : features.keySet()) {
				h.setFeature(key, features.get(key));
			}
			try {
				return h.newTextBorderStyle();
			} catch (TextBorderConfigurationException e) {
				// try another one
			}
		}
		throw new TextBorderFactoryMakerException();
	}
	
	private class TextBorderFactoryMakerException extends TextBorderConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7241556330716217110L;
		
	}

}
