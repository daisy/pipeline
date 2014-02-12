package se.mtm.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

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
