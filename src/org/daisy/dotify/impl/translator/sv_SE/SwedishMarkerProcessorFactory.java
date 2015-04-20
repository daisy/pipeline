package org.daisy.dotify.impl.translator.sv_SE;

import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.MarkerProcessorConfigurationException;
import org.daisy.dotify.api.translator.MarkerProcessorFactory;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.text.FilterLocale;
import org.daisy.dotify.translator.DefaultMarkerProcessor;
import org.daisy.dotify.translator.Marker;
import org.daisy.dotify.translator.MarkerStyleConstants;
import org.daisy.dotify.translator.RegexMarkerDictionary;
import org.daisy.dotify.translator.SimpleMarkerDictionary;
import org.daisy.dotify.translator.TextAttributeFilter;

class SwedishMarkerProcessorFactory implements MarkerProcessorFactory {
	private final static String WHITESPACE_REGEX = "\\s+";
	private final static String ALPHANUM_REGEX = "\\A[a-zA-Z0-9]+\\z";
	private final static FilterLocale sv_SE = FilterLocale.parse("sv-SE");

	public MarkerProcessor newMarkerProcessor(String locale, String mode) throws MarkerProcessorConfigurationException {
		if (FilterLocale.parse(locale).equals(sv_SE)) {
			if (mode.equals(BrailleTranslatorFactory.MODE_UNCONTRACTED)) {
	
				// Svenska skrivregler för punktskrift 2009, page 34
				RegexMarkerDictionary strong = new RegexMarkerDictionary.Builder().
						addPattern(WHITESPACE_REGEX, new Marker("\u2828\u2828", "\u2831"), new Marker("\u2828", "")).
						build();
				
				// Svenska skrivregler för punktskrift 2009, page 34
				RegexMarkerDictionary em = new RegexMarkerDictionary.Builder().
						addPattern(WHITESPACE_REGEX, new Marker("\u2820\u2824", "\u2831"), new Marker("\u2820\u2804", "")).
						build();
	
				// Svenska skrivregler för punktskrift 2009, page 32
				TextAttributeFilter subnodeFilter = new TextAttributeFilter() {
	
					private boolean checkChildren(TextAttribute atts) {
						if (atts.hasChildren()) {
							for (TextAttribute t : atts) {
								if (t.getDictionaryIdentifier() != null) {
									return false;
								} else {
									if (!checkChildren(t)) {
										return false;
									}
								}
							}
						}
						return true;
					}
	
					public boolean appliesTo(TextAttribute atts) {
						return checkChildren(atts);
					}
				};
				RegexMarkerDictionary sub = new RegexMarkerDictionary.Builder().
						addPattern(ALPHANUM_REGEX, new Marker("\u2823", "")).
						filter(subnodeFilter).
						build();
	
				// Svenska skrivregler för punktskrift 2009, page 32
				RegexMarkerDictionary sup = new RegexMarkerDictionary.Builder().
						addPattern(ALPHANUM_REGEX, new Marker("\u282c", "")).
						filter(subnodeFilter).
						build();
				
				// Redigering och avskrivning, page 148
				SimpleMarkerDictionary dd = new SimpleMarkerDictionary(new Marker("\u2820\u2804\u2800", ""));
	
				DefaultMarkerProcessor sap = new DefaultMarkerProcessor.Builder().
						addDictionary(MarkerStyleConstants.STRONG, strong).
						addDictionary(MarkerStyleConstants.EM, em).
						addDictionary(MarkerStyleConstants.SUB, sub).
						addDictionary(MarkerStyleConstants.SUP, sup).
						addDictionary(MarkerStyleConstants.DD, dd).
						build();
	
				return sap;
			}
		} 
		throw new SwedishMarkerProcessorConfigurationException("Factory does not support " + locale + "/" + mode);
	}
	
	private class SwedishMarkerProcessorConfigurationException extends MarkerProcessorConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1435286045359670613L;

		private SwedishMarkerProcessorConfigurationException(String message) {
			super(message);
		}
		
	}

}
