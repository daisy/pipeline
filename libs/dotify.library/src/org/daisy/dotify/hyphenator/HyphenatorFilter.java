package org.daisy.dotify.hyphenator;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;
import org.daisy.dotify.common.text.StringFilter;

/**
 * Provides a hyphenating string filter. This filter will hyphenate the
 * filter input using the supplied hyphenator.
 *
 * @author Joel Håkansson
 */
public class HyphenatorFilter implements StringFilter {
    private final HyphenatorInterface hyphenator;

    /**
     * Creates a new hyphenator filter.
     *
     * @param factory the hyphenator factory service to use
     * @param locale  the locale
     * @throws HyphenatorConfigurationException if the locale is not supported
     */
    public HyphenatorFilter(
        HyphenatorFactoryMakerService factory,
        String locale
    ) throws HyphenatorConfigurationException {
        this(factory.newHyphenator(locale));
    }


    /**
     * Creates a new hyphenator filter.
     *
     * @param hyphenator the hyphenator to use
     */
    public HyphenatorFilter(HyphenatorInterface hyphenator) {
        this.hyphenator = hyphenator;
    }

    /**
     * Gets the begin limit. In other words, the number of unbreakable characters
     * at the beginning of each word.
     *
     * @return returns the begin limit
     */
    public int getBeginLimit() {
        return hyphenator.getBeginLimit();
    }

    /**
     * Sets the begin limit. In other words, the number of unbreakable characters
     * at the beginning of each word.
     *
     * @param beginLimit the begin limit
     */
    public void setBeginLimit(int beginLimit) {
        hyphenator.setBeginLimit(beginLimit);
    }

    /**
     * Gets the end limit. In other words, the number of unbreakable characters
     * at the end of each word.
     *
     * @return returns the end limit
     */
    public int getEndLimit() {
        return hyphenator.getEndLimit();
    }


    /**
     * Sets the end limit. In other words, the number of unbreakable characters
     * at the end of each word.
     *
     * @param endLimit the end limit
     */
    public void setEndLimit(int endLimit) {
        hyphenator.setEndLimit(endLimit);
    }

    @Override
    public String filter(String str) {
        return hyphenator.hyphenate(str);
    }

}
