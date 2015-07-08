package org.daisy.dotify.api.cr;


/**
 * <p>Base class for internal tasks. This class is only
 * intended to be extended by classes in this package. Refer to the 
 * direct subclasses of this class for possible extension points.</p>
 * 
 * @author Joel Håkansson
 */
public class InternalTask {
	protected String name = null;

	protected InternalTask() { }

	/**
	 * Creates a new internal task with the specfied name. The constructor
	 * is intended only for package use.
	 * @param name a descriptive name for the task
	 */
	InternalTask(String name) {
		this.name = name;
	}

	/**
	 * Get the name of the internal task
	 * @return returns the name of this internal task
	 */
	public String getName() {
		return name;
	}

}
