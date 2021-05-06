package org.daisy.dotify.api.formatter;

/**
 * A MarkerReference is a marker value lookup. The lookup is determined by the current position,
 * possibly with an offset, by a scope and a direction.
 */
public class MarkerReference {

    /**
     * Defines marker search directions.
     */
    public enum MarkerSearchDirection {
        /**
         * Defines a forward search direction.
         */
        FORWARD,
        /**
         * Defines a backward search direction.
         */
        BACKWARD
    }

    /**
     * Defines marker search scopes.
     */
    public enum MarkerSearchScope {
        /**
         * Defines page content search scope.
         */
        PAGE_CONTENT,
        /**
         * Defines page search scope.
         */
        PAGE,
        /**
         * Defines spread content search scope.
         */
        SPREAD_CONTENT,
        /**
         * Defines spread search scope.
         */
        SPREAD,
        /**
         * Defines sheet search scope.
         */
        SHEET,
        /**
         * Defines sequence search scope.
         */
        SEQUENCE,
        /**
         * Defines volume search scope.
         */
        VOLUME,
        /**
         * Defines document search scope.
         */
        DOCUMENT
    }

    private final String markerName;
    private final MarkerSearchDirection dir;
    private final MarkerSearchScope scope;
    private final int offset;

    /**
     * Creates a new instance with the specified parameters.
     *
     * @param markerName the name of the marker
     * @param dir        the direction
     * @param scope      the scope
     * @param offset     offsets the search by the specified amount, in pages
     */
    public MarkerReference(String markerName, MarkerSearchDirection dir, MarkerSearchScope scope, int offset) {
        if (markerName == null || dir == null || scope == null) {
            throw new IllegalArgumentException("null arguments not allowed");
        }
        this.markerName = markerName;
        this.dir = dir;
        this.scope = scope;
        this.offset = offset;
    }

    /**
     * Gets the name of this marker reference.
     *
     * @return returns the name
     */
    public String getName() {
        return markerName;
    }

    /**
     * Gets the direction of the search.
     *
     * @return returns the search direction
     */
    public MarkerSearchDirection getSearchDirection() {
        return dir;
    }

    /**
     * Gets the scope of the search.
     *
     * @return returns the search scope
     */
    public MarkerSearchScope getSearchScope() {
        return scope;
    }

    /**
     * Gets the page offset where to start the search.
     *
     * @return the page offset
     */
    public int getOffset() {
        return offset;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + markerName.hashCode();
        result = prime * result + dir.hashCode();
        result = prime * result + scope.hashCode();
        result = prime * result + offset;
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
        MarkerReference other = (MarkerReference) obj;
        if (!markerName.equals(other.markerName)) {
            return false;
        }
        if (!dir.equals(other.dir)) {
            return false;
        }
        if (!scope.equals(other.scope)) {
            return false;
        }
        if (offset != other.offset) {
            return false;
        }
        return true;
    }
}
