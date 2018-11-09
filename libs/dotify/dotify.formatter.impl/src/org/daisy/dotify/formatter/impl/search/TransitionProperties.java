package org.daisy.dotify.formatter.impl.search;

import org.daisy.dotify.formatter.impl.datatype.VolumeKeepPriority;

/**
 * Provides details about a page that are needed for advanced volume transitions.
 *  
 * @author Joel Håkansson
 */
public class TransitionProperties {
	private static final TransitionProperties EMPTY = new TransitionProperties(VolumeKeepPriority.empty(), false);
	private final VolumeKeepPriority keepPriority;
	private final boolean hasBlockBoundary;
	
	/**
	 * Creates a new instance with the specified arguments. 
	 * @param keepPriority the volume keep priority for the page
	 * @param hasBlockBoundary true if this page has a block boundary that is useful for
	 * 		 a volume transition, false otherwise
	 */
	public TransitionProperties(VolumeKeepPriority keepPriority, boolean hasBlockBoundary) {
		this.keepPriority = keepPriority;
		this.hasBlockBoundary = hasBlockBoundary;
	}
	
	/**
	 * Gets the volume keep priority for this page
	 * @return returns the volume keep priority
	 */
	public VolumeKeepPriority getVolumeKeepPriority() {
		return keepPriority;
	}
	
	/**
	 * Returns true if this page has a block boundary that is useful for a volume transition, false otherwise
	 * @return returns true if the page has a block boundary that is useful for volume transitions, false otherwise
	 */
	public boolean hasBlockBoundary() {
		return hasBlockBoundary;
	}
	
	/**
	 * Returns a default transition.
	 * @return returns the default transition instance
	 */
	public static TransitionProperties empty() {
		return EMPTY;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (hasBlockBoundary ? 1231 : 1237);
		result = prime * result + ((keepPriority == null) ? 0 : keepPriority.hashCode());
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
		TransitionProperties other = (TransitionProperties) obj;
		if (hasBlockBoundary != other.hasBlockBoundary)
			return false;
		if (keepPriority == null) {
			if (other.keepPriority != null)
				return false;
		} else if (!keepPriority.equals(other.keepPriority))
			return false;
		return true;
	}

}
