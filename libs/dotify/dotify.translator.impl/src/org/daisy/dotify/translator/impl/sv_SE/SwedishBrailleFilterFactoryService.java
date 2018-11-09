package org.daisy.dotify.translator.impl.sv_SE;

import java.util.ArrayList;
import java.util.Collection;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMaker;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleFilterFactory;
import org.daisy.dotify.api.translator.BrailleFilterFactoryService;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * Provides a Swedish braille filter factory service.
 * @author Joel Håkansson
 *
 */
@Component
public class SwedishBrailleFilterFactoryService implements
		BrailleFilterFactoryService {

	private HyphenatorFactoryMakerService hyphenator = null;
	private final ArrayList<TranslatorSpecification> specs;

	/**
	 * Creates a new Swedish braille filter factory service.
	 */
	public SwedishBrailleFilterFactoryService() {
		this.specs = new ArrayList<>();
		specs.add(new TranslatorSpecification("sv", BrailleTranslatorFactory.MODE_UNCONTRACTED));
		specs.add(new TranslatorSpecification("sv-SE", BrailleTranslatorFactory.MODE_UNCONTRACTED));
	}
	
	@Override
	public boolean supportsSpecification(String locale, String mode) {
		return ("sv".equalsIgnoreCase(locale) || "sv-SE".equalsIgnoreCase(locale)) && mode.equals(BrailleTranslatorFactory.MODE_UNCONTRACTED);
	}

	@Override
	public BrailleFilterFactory newFactory() {
		return new SwedishBrailleFilterFactory(hyphenator);
	}

	/**
	 * Sets the hyphenator factory maker service.
	 * @param hyphenator the hyphenator factory maker service.
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setHyphenator(HyphenatorFactoryMakerService hyphenator) {
		this.hyphenator = hyphenator;
	}

	/**
	 * Unsets the hyphenator factory maker service.
	 * @param hyphenator the instance to unset.
	 */
	public void unsetHyphenator(HyphenatorFactoryMakerService hyphenator) {
		this.hyphenator = null;
	}
	
	@Override
	public Collection<TranslatorSpecification> listSpecifications() {
		return specs;
	}

	@Override
	public void setCreatedWithSPI() {
		setHyphenator(HyphenatorFactoryMaker.newInstance());
	}

}
