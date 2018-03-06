package org.daisy.dotify.api.translator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Provides a braille filter factory maker. This class will look for 
 * implementations of the BrailleFilterFactory interface using the
 * services API. It will return the first implementation that matches the
 * requested specification.
 * 
 * <p>This class can be overridden by extending it and adding a reference
 * to the new implementation to the services API. This class will then
 * choose the new implementation when a new instance is requested.</p>
 * 
 * @author Joel Håkansson
 *
 */
@Component
public class BrailleFilterFactoryMaker implements
		BrailleFilterFactoryMakerService {
	private final List<BrailleFilterFactoryService> factories;
	private final Map<String, BrailleFilterFactoryService> map;
	private final Logger logger;

	/**
	 * Creates a new braille filter factory maker.
	 */
	public BrailleFilterFactoryMaker() {
		logger = Logger.getLogger(this.getClass().getCanonicalName());
		factories = new CopyOnWriteArrayList<>();
		this.map = Collections.synchronizedMap(new HashMap<String, BrailleFilterFactoryService>());
	}

	/**
	 * <p>
	 * Creates a new BrailleFilterFactoryMaker and populates it using the
	 * SPI (java service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the
	 * BrailleFilterFactoryMakerService interface.
	 * </p>
	 * 
	 * @return returns a new braille filter factory maker.
	 */
	public static BrailleFilterFactoryMaker newInstance() {
		BrailleFilterFactoryMaker ret = new BrailleFilterFactoryMaker();
		{
			Iterator<BrailleFilterFactoryService> i = ServiceLoader.load(BrailleFilterFactoryService.class).iterator();
			while (i.hasNext()) {
				BrailleFilterFactoryService f = i.next();
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
	public void addFactory(BrailleFilterFactoryService factory) {
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
	public void removeFactory(BrailleFilterFactoryService factory) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Removing factory: " + factory);
		}
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
	
	@Override
	public boolean supportsSpecification(String locale, String grade) {
		return map.get(toKey(locale, grade)) != null;
	}
	
	/**
	 * Gets a factory for the given specification.
	 * 
	 * @param locale the locale for the factory
	 * @param grade the grade for the factory
	 * @return returns a braille filter factory
	 * @throws TranslatorConfigurationException if the specification is not supported
	 */
	@Override
	public BrailleFilterFactory newFactory(String locale, String grade) throws TranslatorConfigurationException {
		BrailleFilterFactoryService template = map.get(toKey(locale, grade));
		if (template==null) {
			// this is to avoid adding items to the cache that were removed
			// while iterating
			synchronized (map) {
				for (BrailleFilterFactoryService h : factories) {
					if (h.supportsSpecification(locale, grade)) {
						if (logger.isLoggable(Level.FINE)) {
							logger.fine("Found a factory for " + locale + " (" + h.getClass() + ")");
						}
						map.put(toKey(locale, grade), h);
						template = h;
						break;
					}
				}
			}
		}
		if (template==null) {
			throw new BrailleFilterFactoryMakerConfigurationException("Cannot locate a factory for " + toKey(locale, grade));
		}
		return template.newFactory();
	}
	
	@Override
	public BrailleFilter newFilter(String locale, String grade) throws TranslatorConfigurationException {
		return newFactory(locale, grade).newFilter(locale, grade);
	}
	
	private class BrailleFilterFactoryMakerConfigurationException extends TranslatorConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8182145215709906802L;

		BrailleFilterFactoryMakerConfigurationException(String message) {
			super(message);
		}
		
	}

	@Override
	public Collection<TranslatorSpecification> listSpecifications() {
		Set<TranslatorSpecification> ret = new HashSet<>();
		synchronized (map) {
			for (BrailleFilterFactoryService f : factories) {
				ret.addAll(f.listSpecifications());
			}
		}
		return ret;
	}
}
