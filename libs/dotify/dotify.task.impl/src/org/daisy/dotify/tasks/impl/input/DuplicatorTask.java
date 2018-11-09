package org.daisy.dotify.tasks.impl.input;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadOnlyTask;

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
	@Deprecated
	public void execute(File input) throws InternalTaskException {
		execute(DefaultAnnotatedFile.with(input).build());
	}

	@Override
	public void execute(AnnotatedFile input) throws InternalTaskException {
		try {
			Files.copy(input.getPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new InternalTaskException("Exception while copying file " + input + " to " + copy, e);
		}
	}

}
