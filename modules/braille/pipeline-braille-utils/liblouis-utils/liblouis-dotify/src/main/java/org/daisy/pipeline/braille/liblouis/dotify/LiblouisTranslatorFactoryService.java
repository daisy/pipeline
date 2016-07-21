package org.daisy.pipeline.braille.liblouis.dotify;

import java.net.URI;
import java.util.Collection;

import com.google.common.collect.ImmutableList;

import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryService;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.daisy.dotify.text.BreakPointHandler;

import org.daisy.pipeline.braille.liblouis.Liblouis;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;
import org.daisy.pipeline.braille.liblouis.LiblouisTableLookup;
import static org.daisy.pipeline.braille.Utilities.Locales.parseLocale;

public class LiblouisTranslatorFactoryService implements BrailleTranslatorFactoryService {
	
	private Liblouis liblouis;
	private LiblouisTableLookup tableLookup;
	
	protected void bindLiblouis(Liblouis liblouis) {
		this.liblouis = liblouis;
	}
	
	protected void unbindLiblouis(Liblouis liblouis) {
		this.liblouis = null;
	}
	
	protected void bindTableLookup(LiblouisTableLookup tableLookup) {
		this.tableLookup = tableLookup;
	}
	
	protected void unbindTableLookup(LiblouisTableLookup tableLookup) {
		this.tableLookup = null;
	}
	
	public boolean supportsSpecification(String locale, String mode) {
		return tableLookup.lookup(parseLocale(locale)) != null;
	}
	
	private LiblouisTranslator getTranslator(String locale) {
		URI[] table = tableLookup.lookup(parseLocale(locale));
		if (table == null)
			throw new RuntimeException("No liblouis table could be found for " + locale);
		return liblouis.get(table);
	}
	
	public Collection<TranslatorSpecification> listSpecifications() {
		return ImmutableList.of();
	}
	
	public BrailleTranslatorFactory newFactory() {
		return new LiblouisTranslatorFactory();
	}
	
	public <T> void setReference(Class<T> c, T reference) throws TranslatorConfigurationException {}
	
	private class LiblouisTranslatorFactory implements BrailleTranslatorFactory {
		public BrailleTranslator newTranslator(String locale, String mode) throws TranslatorConfigurationException {
			try {
				return new LiblouisBrailleTranslator(getTranslator(locale)); }
			catch (Exception e) {
				throw new TranslatorConfigurationException("Factory does not support " + locale, e); }
		}
	}
	
	private static class LiblouisBrailleTranslator implements BrailleTranslator {
		
		private LiblouisTranslator translator;
		
		private LiblouisBrailleTranslator(LiblouisTranslator translator) {
			this.translator = translator;
		}
		
		public void setHyphenating(boolean value) {}
		
		public boolean isHyphenating() {
			return false;
		}
		
		public BrailleTranslatorResult translate(String text) {
			
			final BreakPointHandler bph = new BreakPointHandler(translator.translate(text, false, null));
			
			return new BrailleTranslatorResult() {
				public String nextTranslatedRow(int limit, boolean force) {
					return bph.nextRow(limit, force).getHead();
				}
				public String getTranslatedRemainder() {
					return bph.getRemaining();
				}
				public int countRemaining() {
					return getTranslatedRemainder().length();
				}
				public boolean hasNext() {
					return bph.hasNext();
				}
			};
		}
		
		public BrailleTranslatorResult translate(String text, String locale) {
			return translate(text);
		}
		
		public BrailleTranslatorResult translate(String text, TextAttribute atts) {
			return translate(text);
		}
		
		public BrailleTranslatorResult translate(String text, String locale, TextAttribute attributes) throws TranslationException {
			return translate(text);
		}
		
		public String getTranslatorMode() {
			return BrailleTranslatorFactory.MODE_BYPASS;
		}
	}
}
