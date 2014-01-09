package org.daisy.dotify.api.translator;


public interface TextBorderFactory {
	/**
	 * Mode feature. The corresponding value should be
	 * a string.
	 */
	public final static String FEATURE_MODE = "mode";

	public void setFeature(String key, Object value);

	public Object getFeature(String key);

	public TextBorderStyle newTextBorderStyle() throws TextBorderConfigurationException;
}
