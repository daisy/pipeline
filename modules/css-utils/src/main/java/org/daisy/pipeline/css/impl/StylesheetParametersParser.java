package org.daisy.pipeline.css.impl;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Iterables;

import org.daisy.pipeline.css.sass.impl.SassMapParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.unbescape.css.CssEscape;

public abstract class StylesheetParametersParser {

	private static final Logger logger = LoggerFactory.getLogger(StylesheetParametersParser.class);
	private static final Pattern regex = Pattern.compile(
		(SassMapParser.asRegex().pattern() + "|" + Query.QUERY.pattern()).replaceAll("\\(\\?<[^>]+>", "(?:"));

	private StylesheetParametersParser() {}

	public static Map<String,Object> parse(String expression) throws IllegalArgumentException {
		Map<String,Object> map = null;
		try {
			map = SassMapParser.parse(expression);
		} catch (IllegalArgumentException e) {
			Query query = null; {
				try {
					query = Query.parse(expression);
					logger.warn(
						"Query syntax is deprecated for stylesheet-parameters. Please use Sass map syntax.");
				} catch (IllegalArgumentException ee) {
				}
			}
			if (query != null) {
				map = new HashMap<>();
				for (Query.Feature f : query)
					map.put(f.getKey(), f.getValue().orElse("true"));
			}
		}
		if (map == null)
			throw new IllegalArgumentException("Could not parse stylesheet parameter set: " + expression);
		return map;
	}

	public static Pattern asRegex() {
		return regex;
	}

	/*
	 * The code below is copied from the Query class in braille-common. The class is not referenced
	 * because css-utils is a dependency of braille-common. For now we chose to copy rather than to
	 * move the class because the query syntax was introduced as a way to select braille
	 * transformers. Later we started using it to parse "stylesheet-parameter" options also, but
	 * only because it was convenient. Now we have a different syntax for stylesheet parameters,
	 * namely the Sass map syntax. For backward compatibility the query syntax is also still supported.
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
