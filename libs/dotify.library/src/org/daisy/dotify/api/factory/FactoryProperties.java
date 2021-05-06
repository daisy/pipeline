package org.daisy.dotify.api.factory;

import java.util.Comparator;

/**
 * Provides factory properties.
 *
 * @author Joel Håkansson
 */
public interface FactoryProperties {
    /**
     * Gets the identifier for this Factory.
     *
     * @return returns the identifier for this Factory
     */
    public String getIdentifier();

    /**
     * Gets the display name for this Factory.
     *
     * @return returns the display name for this Factory
     */
    public String getDisplayName();

    /**
     * Gets the description for this Factory.
     *
     * @return returns the description for this Factory
     */
    public String getDescription();

    /**
     * Creates a new comparator builder for factory properties.
     *
     * @return a new comparator builder
     */
    public static ComparatorBuilder newComparatorBuilder() {
        return new ComparatorBuilder();
    }

    /**
     * Provides a comparator builder for factory properties.
     */
    public static class ComparatorBuilder {
        private SortOrder order;
        private SortProperty field;

        /**
         * Defines the sort order.
         */
        public enum SortOrder {
            /**
             * Sort up.
             */
            UP,
            /**
             * Sort down.
             */
            DOWN
        }

        /**
         * Defines the item to sort by.
         */
        public enum SortProperty {
            /**
             * Sort by display name.
             */
            DISPLAY_NAME,
            /**
             * Sort by identifier.
             */
            IDENTIFIER,
            /**
             * Sort by description.
             */
            DESCRIPTION
        }

        private ComparatorBuilder() {
            this.order = SortOrder.UP;
            this.field = SortProperty.DISPLAY_NAME;
        }

        /**
         * Sets the sort order value for the builder.
         *
         * @param value the value
         * @return this builder
         */
        public ComparatorBuilder sortOrder(SortOrder value) {
            this.order = value;
            return this;
        }

        /**
         * Sets the sort by value for the builder.
         *
         * @param value the value
         * @return this builder
         */
        public ComparatorBuilder sortBy(SortProperty value) {
            this.field = value;
            return this;
        }

        /**
         * Builds the comparator. Note that the returned comparator imposes orderings that are
         * inconsistent with equals.
         *
         * @return a comparator for factory properties
         */
        public Comparator<FactoryProperties> build() {
            SortOrder order = this.order;
            SortProperty field = this.field;
            return (arg0, arg1) -> {
                switch (field) {
                    case DESCRIPTION:
                        return
                            (order == SortOrder.UP ? 1 : -1) * arg0.getDescription().compareTo(arg1.getDescription());
                    case IDENTIFIER:
                        return
                            (order == SortOrder.UP ? 1 : -1) * arg0.getIdentifier().compareTo(arg1.getIdentifier());
                    case DISPLAY_NAME:
                    default:
                        return
                            (order == SortOrder.UP ? 1 : -1) * arg0.getDisplayName().compareTo(arg1.getDisplayName());
                }
            };
        }
    }

}
