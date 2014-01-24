package org.daisy.dotify.api.engine;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides a formatter engine. The formatter engine is the outer layer
 * of the formatter, requiring only an input stream and an output stream.
 * 
 * @author Joel Håkansson
 */
public interface FormatterEngine {

	/**
	 * Converts the OBFL in the input stream and writes the result to the output stream.
	 * @param input the OBFL input
	 * @param output the output stream
	 * @throws LayoutEngineException if the conversion fails
	 */
	public void convert(InputStream input, OutputStream output) throws LayoutEngineException;
}
