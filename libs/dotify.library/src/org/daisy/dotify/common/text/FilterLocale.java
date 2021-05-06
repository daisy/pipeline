package org.daisy.dotify.common.text;

import java.util.Locale;

/**
 * Custom Locale implementation.
 *
 * @author joha
 */
public class FilterLocale {
    private final String lang, country, variant, str;

    private FilterLocale(String lang, String country, String variant) {
        this.lang = lang.intern();
        this.country = country.intern();
        this.variant = variant.intern();
        this.str =
            (lang + ("".equals(country) ? "" : "-" + country + ("".equals(variant) ? "" : "-" + variant))).intern();
    }

    /**
     * Parses the string into a locale.
     *
     * @param locale the locale string
     * @return returns a new filter locale
     * @throws IllegalArgumentException if the locale is not valid as defined by IETF RFC 3066
     */
    public static FilterLocale parse(String locale) {
        if (!locale.matches("([a-zA-Z]{1,8}(\\-[0-9a-zA-Z]{1,8})*)?")) {
            throw new IllegalArgumentException("Not a valid locale as defined by IETF RFC 3066: " + locale);
        }
        String[] parts = locale.split("-", 3);
        String lang = parts[0].toLowerCase();
        String country = "";
        String variant = "";
        if (parts.length >= 2) {
            country = parts[1].toUpperCase();
        }
        if (parts.length >= 3) {
            variant = parts[2];
        }
        return new FilterLocale(lang, country, variant);
    }

    /**
     * Returns this filter locale as a standard java Locale object.
     *
     * @return returns a standard Locale object
     */
    public Locale toLocale() {
        return new Locale(lang, country, variant);
    }

    /**
     * Gets the language of this locale.
     *
     * @return returns the language
     */
    public String getLanguage() {
        return lang;
    }

    /**
     * Gets the country of this locale.
     *
     * @return returns the locale
     */
    public String getCountry() {
        return country;
    }

    /**
     * Gets the variant of this locale.
     *
     * @return returns the variant
     */
    public String getVariant() {
        return variant;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((str == null) ? 0 : str.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FilterLocale other = (FilterLocale) obj;
        if (str == null) {
            if (other.str != null) {
                return false;
            }
        } else if (!str.equals(other.str)) {
            return false;
        }
        return true;
    }

    /**
     * This locale is a subtype of the other locale.
     *
     * @param other the other locale
     * @return returns true if this locale is a subtype of the supplied locale
     */
    public boolean isA(FilterLocale other) {
        // all strings are pooled so str == other.str implies that str.equals(other.str)
        if (other.variant != "" && this.variant != other.variant) {
            return false;
        }
        if (other.country != "" && this.country != other.country) {
            return false;
        }
        if (this.lang != other.lang) {
            return false;
        }
        return true;

    }

    @Override
    public String toString() {
        return str;
    }

}
