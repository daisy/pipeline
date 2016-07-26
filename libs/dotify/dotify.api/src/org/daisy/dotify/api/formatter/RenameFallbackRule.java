package org.daisy.dotify.api.formatter;

public class RenameFallbackRule implements FallbackRule {
	private final String fromCollection;
	private final String toCollection;

	public RenameFallbackRule(String fromCollection, String toCollection) {
		super();
		this.fromCollection = fromCollection;
		this.toCollection = toCollection;
	}

	@Override
	public String applyToCollection() {
		return fromCollection;
	}

	public String getToCollection() {
		return toCollection;
	}

	@Override
	public boolean mustBeContextCollection() {
		return false;
	}

}
