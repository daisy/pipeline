package org.daisy.dotify.api.formatter;

/**
 * Provides a fallback rule that indicates that the collection
 * should be reassigned to another identifier.
 * 
 * @author Joel Håkansson
 */
public class RenameFallbackRule implements FallbackRule {
	private final String fromCollection;
	private final String toCollection;

	/**
	 * Creates a new instance with the specified parameters
	 * @param fromCollection the identifier of the original collection
	 * @param toCollection the new identifier
	 */
	public RenameFallbackRule(String fromCollection, String toCollection) {
		super();
		this.fromCollection = fromCollection;
		this.toCollection = toCollection;
	}

	@Override
	public String applyToCollection() {
		return fromCollection;
	}

	/**
	 * Gets the new identifier for this collection
	 * @return returns the identifier
	 */
	public String getToCollection() {
		return toCollection;
	}

	@Override
	public boolean mustBeContextCollection() {
		return false;
	}

}
