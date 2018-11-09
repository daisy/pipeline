package org.daisy.dotify.formatter.impl.segment;

import org.daisy.dotify.api.formatter.NumeralStyle;


/**
 * Provides a page number reference event object.
 * 
 * @author Joel Håkansson
 */
class PageNumberReference {
	private final String refid;
	private final NumeralStyle style;
	
	PageNumberReference(String refid, NumeralStyle style) {
		this.refid = refid;
		this.style = style;
	}
	
	/**
	 * Gets the identifier to the reference location.
	 * @return returns the reference identifier
	 */
	public String getRefId() {
		return refid;
	}
	
	/**
	 * Gets the numeral style for this page number reference
	 * @return returns the numeral style
	 */
	public NumeralStyle getNumeralStyle() {
		return style;
	}

}
