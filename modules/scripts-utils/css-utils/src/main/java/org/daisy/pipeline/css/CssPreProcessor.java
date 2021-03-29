package org.daisy.pipeline.css;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.net.URL;

public interface CssPreProcessor {

	public boolean supportsMediaType(String mediaType, URL url);

	public InputStream compile(InputStream sass, URL base, Charset encoding) throws IOException;

}
