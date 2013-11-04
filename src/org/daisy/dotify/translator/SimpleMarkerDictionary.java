package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.TextAttribute;


/**
 * Provides a simple marker dictionary which contains one entry only.
 * 
 * @author Joel Håkansson
 * 
 */
public class SimpleMarkerDictionary implements MarkerDictionary {
	private final Marker def;

	/**
	 * Creates a new instance with the supplied marker
	 * 
	 * @param def
	 *            the marker to apply regardless of the contents
	 */
	public SimpleMarkerDictionary(Marker def) {
		this.def = def;
	}

	public Marker getMarkersFor(String str, TextAttribute attributes) throws MarkerNotFoundException, MarkerNotCompatibleException {
		return def;
	}

}
