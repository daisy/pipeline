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
package org.daisy.braille.pef;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Provides a way to generate PEF-files for testing purposes.
 * The files can be configured to contain a specified number of
 * volumes, pages, rows and columns. The duplex property can also
 * be set. The file is filed with random content in the specified
 * braille range (6- or 8-dot). 
 * 
 * @author Joel Håkansson
 *
 */
public class PEFGenerator {
	/**
	 * Key used in the settings map passed to the constructor. Its value defines
	 * the number of volumes
	 * in the generated file 
	 */
	public static String KEY_VOLUMES = "volumes";
	/**
	 * Key used in the settings map passed to the constructor. Its value defines
	 * the number of pages per volume
	 * in the generated file 
	 */

	public static String KEY_PPV = "pages";
	/**
	 * Key used in the settings map passed to the constructor. Its value defines
	 * if eight dot should be used (true/false) 
	 */
	public static String KEY_EIGHT_DOT = "eightdot";
	/**
	 * Key used in the settings map passed to the constructor. Its value defines
	 * the maximum number of rows on a page
	 * in the generated file 
	 */
	public static String KEY_ROWS = "rows";
	/**
	 * Key used in the settings map passed to the constructor. Its value defines
	 * the maximum number of columns on a page
	 * in the generated file 
	 */
	public static String KEY_COLS = "cols";
	/**
	 * Key used in the settings map passed to the constructor. Its value defines
	 * the value of the duplex property (true/false).
	 * Note that the value of this property does not affect the number of pages generated in each volume.
	 */
	public static String KEY_DUPLEX = "duplex";

	private final static Map<String, String> defaults;
	static {
		defaults = new HashMap<String, String>();
		defaults.put(KEY_VOLUMES, "3");
		defaults.put(KEY_PPV, "20");
		defaults.put(KEY_EIGHT_DOT, "false");
		defaults.put(KEY_ROWS, "29");
		defaults.put(KEY_COLS, "32");
		defaults.put(KEY_DUPLEX, "true");
	}
	
	private int volumes;
	private int pagesPerVolume;
	private boolean eightDot;
	private int rows;
	private int cols;
	private boolean duplex;
	
	/**
	 * Creates a new PEFGenerator with the default settings.
	 */
	public PEFGenerator() {
		this(new HashMap<String, String>());
	}
	
	/**
	 * Creates a new PEF generator with the supplied optional settings. See the
	 * enums of this class for a list of possible keys and their values.
	 * @param p a map containing optional settings
	 */
	public PEFGenerator(Map<String, String> p) {
		volumes = Integer.parseInt(get(KEY_VOLUMES, p));
		pagesPerVolume = Integer.parseInt(get(KEY_PPV, p));
		eightDot = Boolean.parseBoolean(get(KEY_EIGHT_DOT, p));
		rows = Integer.parseInt(get(KEY_ROWS, p));
		cols = Integer.parseInt(get(KEY_COLS, p));
		duplex = Boolean.parseBoolean(get(KEY_DUPLEX, p));
		if (volumes<1) {
			throw new IllegalArgumentException("Volumes must be larger than 0");
		}
		if (pagesPerVolume<1) {
			throw new IllegalArgumentException("Pages per volume must be larger than 0");
		}
		if (rows<1) {
			throw new IllegalArgumentException("Rows must be larger than 0");
		}
		if (cols<1) {
			throw new IllegalArgumentException("Cols must be larger than 0");
		}
	}
	
	/**
	 * Gets a list of all keys which has default values 
	 * @return returns a list of keys
	 */
	public static Set<String> getOptionalArgumentKeys() {
		return defaults.keySet();
	}
	
	/**
	 * Gets the default value for a specified key.
	 * @param key The key to get the default value for
	 * @return returns the value for the key, or null if the key is not found
	 */
	public static String getDefaultValue(String key) {
		return defaults.get(key);
	}

	String get(String key, Map<String, String> options) {
		return checkEmpty(options.get(key), defaults.get(key));
	}
	
	String checkEmpty(String input, String def) {
		if (input==null || "".equals(input)) {
			return def;
		}
		return input;
	}

	/**
	 * Generates a new PEF-file and writes it to the supplied path 
	 * @param output the output file
	 * @throws FileNotFoundException If the given file object does not denote an existing, 
	 * writable regular file and a new regular file of that name cannot be created, or if
	 * some other error occurs while opening or creating the file
	 */
	public void generateTestBook(File output) throws FileNotFoundException {
		PrintWriter pw;
		try {
			pw = new PrintWriter(output, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Should never happen for UTF-8
			throw new RuntimeException("Unexpected error.");
		}
		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw.println("<pef xmlns=\"http://www.daisy.org/ns/2008/pef\" version=\"2008-1\">");
		pw.println("\t<head>");
		pw.println("\t\t<meta xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		pw.println("\t\t\t<dc:identifier>"+Integer.toHexString((int)(Math.random()*1000000))+" "+sdf.format((new Date())) + "</dc:identifier>");
		pw.println("\t\t\t<dc:format>application/x-pef+xml</dc:format>");
		pw.println("\t\t\t<dc:title>Generated document</dc:title>");
		pw.println("\t\t\t<dc:creator>PEF Generator</dc:creator>");
		pw.println("\t\t\t<dc:description>Document generated for test purposes containing random characters.</dc:description>");
		pw.println("\t\t</meta>");
		pw.println("\t</head>");
		pw.println("\t<body>");
		int cpr = 0;
		int rpp = 0;
		int range = (eightDot?256:64);
		int rowgap = (eightDot?1:0);
		for (int v=0; v<volumes; v++) {
			pw.println("\t\t<volume cols=\""+cols+"\" rows=\""+(rows + (int)Math.ceil((rows*rowgap)/4d))+"\" rowgap=\""+rowgap+"\" duplex=\""+duplex+"\">");
			pw.println("\t\t\t<section>");
			for (int ppv=0; ppv<pagesPerVolume; ppv++) {
				pw.println("\t\t\t\t<page>");
				rpp = (int)(Math.floor(Math.random()*(rows+1)));
				for (int r=0; r<rpp; r++) {
					pw.print("\t\t\t\t\t<row>");
					cpr = (int)(Math.floor(Math.random()*(cols+1)));
					for (int c=0; c<cpr; c++) {
						pw.print((char)(0x2800+(int)Math.floor(Math.random()*(range))));
					}
					pw.println("</row>");
				}
				pw.println("\t\t\t\t</page>");
			}
			pw.println("\t\t\t</section>");
			pw.println("\t\t</volume>");
		}
		pw.println("\t</body>");
		pw.print("</pef>");
		pw.close();
	}

        /**
	 * Generates a PEF-file with a few test pages to check the embosser setup
         * (the arguments KEY_VOLUMES and KEY_PPV are ignored)
	 * @param output the output file
	 * @throws FileNotFoundException If the given file object does not denote an existing,
	 * writable regular file and a new regular file of that name cannot be created, or if
	 * some other error occurs while opening or creating the file
	 */
        public void generateTestPages(File output) throws FileNotFoundException,
                                                          Exception {
            PrintWriter pw;
            try {
                pw = new PrintWriter(output, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // Should never happen for UTF-8
                throw new RuntimeException("Unexpected error.");
            }
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<pef xmlns=\"http://www.daisy.org/ns/2008/pef\" version=\"2008-1\">");
            pw.println("\t<head>");
            pw.println("\t\t<meta xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            pw.println("\t\t\t<dc:identifier>"+Integer.toHexString((int)(Math.random()*1000000))+" "+sdf.format((new Date())) + "</dc:identifier>");
            pw.println("\t\t\t<dc:format>application/x-pef+xml</dc:format>");
            pw.println("\t\t\t<dc:title>Test document</dc:title>");
            pw.println("\t\t\t<dc:creator>PEF Generator</dc:creator>");
            pw.println("\t\t\t<dc:description>Document generated for testing the embosser setup.</dc:description>");
            pw.println("\t\t</meta>");
            pw.println("\t</head>");
            pw.println("\t<body>");
            int rowgap = (eightDot?1:0);
            pw.println("\t\t<volume cols=\""+cols+"\" rows=\""+(rows + (int)Math.ceil((rows*rowgap)/4d))+"\" rowgap=\""+rowgap+"\" duplex=\""+duplex+"\">");
            pw.println("\t\t\t<section>");

            List<String> chart = new ArrayList<String>();
            char c = (char)0x2800;
            for (int i=0; i<8; i++) {
                StringBuffer sb = new StringBuffer();
                for (int j=0; j<7; j++) {
                    sb.append(c++);
                    sb.append('\u2800');
                }
                sb.append(c++);
                chart.add(sb.toString());
            }

            pw.println("\t\t\t\t<page>");
            for (String row : centerAndBorder(chart)) {
                pw.print("\t\t\t\t\t<row>");
                pw.print(row);
                pw.println("</row>");
            }
            pw.println("\t\t\t\t</page>");

            List<String> butterfly = new ArrayList<String>();
            butterfly.add("\u280F\u2809\u2809\u2809\u2809\u2809\u2809\u2829\u2809\u283F\u2809\u280D\u2809\u2809\u2809\u2809\u2809\u2809\u2839");
            butterfly.add("\u2807\u2800\u2800\u2800\u2800\u2800\u2800\u2800\u2821\u2800\u280C\u2800\u2800\u2800\u2800\u2800\u2800\u2800\u2838");
            butterfly.add("\u2807\u2800\u2800\u2816\u280A\u2831\u2800\u2800\u2830\u2836\u2806\u2800\u2800\u280E\u2811\u2832\u2800\u2800\u2838");
            butterfly.add("\u2807\u2833\u2800\u282B\u2800\u2808\u2826\u283E\u283F\u283F\u283F\u2837\u2834\u2801\u2800\u281D\u2800\u281E\u2838");
            butterfly.add("\u2817\u2812\u2810\u282A\u2805\u2800\u2800\u282D\u283D\u283F\u282F\u282D\u2800\u2800\u2828\u2815\u2802\u2812\u283A");
            butterfly.add("\u2807\u281E\u2800\u282E\u2800\u2820\u280B\u283B\u283F\u283F\u283F\u281F\u2819\u2804\u2800\u2835\u2800\u2833\u2838");
            butterfly.add("\u2807\u2800\u2800\u2813\u2822\u281C\u2800\u2800\u2818\u281B\u2803\u2800\u2800\u2823\u2814\u281A\u2800\u2800\u2838");
            butterfly.add("\u2807\u2800\u2800\u2800\u2800\u2800\u2800\u2800\u280C\u2800\u2821\u2800\u2800\u2800\u2800\u2800\u2800\u2800\u2838");
            butterfly.add("\u2827\u2824\u2824\u2824\u2824\u2824\u2824\u282C\u2824\u283F\u2824\u2825\u2824\u2824\u2824\u2824\u2824\u2824\u283C");

            pw.println("\t\t\t\t<page>");
            for (String row : centerAndBorder(butterfly)) {
                pw.print("\t\t\t\t\t<row>");
                pw.print(row);
                pw.println("</row>");
            }
            pw.println("\t\t\t\t</page>");

            pw.println("\t\t\t</section>");
            pw.println("\t\t</volume>");
            pw.println("\t</body>");
            pw.print("</pef>");
            pw.close();
        }

        private List<String> centerAndBorder(List<String> content) throws Exception {

            int height = content.size();
            int width = 0;
            for (String row : content) { width = Math.max(width, row.length()); }

            if (rows < height + 4 || cols < width + 4) {
                throw new Exception("Paper too small");
            }

            int marginTop = (int)Math.floor((rows-2-height)/2d);
            int marginLeft = (int)Math.floor((cols-2-width)/2d);

            char space = '\u2800';
            char borderLeft = '\u2807';
            char borderRight = '\u2838';
            char borderTop = '\u2809';
            char borderBottom = '\u2824';
            char cornerTopLeft = '\u280F';
            char cornerTopRight = '\u2839';
            char cornerBottomRight = '\u283C';
            char cornerBottomLeft = '\u2827';

            List<String> page = new ArrayList<String>();
            StringBuilder sb = new StringBuilder();

            sb.append(cornerTopLeft);
            for (int i=0; i<cols-2; i++) { sb.append(borderTop); }
            sb.append(cornerTopRight);
            page.add(sb.toString());
            sb.setLength(0);
            for (int i=0; i<marginTop; i++) {
                sb.append(borderLeft);
                for (int j=0; j<cols-2; j++) { sb.append(space); }
                sb.append(borderRight);
                page.add(sb.toString());
                sb.setLength(0);
            }
            for (String row : content) {
                sb.append(borderLeft);
                for (int j=0; j<marginLeft; j++) { sb.append(space); }
                sb.append(row);
                for (int j=0; j<cols-row.length()-marginLeft-2; j++) { sb.append(space); }
                sb.append(borderRight);
                page.add(sb.toString());
                sb.setLength(0);
            }
            for (int i=0; i<rows-height-marginTop-2; i++) {
                sb.append(borderLeft);
                for (int j=0; j<cols-2; j++) { sb.append(space); }
                sb.append(borderRight);
                page.add(sb.toString());
                sb.setLength(0);
            }
            sb.append(cornerBottomLeft);
            for (int i=0; i<cols-2; i++) { sb.append(borderBottom); }
            sb.append(cornerBottomRight);
            page.add(sb.toString());

            return page;
        }
}
