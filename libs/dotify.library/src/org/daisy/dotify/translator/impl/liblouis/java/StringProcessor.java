package org.daisy.dotify.translator.impl.liblouis.java;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StringProcessor {
    private static final Pattern b = Pattern.compile("\\\\{1}([\\\\fnrstve]{1}|x{1}[0-9A-Fa-f]{4})");

    private StringProcessor() {
        //hide constructor
    }

    static String unescape(String input) {
        Matcher m = b.matcher(input);
        StringBuilder ret = new StringBuilder();

        int index = 0;
        while (m.find()) {
            if (m.start() > index) {
                // false
                ret.append(input.substring(index, m.start()));
            }
            // true

            String t = input.substring(m.start(), m.end());
            if (t.startsWith("\\x")) {
                ret.append((char) Integer.parseInt(t.substring(2), 16));
            } else if ("\\\\".equals(t)) {
                ret.append('\\');
            } else if ("\\f".equals(t)) {
                ret.append('\f');
            } else if ("\\n".equals(t)) {
                ret.append('\n');
            } else if ("\\r".equals(t)) {
                ret.append('\r');
            } else if ("\\s".equals(t)) {
                ret.append(' ');
            } else if ("\\t".equals(t)) {
                ret.append('\t');
            } else if ("\\v".equals(t)) {
                ret.append("\0x0B");
            } else if ("\\e".equals(t)) {
                ret.append("\0x1B");
            } else {
                ret.append(t);
            }
            index = m.end();
        }
        if (index == 0) {
            return input;
        }
        // add remaining segment
        if (index < input.length()) {
            // false
            ret.append(input.substring(index, input.length()));
        }

        return ret.toString();
    }

}
