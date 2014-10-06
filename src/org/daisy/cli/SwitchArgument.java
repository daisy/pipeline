package org.daisy.cli;

/**
 * Provides a definition for switch arguments.
 * 
 * @author Joel Håkansson
 */
public class SwitchArgument extends Definition {
	private final char key;
	private final String value;

	/**
	 * Creates a switch argument. If the key is found in the command,
	 * e.g. -c then it should be substituted by [name]=[value]
	 * @param name the name of the argument
	 * @param value the value for the argument
	 * @param desc a description of the switch
	 */
	public SwitchArgument(char key, String name, String value, String desc) {
		super(name, desc);
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Gets the value for the argument when found
	 * @return the argument value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets the switch key
	 * @return the switch key
	 */
	public char getKey() {
		return key;
	}

}
