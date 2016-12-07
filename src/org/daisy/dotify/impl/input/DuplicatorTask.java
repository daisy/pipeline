package org.daisy.dotify.impl.input;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.ReadOnlyTask;

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
			Files.copy(input.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new InternalTaskException("Exception while copying file " + input + " to " + copy, e);
		}
	}

	@Override
	public void execute(AnnotatedFile input) throws InternalTaskException {
		execute(input.getFile());
	}

}
