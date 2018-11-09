package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.Formatter;
import org.daisy.dotify.api.formatter.FormatterFactory;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMaker;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMaker;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMaker;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * Provides a formatter proxy implementation. This class is intended to be instantiated
 * by the formatter factory, and is not part of the public API.
 * @author Joel Håkansson
 */
@Component
public class FormatterFactoryImpl implements FormatterFactory {
	private BrailleTranslatorFactoryMakerService translatorFactory;
	private TextBorderFactoryMakerService borderFactory;
	private MarkerProcessorFactoryMakerService markerProcessorFactory;

	@Override
	public Formatter newFormatter(String locale, String mode) {
		return new FormatterImpl(translatorFactory, borderFactory, markerProcessorFactory, locale, mode);
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
	
	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setMarkerProcessorFactory(MarkerProcessorFactoryMakerService service) {
		this.markerProcessorFactory = service;
	}
	
	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetMarkerProcessorFactory(MarkerProcessorFactoryMakerService service) {
		this.markerProcessorFactory = null;
	}

	@Override
	public void setCreatedWithSPI() {
		setTranslator(BrailleTranslatorFactoryMaker.newInstance());
		setTextBorderFactory(TextBorderFactoryMaker.newInstance());
		setMarkerProcessorFactory(MarkerProcessorFactoryMaker.newInstance());
	}

}
