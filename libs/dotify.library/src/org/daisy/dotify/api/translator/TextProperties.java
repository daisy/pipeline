package org.daisy.dotify.api.translator;

import java.util.Optional;

/**
 * Provides an interface for a text to translate. Note that this interface does
 * not define any way to extract the text.
 *
 * @author Joel Håkansson
 * @see ResolvableText
 */
interface TextProperties {
// Note that this interface is not public. This is done for two reasons:
//  1.  There's no need to expose this interface by itself.
//  2.  The name clashes with TextProperties in the formatter API and if a better name
//      should come to mind, it is possible to change it later without compatibility
//      concerns.

    /**
     * <p>Gets the locale for the text, if specified.</p>
     *
     * <p>Note that this method returns the <i>language
     * that the text is written in</i>. It does not imply association with
     * a particular translator or braille code.</p>
     *
     * @return an optional containing the locale, never null
     */
    public Optional<String> getLocale();

    /**
     * Returns true if the text should be hyphenated.
     *
     * @return true if the text should be hyphenated, false otherwise
     */
    public boolean shouldHyphenate();

    /**
     * Returns true if the text should mark capital letters.
     *
     * @return true if the capital letters should be marked, false otherwise
     */
    public boolean shouldMarkCapitalLetters();

}
