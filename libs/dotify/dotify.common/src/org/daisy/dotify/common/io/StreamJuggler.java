package org.daisy.dotify.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>Provides a juggler for streams.</p>
 * <p>This interface can be used to limit the code needed to handle temporary streams
 * in a sequence of read/write operations. After each step (stream written) 
 * the juggler can be reset by calling reset() and the streams are ready to be used
 * again. Very convenient together with optional steps.</p>
 * 
 * @author Joel Håkansson
 */
public interface StreamJuggler extends Closeable {

	/**
	 * Get the current input stream maker.
	 * 
	 * @return Returns the current input stream or null if StreamJuggler has
	 *         been closed
	 */
	public InputStreamMaker getInputStreamMaker();
	
	/**
	 * Get the current output stream
	 * 
	 * @return Returns the current output stream or null if StreamJuggler has
	 *         been closed
	 * @throws IOException if an IO-problem occurs
	 */
	public OutputStream getOutputStream() throws IOException;
	
	/**
	 * Resets the input and output stream so that the input contains the
	 * written result and the output is cleared.
	 * 
	 * @throws IOException
	 *             throws IOException
	 */
	public void reset() throws IOException;
}
