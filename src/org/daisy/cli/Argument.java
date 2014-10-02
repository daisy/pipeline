package org.daisy.cli;

import java.util.List;

/**
 * Provides the information needed by an application argument.
 * @author Joel Håkansson
 */
public class Argument extends Definition {
	private final List<Definition> values;
	
	/**
	 * Creates a new Argument.
	 * @param name the name of the argument
	 * @param desc the description of the argument
	 */
	public Argument(String name, String desc) {
		this(name, desc, null);
	}
	
	/**
	 * Creates a new Argument with a finite list of acceptable values.
	 * @param name the name of the argument
	 * @param desc the description of the argument
	 * @param values the list of acceptable values
	 */
	public Argument(String name, String desc, List<Definition> values) {
		super(name, desc);
		this.values = values;
	}

	/**
	 * Returns true if this argument has a finite list of acceptable values.
	 * @return returns true if a finite list of acceptable values exist, false otherwise
	 */
	public boolean hasValues() {
		return values!=null && values.size()>0;
	}
	
	/**
	 * Gets the list of acceptable values.
	 * @return returns the list of acceptable values, or null if the list of possible values 
	 * is infinite
	 */
	public List<Definition> getValues() {
		return values;
	}
}