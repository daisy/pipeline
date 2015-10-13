package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.TextAttribute;
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
	private final String locale;
	private final String translatorMode;
	private final BrailleFilter filter;
	private final BrailleFinalizer finalizer;
	
	private boolean hyphenating;
	
	public SimpleBrailleTranslator(BrailleFilter filter, String locale, String translatorMode) {
		this(filter, null, locale, translatorMode);
	}
	
	public SimpleBrailleTranslator(BrailleFilter filter, BrailleFinalizer finalizer, String locale, String translatorMode) {
		this.filter = filter;
		this.finalizer = finalizer;
		this.locale = locale;
		this.translatorMode = translatorMode;
		this.hyphenating = true;
	}
	
	@Override
	public BrailleTranslatorResult translate(Translatable specification) throws TranslationException {
		BreakPointHandler bph = new BreakPointHandler(filter.filter(specification));
		return new DefaultBrailleTranslatorResult(bph, finalizer);
	}

	public BrailleTranslatorResult translate(String text, String locale, TextAttribute atts) throws TranslationException {
		return translate(Translatable.text(text).locale(locale).attributes(atts).hyphenate(isHyphenating()).build());
	}

	public BrailleTranslatorResult translate(String text, TextAttribute atts) {
		try {
			return translate(text, this.locale.toString(), atts);
		} catch (TranslationException e) {
			throw new RuntimeException("Coding error. This translator does not support the language it claims to support.", e);
		}
	}

	public BrailleTranslatorResult translate(String text, String locale) throws TranslationException {
		return translate(text, locale, null);
	}

	public BrailleTranslatorResult translate(String text) {
		try {
			return translate(text, this.locale.toString());
		} catch (TranslationException e) {
			throw new RuntimeException("Coding error. This translator does not support the language it claims to support.", e);
		}
	}

	public void setHyphenating(boolean value) {
		this.hyphenating = value;
	}

	public boolean isHyphenating() {
		return hyphenating;
	}

	public String getTranslatorMode() {
		return translatorMode;
	}

}
