package org.daisy.pipeline.job;

/**
 *  The index allows to identify a single result among a set of outputs from a single port or option.
 *
 *
 */
public final class Index {
	private final String index;

	/**
	 * Creates an index based on a string
	 * 
	 * @param index
	 */
	public Index(String index) {
		if (index == null)
			throw new NullPointerException();
		this.index = index;
	}

	public Index stripPrefix() {
		int idx = this.index.indexOf('/');
		if (idx != 0) {
			return new Index(this.index.substring(idx + 1));
		}
		return this;
	}

	@Override
	public String toString() {
		return this.index;
	}

}
