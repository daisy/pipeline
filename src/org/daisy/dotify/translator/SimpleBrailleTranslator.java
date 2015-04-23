package org.daisy.dotify.translator;

import java.util.HashMap;
import java.util.Map;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.common.text.BreakPointHandler;
import org.daisy.dotify.common.text.StringFilter;

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
	private final StringFilter filter;
	private final MarkerProcessor tap;
	private final HyphenatorFactoryMakerService hyphenatorFactoryMaker;
	private final Map<String, HyphenatorInterface> hyphenators;
	
	private boolean hyphenating;
	
	public SimpleBrailleTranslator(StringFilter filter, String locale, String translatorMode, MarkerProcessor tap, HyphenatorFactoryMakerService hyphenatorFactoryMaker) {
		this.filter = filter;
		this.locale = locale;
		this.translatorMode = translatorMode;
		this.tap = tap;
		this.hyphenating = true;
		this.hyphenators = new HashMap<String, HyphenatorInterface>();
		this.hyphenatorFactoryMaker = hyphenatorFactoryMaker;
	}

	public SimpleBrailleTranslator(StringFilter filter, String locale, String translatorMode, HyphenatorFactoryMakerService hyphenatorFactoryMaker) {
		this(filter, locale, translatorMode, null, hyphenatorFactoryMaker);
	}

	public BrailleTranslatorResult translate(String text, String locale, TextAttribute atts) throws TranslationException {
		HyphenatorInterface h = hyphenators.get(locale);
		if (h == null && isHyphenating()) {
			// if we're not hyphenating the language in question, we do not
			// need to
			// add it, nor throw an exception if it cannot be found.
			try {
				h = hyphenatorFactoryMaker.newHyphenator(locale);
			} catch (HyphenatorConfigurationException e) {
				throw new SimpleBrailleTranslationException(e);
			}
			hyphenators.put(locale, h);
		}
		if (tap != null) {
			text = tap.processAttributes(atts, text);
		}
		//translate braille using the same filter, regardless of language
		BreakPointHandler bph = new BreakPointHandler(filter.filter(isHyphenating()?h.hyphenate(text):text));
		return new DefaultBrailleTranslatorResult(bph, filter);
	}

	public BrailleTranslatorResult translate(String text, TextAttribute atts) {
		try {
			return translate(text, this.locale.toString(), atts);
		} catch (TranslationException e) {
			throw new RuntimeException("Coding error. This translator does not support the language it claims to support.");
		}
	}

	public BrailleTranslatorResult translate(String text, String locale) throws TranslationException {
		return translate(text, locale, null);
	}

	public BrailleTranslatorResult translate(String text) {
		try {
			return translate(text, this.locale.toString());
		} catch (TranslationException e) {
			throw new RuntimeException("Coding error. This translator does not support the language it claims to support.");
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
	
	private class SimpleBrailleTranslationException extends TranslationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6102686243949860112L;

		SimpleBrailleTranslationException(Throwable cause) {
			super(cause);
		}
		
	}

}
