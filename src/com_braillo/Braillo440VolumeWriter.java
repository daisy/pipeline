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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.daisy.braille.embosser.EmbosserTools;
import org.daisy.paper.Dimensions;


class Braillo440VolumeWriter extends BrailloVolumeWriter {
	public enum Mode {SW_TWO_PAGE, SW_FOUR_PAGE, SWSF};
	private final int width;
	@SuppressWarnings("unused")
	private final int height;
	private final Dimensions pageFormat;
	private final Mode mode;
	private final double columnWidthMM;
	
	Braillo440VolumeWriter(Dimensions pageFormat, Mode mode, int width, int height, double columnWidthMM) {
		/*
		int margin = 4; // approximate width of unprintable margins.
		if (Paper.newPaper(PaperSize.A4).equals(paper)) {
			// Underestimate margin width for A4 in order to get 32 chars width (pages will be 21,74 cm wide)
			margin = 3;
		}
		this.width = paper.getWidth(cellWidth)-margin;*/
		
		// Paper.INCH_IN_MM = the smallest possible margins (0.5 inch + 0.5 inch)
		this.width = width;
		this.height = height;
		this.pageFormat = pageFormat;
		this.mode = mode;
		this.columnWidthMM = columnWidthMM;
		int inchHeight = (int)Math.ceil(pageFormat.getHeight()/EmbosserTools.INCH_IN_MM);
		if (width > 44 || inchHeight > 13) { 
			throw new IllegalArgumentException("Paper too wide or high: " + width + " chars x " + inchHeight + " inches."); 
		}
		if (width < 10) {
			throw new IllegalArgumentException("Paper too narrow: " + width + " chars.");
		}
	}

	@SuppressWarnings("unchecked")
	private List<Byte>[] newList(int s) {
		return new List[s];
	}

	public List<? extends List<Byte>> reorder(List<? extends List<Byte>> pages) {
		switch (mode) {
			case SW_FOUR_PAGE: case SWSF: {
					int s = (int)(Math.ceil(pages.size()/4d)*4);
					List<Byte>[] r = newList(s);
					for (int i=1;i<=s;i++) {
						int pos;
						if (mode==Mode.SW_FOUR_PAGE) {
							pos = getPos(i, s);
						} else if (mode==Mode.SWSF) {
							pos = getPosReversed(i, s);
						} else {
							throw new RuntimeException("Unexpected error.");
						}
						if (i>pages.size()) {
							r[pos] = new ArrayList<Byte>();
							for (byte b:ffSeq) {
								r[pos].add(b);
							}
						} else {
							r[pos] = pages.get(i-1);
						}
					}
					ArrayList<List<Byte>> ret = new ArrayList<List<Byte>>();
					for (List<Byte> r2 : r) {
						ret.add(r2);
					}
					return ret;
				}
			case SW_TWO_PAGE: {
					int s = (int)(Math.ceil(pages.size()/2d)*2);
					List<Byte>[] r = newList(s);
					for (int i=0;i<s;i++) {
						if (i>=pages.size()) {
							r[i] = new ArrayList<Byte>();
							for (byte b:ffSeq) {
								r[i].add(b);
							}
						} else {
							r[i] = pages.get(i);
						}
					}
					ArrayList<List<Byte>> ret = new ArrayList<List<Byte>>();
					for (List<Byte> r2 : r) {
						ret.add(r2);
					}
					return ret;
				}
			default:
				throw new RuntimeException("Unexpected error.");
		}
	}
	
	private int getPos(int pos, int len) {
		if (len % 4 != 0) {
			throw new IllegalArgumentException("len % 4 must be 0");
		}
		if (pos>len) {
			throw new IllegalArgumentException("len must be larger than pos");
		}
		if (pos<=len/2) {
			int s = (int)Math.ceil(pos / 2d);
			return (s - 1) * 4 + (pos - 1) % 2; 
		} else {
			int s = (int)Math.ceil((len - pos + 1) / 2d);
			return (s - 1) * 4 + (pos - 1) % 2 + 2;
		}
	}
	
	private int getPosReversed(int pos, int len) {
		return len-(getPos(pos, len)+1);
	}

	//jvm1.6@Override
	public byte[] getFooter(int len) {
		return new byte[0];
	}

	//jvm1.6@Override
	public byte[] getHeader(int len) throws IOException {
		// Round to the closest possible higher value, so that all characters fit on the page
		byte[] w = EmbosserTools.toBytes(width, 2);
		byte[] p;
		int pi = 0;
		byte m;
		double m1 = Math.round(((pageFormat.getWidth() - columnWidthMM) / (2 * EmbosserTools.INCH_IN_MM)) * 10) / 10d;
		double m2 = Math.round(((pageFormat.getWidth() - m1 * EmbosserTools.INCH_IN_MM - columnWidthMM) / EmbosserTools.INCH_IN_MM) * 10) / 10d;
		byte[] mb1 = EmbosserTools.toBytes((int)(m1 * 10), 2);
		byte[] mb2 = EmbosserTools.toBytes((int)(m2 * 10), 2);
		switch (mode) {
			case SW_FOUR_PAGE: case SWSF: {
				pi = (int)Math.ceil(len/4d);
				p = EmbosserTools.toBytes(pi, 2);
				m = '1';
				break;
			}
			case SW_TWO_PAGE: {
				pi = (int)Math.ceil(len/2d);
				p = EmbosserTools.toBytes(pi, 2);
				m = '0';
				break;
			}
			default:
				throw new RuntimeException("Unexpected error.");
		}
		if (pi>99) {
			throw new IOException("Too many sheets in one volume: " + pi);
		}
		return new byte[] {
			0x1b, 'A', m, 			// page mode
			0x1b, 'B', w[0], w[1], 	// width, in characters
			0x1b, 'D', mb1[0], mb1[1], 	// margin 1 
			0x1b, 'E', mb2[0], mb2[1], 	// margin 2
			0x1b, 'H', '1', 		// interpoint
			0x1b, 'P', p[0], p[1]	// number of sheets
			};
	}


}
