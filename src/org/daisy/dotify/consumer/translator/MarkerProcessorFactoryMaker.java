package org.daisy.dotify.consumer.translator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.MarkerProcessorConfigurationException;
import org.daisy.dotify.api.translator.MarkerProcessorFactory;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMakerService;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryService;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * Provides a marker processor factory maker. This class will look for
 * implementations of the MarkerProcessorFactory interface using the
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
public class MarkerProcessorFactoryMaker implements
		MarkerProcessorFactoryMakerService {
	private final List<MarkerProcessorFactoryService> factories;
	private final Map<String, MarkerProcessorFactoryService> map;
	private final Logger logger;

	public MarkerProcessorFactoryMaker() {
		logger = Logger.getLogger(this.getClass().getCanonicalName());
		factories = new CopyOnWriteArrayList<MarkerProcessorFactoryService>();
		this.map = Collections.synchronizedMap(new HashMap<String, MarkerProcessorFactoryService>());
	}

	/**
	 * <p>
	 * Creates a new MarkerProcessorFactoryMaker and populates it using the SPI
	 * (java service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the
	 * MarkerProcessorFactoryMakerService interface.
	 * </p>
	 * 
	 * @return returns a new marker processor factory maker.
	 */
	public static MarkerProcessorFactoryMakerService newInstance() {
		MarkerProcessorFactoryMaker ret = new MarkerProcessorFactoryMaker();
		{
			Iterator<MarkerProcessorFactoryService> i = ServiceLoader.load(MarkerProcessorFactoryService.class).iterator();
			while (i.hasNext()) {
				ret.addFactory(i.next());
			}
		}
		return ret;
	}
	
	@Reference(type = '*')
	public void addFactory(MarkerProcessorFactoryService factory) {
		logger.finer("Adding factory: " + factory);
		factories.add(factory);
	}

	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(MarkerProcessorFactoryService factory) {
		logger.finer("Removing factory: " + factory);
		// this is to avoid adding items to the cache that were removed while
		// iterating
		synchronized (map) {
			factories.remove(factory);
			map.clear();
		}
	}

	private static String toKey(String locale, String grade) {
		return locale.toLowerCase() + "(" + grade.toUpperCase() + ")";
	}
	
	public boolean supportsSpecification(String locale, String grade) {
		return map.get(toKey(locale, grade)) != null;
	}
	
	public MarkerProcessorFactory newFactory(String locale, String grade) throws MarkerProcessorFactoryMakerException {
		MarkerProcessorFactoryService template = map.get(toKey(locale, grade));
		if (template==null) {
			// this is to avoid adding items to the cache that were removed
			// while iterating
			synchronized (map) {
				for (MarkerProcessorFactoryService h : factories) {
					if (h.supportsSpecification(locale.toString(), grade)) {
						logger.fine("Found a factory for " + locale + " (" + h.getClass() + ")");
						map.put(toKey(locale, grade), h);
						template = h;
						break;
					}
				}
			}
		}
		if (template==null) {
			throw new MarkerProcessorFactoryMakerException("Cannot locate a factory for " + toKey(locale, grade));
		}
		return template.newFactory();
	}
	
	public MarkerProcessor newMarkerProcessor(String locale, String grade) throws MarkerProcessorConfigurationException {
		return newFactory(locale, grade).newMarkerProcessor(locale, grade);
	}

	private class MarkerProcessorFactoryMakerException extends
			MarkerProcessorConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1320278713114059279L;


		protected MarkerProcessorFactoryMakerException(String message) {
			super(message);
		}

	}
}
