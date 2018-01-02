package org.daisy.common.properties;

public class Property {
	private String bundleName;
	private long bundleId;
	private String propertyName;
	private String value;


	@Override
	public boolean equals(Object obj) {
		if (!( obj instanceof Property))
			return false;
		Property other=(Property) obj;
		boolean eq=true;
		eq&=this.bundleId==other.bundleId;
		eq&=this.bundleName.equals(other.bundleName);
		eq&=this.propertyName.equals(other.propertyName);
		eq&=(this.value==null && other.value==null || this.value != null && this.value.equals(other.value));
		return eq;

	}

	@Override
	public String toString() {
		return String.format("%s: %s (Provided by %s [%d])",this.propertyName,this.value,this.bundleName,this.bundleId);
	}

	public static class Builder {
		private String bundleName;
		private long bundleId;
		private String propertyName;
		private String value;

		public Builder withBundleName(String name){
			this.bundleName=name;
			return this;
		}

		public Builder withBundleId(long id){
			this.bundleId=id;
			return this;
		}

		public Builder withPropertyName(String propertyName){
			this.propertyName=propertyName;
			return this;
		}

		public Builder withValue(String value){
			this.value=value;
			return this;
		}

		public Property build(){
			return new Property(this.bundleName,this.bundleId,this.propertyName,this.value);
		}

	}


	/**
	 * Constructs a new instance.
	 *
	 * @param bundleName The bundleName for this instance.
	 * @param bundleId The bundleId for this instance.
	 * @param propertyName The propertyName for this instance.
	 * @param value The value for this instance.
	 */
	private Property(String bundleName, long bundleId, String propertyName,
			String value) {
		this.bundleName = bundleName;
		this.bundleId = bundleId;
		this.propertyName = propertyName;
		this.value = value;
	}

	/**
	 * Gets the bundleName for this instance.
	 *
	 * @return The bundleName.
	 */
	public String getBundleName() {
		return this.bundleName;
	}


	/**
	 * Gets the bundleId for this instance.
	 *
	 * @return The bundleId.
	 */
	public long getBundleId() {
		return this.bundleId;
	}

	/**
	 * Gets the propertyName for this instance.
	 *
	 * @return The propertyName.
	 */
	public String getPropertyName() {
		return this.propertyName;
	}


	/**
	 * Gets the value for this instance.
	 *
	 * @return The value.
	 */
	public String getValue() {
		return this.value;
	}



}
