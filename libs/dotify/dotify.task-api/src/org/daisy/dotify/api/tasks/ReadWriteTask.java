package org.daisy.dotify.api.tasks;

import java.io.File;

/**
 * Provides an abstract base for read/write tasks. 
 * 
 * @author Joel Håkansson
 *
 */
public abstract class ReadWriteTask extends InternalTask { //NOPMD

	/**
	 * Creates a new read/write task with the specified name
	 * @param name the name of the task
	 */
	public ReadWriteTask(String name) {
		super(name);
	}

	/**
	 * Apply the task to <code>input</code> and place the result in <code>output</code>.
	 * @param input input file
	 * @param output output file
	 * @throws InternalTaskException throws InternalTaskException if something goes wrong.
	 */
	public abstract void execute(File input, File output) throws InternalTaskException;
	
	/**
	 * 
	 * @param input input file
	 * @param output output file
	 * @return returns the annotated output file
	 * @throws InternalTaskException
	 */
	public abstract AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException;

}
