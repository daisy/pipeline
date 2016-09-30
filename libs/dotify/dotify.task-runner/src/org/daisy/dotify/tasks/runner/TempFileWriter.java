package org.daisy.dotify.tasks.runner;

import java.io.File;
import java.io.IOException;

public interface TempFileWriter {

	/**
	 * Writes the source file to a temporary folder
	 * @param source the source file
	 * @param identifier a string that can help identify the file in the temporary folder
	 * @throws IOException if something goes wrong
	 */
	public void writeTempFile(File source, String identifier) throws IOException;
	/**
	 * Deletes all temporary files written by this writer
	 */
	public void deleteTempFiles();
}
