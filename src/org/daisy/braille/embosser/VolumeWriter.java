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
package org.daisy.braille.embosser;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Provides an interface for writing a volume of braille to a file.
 * @author Joel Håkansson
 */
public interface VolumeWriter {

	/**
	 * Writes the pages in this volume to a file
	 * @param pages the pages to write
	 * @param f the file to write to
	 * @return returns true if writing was successful, false otherwise
	 * @throws IOException
	 */
	public boolean write(List<? extends List<Byte>> pages, File f) throws IOException;

}
