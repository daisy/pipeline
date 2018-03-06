package org.daisy.dotify.api.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
 * Provides a integer2text factory maker. This is the entry point for
 * creating integer2text instances.
 * 
 * @author Joel Håkansson
 */
@Component
public class Integer2TextFactoryMaker implements
		Integer2TextFactoryMakerService {
	private final List<Integer2TextFactoryService> filters;
	private final Map<String, Integer2TextFactoryService> map;
	private final Logger logger;

	/**
	 * Creates a new integer to text factory maker.
	 */
	public Integer2TextFactoryMaker() {
		logger = Logger.getLogger(this.getClass().getCanonicalName());
		filters = new CopyOnWriteArrayList<>();
		this.map = Collections.synchronizedMap(new HashMap<String, Integer2TextFactoryService>());
	}
	
	/**
	 * <p>
	 * Creates a new Integer2TextFactoryMaker and populates it using the SPI
	 * (java service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the Integer2TextFactoryMakerService
	 * interface.
	 * </p>
	 * 
	 * @return returns a new Integer2TextFactoryMaker
	 */
	public static Integer2TextFactoryMaker newInstance() {
		Integer2TextFactoryMaker ret = new Integer2TextFactoryMaker();
		{
			Iterator<Integer2TextFactoryService> i = ServiceLoader.load(Integer2TextFactoryService.class).iterator();
			while (i.hasNext()) {
				Integer2TextFactoryService f = i.next();
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
	public void addFactory(Integer2TextFactoryService factory) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Adding factory: " + factory);
		}
		filters.add(factory);
	}

	/**
	 * Removes a factory (intended for use by the OSGi framework)
	 * @param factory the factory to remove
	 */
	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(Integer2TextFactoryService factory) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Removing factory: " + factory);
		}
		// this is to avoid adding items to the cache that were removed while
		// iterating
		synchronized (map) {
			filters.remove(factory);
			map.clear();
		}
	}

	@Override
	public Integer2TextFactory getFactory(String target) throws Integer2TextConfigurationException {
		Integer2TextFactoryService template = map.get(target.toLowerCase());
		if (template==null) {
			// this is to avoid adding items to the cache that were removed
			// while iterating
			synchronized (map) {
				for (Integer2TextFactoryService h : filters) {
					if (h.supportsLocale(target)) {
						if (logger.isLoggable(Level.FINE)) {
							logger.fine("Found an integer2text factory for " + target + " (" + h.getClass() + ")");
						}
						map.put(target.toLowerCase(), h);
						template = h;
						break;
					}
				}
			}
		}
		if (template==null) {
			throw new Integer2TextFactoryMakerConfigurationException("Cannot find integer2text factory for " + target);
		}
		return template.newFactory();
	}

	@Override
	public Integer2Text newInteger2Text(String target) throws Integer2TextConfigurationException {
		return getFactory(target).newInteger2Text(target);
	}
	
	private class Integer2TextFactoryMakerConfigurationException extends Integer2TextConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1296708210640963472L;

		Integer2TextFactoryMakerConfigurationException(String message) {
			super(message);
		}
		
	}

	@Override
	public Collection<String> listLocales() {
		ArrayList<String> ret = new ArrayList<>();
		for (Integer2TextFactoryService s : filters) {
			ret.addAll(s.listLocales());
		}
		return ret;
	}

}
