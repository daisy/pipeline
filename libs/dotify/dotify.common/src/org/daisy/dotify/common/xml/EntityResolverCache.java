package org.daisy.dotify.common.xml;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class EntityResolverCache implements EntityResolver {
	
	private final URLCache cache;
	
	public EntityResolverCache() {
		cache = new URLCache();
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		try {
			URL url = new URI(systemId).toURL();
			InputSource is = new InputSource(cache.openStream(url));
			is.setPublicId(publicId);
			is.setSystemId(systemId);
			return is;
		} catch (URISyntaxException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}


}
