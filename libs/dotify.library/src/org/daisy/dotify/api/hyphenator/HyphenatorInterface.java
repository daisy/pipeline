package org.daisy.dotify.api.hyphenator;


/**
 * Provides a hyphenator. The hyphenator hyphenates text using rules specific to the
 * language that the hyphenator supports.
 *
 * @author Joel Håkansson
 */
public interface HyphenatorInterface {

    /**
     * Hyphenates the phrase, inserting soft hyphens at all possible breakpoints.
     *
     * @param phrase the phrase to hyphenate
     * @return the hyphenated string
     */
    public String hyphenate(String phrase);

    /**
     * Gets the begin limit. In other words, the number of unbreakable characters
     * at the beginning of each word.
     *
     * @return returns the begin limit
     */
    public int getBeginLimit();

    /**
     * Sets the begin limit. In other words, the number of unbreakable characters
     * at the beginning of each word.
     *
     * @param beginLimit the begin limit
     */
    public void setBeginLimit(int beginLimit);

    /**
     * Gets the end limit. In other words, the number of unbreakable characters
     * at the end of each word.
     *
     * @return returns the end limit
     */
    public int getEndLimit();

    /**
     * Sets the end limit. In other words, the number of unbreakable characters
     * at the end of each word.
     *
     * @param endLimit the end limit
     */
    public void setEndLimit(int endLimit);

}
