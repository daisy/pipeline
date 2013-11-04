package org.daisy.dotify.impl.translator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.TextBorderConfigurationException;
import org.daisy.dotify.api.translator.TextBorderFactory;
import org.daisy.dotify.api.translator.TextBorderStyle;

class BrailleTextBorderFactory implements TextBorderFactory {
	/**
	 * Mode feature. The corresponding value should be
	 * a string.
	 */
	public final static String FEATURE_MODE = "mode";
	/**
	 * Style feature. The corresponding value should be
	 * Set<String> with style values.
	 */
	public final static String FEATURE_STYLE = "style";
	public final static String STYLE_SOLID = "solid";
	public final static String STYLE_WIDE = "wide";
	public final static String STYLE_THIN = "thin";
	public final static String STYLE_INNER = "inner";
	public final static String STYLE_OUTER = "outer";

	private Map<String, Object> features;

	public BrailleTextBorderFactory() {
		this.features = new HashMap<String, Object>();
	}

	public void setFeature(String key, Object value) {
		features.put(key, value);
	}

	public Object getFeature(String key) {
		return features.get(key);
	}

	@SuppressWarnings("unchecked")
	public TextBorderStyle newTextBorderStyle() throws TextBorderConfigurationException {
		String mode = "";
		try {
			mode = (String) getFeature(FEATURE_MODE);
		} catch (Exception e) {
		}
		Set<String> set = new HashSet<String>();
		try {
			set = (Set<String>) getFeature(FEATURE_STYLE);
		} catch (Exception e) {
		}
		if (set == null) {
			set = new HashSet<String>();
		}

		if (!mode.equals(BrailleTranslatorFactory.MODE_BYPASS)) {

			// this is pretty stupid
			if (set.contains(STYLE_SOLID)) {
				if (set.contains(STYLE_WIDE)) {
					if (set.contains(STYLE_INNER)) {
						return BrailleTextBorderStyle.SOLID_WIDE_INNER;
					} else if (set.contains(STYLE_OUTER)) {
						return BrailleTextBorderStyle.SOLID_WIDE_OUTER;
					} else {
						Logger.getLogger(this.getClass().getCanonicalName()).fine("Ignoring unknown frame " + set);
					}
				} else if (set.contains(STYLE_THIN)) {
					if (set.contains(STYLE_INNER)) {
						return BrailleTextBorderStyle.SOLID_THIN_INNER;
					} else if (set.contains(STYLE_OUTER)) {
						return BrailleTextBorderStyle.SOLID_THIN_OUTER;
					} else {
						Logger.getLogger(this.getClass().getCanonicalName()).fine("Ignoring unknown frame " + set);
					}
				} else {
					Logger.getLogger(this.getClass().getCanonicalName()).fine("Ignoring unknown frame " + set);
				}
			}
		}
		throw new BrailleTextBorderFactoryConfigurationException();
	}
	
	private class BrailleTextBorderFactoryConfigurationException extends TextBorderConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2874595503401168992L;
		
	}

}
