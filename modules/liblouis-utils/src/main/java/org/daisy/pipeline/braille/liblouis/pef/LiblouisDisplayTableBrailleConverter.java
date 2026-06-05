package org.daisy.pipeline.braille.liblouis.pef;

import java.util.HashMap;
import java.util.Map;
import java.nio.charset.Charset;

import org.daisy.dotify.api.table.BrailleConverter;

import org.liblouis.DisplayTable;

public class LiblouisDisplayTableBrailleConverter implements BrailleConverter {
	
	private final Map<Character,Character> b2t = new HashMap<Character,Character>();
	private final Map<Character,Character> t2b = new HashMap<Character,Character>();
	
	private final DisplayTable table;

	public LiblouisDisplayTableBrailleConverter(DisplayTable table) {
		this.table = table;
		try {
			char[] brailleRange = new char[256];
			int i = 0;
			for (; i < 256; i++)
				brailleRange[i] = (char)(0x2800+i);
			char[] tableDef = table.encode(String.valueOf(brailleRange)).toCharArray();
			for (i = 255; i >= 0; i--) {
				t2b.put(tableDef[i], brailleRange[i]);
				b2t.put(brailleRange[i], tableDef[i]); }}
		catch (Throwable e) {
			throw new RuntimeException(e); }
	}
	
	/**
	 * @return Unicode braille string
	 */
	public String toBraille(String text) {
		StringBuffer buf = new StringBuffer();
		Character b;
		for (char t : text.toCharArray()) {
			b = t2b.get(t);
			if (b == null) {
				// character might map to a virtual dot pattern
				// DisplayTable.decode() will return the base pattern without virtual dots
				b = table.decode(t);
				// assume that blank means the table does not contain the character
				if (b != '\u2800')
					t2b.put(t, b);
				else
					throw new IllegalArgumentException("Character '" + t + "' (0x" + Integer.toHexString((int)(t)) + ") not found.");
			}
			buf.append(b); }
		return buf.toString();
	}
	
	/**
	 * @param braille Unicode braille string
	 */
	public String toText(String braille) {
		StringBuffer buf = new StringBuffer();
		Character t;
		for (char b : braille.toCharArray()) {
			t = b2t.get(b);
			if (t == null)
				throw new IllegalArgumentException("Braille pattern '" + b + "' (0x" + Integer.toHexString((int)(b)) + ") not found.");
			buf.append(t); }
		return buf.toString();
	}
	
	private static final Charset charset = Charset.forName("ISO-8859-1");
	
	public Charset getPreferredCharset() {
		return charset;
	}
	
	public boolean supportsEightDot() {
		return true;
	}

	@Override
	public String toString() {
		return table.toString();
	}
}
