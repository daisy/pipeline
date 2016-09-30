package org.daisy.dotify.api.tasks;

/**
 * Provides a value and the description of that value, to be used
 * in a finite list of acceptable values for a task option.
 * 
 * @author Joel Håkansson
 *
 */
public final class TaskOptionValue {
	private final String 	name,
							description;
	
	public static class Builder {
		private final String name;
		private String description = "";
		
		public Builder(String name) {
			this.name = name;
		}
		
		public Builder description(String value) {
			this.description = value;
			return this;
		}
		
		public TaskOptionValue build() {
			return new TaskOptionValue(this);
		}
	}
	
	/**
	 * Creates a new task option value with the specified
	 * name.
	 * @param name the name of the value
	 * @return returns a new builder
	 */
	public static TaskOptionValue.Builder withName(String name) {
		return new TaskOptionValue.Builder(name);
	}

	private TaskOptionValue(Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
	}

	/**
	 * Gets the name for the option value. Note that the name should be 
	 * a unique <b>value</b> in the list of values for the option. It is
	 * <b>NOT</b> the option's key.
	 * @return returns the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the description of the option.
	 * @return returns the description
	 */
	public String getDescription() {
		return description;
	}

}
