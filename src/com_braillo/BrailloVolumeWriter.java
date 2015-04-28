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
package com_braillo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.daisy.braille.embosser.VolumeWriter;

/**
 * Provides a volume writer for Braillo embossers.
 * Note that, this volume writer assumes LineBreaks.Type.DOS
 * and Padding.BEFORE.
 * @author Joel Håkansson
 *
 */
public abstract class BrailloVolumeWriter implements VolumeWriter {
	public final static byte[] ffSeq = new byte[]{'\r', '\n', 0x0c}; 

	public abstract List<? extends List<Byte>> reorder(List<? extends List<Byte>> pages);
	public abstract byte[] getHeader(int pages) throws IOException;
	public abstract byte[] getFooter(int pages) throws IOException;

	public boolean write(List<? extends List<Byte>> input, File out) throws IOException {
		FileOutputStream os = new FileOutputStream(out);

		List<? extends List<Byte>> pages = reorder(input);
		
		int len = pages.size();
		os.write(getHeader(len));
		//write contents
		// debug: int j = 1;
		for (List<Byte> page : pages) {
			byte[] b = new byte[page.size()];
			for (int i=0; i<page.size(); i++) {
				b[i] = page.get(i);
			}
			// debug: os.write(("---- page --- " + j + " [").getBytes());
			os.write(b);
			// debug: os.write("]--- page ---- ".getBytes());
			// debug: j++;
		}
		os.write(getFooter(len));
		os.close();
		return true;
	}

	public boolean supportsVolumes() {
		return true;
	}

}
