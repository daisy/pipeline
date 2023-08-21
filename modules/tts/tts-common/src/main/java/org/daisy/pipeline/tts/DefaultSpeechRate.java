package org.daisy.pipeline.tts;

import java.util.HashMap;
import java.util.Map;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.SpeechRate;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermPercent;
import cz.vutbr.web.domassign.DeclarationTransformer;
import cz.vutbr.web.domassign.SupportedCSS21;

import org.daisy.braille.css.BrailleCSSParserFactory; // BrailleCSSParserFactory can be used for regular CSS too
import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.pipeline.css.speech.SpeechDeclarationTransformer;

public class DefaultSpeechRate {

	private static final Property SPEECH_RATE = Properties.getProperty("org.daisy.pipeline.tts.speech-rate",
	                                                                   true,
	                                                                   "Default speaking rate",
	                                                                   false,
	                                                                   "100%");

	private static final SupportedCSS speechCSS = SupportedCSS21.getInstance();
	private static final DeclarationTransformer declarationTransformer = new SpeechDeclarationTransformer();
	private static final BrailleCSSParserFactory parserFactory = new BrailleCSSParserFactory();

	public DefaultSpeechRate() {}

	/**
	 * Get current value of {@code org.daisy.pipeline.tts.speech-rate} property as a relative value,
	 * with 1.0 corresponding with the normal speaking rate of 200 words per minute.
	 */
	public float getValue(Map<String,String> properties) {
		Iterable<? extends Declaration> declarations
			= parserFactory.parseSimpleInlineStyle("speech-rate: " + SPEECH_RATE.getValue(properties));
		if (declarations != null)
			for (Declaration d : declarations) {
				Map<String,CSSProperty> props = new HashMap<>();
				Map<String,Term<?>> terms = new HashMap<>();
				if (declarationTransformer.parseDeclaration(d, props, terms))
					for (String name: props.keySet()) {
						CSSProperty p = props.get(name);
						if (p instanceof SpeechRate)
							switch ((SpeechRate)p) {
							case X_SLOW:
								return 0.4f; // 80 words per minute
							case SLOW:
								return 0.6f; // 120 words per minute
							case MEDIUM:
								return 1.0f; // 200 words per minute
							case FAST:
								return 1.5f; // 300 words per minute
							case X_FAST:
								return 2.5f; // 500 words per minute
							case number:
								Term<?> t = terms.get(name);
								if (t != null)
									if (t instanceof TermPercent)
										return ((TermPercent)t).getValue() / 100;
									else if (t instanceof TermInteger) // words per minute
										return ((TermInteger)t).getValue() / 200;
								break;
							case FASTER:
							case SLOWER:
							case INHERIT:
							case INITIAL:
							default:
							}
					}
			}
		return 1.0f;
	}
}
