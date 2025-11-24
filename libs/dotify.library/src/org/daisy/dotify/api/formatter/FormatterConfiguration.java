package org.daisy.dotify.api.formatter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides global settings for a formatter.
 *
 * @author Joel Håkansson
 */
public class FormatterConfiguration {
    private final String translationMode;
    private final String locale;
    private final boolean allowsTextOverflowTrimming;
    private final boolean allowsEndingPageOnHyphen;
    private final boolean allowsEndingVolumeOnHyphen;
    private final boolean hyphenating;
    private final boolean marksCapitalLetters;
    private final Set<String> ignoredStyles;
    private final int pagesPerSheet;

    /**
     * Provides a builder for formatter configuration.
     *
     * @author Joel Håkansson
     */
    public static class Builder {
        private final String translationMode;
        private final String locale;
        private boolean allowsTextOverflowTrimming = false;
        private boolean allowsEndingPageOnHyphen = false;
        private boolean allowsEndingVolumeOnHyphen = false;
        private boolean hyphenating = true;
        private boolean marksCapitalLetters = true;
        private Set<String> ignoredStyles = new HashSet<String>();
        private int pagesPerSheet = 2;

        /**
         * Creates a new builder with the specified properties.
         *
         * @param locale the locale
         * @param mode   the braille translation mode
         */
        public Builder(String locale, String mode) {
            this.translationMode = mode;
            this.locale = locale;
        }

        /**
         * Sets the text overflow trimming policy. If the value is true, text that overflows
         * its boundaries may be truncated if needed. If the value is false, an
         * error should be thrown and the process aborted (default).
         *
         * @param value the value of the text overflow trimming policy
         * @return returns this builder
         */
        public Builder allowsTextOverflowTrimming(boolean value) {
            this.allowsTextOverflowTrimming = value;
            return this;
        }

        /**
         * Sets the hyphenation policy for the last line (of the main flow) in each page.
         *
         * @param value true if the last line may be hyphenated, false otherwise.
         * @return returns this builder
         */
        public Builder allowsEndingPageOnHyphen(boolean value) {
            this.allowsEndingPageOnHyphen = value;
            return this;
        }

        /**
         * Sets the hyphenation policy for the last line (of the main flow) in each volume.
         *
         * If the {@link #allowsEndingPageOnHyphen()} setting is <code>false</code>, it overrides
         * this setting.
         *
         * @param value true if the last line may be hyphenated, false otherwise.
         * @return returns this builder
         */
        public Builder allowsEndingVolumeOnHyphen(boolean value) {
            this.allowsEndingVolumeOnHyphen = value;
            return this;
        }

        /**
         * Sets the global hyphenation policy.
         *
         * @param value the value of the global hyphenation policy
         * @return returns this builder
         */
        public Builder hyphenate(boolean value) {
            hyphenating = value;
            return this;
        }

        /**
         * Sets the global capital letter policy.
         *
         * @param value the value of the capital letters policy
         * @return returns this builder
         */
        public Builder markCapitalLetters(boolean value) {
            marksCapitalLetters = value;
            return this;
        }

        /**
         * Adds a style to ignore.
         *
         * @param style a style to ignore
         * @return returns this builder
         */
        public Builder ignoreStyle(String style) {
            ignoredStyles.add(style);
            return this;
        }

        /**
         * Sets the the number of pages that a sheet of paper can hold.
         *
         * This is a property of the output medium. The value can be either two or four. A page is
         * one side of a "leaf", which can be either a sheet or a half-sheet. A sheet that is not
         * folded has one leaf, or two pages. A sheet folded in half has two leaves, or four pages.
         *
         * Note that this meaning of sheet differs from what a sheet means in OBFL. A sheet in
         * OBFL is a leaf. Also note that a leaf is always composed of two pages, whether or not
         * both sides are printed (see the <a
         * href="https://mtmse.github.io/obfl/obfl-specification.html#L5024">duplex</a> attribute in
         * OBFL).
         *
         * @param value the number of pages per sheet
         * @return returns this builder
         */
        public Builder pagesPerSheet(int value) {
            switch (value) {
            case 2:
            case 4:
                pagesPerSheet = value;
                return this;
            default:
                throw new IllegalArgumentException("Value of pagesPerSheet must be 2 or 4");
            }
        }

        /**
         * Creates new configuration.
         *
         * @return returns a new configuration instance
         */
        public FormatterConfiguration build() {
            return new FormatterConfiguration(this);
        }

    }

    /**
     * Creates a new builder with the specified properties.
     *
     * @param locale the locale
     * @param mode   the braille translation mode
     * @return returns a new builder
     */
    public static Builder with(String locale, String mode) {
        return new Builder(locale, mode);
    }

    private FormatterConfiguration(Builder builder) {
        locale = builder.locale;
        translationMode = builder.translationMode;
        allowsTextOverflowTrimming = builder.allowsTextOverflowTrimming;
        allowsEndingPageOnHyphen = builder.allowsEndingPageOnHyphen;
        allowsEndingVolumeOnHyphen = builder.allowsEndingVolumeOnHyphen;
        hyphenating = builder.hyphenating;
        marksCapitalLetters = builder.marksCapitalLetters;
        ignoredStyles = Collections.unmodifiableSet(new HashSet<>(builder.ignoredStyles));
        pagesPerSheet = builder.pagesPerSheet;
    }

    /**
     * Gets the translation mode.
     *
     * @return returns the translation mode
     */
    public String getTranslationMode() {
        return translationMode;
    }

    /**
     * Gets the locale.
     *
     * @return returns the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Returns true if text that overflows is allowed to be truncated, false otherwise.
     *
     * @return returns true if text that overflows is allowed, false otherwise
     */
    public boolean isAllowingTextOverflowTrimming() {
        return allowsTextOverflowTrimming;
    }

    /**
     * Returns true if the last line (of the main flow) in each page may be
     * hyphenated, if necessary.
     *
     * @return returns true if the last line may be hyphenated, false otherwise
     */
    public boolean allowsEndingPageOnHyphen() {
        return allowsEndingPageOnHyphen;
    }

    /**
     * Returns true if the last line (of the main flow) in each volume may be
     * hyphenated, if necessary.
     *
     * @return returns true if the last line may be hyphenated, false otherwise
     */
    public boolean allowsEndingVolumeOnHyphen() {
        return allowsEndingVolumeOnHyphen;
    }

    /**
     * Returns true if the formatter is hyphenating.
     *
     * @return returns true if the formatter is hyphenating, false otherwise
     */
    public boolean isHyphenating() {
        return hyphenating;
    }

    /**
     * Returns true if capital letters should be marked, false otherwise.
     *
     * @return returns true if capital letters should be marked, false otherwise
     */
    public boolean isMarkingCapitalLetters() {
        return marksCapitalLetters;
    }

    /**
     * Gets a set of ignored styles.
     *
     * @return returns the set of ignored styles
     */
    public Set<String> getIgnoredStyles() {
        return ignoredStyles;
    }

    /**
     * Returns the number of pages that a sheet can hold.
     *
     * @return the number of pages per sheet
     */
    public int getPagesPerSheet() {
        return pagesPerSheet;
    }
}
