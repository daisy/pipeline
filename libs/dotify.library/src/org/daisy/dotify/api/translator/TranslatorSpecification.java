package org.daisy.dotify.api.translator;

import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Provides a translator specification.
 *
 * @author Joel Håkansson
 */
public class TranslatorSpecification implements Comparable<TranslatorSpecification> {
    private static final Logger logger = Logger.getLogger(TranslatorSpecification.class.getCanonicalName());
    private static final Pattern LOCALE_PATTERN = Pattern.compile("([a-zA-Z]{1,8}(\\-[0-9a-zA-Z]{1,8})*)?");
    private final String locale;
    private final String localeKey; // holds the lower case value of locale for compare, equals and hashcode
    private final String mode;
    private final String modeKey; // holds the lower case value of mode for compare, equals and hashcode
    private final TranslatorMode translatorMode;

    /**
     * Creates a new translator specification with the supplied values.
     *
     * @param locale a locale as defined by IETF RFC 3066
     * @param mode   a translator mode
     * @throws IllegalArgumentException if null/empty values are inserted
     */
    public TranslatorSpecification(String locale, String mode) {
        this.locale = verifyLocale(locale);
        this.mode = verifyMode(mode);
        this.localeKey = locale.toLowerCase(Locale.ROOT);
        this.modeKey = mode.toLowerCase(Locale.ROOT);
        this.translatorMode = TranslatorMode.withIdentifier(mode);
    }

    /**
     * Creates a new translator specification with the supplied values.
     *
     * @param locale a locale as defined by IETF RFC 3066
     * @param mode   a translator mode
     * @throws IllegalArgumentException if null/empty values are inserted
     */
    public TranslatorSpecification(String locale, TranslatorMode mode) {
        this.locale = verifyLocale(locale);
        this.mode = verifyMode(mode.getIdentifier());
        this.localeKey = locale.toLowerCase(Locale.ROOT);
        this.modeKey = this.mode.toLowerCase(Locale.ROOT);
        this.translatorMode = mode;
    }

    private static String verifyLocale(String value) {
        if ("".equals(Objects.requireNonNull(value))) {
            throw new IllegalArgumentException("Empty locale not allowed");
        }
        if (!LOCALE_PATTERN.matcher(value).matches()) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Not a valid locale as defined by IETF RFC 3066: " + value);
            }
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return value;
    }

    private static String verifyMode(String value) {
        if ("".equals(Objects.requireNonNull(value))) {
            throw new IllegalArgumentException("Empty mode not allowed");
        }
        return value;
    }

    /**
     * Gets the locale.
     *
     * @return the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Gets the translator mode.
     *
     * @return the translator mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * Gets the translator mode details.
     *
     * @return the translator mode details
     */
    public TranslatorMode getModeDetails() {
        return translatorMode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((localeKey == null) ? 0 : localeKey.hashCode());
        result = prime * result + ((modeKey == null) ? 0 : modeKey.hashCode());
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
        TranslatorSpecification other = (TranslatorSpecification) obj;
        if (localeKey == null) {
            if (other.localeKey != null) {
                return false;
            }
        } else if (!localeKey.equals(other.localeKey)) {
            return false;
        }
        if (modeKey == null) {
            if (other.modeKey != null) {
                return false;
            }
        } else if (!modeKey.equals(other.modeKey)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(TranslatorSpecification o) {
        int l = this.localeKey.compareTo(o.localeKey);
        if (l != 0) {
            return l;
        } else {
            return this.modeKey.compareTo(o.modeKey);
        }
    }

    @Override
    public String toString() {
        return locale + "/" + mode;
    }

}
