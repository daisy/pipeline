package org.daisy.dotify.impl.translator.sv_SE;

import java.util.ArrayList;
import java.util.Collection;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryService;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.daisy.dotify.impl.translator.SPIHelper;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class SwedishBrailleTranslatorFactoryService implements
		BrailleTranslatorFactoryService {

	private HyphenatorFactoryMakerService hyphenator = null;
	private final ArrayList<TranslatorSpecification> specs;

	public SwedishBrailleTranslatorFactoryService() {
		this.specs = new ArrayList<>();
		specs.add(new TranslatorSpecification("sv-SE", BrailleTranslatorFactory.MODE_UNCONTRACTED));
		specs.add(new TranslatorSpecification("sv-SE", SwedishBrailleTranslatorFactory.PRE_TRANSLATED));
	}
	
	@Override
	public boolean supportsSpecification(String locale, String mode) {
		TranslatorSpecification target = new TranslatorSpecification(locale, mode);
		for (TranslatorSpecification spec : specs) {
			if (target.equals(spec)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public BrailleTranslatorFactory newFactory() {
		return new SwedishBrailleTranslatorFactory(hyphenator);
	}

	@Reference
	public void setHyphenator(HyphenatorFactoryMakerService hyphenator) {
		this.hyphenator = hyphenator;
	}

	public void unsetHyphenator(HyphenatorFactoryMakerService hyphenator) {
		this.hyphenator = null;
	}
	
	@Override
	@Deprecated
	public <T> void setReference(Class<T> c, T reference)
			throws TranslatorConfigurationException {
		if (c.equals(HyphenatorFactoryMakerService.class)) {
			setHyphenator((HyphenatorFactoryMakerService)reference);
		} else {
			throw new TranslatorConfigurationException("Unrecognized reference: " + reference);
		}
	}

	@Override
	public Collection<TranslatorSpecification> listSpecifications() {
		return specs;
	}

	@Override
	public void setCreatedWithSPI() {
		setHyphenator(SPIHelper.getHyphenatorFactoryMakerService());
	}

}
