package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.Formatter;
import org.daisy.dotify.api.formatter.FormatterFactory;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMaker;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMaker;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * <p>Provides a {@link FormatterFactory formatter proxy implementation}. This class is intended to
 * be instantiated by the formatter factory, and is not part of the public API.</p>
 *
 * <p>Produces instances of {@link FormatterImpl}.</p>
 *
 * @author Joel Håkansson
 */
@Component
public class FormatterFactoryImpl implements FormatterFactory {
	private BrailleTranslatorFactoryMakerService translatorFactory;
	private TextBorderFactoryMakerService borderFactory;

	@Override
	public Formatter newFormatter(String locale, String mode) {
		return new FormatterImpl(translatorFactory, borderFactory, locale, mode);
	}

	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setTranslator(BrailleTranslatorFactoryMakerService service) {
		this.translatorFactory = service;
	}

	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetTranslator(BrailleTranslatorFactoryMakerService service) {
		this.translatorFactory = null;
	}
	
	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setTextBorderFactory(TextBorderFactoryMakerService service) {
		this.borderFactory = service;
	}
	
	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetTextBorderFactory(TextBorderFactoryMakerService service) {
		this.borderFactory = null;
	}

	@Override
	public void setCreatedWithSPI() {
		setTranslator(BrailleTranslatorFactoryMaker.newInstance());
		setTextBorderFactory(TextBorderFactoryMaker.newInstance());
	}

}
