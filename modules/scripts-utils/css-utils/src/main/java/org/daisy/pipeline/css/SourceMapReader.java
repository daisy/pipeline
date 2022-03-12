package org.daisy.pipeline.css;

import java.net.URI;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import cz.vutbr.web.css.SourceLocator;
import cz.vutbr.web.csskit.antlr.SourceMap;

import mjson.Json;
import mjson.Json.MalformedJsonException;

import org.daisy.common.file.URLs;

/**
 * Reads a source map from a JSON object and returns it as a {@link SourceMap} object.
 */
public final class SourceMapReader {

	private SourceMapReader() {}

	public static SourceMap read(String json, URI base) {
		NavigableMap<LineColumn,SourceLocator> sourceMap; {
			try {
				Json jsonObject = Json.read(json);
				List<URL> sources; {
					Json jsonArray = jsonObject.at("sources");
					if (jsonArray == null)
						throw new RuntimeException("Missing 'sources'");
					if (!jsonArray.isArray())
						throw new RuntimeException("Expected array");
					sources = new ArrayList<>();
					for (Json s : jsonArray.asJsonList()) {
						if (!s.isString())
							throw new RuntimeException("Expected string");
						// the source can be either
						if (s.asString().matches("^\\w+:"))
							// an absolute URL (with encoded path)
							sources.add(URLs.asURL(URLs.resolve(base, URLs.asURI(s.asString()))));
						else
							// or a (unencoded) file path (relative to the current working directory)
							try {
								sources.add(URLs.asURL(URLs.resolve(base, new URI(null, null, s.asString(), null)))); }
							catch (java.net.URISyntaxException e) {
								throw new RuntimeException(e); } // should not happen
					}
				}
				CharacterIterator mappings; {
					Json jsonString = jsonObject.at("mappings");
					if (jsonString == null)
						throw new RuntimeException("Missing 'mappings'");
					if (!jsonString.isString())
						throw new RuntimeException("Expected string");
					mappings = new StringCharacterIterator(jsonString.asString());
				}
				sourceMap = new TreeMap<>();
				int line = 0;
				int column = 0;
				int source = 0;
				int sourceLine = 0;
				int sourceColumn = 0;
				char c = mappings.first();
				while (c != CharacterIterator.DONE) {
					if (c != ',' && c != ';') {
						// first value is the column within the generated file
						column += Base64VlqDecoder.readInteger(mappings);
						c = mappings.next();
						if (c != CharacterIterator.DONE && c != ',' && c != ';') {
							// second value is the original file
							source += Base64VlqDecoder.readInteger(mappings);
							if (source < 0)
								throw new RuntimeException("Negative index");
							if (source >= sources.size())
								throw new RuntimeException("Index exceeds size of sources array");
							c = mappings.next();
							if (c == CharacterIterator.DONE || c == ',' || c == ';')
								throw new RuntimeException("Segment must have of 1, 4 or 5 values");
							// third value is the line withing the original file
							sourceLine += Base64VlqDecoder.readInteger(mappings);
							c = mappings.next();
							if (c == CharacterIterator.DONE || c == ',' || c == ';')
								throw new RuntimeException("Segment must have of 1, 4 or 5 values");
							// fourth value is the column withing the original file
							sourceColumn += Base64VlqDecoder.readInteger(mappings);
							c = mappings.next();
							// ignore fifth value if present
							if (c != CharacterIterator.DONE && c != ',' && c != ';')
								c = mappings.next();
							if (c != CharacterIterator.DONE && c != ',' && c != ';')
								throw new RuntimeException("Segment must have of 1, 4 or 5 values");
							LineColumn pos = new LineColumn(line, column);
							if (!sourceMap.containsKey(pos))
								sourceMap.put(pos, new SourceLineColumn(sources.get(source), sourceLine, sourceColumn));
						}
					}
					if (c == ',') {
						c = mappings.next();
						continue;
					}
					if (c == ';') {
						line++;
						column = 0;
						c = mappings.next();
						continue;
					}
					break;
				}
			} catch (MalformedJsonException e) {
				throw new IllegalArgumentException("Source map could not be parsed: " + json, e);
			} catch (RuntimeException e) {
				throw new IllegalArgumentException("Source map could not be parsed: " + json, e);
			}
		}
		return new SourceMap() {
			public SourceLocator get(int line, int column) {
				return sourceMap.get(new LineColumn(line, column));
			}
			public SourceLocator floor(int line, int column) {
				Map.Entry<LineColumn,SourceLocator> entry = sourceMap.floorEntry(new LineColumn(line, column));
				return entry == null ? null : entry.getValue();
			}
			public SourceLocator ceiling(int line, int column) {
				Map.Entry<LineColumn,SourceLocator> entry = sourceMap.ceilingEntry(new LineColumn(line, column));
				return entry == null ? null : entry.getValue();
			}
			@Override
			public String toString() {
				return sourceMap.toString();
			}
		};
	}

	private static class LineColumn implements Comparable<LineColumn> {
		private final int line;
		private final int column;
		public LineColumn(int line, int column) {
			this.line = line;
			this.column = column;
		}
		public int compareTo(LineColumn that) {
			if (this.line < that.line)
				return -1;
			else if (this.line > that.line)
				return 1;
			else if (this.column < that.column)
				return -1;
			else if (this.column > that.column)
				return 1;
			else
				return 0;
		}
		public String toString() {
			return line + ":" + column;
		}
	}

	private static class SourceLineColumn implements SourceLocator {
		private final URL url;
		private final int line;
		private final int column;
		public SourceLineColumn(URL url, int line, int column) {
			this.url = url;
			this.line = line;
			this.column = column;
		}
		public URL getURL() {
			return url;
		}
		public int getLineNumber() {
			return line;
		}
		public int getColumnNumber() {
			return column;
		}
		public String toString() {
			return url + ":" + line + ":" + column;
		}
	}

	private static class Base64VlqDecoder {

		private static final String base64Decoder = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

		private static int readInteger(CharacterIterator base64String) throws IllegalArgumentException {
			char c = base64String.current();
			if (c == CharacterIterator.DONE)
				throw new RuntimeException("End of string");
			int result = 0;
			int shift = 0;
			while (true) {
				int i = base64Decoder.indexOf(c);
				if (i < 0)
					throw new RuntimeException("Unexpected base64 character: " + c);
				if (i < 32) {
					result += (i << shift);
					break;
				} else {
					i &= 31;
					result += (i << shift);
					shift += 5;
					c = base64String.next();
					if (c == CharacterIterator.DONE)
						throw new RuntimeException("Expected character after continuation character");
				}
			}
			if (result % 2 == 0)
				return result >> 1;
			else
				return - (result >> 1);
		}
	}
}
