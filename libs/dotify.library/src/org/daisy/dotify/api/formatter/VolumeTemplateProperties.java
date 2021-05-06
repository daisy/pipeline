package org.daisy.dotify.api.formatter;

/**
 * <p>Defines properties for a volume template.</p>
 *
 * @author Joel Håkansson
 */
public class VolumeTemplateProperties {
    private final Condition condition;
    private final int splitterMax;

    /**
     * Provides a builder for creating volume template properties instances.
     *
     * @author Joel Håkansson
     */
    public static class Builder {
        private final int splitterMax;
        private Condition condition = null;

        /**
         * Creates a new Builder.
         *
         * @param splitterMax the maximum number of sheets in a volume using this template
         */
        public Builder(int splitterMax) {
            this.splitterMax = splitterMax;
        }

        /**
         * Sets the condition for applying the volume template.
         *
         * @param condition the condition
         * @return returns the builder
         */
        public Builder condition(Condition condition) {
            this.condition = condition;
            return this;
        }

        /**
         * Creates a new VolumeTemplateProperties instance based on the current
         * configuration.
         *
         * @return a new VolumeTemplateProperties instance
         */
        public VolumeTemplateProperties build() {
            return new VolumeTemplateProperties(this);
        }
    }

    protected VolumeTemplateProperties(Builder builder) {
        this.splitterMax = builder.splitterMax;
        this.condition = builder.condition;
    }


    /**
     * Gets the condition for applying the volume template.
     *
     * @return returns the condition
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * Gets the maximum number of sheets allowed in a volume that uses this template.
     *
     * @return returns the maximum number of sheets
     */
    public int getSplitterMax() {
        return splitterMax;
    }

}
