package org.daisy.dotify.consumer.translator;

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

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryService;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.daisy.dotify.consumer.hyphenator.HyphenatorFactoryMaker;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * Provides a braille translator factory maker. This class will look for 
 * implementations of the BrailleTranslatorFactory interface using the
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
public class BrailleTranslatorFactoryMaker implements
		BrailleTranslatorFactoryMakerService {
	private final List<BrailleTranslatorFactoryService> factories;
	private final Map<String, BrailleTranslatorFactoryService> map;
	private final Logger logger;

	public BrailleTranslatorFactoryMaker() {
		logger = Logger.getLogger(this.getClass().getCanonicalName());
		factories = new CopyOnWriteArrayList<BrailleTranslatorFactoryService>();
		this.map = Collections.synchronizedMap(new HashMap<String, BrailleTranslatorFactoryService>());
	}

	/**
	 * <p>
	 * Creates a new BrailleTranslatorFactoryMaker and populates it using the
	 * SPI (java service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the
	 * BrailleTranslatorFactoryMakerService interface.
	 * </p>
	 * 
	 * @return returns a new braille translator factory maker.
	 */
	public static BrailleTranslatorFactoryMaker newInstance() {
		BrailleTranslatorFactoryMaker ret = new BrailleTranslatorFactoryMaker();
		{
			HyphenatorFactoryMakerService hyph = HyphenatorFactoryMaker.newInstance();
			Iterator<BrailleTranslatorFactoryService> i = ServiceLoader.load(BrailleTranslatorFactoryService.class).iterator();
			while (i.hasNext()) {
				BrailleTranslatorFactoryService f = i.next();
				try {
					f.setReference(HyphenatorFactoryMakerService.class, hyph);
				} catch (TranslatorConfigurationException e) {
					Logger.getLogger(BrailleTranslatorFactoryMaker.class.getCanonicalName()).log(Level.WARNING, "Failed to set reference.", e);
				}
				ret.addFactory(f);
			}
		}
		return ret;
	}
	
	@Reference(type = '*')
	public void addFactory(BrailleTranslatorFactoryService factory) {
		logger.finer("Adding factory: " + factory);
		factories.add(factory);
	}

	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(BrailleTranslatorFactoryService factory) {
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
	
	/**
	 * Gets a factory for the given specification.
	 * 
	 * @param locale the locale for the factory
	 * @param grade the grade for the factory
	 * @return returns a braille translator factory
	 * @throws TranslatorConfigurationException if the specification is not supported
	 */
	public BrailleTranslatorFactory newFactory(String locale, String grade) throws TranslatorConfigurationException {
		BrailleTranslatorFactoryService template = map.get(toKey(locale, grade));
		if (template==null) {
			// this is to avoid adding items to the cache that were removed
			// while iterating
			synchronized (map) {
				for (BrailleTranslatorFactoryService h : factories) {
					if (h.supportsSpecification(locale, grade)) {
						logger.fine("Found a factory for " + locale + " (" + h.getClass() + ")");
						map.put(toKey(locale, grade), h);
						template = h;
						break;
					}
				}
			}
		}
		if (template==null) {
			throw new BrailleTranslatorFactoryMakerConfigurationException("Cannot locate a factory for " + toKey(locale, grade));
		}
		return template.newFactory();
	}
	
	public BrailleTranslator newTranslator(String locale, String grade) throws TranslatorConfigurationException {
		return newFactory(locale, grade).newTranslator(locale, grade);
	}
	
	private class BrailleTranslatorFactoryMakerConfigurationException extends TranslatorConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8182145215709906802L;

		BrailleTranslatorFactoryMakerConfigurationException(String message) {
			super(message);
		}
		
	}

	public Collection<TranslatorSpecification> listSpecifications() {
		Set<TranslatorSpecification> ret = new HashSet<TranslatorSpecification>();
		synchronized (map) {
			for (BrailleTranslatorFactoryService f : factories) {
				ret.addAll(f.listSpecifications());
			}
		}
		return ret;
	}
}
