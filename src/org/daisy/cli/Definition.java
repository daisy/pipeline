package org.daisy.cli;
/**
 * Provides a definition, that is a name and a description
 * @author Joel Håkansson
 */
public class Definition {
	private final String name;
	private final String desc;
	
	/**
	 * Creates a new Definition.
	 * @param name the name of the definition
	 * @param desc the description of the definition
	 */
	public Definition(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}

	/**
	 * Gets the name of the definition
	 * @return returns the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the description of the definition
	 * @return returns the description
	 */
	public String getDescription() {
		return desc;
	}
}