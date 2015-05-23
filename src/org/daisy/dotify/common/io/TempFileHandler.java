/*
 * org.daisy.util (C) 2005-2008 Daisy Consortium
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.dotify.common.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Given an initial input file and a final output file, this class can be
 * used to limit the code needed to handle temporary files in a file to file
 * chain. After each step (file written) the temporary files are
 * reset by calling reset() and you are ready to use the files again.
 * Very convenient together with optional steps.
 * 
 * Note: TempFileHandler does not work on zero byte files. The output file
 * must contain data when a call to swap() is made.
 * 
 * @author Joel Håkansson
 */
public class TempFileHandler implements StreamJuggler {
	private File t1;
	private File t2;
	private InputStreamMaker is;
	private OutputStream os;
	private File output;
	private boolean toggle;
	
	/**
	 * Constructs a new TempFileHandler object
	 * 
	 * @param input
	 *            An existing input file
	 * @param output
	 *            An output file
	 * @throws IOException
	 *             An IOException is thrown if the input does not exist
	 *             or if the input or output is a directory or if the temporary
	 *             files could not be created.
	 */
	public TempFileHandler(File input, File output) throws IOException {
		if (!input.exists()) { throw new FileNotFoundException(); }
		if (!input.isFile() || (output.exists() && !output.isFile())) {
			throw new IOException("Cannot perform this operation on directories.");
		}
		this.toggle = true;
		this.output = output;
		this.t1 = FileIO.createTempFile();
		this.t2 = FileIO.createTempFile();
		FileIO.copy(input, this.t1);
		is = null;
		os = null;
	}

	/**
	 * Get the current input file
	 * 
	 * @return Returns the current input file or null if TempFileHandler has
	 *         been closed
	 */
	public File getInput() {
		return toggle ? t1 : t2;
	}
	
	/**
	 * Get the current output file
	 * 
	 * @return Returns the current output file or null if TempFileHandler has
	 *         been closed
	 */
	public File getOutput() {
		return toggle ? t2 : t1;
	}
	
	/**
	 * Resets the input and output file before writing to the output again
	 * 
	 * @throws IOException
	 *             An IOException is thrown if TempFileHandler has been
	 *             closed or if the output file is open or empty.
	 */
	public void reset() throws IOException {
		if (t1==null || t2==null) {
			throw new IllegalStateException("Cannot swap after close.");
		}
		is = null;
		os = null;
		if (getOutput().length()>0) {
			toggle = !toggle;
			// reset the new output to length()=0
			new FileOutputStream(getOutput()).close();
		} else {
			throw new IOException("Cannot swap to an empty file.");
		}
	}
	
	/**
	 * Closes the temporary files and copies the result to the output file.
	 * Closing the TempFileHandler is a mandatory last step after which no other
	 * calls to the object should be made.
	 * 
	 * @throws IOException
	 *             An IOException is thrown if the temporary files have been
	 *             deleted, or are empty.
	 */
	public void close() throws IOException {
		if (t1==null || t2==null) {
			return;
		}
		try {
			if (getOutput().length() > 0) { FileIO.copy(getOutput(), output); }
			else if (getInput().length() > 0) { FileIO.copy(getInput(), output); }
			else {
				throw new IOException("Temporary files corrupted.");
			}
		} finally {
			t1.delete();
			t2.delete();
			t1 = null;
			t2 = null;
		}
	}

	public InputStreamMaker getInputStreamMaker() {
		if (t1==null || t2==null) {
			throw new IllegalStateException("Closed.");
		}
		if (is==null) {
			is = new FileInputStreamMaker(getInput());
		}
		return is;
	}

	public OutputStream getOutputStream() throws IOException {
		if (t1==null || t2==null) {
			throw new IllegalStateException("Closed.");
		}
		if (os==null) {
			os = new FileOutputStream(getOutput());
		}
		return os;
	}

}
