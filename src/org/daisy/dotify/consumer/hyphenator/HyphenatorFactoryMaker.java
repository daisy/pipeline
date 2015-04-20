package org.daisy.dotify.consumer.hyphenator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorFactory;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryService;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * Provides a hyphenator factory maker. This is the entry point for
 * creating hyphenator instances.
 * 
 * @author Joel Håkansson
 */
@Component
public class HyphenatorFactoryMaker implements HyphenatorFactoryMakerService {
	private final List<HyphenatorFactoryService> filters;
	private final Map<String, HyphenatorFactoryService> map;
	private final Logger logger;
	
	public HyphenatorFactoryMaker() {
		logger = Logger.getLogger(this.getClass().getCanonicalName());
		filters = new CopyOnWriteArrayList<HyphenatorFactoryService>();
		this.map = Collections.synchronizedMap(new HashMap<String, HyphenatorFactoryService>());
	}
	
	/**
	 * <p>
	 * Creates a new HyphenatorFactoryMaker and populates it using the SPI (java
	 * service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the HyphenatorFactoryMakerService
	 * interface.
	 * </p>
	 * 
	 * @return returns a new HyphenatorFactoryMakerService
	 */
	public static HyphenatorFactoryMaker newInstance() {
		HyphenatorFactoryMaker ret = new HyphenatorFactoryMaker();
		{
			Iterator<HyphenatorFactoryService> i = ServiceLoader.load(HyphenatorFactoryService.class).iterator();
			while (i.hasNext()) {
				ret.addFactory(i.next());
			}
		}
		return ret;
	}

	@Reference(type = '*')
	public void addFactory(HyphenatorFactoryService factory) {
		logger.finer("Adding factory: " + factory);
		filters.add(factory);
	}

	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(HyphenatorFactoryService factory) {
		logger.finer("Removing factory: " + factory);
		// this is to avoid adding items to the cache that were removed while
		// iterating
		synchronized (map) {
			filters.remove(factory);
			map.clear();
		}
	}

	public HyphenatorFactory newFactory(String target) throws HyphenatorConfigurationException {
		HyphenatorFactoryService template = map.get(target.toLowerCase());
		if (template==null) {
			// this is to avoid adding items to the cache that were removed
			// while iterating
			synchronized (map) {
				for (HyphenatorFactoryService h : filters) {
					if (h.supportsLocale(target)) {
						logger.fine("Found a hyphenator factory for " + target + " (" + h.getClass() + ")");
						map.put(target.toLowerCase(), h);
						template = h;
						break;
					}
				}
			}
		}
		if (template==null) {
			throw new HyphenatorFactoryMakerConfigurationException("Cannot find hyphenator factory for " + target);
		}
		return template.newFactory();
	}

	public HyphenatorInterface newHyphenator(String target) throws HyphenatorConfigurationException {
		HyphenatorInterface ret = newFactory(target).newHyphenator(target);
		return ret;
	}
	
	private class HyphenatorFactoryMakerConfigurationException extends HyphenatorConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8869650439082769112L;

		HyphenatorFactoryMakerConfigurationException(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}
	}
	
	public Collection<String> listLocales() {
		ArrayList<String> ret = new ArrayList<String>();
		for (HyphenatorFactoryService s : filters) {
			ret.addAll(s.listLocales());
		}
		return ret;
	}

}
