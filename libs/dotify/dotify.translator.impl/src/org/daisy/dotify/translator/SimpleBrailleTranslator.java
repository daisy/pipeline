package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.common.text.BreakPointHandler;

/**
 * Provides a simple braille translator that translates
 * all texts using the same filter, regardless of language.
 * The translator does however switch hyphenation
 * rules based on language and it will throw an
 * exception if it cannot find the appropriate hyphenation
 * rules for the language.
 * @author Joel Håkansson
 *
 */
public class SimpleBrailleTranslator implements BrailleTranslator {
	private final String translatorMode;
	private final BrailleFilter filter;
	private final BrailleFinalizer finalizer;
	
	/**
	 * Creates a new simple braille translator.
	 * @param filter the braille filter to use
	 * @param translatorMode the translator mode
	 */
	public SimpleBrailleTranslator(BrailleFilter filter, String translatorMode) {
		this(filter, null, translatorMode);
	}
	
	/**
	 * Creates a new simple braille translator.
	 * @param filter the braille filter to use
	 * @param finalizer the braille finalizer to use
	 * @param translatorMode the translator mode
	 */
	public SimpleBrailleTranslator(BrailleFilter filter, BrailleFinalizer finalizer, String translatorMode) {
		this.filter = filter;
		this.finalizer = finalizer;
		this.translatorMode = translatorMode;
	}
	
	@Override
	public BrailleTranslatorResult translate(Translatable specification) throws TranslationException {
		BreakPointHandler bph = new BreakPointHandler(filter.filter(specification));
		return new DefaultBrailleTranslatorResult(bph, finalizer);
	}

	@Override
	public String getTranslatorMode() {
		return translatorMode;
	}

}
