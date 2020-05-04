package org.daisy.dotify.formatter.impl.search;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * <p>Provides a simple data type for volume keep priority, where 1 represents the highest priority
 * and 9 the lowest. A high priority means that we do more effort to avoid splitting a volume within
 * the area the priority is declared on.</p>
 *
 * <p>This class is modeled on Java's Optional and provides similar capabilities. As with Optional,
 * setting a field with this type to null is strongly discouraged. Instead, use {@link #empty()} to
 * create an empty instance.</p>
 *
 * <p>The primary reason for creating a special class is to make the meaning of the
 * number contained in it clearer. But in addition, it makes it possible to enforce
 * internal bounds checks. The caller can therefore rely on this fact when getting the
 * value.</p>
 *
 * @author Joel Håkansson
 */
public final class VolumeKeepPriority implements Comparable<VolumeKeepPriority> {

    // For performance reasons, -1 is used to represent the empty object
    private static final VolumeKeepPriority EMPTY = new VolumeKeepPriority(-1);
    private final int value;

    private VolumeKeepPriority(int value) {
        this.value = value;
    }

    /**
     * Creates an new instance with the specified priority.
     *
     * @param value the priority
     * @return returns a new instance
     */
    public static VolumeKeepPriority of(int value) {
        if (value < 1 || value > 9) {
            throw new IllegalArgumentException("Value out of range [1, 9]: " + value);
        }
        return new VolumeKeepPriority(value);
    }

    /**
     * Creates a new instance with the specified priority (may be null).
     *
     * @param value the priority, or null
     * @return returns a new instance
     */
    public static VolumeKeepPriority ofNullable(Number value) {
        return value == null ? EMPTY : new VolumeKeepPriority(value.intValue());
    }

    /**
     * Returns an instance with no priority (lowest).
     *
     * @return returns an instance with no priority
     */
    public static VolumeKeepPriority empty() {
        return EMPTY;
    }

    /**
     * Gets the priority value, if present. The value is in the range
     * [1, 9] where 1 represents the highest priority and 9 the lowest.
     *
     * @return returns the priority
     * @throws NoSuchElementException if no priority is set.
     */
    public int getValue() {
        if (!hasValue()) {
            throw new NoSuchElementException();
        }
        return value;
    }

    /**
     * Returns true if a priority is set, false otherwise. If a priority
     * is set, it can be retrieved using {@link #getValue()}.
     *
     * @return returns true if a priority is available, false otherwise
     */
    public boolean hasValue() {
        return value >= 1;
    }

    /**
     * Gets the priority if set, otherwise returns the supplied value.
     *
     * @param other the value to return if not set
     * @return returns the priority, or the supplied value
     */
    public int orElse(int other) {
        return hasValue() ? getValue() : other;
    }

    /**
     * <p>Compares this priority with the specified priority.</p>
     *
     * <p>Returns a negative integer, zero, or a positive integer as this priority is lower, equal
     * to, or higher than the specified priority. An {@link #empty() absent} priority is considered
     * lower than the lowest priority.</p>
     *
     * <p>Note that this method does not compare the <em>integer values</em> returned by {@link
     * #getValue()}, but rather represents the "natural" ordering. (The fact that a higher priority
     * is represented by a lower integer value may cause some confusion in this regard.)</p>
     *
     * @param o The other priority
     * @return The value {@code 0} if the priority of {@code this} is equal to {@code o}; a value
     * less than {@code 0} if the priority of {@code this} is lower than the priority of
     * {@code o}; and a value greater than {@code 0} if the priority of {@code this} is
     * greater than the priority of {@code o}; if {@code this} does not have a priority
     * value and {@code o} does, a value greater than {@code 0} is returned; if {@code
     * this} has a priority value and {@code o} does not, a value less than {@code 0} is
     * returned; if neither {@code this} nor {@code o} has a priority value, the value
     * {@code 0} is returned.
     */
    public int compareTo(VolumeKeepPriority o) {
        return Integer.compare(o.orElse(10), this.orElse(10));
    }

    /**
     * <p>Returns a comparator that imposes the natural ordering on {@link VolumeKeepPriority} objects.</p>
     *
     * <p>Objects are ordered according to {@link #compareTo(VolumeKeepPriority)}, from lower to higher priority.</p>
     *
     * @return a comparator that imposes the natural ordering on {@link VolumeKeepPriority} objects.
     */
    public static Comparator<VolumeKeepPriority> naturalOrder() {
        if (comparator == null) {
            comparator = Comparator.<VolumeKeepPriority>naturalOrder();
        }
        return comparator;
    }

    private static Comparator<VolumeKeepPriority> comparator = null;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
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
        VolumeKeepPriority other = (VolumeKeepPriority) obj;
        if (value != other.value) {
            return false;
        }
        return true;
    }

}
