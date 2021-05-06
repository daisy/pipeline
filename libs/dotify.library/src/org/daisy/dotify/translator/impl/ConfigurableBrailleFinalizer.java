package org.daisy.dotify.translator.impl;

import org.daisy.dotify.translator.BrailleFinalizer;

/**
 * Provides a configurable braille finalizer.
 *
 * @author Joel Håkansson
 */
public final class ConfigurableBrailleFinalizer implements BrailleFinalizer {
    private final String space;
    private final String hyphen;

    /**
     * Provides a builder.
     */
    public static class Builder {
        private String space = "\u2800";
        private String hyphen = "\u2824";

        /**
         * Creates a new builder.
         */
        public Builder() {
        }

        /**
         * Sets the character to replace whitespaces in the input with.
         *
         * @param value the whitespace value
         * @return the builder
         */
        public Builder space(String value) {
            this.space = value;
            return this;
        }

        /**
         * Sets the character to replace hyphens in the input with.
         *
         * @param value the value for hyphens
         * @return the builder
         */
        public Builder hyphen(String value) {
            this.hyphen = value;
            return this;
        }

        /**
         * Builds a {@link ConfigurableBrailleFinalizer} based on the current configuration of this builder.
         *
         * @return a new {@link ConfigurableBrailleFinalizer} instance
         */
        public ConfigurableBrailleFinalizer build() {
            return new ConfigurableBrailleFinalizer(this);
        }
    }

    private ConfigurableBrailleFinalizer(Builder builder) {
        this.space = builder.space;
        this.hyphen = builder.hyphen;
    }

    @Override
    public String finalizeBraille(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case ' ':
                    sb.append(space);
                    break;
                case '\u00a0':
                    sb.append(space);
                    break;
                case '-':
                    sb.append(hyphen);
                    break;
                case '\u00ad':
                    sb.append(hyphen);
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
