package org.daisy.dotify.api.formatter;

/**
 * Provides a marker indicator.
 *
 * @author Joel Håkansson
 */
public class MarkerIndicator {
    private final String name, indicator;

    /**
     * Creates a new marker indicator with the specified parameters.
     *
     * @param name      the name of the markers to indicate
     * @param indicator the string indicating an occurrence
     */
    public MarkerIndicator(String name, String indicator) {
        this.name = name;
        this.indicator = indicator;
    }

    /**
     * Gets the name of the markers to indicate.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the occurrence indicator string.
     *
     * @return the occurrence indicator
     */
    public String getIndicator() {
        return indicator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((indicator == null) ? 0 : indicator.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        MarkerIndicator other = (MarkerIndicator) obj;
        if (indicator == null) {
            if (other.indicator != null) {
                return false;
            }
        } else if (!indicator.equals(other.indicator)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
