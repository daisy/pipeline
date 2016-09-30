package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;

public class PreTranslatedBrailleFilter implements BrailleFilter {

	public PreTranslatedBrailleFilter() {
	}

	@Override
	public String filter(Translatable specification) throws TranslationException {
		return specification.getText();
	}

}
