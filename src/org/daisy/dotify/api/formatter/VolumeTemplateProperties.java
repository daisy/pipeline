package org.daisy.dotify.api.formatter;

public class VolumeTemplateProperties {
	private final String volumeNumberVariable;
	private final String volumeCountVariable;
	private final String useWhen;
	private final int splitterMax;
	
	public static class Builder {
		private final int splitterMax;
		private String useWhen = null;
		private String volumeNumberVariable = null;
		private String volumeCountVariable = null;

		public Builder(int splitterMax) {
			this.splitterMax = splitterMax;
		}
		
		public Builder useWhen(String useWhen) {
			this.useWhen = useWhen;
			return this;
		}
		
		public Builder volumeNumberVariable(String value) {
			volumeNumberVariable = value;
			return this;
		}
		
		public Builder volumeCountVariable(String name) {
			volumeCountVariable = name;
			return this;
		}
		
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

	public String getVolumeNumberVariable() {
		return volumeNumberVariable;
	}

	public String getVolumeCountVariable() {
		return volumeCountVariable;
	}

	public String getUseWhen() {
		return useWhen;
	}

	public int getSplitterMax() {
		return splitterMax;
	}

}
