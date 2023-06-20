package org.daisy.pipeline.css;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

public interface CssPreProcessor {

	public boolean supportsMediaType(String mediaType, URL url);

	public PreProcessingResult compile(PreProcessingSource source) throws IOException;

	public static class PreProcessingSource {

		/**
		 * The character stream.
		 */
		public final Reader stream;

		/**
		 * The base.
		 */
		public final URI base;

		/**
		 * Read the source again using a different character encoding. Closes {@code stream}.
		 *
		 * @throws IOException if the character encoding can not be changed or if the source can not
		 *                     be read a second time.
		 */
		public Reader reread(Charset encoding) throws IOException {
			throw new IOException();
		}

		public PreProcessingSource(Reader stream, URI base) {
			this.stream = stream;
			this.base = base;
		}
	}

	public static final class PreProcessingResult {

		/**
		 * The CSS stream.
		 */
		public final Reader stream;

		/**
		 * The source map (JSON).
		 */
		public final String sourceMap;

		/**
		 * The base for resolving relative URIs in the CSS stream and source map.
		 */
		public final URI base;

		public PreProcessingResult(Reader stream, String sourceMap, URI base) {
			this.stream = stream;
			this.sourceMap = sourceMap;
			this.base = base;
		}
	}
}
