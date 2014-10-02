package org.daisy.cli;

import java.util.List;

/**
 * Provides the information needed by an optional argument.
 * @author Joel Håkansson
 */
public class OptionalArgument extends Argument {
	private final String defaultValue;
	
	/**
	 * Creates a new optional argument
	 * @param name the name of the argument
	 * @param description the description of the argument
	 * @param defaultValue the default value for the argument
	 */
	public OptionalArgument(String name, String description, String defaultValue) {
		super(name, description);
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Creates a new optional argument with a finite list of acceptable values.
	 * @param name the name of the argument
	 * @param description the description of the argument
	 * @param values the list of acceptable values
	 * @param defaultValue the default value for the argument
	 */
	public OptionalArgument(String name, String description, List<Definition> values, String defaultValue) {
		super(name, description, values);
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Gets the default value.
	 * @return returns the default value for the argument
	 */
	public String getDefault() {
		return defaultValue;
	}

}