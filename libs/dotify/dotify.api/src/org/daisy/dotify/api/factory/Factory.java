package org.daisy.dotify.api.factory;

/**
 * Provides an interface for common properties of a Factory.
 * @author Joel Håkansson
 *
 */
public interface Factory extends FactoryProperties {

	/**
	 * Gets the value of a read-only property that applies to all objects returned
	 * by this Factory.
	 * @param key the name of the property to get
	 * @return returns the value associated with this property or null if none is found
	 */
	public Object getProperty(String key);
	/**
	 * Gets the value of a feature used by this Factory
	 * @param key the key for the feature
	 * @return returns the current value of the feature
	 * @throws IllegalArgumentException if the underlying implementation does not recognize the feature
	 */
	public Object getFeature(String key);
	/**
	 * Sets a feature for new Objects returned by this Factory
	 * @param key the key for the feature
	 * @param value the value of the feature
	 * @throws IllegalArgumentException if the underlying implementation does not recognize the feature
	 */
	public void setFeature(String key, Object value);

}
