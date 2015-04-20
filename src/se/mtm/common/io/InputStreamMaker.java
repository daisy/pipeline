package se.mtm.common.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides an interface for recreating the same input
 * stream many times.
 * 
 * @author Joel Håkansson
 *
 */
public interface InputStreamMaker {

	/**
	 * Returns a new input stream for the source.
	 * @return a new input stream
	 * @throws IOException
	 */
	public InputStream newInputStream() throws IOException;
	
}
