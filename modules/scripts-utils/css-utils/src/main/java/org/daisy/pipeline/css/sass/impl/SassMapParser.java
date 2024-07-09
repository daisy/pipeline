package org.daisy.pipeline.css.sass.impl;

import java.util.HashMap;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unbescape.css.CssEscape;

/**
 * <a href="https://sass-lang.com/documentation/values/maps/">Sass map</a> parser.
 */
public abstract class SassMapParser {

	private static final String IDENT_RE = "[_a-zA-Z][_a-zA-Z0-9-]*";
	private static final String STRING_RE = "'[^']*'|\"[^\"]*\"";
	private static final String INTEGER_RE = "0|-?[1-9][0-9]*";
	private static final Pattern VALUE_RE = Pattern.compile(
		"(?<ident>" + IDENT_RE + ")|(?<string>" + STRING_RE + ")|(?<integer>" + INTEGER_RE + ")"
	);
	private static final Pattern KEY_VALUE_RE = Pattern.compile(
		"(?<key>" + IDENT_RE + ")\\s*:\\s*(?<value>" + VALUE_RE.pattern() + ")"
	);
	private static final Pattern MAP_RE = Pattern.compile(
		("\\s*\\(\\s*(?:" + KEY_VALUE_RE.pattern() + "\\s*(?:,\\s*" + KEY_VALUE_RE.pattern()  + "\\s*)*)?\\)\\s*")
		.replaceAll("\\(\\?<[^>]+>", "(?:")
	);

	private SassMapParser() {}

	public static Map<String,Object> parse(String expression) throws IllegalArgumentException {
		if (!MAP_RE.matcher(expression).matches())
			throw new IllegalArgumentException("Could not parse Sass map: " + expression);
		Map<String,Object> map = new HashMap<>();
		Matcher m = KEY_VALUE_RE.matcher(expression);
		while (m.find()) {
			String key = m.group("key");
			String value = m.group("value");
			boolean isString = false;
			Matcher m2 = VALUE_RE.matcher(value);
			if (!m2.matches())
				throw new RuntimeException("Coding error");
			String ident = m2.group("ident");
			String string = m2.group("string");
			String integer = m2.group("integer");
			if (ident != null) {
				if ("true".equals(ident))
					map.put(key, Boolean.TRUE);
				else if ("false".equals(ident))
					map.put(key, Boolean.FALSE);
				else
					map.put(key, ident);;
			} else if (string != null && !string.equals(""))
				map.put(key, CssEscape.unescapeCss(string.substring(1, string.length() - 1)));
			else if (integer != null && !integer.equals(""))
				map.put(key, Integer.parseInt(integer));
			else
				throw new RuntimeException("Coding error");
		}
		return map;
	}

	public static Pattern asRegex() {
		return MAP_RE;
	}
}
