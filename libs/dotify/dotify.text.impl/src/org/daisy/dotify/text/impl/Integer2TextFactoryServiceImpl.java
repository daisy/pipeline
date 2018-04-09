package org.daisy.dotify.text.impl;

import java.util.Collection;
import java.util.Locale;

import org.daisy.dotify.api.text.Integer2TextFactory;
import org.daisy.dotify.api.text.Integer2TextFactoryService;
import org.osgi.service.component.annotations.Component;

/**
 * Provides an integer to text factory service.
 * @author Joel Håkansson
 */
@Component
public class Integer2TextFactoryServiceImpl implements Integer2TextFactoryService {
	
	@Override
	public boolean supportsLocale(String locale) {
		return Integer2TextFactoryImpl.locales.containsKey(locale.toLowerCase(Locale.ENGLISH));
	}

	@Override
	public Integer2TextFactory newFactory() {
		return new Integer2TextFactoryImpl();
	}

	@Override
	public Collection<String> listLocales() {
		return Integer2TextFactoryImpl.displayNames;
	}

}
