package cz.vutbr.web.csskit.antlr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import cz.vutbr.web.css.NetworkProcessor;
import cz.vutbr.web.csskit.DefaultNetworkProcessor;

/**
 * CSSSourceReader driven by a NetworkProcessor that can handle media type text/css.
 *
 * @author bertfrees
 */
public class DefaultCSSSourceReader implements CSSSourceReader {

	private final NetworkProcessor network;

	public DefaultCSSSourceReader() {
		this(new DefaultNetworkProcessor());
	}

	public DefaultCSSSourceReader(NetworkProcessor network) {
		this.network = network;
	}

	/**
	 * Returns true when mediaType is "text/css" or null, unless the URL does not end with ".css".
	 */
	public boolean supportsMediaType(String mediaType, URL url) {
		return "text/css".equals(mediaType)
			|| (mediaType == null && (url == null || url.toString().endsWith(".css")));
	}

	public CSSInputStream read(CSSSource source) throws IOException {
		switch (source.type) {
		case INLINE:
		case EMBEDDED:
			if (!supportsMediaType(source.mediaType, null))
				throw new IllegalArgumentException();
			return new CSSInputStream(
				new ByteArrayInputStream(((String)source.source).getBytes()),
				source.encoding,
				source.base);
		case URL:
			URL url = source.base;
			if (!supportsMediaType(source.mediaType, url))
				throw new IllegalArgumentException();
			return new CSSInputStream(
				network.fetch(url),
				source.encoding,
				url);
		default:
			throw new RuntimeException("coding error");
		}
	}
}
