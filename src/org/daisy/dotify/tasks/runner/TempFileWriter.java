package org.daisy.dotify.tasks.runner;

import java.io.File;
import java.io.IOException;

//@FunctionalInterface
public interface TempFileWriter {

	public void writeTempFile(File source, String taskName, int i) throws IOException;
}
