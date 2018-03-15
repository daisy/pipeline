package org.daisy.pipeline.braille.liblouis;

import java.io.File;

import org.daisy.pipeline.braille.common.ResourceResolver;

public interface LiblouisTableResolver extends ResourceResolver {
	
	public File[] resolveLiblouisTable(LiblouisTable table, File base);
	
}
