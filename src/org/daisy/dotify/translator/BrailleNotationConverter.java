package org.daisy.dotify.translator;

/**
 * Converts braille notation into Unicode braille patterns.
 *  
 * @author Joel Håkansson
 */
public class BrailleNotationConverter {
	private final String separator;
	
	/**
	 * Creates a new converter with the specified braille cell separator.
	 * @param separator
	 */
	public BrailleNotationConverter(String separator) {
		this.separator = separator;
	}
	
	public String parseBrailleNotation(String p) {
		String[] s = p.split(separator);
		if (s.length == 0) {
			throw new IllegalArgumentException("Illegal sequence");
		}
		StringBuilder sb = new StringBuilder();
		for (String t : s) {
			if (!t.equals("")) {
				sb.append(numberStringToUnicode(t));
			}
		}
		return sb.toString();
	}
	
	private static char numberStringToUnicode(String p) {
		int v = 0;
		int prv = 0;
		char prvC = (char) 0;
		for (char c : p.toCharArray()) {
			if (prvC > c) {
				throw new IllegalArgumentException("Illegal format");
			} else {
				prvC = c;
			}
			switch (c) {
				case '0': v |= 0x2800; break;
				case '1': v |= 0x2801; break;
				case '2': v |= 0x2802; break;
				case '3': v |= 0x2804; break;
				case '4': v |= 0x2808; break;
				case '5': v |= 0x2810; break;
				case '6': v |= 0x2820; break;
				case '7': v |= 0x2840; break;
				case '8': v |= 0x2880; break;
				default:
					throw new IllegalArgumentException("Illegal character: " + c);
			}
			if (v == prv) {
				throw new IllegalArgumentException("Illegal format");
			} else {
				prv = v;
			}
		}
		return (char)v;
	}

}
