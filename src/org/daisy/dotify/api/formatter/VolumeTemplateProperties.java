package org.daisy.dotify.api.formatter;

/**
 * <p>Defines properties for a volume template.</p>
 * 
 * @author Joel Håkansson
 */
public class VolumeTemplateProperties {
	private final String volumeNumberVariable;
	private final String volumeCountVariable;
	private final String useWhen;
	private final int splitterMax;
	
	/**
	 * Provides a builder for creating volume template properties instances.
	 * 
	 * @author Joel Håkansson
	 */
	public static class Builder {
		private final int splitterMax;
		private String useWhen = null;
		private String volumeNumberVariable = null;
		private String volumeCountVariable = null;

		/**
		 * Creates a new Builder.
		 * 
		 * @param splitterMax the maximum number of sheets in a volume using this template
		 */
		public Builder(int splitterMax) {
			this.splitterMax = splitterMax;
		}
		
		/**
		 * Sets the condition for applying the volume template.
		 * @param useWhen the condition
		 * @return returns the builder
		 */
		public Builder useWhen(String useWhen) {
			this.useWhen = useWhen;
			return this;
		}
		
		/**
		 * Sets the variable name for the volume number that can be used in the condition.
		 * @param value the variable name
		 * @return returns the builder
		 */
		public Builder volumeNumberVariable(String value) {
			volumeNumberVariable = value;
			return this;
		}
		
		/**
		 * Sets the variable name for the volume count that can be used in the condition.
		 * @param name the variable name
		 * @return returns the builder
		 */
		public Builder volumeCountVariable(String name) {
			volumeCountVariable = name;
			return this;
		}
		
		/**
		 * Creates a new VolumeTemplateProperties instance based on the current
		 * configuration.
		 * 
		 * @return a new VolumeTemplateProperties instance
		 */
		public VolumeTemplateProperties build() {
			return new VolumeTemplateProperties(this);
		}
	}

	protected VolumeTemplateProperties(Builder builder) {
		this.splitterMax = builder.splitterMax;
		this.useWhen = builder.useWhen;
		this.volumeNumberVariable = builder.volumeNumberVariable;
		this.volumeCountVariable = builder.volumeCountVariable;
	}

	/**
	 * Gets the variable name used for volume number.
	 * 
	 * @return returns the name of the variable
	 */
	public String getVolumeNumberVariable() {
		return volumeNumberVariable;
	}

	/**
	 * Gets the variable name used for volume count.
	 * 
	 * @return returns the name of the variable
	 */
	public String getVolumeCountVariable() {
		return volumeCountVariable;
	}

	/**
	 * Gets the condition for applying the volume template
	 * 
	 * @return returns the condition
	 */
	public String getUseWhen() {
		return useWhen;
	}

	/**
	 * Gets the maximum number of sheets allowed in a volume that uses this template.
	 * 
	 * @return returns the maximum number of sheets
	 */
	public int getSplitterMax() {
		return splitterMax;
	}

}
