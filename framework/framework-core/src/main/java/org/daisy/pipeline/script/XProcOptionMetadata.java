/*
 *
 */
package org.daisy.pipeline.script;

/**
 * Option related metadata.
 */
public class XProcOptionMetadata {

        public enum Output {
                RESULT,
                TEMP,
                NA
        }

        /** The Constant SEPARATOR_DEFAULT */
        public static final String DEFAULT_SEPARATOR= " ";

        /** The nice name. */
        final private String niceName;

        /** The description. */
        final private String description;

        /** The type. */
        final private String type;

        /** The media type. */
        final private String mediaType;

        /** if the option is a primary output. */
        final private boolean primary;

        final private Output output;
        final private boolean isSequence;
        final private boolean isOrdered;
        final private String separator;

        /**
         * Instantiates a new {@link XProcOptionMetadata} object.
         *
         * @param niceName
         *            the nice name
         * @param description
         *            the description
         * @param type
         *            the type
         * @param mediaType
         *            the media type
         */
        private XProcOptionMetadata(String niceName, String description,
                        String type, String mediaType, Output output,
                        boolean sequence, boolean ordered, String separator, boolean primary) {
                super();
                this.niceName = niceName;
                this.description = description;
                this.type = type;
                this.mediaType = mediaType;
                this.output = output;
                this.isSequence = sequence;
                this.isOrdered = ordered;
                this.separator=separator;
                this.primary=primary;
        }

        /**
         * Gets the nice name.
         *
         * @return the nice name
         */
        public String getNiceName() {
                return niceName;
        }

        /**
         * Gets the description.
         *
         * @return the description
         */
        public String getDescription() {
                return description;
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public String getType() {
                return type;
        }

        /**
         * Gets the media type.
         *
         * @return the media type
         */
        public String getMediaType() {
                return mediaType;
        }

        public Output getOutput() {
                return output;
        }

        public boolean isOrdered() {
                return isOrdered;
        }

        public boolean isPrimary() {
                return this.primary;
        }
        public boolean isSequence() {
                return isSequence;
        }

        public String getSeparator() {
                return separator;
        }

        /**
         * Builds the {@link XProcOptionMetadata} object.
         */
        public static final class Builder {

                /** The nice name. */
                private String niceName;

                /** The description. */
                private String description;

                /** The type. */
                private String type;

                /** The media type. */
                private String mediaType;

                // specify some defaults for optional attributes

                /** Indicates whether the order in a sequence matters */
                private boolean ordered = true;

                /** The nature of the output -- temp, result, or na (which means not output at all) */
                private Output output = Output.NA;

                /** Indicates whether this option takes a sequence */
                private boolean sequence = false;

                /** The separator for a sequence */
                private String separator = XProcOptionMetadata.DEFAULT_SEPARATOR;

                private boolean primary;

                /**
                 * With description.
                 *
                 * @param description
                 *            the description
                 * @return the builder
                 */
                public Builder withDescription(String description) {
                        this.description = description;
                        return this;
                }

                /**
                 * With nice name.
                 *
                 * @param niceName
                 *            the nice name
                 * @return the builder
                 */
                public Builder withNiceName(String niceName) {
                        this.niceName = niceName;
                        return this;
                }

                /**
                 * With type.
                 *
                 * @param type
                 *            the type
                 * @return the builder
                 */
                public Builder withType(String type) {
                        this.type = type;
                        return this;
                }

                /**
                 * With media type.
                 *
                 * @param mediaType
                 *            the media type
                 * @return the builder
                 */
                public Builder withMediaType(String mediaType) {
                        this.mediaType = mediaType;
                        return this;
                }



                public Builder withOutput(String value) {
                        if (value.equalsIgnoreCase(Output.RESULT.toString())) {
                                output = Output.RESULT;
                        }
                        else if (value.equalsIgnoreCase(Output.TEMP.toString())) {
                                output = Output.TEMP;
                        }
                        else {
                                output = Output.NA;
                        }
                        return this;
                }

                public Builder withSequence(String value) {
                        if (value.equalsIgnoreCase("true")) {
                                sequence = true;
                        }
                        else {
                                sequence = false;
                        }
                        return this;
                }

                public Builder withOrdered(String value) {
                        if (value.equalsIgnoreCase("true")) {
                                ordered = true;
                        }
                        else {
                                ordered = false;
                        }
                        return this;
                }

                public Builder withSeparator(String value) {
                        if(value!=null && !value.isEmpty()){
                                this.separator=value;
                        }
                        return this;
                }

                public Builder withPrimary(boolean primary) {
                        this.primary=primary;
                        return this;
                }
                /**
                 * Builds instance
                 *
                 * @return the {@link XProcOptionMetadata}
                 */
                public XProcOptionMetadata build() {
                        return new XProcOptionMetadata(niceName, description, type,
                                        mediaType, output, sequence, ordered, separator, primary);
                }


        }

}
