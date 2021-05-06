package org.daisy.dotify.hyphenator.impl;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorFactory;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;

import java.util.logging.Logger;

/**
 * Provides a hyphenator factory for compound words.
 *
 * @author Joel Håkansson
 */
public class CWHyphenatorFactory implements HyphenatorFactory {
    private static final Logger logger = Logger.getLogger(CWHyphenatorFactory.class.getCanonicalName());
    private int accuracy = 5;

    /**
     * Returns true if the specified locale is supported, false otherwise.
     *
     * @param locale the locale to test
     * @return returns true if the locale is supported, false otherwise
     */
    public boolean supportsLocale(String locale) {
        return CWHyphenator.supportsLocale(locale);
    }

    @Override
    public HyphenatorInterface newHyphenator(String locale) throws HyphenatorConfigurationException {
        return new CWHyphenator(locale, accuracy);
    }

    @Override
    public Object getFeature(String key) {
        if (key.equals(HyphenatorFactory.FEATURE_HYPHENATION_ACCURACY)) {
            return accuracy;
        }
        return null;
    }

    @Override
    public void setFeature(String key, Object value) throws HyphenatorConfigurationException {
        if (key.equals(HyphenatorFactory.FEATURE_HYPHENATION_ACCURACY)) {
            accuracy = (Integer) value;
            if (accuracy != 5 && accuracy != 3) {
                logger.fine(
                    "Feature " + HyphenatorFactory.FEATURE_HYPHENATION_ACCURACY +
                        " set to an unsupported value: " + accuracy + ". Supported values are 3 and 5."
                );
            }
        } else {
            throw new LatexHyphenatorConfigurationException();
        }
    }

}
