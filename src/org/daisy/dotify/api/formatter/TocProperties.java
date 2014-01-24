package org.daisy.dotify.api.formatter;

public class TocProperties extends SequenceProperties {
	/**
	 * Defines TOC ranges.
	 */
	enum TocRange {
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
	
	public static class Builder extends SequenceProperties.Builder {
		private final String tocName;
		private final TocRange range; 
		private final String useWhen;
		
		public Builder(String masterName, String tocName, TocRange range, String useWhen) {
			super(masterName);
			this.tocName = tocName;
			this.range = range;
			this.useWhen = useWhen;
		}
	}

	private TocProperties(Builder builder) {
		super(builder);
		this.tocName = builder.tocName;
		this.range = builder.range;
		this.useWhen = builder.useWhen;
	}

	public String getTocName() {
		return tocName;
	}

	public TocRange getRange() {
		return range;
	}

	public String getUseWhen() {
		return useWhen;
	}

}
