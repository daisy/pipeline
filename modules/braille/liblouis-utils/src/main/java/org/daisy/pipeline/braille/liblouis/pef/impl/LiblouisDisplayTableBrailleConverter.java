package org.daisy.pipeline.braille.liblouis.pef.impl;

import java.util.HashMap;
import java.util.Map;
import java.nio.charset.Charset;

import org.daisy.dotify.api.table.BrailleConverter;

import org.liblouis.Translator;

public class LiblouisDisplayTableBrailleConverter implements BrailleConverter {
	
	private static final Map<Character,Character> b2t = new HashMap<Character,Character>();
	private static final Map<Character,Character> t2b = new HashMap<Character,Character>();
	
	public LiblouisDisplayTableBrailleConverter(Translator translator) {
		try {
			char[] brailleRange = new char[256];
			int i = 0;
			for (; i < 256; i++)
				brailleRange[i] = (char)(0x2800+i);
			char[] tableDef = translator.display(String.valueOf(brailleRange)).toCharArray();
			for (i = 255; i >= 0; i--) {
				t2b.put(tableDef[i], brailleRange[i]);
				b2t.put(brailleRange[i], tableDef[i]); }}
		catch (Throwable e) {
			throw new RuntimeException(e); }
	}
	
	public String toBraille(String text) {
		StringBuffer buf = new StringBuffer();
		Character b;
		for (char t : text.toCharArray()) {
			b = t2b.get(t);
			if (b == null)
				throw new IllegalArgumentException("Character '" + t + "' (0x" + Integer.toHexString((int)(t)) + ") not found.");
			buf.append(b); }
		return buf.toString();
	}
	
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
}
