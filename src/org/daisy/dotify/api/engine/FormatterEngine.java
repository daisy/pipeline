package org.daisy.dotify.api.engine;

import java.io.InputStream;
import java.io.OutputStream;


public interface FormatterEngine {

	public void convert(InputStream input, OutputStream output) throws LayoutEngineException;
}
