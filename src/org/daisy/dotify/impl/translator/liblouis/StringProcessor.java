package org.daisy.dotify.impl.translator.liblouis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StringProcessor {
	private final static Pattern b = Pattern.compile("\\\\{1}([\\\\fnrstve]{1}|x{1}[0-9A-Fa-f]{4})");
	private StringProcessor() {
		//hide constructor
	}
	static String unescape(String input) {
		Matcher m = b.matcher(input);
		StringBuilder ret = new StringBuilder();

		int index = 0;
		while (m.find()) {
			if (m.start()>index) {
				// false
				ret.append(input.substring(index, m.start()));
			}
			// true
			
			String t = input.substring(m.start(), m.end());
			if (t.startsWith("\\x")) {
				ret.append((char)Integer.parseInt(t.substring(2), 16));
			} else if (t.equals("\\\\")) {
				ret.append("\\");
			} else if (t.equals("\\f")) {
				ret.append("\f");
			} else if (t.equals("\\n")) {
					ret.append("\n");
			} else if (t.equals("\\r")) {
				ret.append("\r");
			} else if (t.equals("\\s")) {
				ret.append(" ");
			} else if (t.equals("\\t")) {
				ret.append("\t");
			} else if (t.equals("\\v")) {
				ret.append("\0x0B");
			} else if (t.equals("\\e")) {
				ret.append("\0x1B");
			}
			
			else {
				ret.append(t);
			}
			index = m.end();
		}
		if (index==0) {
			return input;
		}
		// add remaining segment
		if (index<input.length()) {
			// false
			ret.append(input.substring(index, input.length()));
		}

		return ret.toString();
	}

}
