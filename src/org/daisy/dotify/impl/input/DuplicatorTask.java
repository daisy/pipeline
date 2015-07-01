package org.daisy.dotify.impl.input;

import java.io.File;
import java.io.IOException;

import org.daisy.dotify.api.cr.InternalTaskException;
import org.daisy.dotify.api.cr.ReadOnlyTask;
import org.daisy.dotify.common.io.FileIO;

/**
 * <p>DuplicatorTask copies the input file both to output and to a separate file.
 * This can be useful for example for debugging.</p>
 * <p>No specific input requirements.</p>
 * 
 * @author Joel Håkansson
 */
public class DuplicatorTask extends ReadOnlyTask {
	private final File copy;
	
	/**
	 * Create a new DuplicatorTask with the specified parameters
	 * @param name a descriptive name for the task
	 * @param copy path to debug output
	 */
	public DuplicatorTask(String name, File copy) {
		super(name);
		this.copy = copy;
	}

	@Override
	public void execute(File input) throws InternalTaskException {
		try {
			FileIO.copyFile(input, copy);
		} catch (IOException e) {
			throw new InternalTaskException("Exception while copying file.", e);
		}
	}

}
