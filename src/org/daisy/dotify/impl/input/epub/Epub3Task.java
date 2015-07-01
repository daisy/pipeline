package org.daisy.dotify.impl.input.epub;

import java.io.File;

import org.daisy.dotify.api.cr.InternalTaskException;
import org.daisy.dotify.api.cr.ReadWriteTask;

public class Epub3Task extends ReadWriteTask {

	Epub3Task(String name) {
		super(name);
	}

	@Override
	public void execute(File input, File output) throws InternalTaskException {

	}

}
