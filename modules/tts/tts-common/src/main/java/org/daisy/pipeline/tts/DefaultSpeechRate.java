package org.daisy.pipeline.tts;

import java.util.Map;

import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermPercent;
import cz.vutbr.web.domassign.DeclarationTransformer;
import cz.vutbr.web.domassign.SupportedCSS21;

import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.SimpleInlineStyle; // SimpleInlineStyle lives in braille-css but can be used for regular CSS too
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
		SimpleInlineStyle s = new SimpleInlineStyle(
			parserFactory.parseSimpleInlineStyle("speech-rate: " + SPEECH_RATE.getValue(properties)),
			null,
			declarationTransformer,
			speechCSS);
		Term<?> t = s.getValue("speech-rate");
		if (t != null) {
			if (t instanceof TermPercent) {
				return ((TermPercent)t).getValue() / 100;
			} else if (t instanceof TermInteger) { // words per minute
				return ((TermInteger)t).getValue() / 200;
			} else if (t instanceof TermIdent) {
				String i = ((TermIdent)t).getValue();
				if ("x-slow".equalsIgnoreCase(i)) {
					return 0.4f; // 80 words per minute
				} else if ("slow".equalsIgnoreCase(i)) {
					return 0.6f; // 120 words per minute
				} else if ("medium".equalsIgnoreCase(i)) {
					return 1.0f; // 200 words per minute
				} else if ("fast".equalsIgnoreCase(i)) {
					return 1.5f; // 300 words per minute
				} else if ("x-fast".equalsIgnoreCase(i)) {
					return 2.5f; // 500 words per minute
				}
			}
		}
		return 1.0f;
	}
}
