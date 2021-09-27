package org.daisy.dotify.api.formatter;

/**
 * Provides a marker indicator.
 *
 * @author Joel Håkansson
 */
public class MarkerIndicator {
    private final String name, indicator, textStyle;

    /**
     * Creates a new marker indicator with the specified parameters.
     *
     * @param name      the name of the markers to indicate
     * @param indicator the string indicating an occurrence
     * @param textStyle the text style
     */
    public MarkerIndicator(String name, String indicator, String textStyle) {
        this.name = name;
        this.indicator = indicator;
        this.textStyle = textStyle;
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

    /**
     * The text style of the marker indicator, or null if no special style is used.
     *
     * @return the text style
     */
    public String getTextStyle() {
        return textStyle;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((indicator == null) ? 0 : indicator.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((textStyle == null) ? 0 : textStyle.hashCode());
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
        if (textStyle == null) {
            if (other.textStyle != null) {
                return false;
            }
        } else if (!textStyle.equals(other.textStyle)) {
            return false;
        }
        return true;
    }

}
