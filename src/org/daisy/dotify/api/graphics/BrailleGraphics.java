package org.daisy.dotify.api.graphics;

import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides basic braille graphics support using standard braille patterns.
 * @author Joel Håkansson
 *
 */
public class BrailleGraphics {
	private static final int dx = 2;
	
	private final boolean eightDot;
	private final int t = 0;
	private final int dy;


	/**
	 * Creates a new instance of braille graphics, with either full 8-dot or
	 * 6-dot patterns.
	 * 
	 * @param eightDot true if full 8-dot patterns should be used, false otherwise.
	 */
	public BrailleGraphics(boolean eightDot) {
		this.eightDot = eightDot;
		if (eightDot) {
			dy = 4;
		} else {
			dy = 3;
		}
	}
	
	/**
	 * Renders the raster using braille characters. It is assumed, in both 6-dot and
	 * 8-dot mode, that the next line follows directly after the previous line ends.
	 * In other words, no pixels are skipped in either mode. To get a better diagonal
	 * representation in 6-dot mode, render in 8-dot mode and filter the result using 
	 * sixDotFilter.
	 * 
	 * @param r the raster to render
	 * @return a braille image using characters in the range 0x2800-0x28FF or 0x2800-0x283F
	 */
	public List<String> renderGraphics(Raster r) {
		if (r.getNumBands()!=1) {
			throw new IllegalArgumentException("Only single band images can be processed. See BufferedImage.TYPE_BYTE_BINARY");
		}

		int mx = r.getMinX();
		int my = r.getMinY();
		int w = r.getWidth();
		int h = r.getHeight();
		int[]p1 = new int[1];
		int[]p2 = new int[1];
		int[]p3 = new int[1];
		int[]p4 = new int[1];
		int[]p5 = new int[1];
		int[]p6 = new int[1];
		int[]p7 = new int[1];
		int[]p8 = new int[1];
		int c = 0;
		StringBuilder sb;
		ArrayList<String> ret = new ArrayList<>();
		for (int y = my; y < h; y=y+dy) {
			sb = new StringBuilder();
			for (int x = mx; x < w; x=x+dx) {
				if (x<w&&y<h) { r.getPixel(x, y, p1); } else { p1[0]=0; }
				if (x<w&&y+1<h) { r.getPixel(x, y+1, p2); } else { p2[0]=0; }
				if (x<w&&y+2<h) { r.getPixel(x, y+2, p3); } else { p3[0]=0; }
				if (x+1<w&&y<h) { r.getPixel(x+1, y, p4); } else { p4[0]=0; }
				if (x+1<w&&y+1<h) { r.getPixel(x+1, y+1, p5); } else { p5[0]=0; }
				if (x+1<w&&y+2<h) { r.getPixel(x+1, y+2, p6); } else { p6[0]=0; }
				if (eightDot) {
					if (x<w&&y+3<h) { r.getPixel(x, y+3, p7); } else { p7[0]=0; }
					if (x+1<w&&y+3<h) { r.getPixel(x+1, y+3, p8); }	else { p8[0]=0; }
				} else {
					p7[0]=0;
					p8[0]=0;
				}
				c = 0;
				c |= (p1[0]>t?0x01:0);
				c |= (p2[0]>t?0x02:0);
				c |= (p3[0]>t?0x04:0);
				c |= (p4[0]>t?0x08:0);
				c |= (p5[0]>t?0x10:0);
				c |= (p6[0]>t?0x20:0);
				c |= (p7[0]>t?0x40:0);
				c |= (p8[0]>t?0x80:0);
				sb.append((char)(0x2800|c));
			}
			ret.add(sb.toString());
		}
		return ret;
	}
	
	/**
	 * Filters the input braille image by discarding dots 7 and 8.
	 * @param input the braille input, characters in the range 0x2800-0x28FF
	 * @return the filtered braille image, characters in the range 0x2800-0x283F
	 */
	public static List<String> sixDotFilter(List<String> input) {
		ArrayList<String> ret = new ArrayList<>();
		StringBuilder sb;
		for (String s : input) {
			sb = new StringBuilder();
			for (char c : s.toCharArray()) {
				sb.append((char)(((int)c)&0x283F));
			}
			ret.add(sb.toString());
		}
		return ret;
	}
	
	/**
	 * Combines two braille images by combining characters from the two input
	 * images. The overlay image overwrites a portion of the background image
	 * where it is placed. Bounds checking is performed to ensure that the 
	 * overlay image is placed completely within the size of the background data.
	 * 
	 * @param background the image onto which data is to be placed
	 * @param overlay the image to place onto the background
	 * @param x the overlay offset in characters
	 * @param y the overlay offset in rows
	 * @return returns the combined image
	 * @throws IllegalArgumentException if x or y is negative, or if the overlay are out of
	 * bounds of the background image.
	 */
	public static List<String> combine(List<String> background, List<String> overlay, int x, int y) {
		if (x<0||y<0) {
			throw new IllegalArgumentException("Negative offset not allowed.");
		} else if (y>background.size()) {
			throw new IllegalArgumentException("y-axis offset out of bounds.");
		}		
		if (overlay.size()+y>background.size()) {
			throw new IllegalArgumentException("Cannot fit overlay within background (y-axis)");
		}
		ArrayList<String> ret = new ArrayList<>();
		for (int i = 0; i<background.size();i++) {
			int y2 = i-y;
			String a = null;
			String b = null;
			if (y2>=0 && y2<overlay.size()) {
				a = overlay.get(y2);
			}
			if (i<background.size()) {
				b = background.get(i);
			}
			if (a!=null&&b!=null) {
				//combine
				if (a.length()+x>b.length()) {
					throw new IllegalArgumentException("Cannot fit overlay within background (x-axis, line " + i + ")");
				}
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j<b.length(); j++) {
					int x2 = j-x;
					if (x2>=0 && x2<a.length()) {
						sb.append(a.charAt(x2));
					} else if (j<b.length()) {
						sb.append(b.charAt(j));
					} else {
						throw new RuntimeException("Error in code.");
					}
				}
				ret.add(sb.toString());
			} else if (b==null && a!=null) {
				/*
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j<x; j++) {
					sb.append("\u2800");
				}
				sb.append(a);
				ret.add(sb.toString());*/
				throw new RuntimeException("Error in code.");
			} else if (a==null && b!=null) {
				ret.add(b);
			} else {
				throw new RuntimeException("Error in code.");
			}
		}
		return ret;
	}

}
