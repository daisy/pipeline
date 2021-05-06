package org.daisy.dotify.api.translator;

import java.util.Locale;
import java.util.Objects;

/**
 * Provides braille translator types, see also {@link TranslatorMode}.
 *
 * @author Joel Håkansson
 */
public enum TranslatorType {
    /**
     * Uncontracted braille.
     */
    UNCONTRACTED,
    /**
     * Contracted braille.
     */
    CONTRACTED,
    /**
     * The contents is already braille, but may also contain spaces, soft hyphens and zero width spaces.
     */
    PRE_TRANSLATED,
    /**
     * An identity translator that returns the characters of the input.
     */
    BYPASS;

    @Override
    public String toString() {
        return name().replace('_', '-').toLowerCase(Locale.ROOT);
    }

    /**
     * Parses a string and tries to find a matching constant.
     *
     * @param name the name to parse
     * @return a constant
     * @throws IllegalArgumentException if there is no constant with the specified name
     */
    public static TranslatorType parse(String name) {
        return valueOf(Objects.requireNonNull(name).replace('-', '_').toUpperCase(Locale.ROOT));
    }

}
