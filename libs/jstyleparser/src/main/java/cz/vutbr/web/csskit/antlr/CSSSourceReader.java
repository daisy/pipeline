package cz.vutbr.web.csskit.antlr;

import java.io.IOException;
import java.io.InputStream;
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

		public CSSInputStream(InputStream inputStream, Charset encoding) {
			this.stream = inputStream;
			this.encoding = encoding;
		}
	}

	/**
	 * Whether this reader supports sources of the given media type.
	 */
	public boolean supportsMediaType(String mediaType);

	/**
	 * Read the source.
	 */
	public CSSInputStream read(CSSSource source) throws IOException;

}
