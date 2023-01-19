package org.daisy.pipeline.xmlcatalog;

import java.net.URI;


/**
 * The Interface XmlCatalogParser is used to parse xml catalogs
 */
public interface XmlCatalogParser {

	/**
	 * Parses the xml catalog at uri and returns a {@link XmlCatalog} object
	 *
	 * @param uri the uri
	 * @return the xml catalog
	 */
	public XmlCatalog parse(URI uri);
}
