package org.daisy.cli;

/**
 * Provides a definition for switch arguments.
 * 
 * @author Joel Håkansson
 */
public class SwitchArgument extends Definition {
	private final char key;
	private final String alias;
	private final String value;

	/**
	 * Creates a switch argument. If the key is found in the command,
	 * e.g. -c then it should be substituted by [name]=[value]
	 * @param key the switch key
	 * @param name the name of the argument
	 * @param value the value for the argument
	 * @param desc a description of the switch
	 * @throws IllegalArgumentException if key is null
	 */
	public SwitchArgument(char key, String name, String value, String desc) {
		this(key, null, name, value, desc);
	}
	
	/**
	 * Creates a switch argument. If the key is found in the command,
	 * e.g. -c then it should be substituted by [name]=[value]
	 * @param key the switch key
	 * @param alias the switch alias
	 * @param name the name of the argument
	 * @param value the value for the argument
	 * @param desc a description of the switch
	 * @throws IllegalArgumentException if alias is less than two characters
	 * @throws IllegalArgumentException if alias is null
	 */
	public SwitchArgument(String alias, String name, String value, String desc) {
		this(null, alias, name, value, desc);
	}
	
	/**
	 * Creates a switch argument. If the key is found in the command,
	 * e.g. -c then it should be substituted by [name]=[value]
	 * @param key the switch key
	 * @param alias the switch alias
	 * @param name the name of the argument
	 * @param value the value for the argument
	 * @param desc a description of the switch
	 * @throws IllegalArgumentException if alias is less than two characters
	 * @throws IllegalArgumentException if both key and alias are null
	 */
	public SwitchArgument(Character key, String alias, String name, String value, String desc) {
		super(name, desc);
		if (key==null && alias==null) {
			throw new IllegalArgumentException("'key' and 'alias' cannot both be null.");
		}
		if (alias!=null && alias.length()<2) {
			throw new IllegalArgumentException("Argument 'alias' must be at least two characters.");
		}
		this.key = key;
		this.value = value;
		this.alias = alias;
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
	 * @return the switch key, or null if not set
	 */
	public Character getKey() {
		return key;
	}

	/**
	 * Gets the switch alias (long name)
	 * @return returns the alias, or null if not set
	 */
	public String getAlias() {
		return alias;
	}

}
