package org.daisy.dotify.api.formatter;


/**
 * Provides a reference to some property of the physical pages in
 * the final document. Its value is resolved when its 
 * location in the flow is known.
 * 
 * @author Joel Håkansson
 */
public class PagenumField extends NumeralField {
	
	/**
	 * Creates a new page number field with the specified numeral style
	 * @param style the numeral style
	 */
	public PagenumField(NumeralStyle style) {
		super(style);
	}

	/**
	 * Creates a new page number field with the specified numeral and text styles
	 * @param style the numeral style
	 * @param textStyle the text style
	 */
	public PagenumField(NumeralStyle style, String textStyle) {
		super(style, textStyle);
	}

}
