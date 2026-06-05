package org.daisy.pipeline.css.impl;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.vutbr.web.css.MediaQuery;

import com.google.common.collect.Iterables;

import org.daisy.pipeline.css.MediaQueryParser;
import org.daisy.pipeline.css.Medium;
import org.daisy.pipeline.css.MediumProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.unbescape.css.CssEscape;

/**
 * This class is similar to {@link Medium.MediumBuilder}, but uses the {@link MediumProvider} API to
 * return specialized {@link Medium}, and handles unparsed media query expressions.
 */
public class MediumParser {

	private static final Logger logger = LoggerFactory.getLogger(MediumParser.class);

	private final MediumProvider mediumProvider;

	public MediumParser(MediumProvider provider) {
		mediumProvider = provider;
	}

	private final List<Object> buffer = new ArrayList<>();
	private Map<String,Object> currentMap = null;
	private boolean closed = false;

	public MediumParser add(String expression) {
		buffer.add(expression);
		currentMap = null;
		return this;
	}

	public MediumParser add(String feature, Object value) {
		if (currentMap == null || currentMap.containsKey(feature)) {
			currentMap = new HashMap<>();
			buffer.add(currentMap);
		}
		currentMap.put(feature, value);
		return this;
	}

	public MediumParser add(Map<String,Object> features) {
		MediumParser parser = this;
		for (Map.Entry<String,Object> e : features.entrySet())
			parser = parser.add(e.getKey(), e.getValue());
		return parser;
	}

	public Medium parse() throws IllegalArgumentException, NoSuchElementException {
		if (buffer.isEmpty())
			throw new NoSuchElementException();
		MediaQuery query = null;
		for (Object o : buffer)
			if (o instanceof String) {
				String expression = (String)o;
				MediaQuery q; {
					try {
						q = MediaQueryParser.parse(expression);
					} catch (IllegalArgumentException e) {
						// query syntax may be used for output-file-format option in PEF scripts
						Query tq; {
							try {
								tq = Query.parse(expression);
								logger.warn(
									"Query syntax is deprecated for output-file-format. Please use media query syntax.");
							} catch (IllegalArgumentException ee) {
								throw new IllegalArgumentException("Invalid media query: " + expression, e);
							}
						}
						Map<String,Object> features = new HashMap<>();
						for (Query.Feature f : tq)
							features.put(f.getKey(), f.getValue().orElse("true"));
						q = MediaQueryParser.EMBOSSED.and(MediaQueryParser.parse(features));
					}
				}
				query = query != null ? query.and(q) : q;
			} else { // o instanceof Map
				Map<String,Object> features = (Map<String,Object>)o;
				MediaQuery q = MediaQueryParser.parse(features);
				query = query != null ? query.and(q) : q;
			}
		logger.info("Selecting medium for query: " + query);
		try {
			return mediumProvider.get(query).iterator().next();
		} finally {
			buffer.clear();
		}
	}

	private static Pattern regex = null;

	public static Pattern asRegex() {
		if (regex == null) {
			String TYPE_RE = "embossed|braille|speech|screen|print|all";
			// ident (https://drafts.csswg.org/css-syntax-3/#ident-token-diagram)
			String WHITESPACE_RE = "\\r\\n|[ \\n\\r\\t\\f]";
			String ESCAPE_RE = "\\\\([0-9a-fA-F]{1,6}(" + WHITESPACE_RE + ")?|[^\\n\\r\\f0-9a-fA-F])";
			String NON_ASCII_RE = "[^\\x00-\\x7F]";
			String IDENT_RE =
				"(--|-?([_a-zA-Z]|" + NON_ASCII_RE + "|" + ESCAPE_RE + "))" +
				"([-_a-zA-Z0-9]|" + NON_ASCII_RE + "|" + ESCAPE_RE + ")*";
			// integer, number, dimension or percentage
			String NUMERIC_RE = "[+-]?([0-9]*\\.)?[0-9]+([eE][+-]?[0-9]+)?([a-zA-Z]+|%)?";
			String VALUE_RE = IDENT_RE + "|" + NUMERIC_RE;
			String FEATURE_RE = "\\(\\s*(" + IDENT_RE + ")(\\s*\\:\\s*(" + VALUE_RE + "))?\\s*\\)";
			String QUERY_RE = "\\s*(" + TYPE_RE + "|" + FEATURE_RE + ")" +
				"(\\s+(AND|and)\\s+(" + TYPE_RE + "|" + FEATURE_RE + "))*\\s*";
			regex = Pattern.compile(
				(QUERY_RE + "|" + Query.QUERY.pattern()).replaceAll("\\(\\?<[^>]+>", "(?:"));
		}
		return regex;
	}

	/*
	 * The code below is copied from the Query class in braille-common. The class is not referenced
	 * because css-utils is a dependency of braille-common. For now we chose to copy rather than to
	 * move the class because the query syntax was introduced as a way to select braille
	 * transformers. Later we started using it to parse "output-file-format" options also, but only
	 * because it was convenient. Now we have a different syntax for medium specifications, namely
	 * the media query syntax. For backward compatibility the old query syntax is also still
	 * supported.
	 */
	private static class Query extends AbstractCollection<Query.Feature> {

		private static final String IDENT_RE = "[_a-zA-Z][_a-zA-Z0-9-]*";
		private static final String STRING_RE = "'[^']*'|\"[^\"]*\"";
		private static final String INTEGER_RE = "0|-?[1-9][0-9]*";
		private static final Pattern VALUE_RE = Pattern.compile(
			"(?<ident>" + IDENT_RE + ")|(?<string>" + STRING_RE + ")|(?<integer>" + INTEGER_RE + ")"
		);
		private static final Pattern FEATURE_RE = Pattern.compile(
			"\\(\\s*(?<key>" + IDENT_RE + ")(?:\\s*\\:\\s*(?<value>" + VALUE_RE.pattern() + "))?\\s*\\)"
		);
		private static final Pattern FEATURES_RE = Pattern.compile(
			"\\s*(?:" + FEATURE_RE.pattern() + "\\s*)*"
		);
		private static final Pattern QUERY = FEATURES_RE;

		/**
		 * Parse string to query
		 */
		public static Query parse(String query) throws IllegalArgumentException {
			if (FEATURES_RE.matcher(query).matches()) {
				Query q = new Query();
				Matcher m = FEATURE_RE.matcher(query);
				while (m.find()) {
					String key = m.group("key");
					String value = m.group("value");
					boolean isString = false;
					if (value != null) {
						Matcher m2 = VALUE_RE.matcher(value);
						if (!m2.matches())
							throw new RuntimeException("Coding error");
						String ident = m2.group("ident");
						String string = m2.group("string");
						String integer = m2.group("integer");
						if (ident != null)
							value = ident;
						else if (string != null && !string.equals("")) {
							value = CssEscape.unescapeCss(string.substring(1, string.length() - 1));
							isString = true; }
						else if (integer != null && !integer.equals(""))
							value = integer;
						else
							throw new RuntimeException("Coding error"); }
					q.add(new Feature(key, Optional.ofNullable(value), isString)); }
				return q; }
			throw new IllegalArgumentException("Could not parse query: " + query);
		}

		private final List<Feature> list;

		private Query() {
			this.list = new ArrayList<Feature>();
		}

		@Override
		public boolean add(Feature feature) {
			int index = 0;
			String key = feature.getKey();
			for (Feature f : list) {
				int cmp = f.getKey().compareTo(key);
				if (cmp > 0) {
					break;
				} else if (cmp > 0) {
					index++;
					break;
				}
				index++;
			}
			list.add(index, feature);
			return true;
		}

		@Override
		public int size() {
			return list.size();
		}

		@Override
		public Iterator<Feature> iterator() {
			return list.iterator();
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			for (Feature f : this)
				b.append(f);
			return b.toString();
		}

		@Override
		public int hashCode() {
			return list.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Query))
				return false;
			return Iterables.elementsEqual(this, (Query)obj);
		}

		private static class Feature {

			private final String key;
			private final Optional<String> value;
			private final Optional<String> literal;

			private Feature(String key, Optional<String> value) {
				this(key, value, false);
			}

			private Feature(String key, Optional<String> value, boolean specifiedAsString) {
				this.key = key;
				this.value = value;
				if (value.isPresent()) {
					String v = value.get();
					if (!specifiedAsString && (v.matches(IDENT_RE) || v.matches(INTEGER_RE)))
						this.literal = Optional.of(v);
					else
						this.literal = Optional.of("\"" + v.replace("\n", "\\A ").replace("\"","\\22 ") + "\"");
				} else
					this.literal = Optional.empty();
			}

			public String getKey() {
				return key;
			}

			public boolean hasValue() {
				return getValue().isPresent();
			}

			public Optional<String> getValue() {
				return value;
			}

			public Optional<String> getLiteral() {
				return literal;
			}

			@Override
			public String toString() {
				StringBuilder b = new StringBuilder();
				String k = getKey();
				if (!k.matches(IDENT_RE))
					throw new RuntimeException();
				b.append("(" + k);
				if (hasValue()) {
					b.append(":");
					b.append(getLiteral().get());
				}
				b.append(")");
				return b.toString();
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((key == null) ? 0 : key.hashCode());
				result = prime * result + ((value == null) ? 0 : value.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Feature other = (Feature)obj;
				if (key == null) {
					if (other.getKey() != null)
						return false;
				} else if (!key.equals(other.getKey()))
					return false;
				if (value == null) {
					if (other.getValue() != null)
						return false;
				} else if (!value.equals(other.getValue()))
					return false;
				return true;
			}
		}
	}
}
