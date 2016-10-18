package org.daisy.dotify.api.translator;

/**
 * Provides a factory for text borders.
 * 
 * @author Joel Håkansson
 */
public interface TextBorderFactory {
	
	/**
	 * Mode feature. The corresponding value should be
	 * a string.
	 */
	public static final String FEATURE_MODE = "mode";

	/**
	 * Sets a feature.
	 * @param key a feature key
	 * @param value a feature value
	 */
	public void setFeature(String key, Object value);

	/**
	 * Gets the value for the specified feature key.
	 * @param key the feature key
	 * @return returns the feature value, or null if not defined
	 */
	public Object getFeature(String key);

	/**
	 * Creates a new TextBorderStyle based on the current features.
	 * @return a new TextBorderStyle instance
	 * 
	 * @throws TextBorderConfigurationException if no text border could be created
	 * based on the currently set features.
	 */
	public TextBorderStyle newTextBorderStyle() throws TextBorderConfigurationException;
}
