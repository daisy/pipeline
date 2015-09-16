package org.daisy.dotify.impl.translator.sv_SE;

import java.util.ArrayList;
import java.util.Collection;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleFilterFactory;
import org.daisy.dotify.api.translator.BrailleFilterFactoryService;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.daisy.dotify.impl.translator.SPIHelper;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class SwedishBrailleFilterFactoryService implements
		BrailleFilterFactoryService {

	private HyphenatorFactoryMakerService hyphenator = null;
	private final ArrayList<TranslatorSpecification> specs;

	public SwedishBrailleFilterFactoryService() {
		this.specs = new ArrayList<TranslatorSpecification>();
		specs.add(new TranslatorSpecification("sv-SE", BrailleTranslatorFactory.MODE_UNCONTRACTED));
	}
	
	public boolean supportsSpecification(String locale, String mode) {
		return "sv-SE".equalsIgnoreCase(locale) && mode.equals(BrailleTranslatorFactory.MODE_UNCONTRACTED);
	}

	public BrailleFilterFactory newFactory() {
		return new SwedishBrailleFilterFactory(hyphenator);
	}

	@Reference
	public void setHyphenator(HyphenatorFactoryMakerService hyphenator) {
		this.hyphenator = hyphenator;
	}

	public void unsetHyphenator(HyphenatorFactoryMakerService hyphenator) {
		this.hyphenator = null;
	}
	
	public Collection<TranslatorSpecification> listSpecifications() {
		return specs;
	}

	@Override
	public void setCreatedWithSPI() {
		setHyphenator(SPIHelper.getHyphenatorFactoryMakerService());
	}

}
