package cz.vutbr.web.csskit.antlr;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Object that is able to obtain an input stream from a CSSSource.
 *
 * @author bertfrees
 */
public interface CSSSourceReader {

	public static final class CSSInputStream {

		/**
		 * The stream
		 */
		public final InputStream stream;

		/**
		 * The encoding, or null if not known.
		 */
		public final Charset encoding;

		/**
		 * Base URL for resolving relative URLs against if <code>sourceMap</code> is
		 * <code>null</code>, if there is no mapping for a certain token, or when the base URL is
		 * needed in contexts where there is no current token.
		 */
		public final URL base;

		/**
		 * Maps locations within this {@link CSSInputStream} to locations within the original
		 * sources from which the {@link CSSInputStream} was read or compiled.
		 */
		public final SourceMap sourceMap;

		public CSSInputStream(InputStream inputStream, Charset encoding, URL base, SourceMap sourceMap) {
			this.stream = inputStream;
			this.encoding = encoding;
			this.base = base;
			this.sourceMap = sourceMap;
		}
	}

	/**
	 * Whether this reader supports sources of the given media type.
	 *
	 * @param mediaType The media type.
	 * @param url If the source is of type {@link CSSSource.SourceType.URL}, the URL is provided in
	 *            case <code>mediaType</code> is null and the media type needs to be inferred.
	 */
	public boolean supportsMediaType(String mediaType, URL url);

	/**
	 * Read the source.
	 */
	public CSSInputStream read(CSSSource source) throws IOException;

}
