package org.daisy.dotify.api.formatter;

/**
 * Position is a data object for an integer position.
 *
 * @author Joel Håkansson
 */
public class Position {

    final boolean isRelative;
    final double value;

    /**
     * Create a new Position with the supplied value.
     *
     * @param value      the position
     * @param isRelative if true, the value is a percentage
     * @throws IllegalArgumentException if the value is less than zero, or if the Position is absolute and its
     *                   value is not an integer
     */
    public Position(double value, boolean isRelative) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be positive " + value);
        }
        if (!isRelative && (int) value != value) {
            throw new IllegalArgumentException("Absolute value must be an integer " + value);
        }
        this.isRelative = isRelative;
        this.value = value;
    }

    /**
     * Parses the supplied String as a Position. If the string ends with '%',
     * the position will be parsed as a relative position, otherwise
     * it will be parsed as an integer.
     *
     * @param pos the string to parse
     * @return returns the new Position
     * @throws IllegalArgumentException if the value is less than zero, or if the Position is absolute and its
 *              value is not an integer
     */
    public static Position parsePosition(String pos) {
        pos = pos.trim();
        if (pos.endsWith("%")) {
            // remove %
            pos = pos.substring(0, pos.length() - 1).trim();
            return new Position(Double.parseDouble(pos) / 100, true);
        } else {
            return new Position(Double.parseDouble(pos), false);
        }
    }

    /**
     * Returns true if this Position is relative, false otherwise.
     *
     * @return returns true if this Position is relative, false otherwise
     */
    public boolean isRelative() {
        return isRelative;
    }

    /**
     * Gets the position value.
     *
     * @return returns the position value
     */
    public double getValue() {
        return value;
    }

    /**
     * Gets the absolute value for this Position by multiplying with the given width.
     * If this position is already absolute, the original value is returned.
     *
     * @param width the width to use
     * @return returns the absolute value
     */
    public int makeAbsolute(int width) {
        double ret;
        if (isRelative()) {
            ret = width * getValue();
        } else {
            ret = getValue();
        }
        return (int) Math.round(ret);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isRelative ? 1231 : 1237);
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        Position other = (Position) obj;
        if (isRelative != other.isRelative) {
            return false;
        }
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value)) {
            return false;
        }
        return true;
    }
}
