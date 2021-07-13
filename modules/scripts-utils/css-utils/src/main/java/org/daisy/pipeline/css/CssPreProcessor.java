package org.daisy.pipeline.css;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.net.URI;
import java.net.URL;

public interface CssPreProcessor {

	public boolean supportsMediaType(String mediaType, URL url);

	public PreProcessingResult compile(InputStream sass, URL base, Charset encoding) throws IOException;

	public static final class PreProcessingResult {
		/**
		 * The CSS stream.
		 */
		public final InputStream stream;
		/**
		 * The source map (JSON).
		 */
		public final String sourceMap;
		/**
		 * The base for resolving relative URIs in the CSS stream and source map.
		 */
		public final URI base;
		public PreProcessingResult(InputStream stream, String sourceMap, URI base) {
			this.stream = stream;
			this.sourceMap = sourceMap;
			this.base = base;
		}
	}
}
