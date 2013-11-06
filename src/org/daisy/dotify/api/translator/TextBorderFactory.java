package org.daisy.dotify.api.translator;


public interface TextBorderFactory {
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

	public void setFeature(String key, Object value);

	public Object getFeature(String key);

	public TextBorderStyle newTextBorderStyle() throws TextBorderConfigurationException;
}
