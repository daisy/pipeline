package org.daisy.pipeline.script;

/**
 * Metadata associated with an option of an XProc step through custom <code>px:*</code> attributes.
 */
public class XProcOptionMetadata {

        public static final String ANY_URI = "anyURI";
        public static final String ANY_FILE_URI = "anyFileURI";
        public static final String ANY_DIR_URI = "anyDirURI";

        public enum Output {
                /**
                 * Option for determining location of a result.
                 */
                RESULT,
                /**
                 * Option for determining location of temporary files.
                 */
                TEMP,
                /**
                 * Regular option (not an output option).
                 */
                NA
        }

        /**
         * The default separator for serializing a sequence of values.
         */
        public static final String DEFAULT_SEPARATOR = " ";

        final private String niceName;
        final private String description;
        final private String type;
        final private String mediaType;
        final private Output output;
        final private boolean primary;
        final private boolean isSequence;
        final private boolean isOrdered;
        final private String separator;

        public XProcOptionMetadata(String niceName, String description, String type, String mediaType) {
                this(niceName, description, type, mediaType,
                     Output.NA, true,
                     false, true, DEFAULT_SEPARATOR);
        }

        public XProcOptionMetadata(String niceName, String description, String type, String mediaType,
                                   Output output, boolean primary) {
                this(niceName, description, type, mediaType,
                     output, primary,
                     false, true, DEFAULT_SEPARATOR);
        }

        public XProcOptionMetadata(String niceName, String description, String type, String mediaType,
                                   boolean sequence, boolean ordered, String separator) {
                this(niceName, description, type, mediaType,
                     Output.NA, true,
                     sequence, ordered, separator);
        }

        public XProcOptionMetadata(String niceName, String description, String type, String mediaType,
                                   Output output, boolean primary,
                                   boolean sequence, boolean ordered, String separator) {
                this.niceName = niceName;
                this.description = description;
                this.type = type;
                this.mediaType = mediaType;
                this.output = output;
                this.primary = primary;
                this.isSequence = sequence;
                this.isOrdered = ordered;
                this.separator = separator;
        }

        /**
         * The nice name.
         */
        public String getNiceName() {
                return niceName;
        }

        /**
         * The description.
         */
        public String getDescription() {
                return description;
        }

        /**
         * The type.
         */
        public String getType() {
                return type;
        }

        /**
         * The media type.
         */
        public String getMediaType() {
                return mediaType;
        }

        /**
         * Whether the option determines the location of a result or temporary files.
         */
        public Output getOutput() {
                return output;
        }

        /**
         * Whether the option is a primary output.
         */
        public boolean isPrimary() {
                return primary;
        }

        /**
         * Whether this option takes a sequence of values.
         */
        public boolean isSequence() {
                return isSequence;
        }

        /**
         * Whether the order in a sequence matters.
         */
        public boolean isOrdered() {
                return isOrdered;
        }

        /**
         * Separator for serializing a sequence of values.
         */
        public String getSeparator() {
                return separator;
        }
}
