package org.daisy.dotify.impl.translator.liblouis;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Resolves resources using the class loader for this class.
 * @author Joel Håkansson
 *
 */
class ClassLoaderResourceResolver implements ResourceResolver {
	private final Charset encoding;
	private final String basepath;
	
	public ClassLoaderResourceResolver(Charset encoding) {
		this(null, encoding);
	}

	public ClassLoaderResourceResolver(String basepath, Charset encoding) {
		this.basepath = basepath;
		this.encoding = encoding;
	}

	@Override
	public ResourceDescriptor resolve(String subpath) {
		InputStream is = this.getClass().getResourceAsStream((basepath!=null?basepath:"")+subpath);
		if (is!=null) {
			return new ResourceDescriptor(is, encoding);
		} else {
			return null;
		}
	}

}
