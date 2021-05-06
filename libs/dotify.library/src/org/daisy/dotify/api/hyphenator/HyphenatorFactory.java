package org.daisy.dotify.api.hyphenator;

/**
 * Provides a hyphenation factory interface. This interface is used to retreive
 * a hyphenator instance.
 *
 * @author Joel Håkansson
 */
public interface HyphenatorFactory {

    /**
     * <p>
     * Defines hyphenation accuracy on a scale from 1 to 5 (integer):
     * </p>
     * <ul>
     * <li>5 is high accuracy, low performance</li>
     * <li>3 is medium accuracy, medium performance</li>
     * <li>1 is low accuracy, high performance</li>
     * </ul>
     *
     * <p>
     * Not all values must be implemented to support this feature. It is
     * recommended, but not strictly required, that an implementation sends a
     * log message when this feature is set to an unsupported value.
     * </p>
     */
    public String FEATURE_HYPHENATION_ACCURACY = "hyphenation-accuracy";

    /**
     * Returns a new hyphenator configured for the specified locale.
     *
     * @param locale a valid locale for the new hyphenator, as defined by IETF RFC
     *               3066
     * @return returns a new hyphenator
     * @throws HyphenatorConfigurationException if the locale is not supported
     */
    public HyphenatorInterface newHyphenator(String locale) throws HyphenatorConfigurationException;

    /**
     * Gets the value of a hyphenation feature.
     *
     * @param key the feature to get the value for
     * @return returns the value, or null if not set
     */
    public Object getFeature(String key);

    /**
     * Sets the value of a hyphenation feature.
     *
     * @param key   the feature to set the value for
     * @param value the value for the feature
     * @throws HyphenatorConfigurationException if the feature is not supported
     */
    public void setFeature(String key, Object value) throws HyphenatorConfigurationException;
}
