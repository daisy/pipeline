/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
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
package org.daisy.braille.utils.pef;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides functionality to check if files are equal.
 * @author Joel Håkansson
 */
public class FileCompare {
	private int pos;

	/**
	 * Creates a new FileCompare object
	 */
	public FileCompare() {
		super();
	}

	/**
	 * Creates a new FileCompare object
	 * @param keepTempFiles true if temporary files should be kept, false otherwise
	 * @deprecated this constructor serves no purpose
	 */
	@Deprecated
	public FileCompare(boolean keepTempFiles) {
		super();
	}

	/**
	 * Gets the intermediary file created  
	 * from the first argument of the latest call to compareXML 
	 * (as base for the post normalization binary compare).
	 * @return returns the first file
	 * @throws IllegalStateException if temporary files are not kept
	 * or if compareXML has not been called.
	 * @deprecated this method serves no purpose here
	 */
	@Deprecated
	public File getFileOne() {
		throw new IllegalStateException();
	}

	/**
	 * Gets the intermediary file created  
	 * from the second argument of the latest call to compareXML 
	 * (as base for the post normalization binary compare).
	 * @return returns the second file
	 * @throws IllegalStateException if temporary files are not kept
	 * or if compareXML has not been called.
	 * @deprecated this method serves no purpose here
	 */
	@Deprecated
	public File getFileTwo() {
		throw new IllegalStateException();
	}

	/**
	 * Gets the byte position where the latest call to compareBinary or compareXML failed, or -1
	 * if compare was successful
	 * @return returns the byte position
	 */
	public int getPos() {
		return pos;
	}

	/**
	 * Compares the input streams binary.
	 * @param f1 the first input stream
	 * @param f2 the second input stream
	 * @return returns true if the streams are equal, false otherwise
	 * @throws IOException if IO fails
	 */
	public boolean compareBinary(InputStream f1, InputStream f2) throws IOException {
		try (	InputStream bf1 = new BufferedInputStream(f1);
				InputStream bf2 = new BufferedInputStream(f2) ){
			int b1;
			int b2;
			pos = 0;
			while ((b1 = bf1.read())!=-1 & b1 == (b2 = bf2.read())) {
				pos++;
				//continue
			}
			if (b1!=-1 || b2!=-1) {
				return false;
			}
			pos = -1;
			return true;
		}
	}
}
