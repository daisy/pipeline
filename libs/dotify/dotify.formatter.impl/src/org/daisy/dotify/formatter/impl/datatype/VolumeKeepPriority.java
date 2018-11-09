package org.daisy.dotify.formatter.impl.datatype;

import java.util.NoSuchElementException;

/**
 * <p>Provides a simple data type for volume keep priority, where 1 represents the highest priority
 * and 9 the lowest. This class is modeled on Java's Optional and provides similar
 * capabilities. As with Optional, setting a field with this type to null is strongly
 * discouraged. Instead, use {@link #empty()} to create an empty instance.</p>
 * 
 * <p>The primary reason for creating a special class is to make the meaning of the 
 * number contained in it clearer. But in addition, it makes it possible to enforce 
 * internal bounds checks. The caller can therefore rely on this fact when getting the
 * value.</p>
 * 
 * @author Joel Håkansson
 */
public final class VolumeKeepPriority {
	// For performance reasons, -1 is used to represent the empty object
	private static final VolumeKeepPriority EMPTY = new VolumeKeepPriority(-1);
	private final int value;

	private VolumeKeepPriority(int value) {
		this.value = value;
	}
	
	/**
	 * Creates an new instance with the specified priority.
	 * @param value the priority
	 * @return returns a new instance
	 */
	public static VolumeKeepPriority of(int value) {
		if (value<1 || value>=10) {
			throw new IllegalArgumentException("Value out of range [1, 10): " + value);
		}
		return new VolumeKeepPriority(value);
	}
	
	/**
	 * Creates a new instance with the specified priority (may be null).
	 * @param value the priority, or null
	 * @return returns a new instance
	 */
	public static VolumeKeepPriority ofNullable(Number value) {
		return value==null?EMPTY:new VolumeKeepPriority(value.intValue());
	}
	
	/**
	 * Returns an instance with no priority (lowest).
	 * @return returns an instance with no priority
	 */
	public static VolumeKeepPriority empty() {
		return EMPTY;
	}
	
	/**
	 * Gets the priority, if present. The value is in the range
	 * [1, 10) where 1 represents the highest priority and 9 the
	 * lowest.
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
	 * @return returns true if a priority is available, false otherwise
	 */
	public boolean hasValue() {
		return value>=1;
	}

	/**
	 * Gets the priority if set, otherwise returns the supplied value.
	 * @param other the value to return if not set
	 * @return returns the priority, or the supplied value
	 */
	public int orElse(int other) {
		return hasValue()?getValue():other;
	}

	/**
	 * <p>Compares the specified values.</p>
	 * <p>Note that, because a higher priority is represented by a lower value,
	 * this function may appear to be incorrect in some contexts.</p>
	 * @param p1 the first value
	 * @param p2 the second value
	 * @return the value {@code 0} if the priority of {@code p1} is
	 *			equal to {@code p2}; a value less than
	 *			{@code 0} if the priority of {@code p1} is less than
	 *			the priority of {@code p2}; and a value greater than {@code 0}
	 *			if the priority of {@code p1} is greater than
	 *			the priority of {@code p2}; if {@code p1} does not have 
	 * 			a priority value and {@code p2} does, a value greater than {@code 0}
	 * 			is returned; if {@code p1} has a priority value and {@code p2} does not,
	 * 			a value less than {@code 0} is returned; if neither {@code p1} 
	 * 			nor {@code p2} has a priority value, the value {@code 0} is returned.
	 */
	public static int compare(VolumeKeepPriority p1, VolumeKeepPriority p2) {
		return Integer.compare(p1.orElse(10), p2.orElse(10));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VolumeKeepPriority other = (VolumeKeepPriority) obj;
		if (value != other.value)
			return false;
		return true;
	}

}
