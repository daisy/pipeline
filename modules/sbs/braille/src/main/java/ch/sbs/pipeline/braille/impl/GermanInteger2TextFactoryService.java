package ch.sbs.pipeline.braille.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.daisy.dotify.api.text.Integer2TextFactory;
import org.daisy.dotify.api.text.Integer2TextFactoryService;

import org.osgi.service.component.annotations.Component;

@Component
public class GermanInteger2TextFactoryService implements Integer2TextFactoryService {
	
	private final static List<String> locales;
	static {
		locales = new ArrayList<>();
		locales.add("de");
		locales.add("de-DE");
		locales.add("de-CH");
	}

	@Override
	public boolean supportsLocale(String locale) {
		for (String l : locales) {
			if (l.equalsIgnoreCase(locale)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Integer2TextFactory newFactory() {
		return new GermanInteger2TextFactory();
	}

	@Override
	public Collection<String> listLocales() {
		return locales;
	}
	
	@Override
	public void setCreatedWithSPI() {
	}
}
