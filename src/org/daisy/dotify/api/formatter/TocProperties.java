package org.daisy.dotify.api.formatter;

/**
 * <p>Defines properties specific for a TOC sequence.</p>
 * 
 * @author Joel Håkansson
 */
public class TocProperties extends SequenceProperties {
	/**
	 * Defines TOC ranges.
	 */
	public enum TocRange {
		/**
		 * Defines the TOC range to include the entire document
		 */
		DOCUMENT,
		/**
		 * Defines the TOC range to include entries within the volume
		 */
		VOLUME};

	private final String tocName;
	private final TocRange range; 
	private final String useWhen;
	
	/**
	 * Provides a builder for creating TOC properties instances.
	 * 
	 * @author Joel Håkansson
	 */
	public static class Builder extends SequenceProperties.Builder {
		private final String tocName;
		private final TocRange range; 
		private final String useWhen;
		
		/**
		 * Creates a new builder with the supplied arguments.
		 * 
		 * @param masterName the master identifier
		 * @param tocName the toc identifier
		 * @param range a range for the TOC
		 * @param useWhen a condition for when to apply the TOC sequence
		 */
		public Builder(String masterName, String tocName, TocRange range, String useWhen) {
			super(masterName);
			this.tocName = tocName;
			this.range = range;
			this.useWhen = useWhen;
		}
		
		public TocProperties build() {
			return new TocProperties(this);
		}
	}

	private TocProperties(Builder builder) {
		super(builder);
		this.tocName = builder.tocName;
		this.range = builder.range;
		this.useWhen = builder.useWhen;
	}

	/**
	 * Gets the toc identifier.
	 * @return returns the toc identifier
	 */
	public String getTocName() {
		return tocName;
	}

	/**
	 * Gets the toc range.
	 * @return returns the toc range
	 */
	public TocRange getRange() {
		return range;
	}

	/**
	 * Gets the condition for applying the TOC
	 * @return the condition
	 */
	public String getUseWhen() {
		return useWhen;
	}

}
