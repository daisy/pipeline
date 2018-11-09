package org.daisy.dotify.api.formatter;


/**
 * CurrentPageField is a reference to the current page in
 * the final document. Its value is resolved when its 
 * location in the flow is known.
 * 
 * @author Joel Håkansson
 *
 */
public class CurrentPageField extends PagenumField {
	/**
	 * Defines page number types
	 */
	public enum PagenumType {
		/**
		 * The field represents the current page
		 */
		CURRENT_PAGE
		}

	/**
	 * Creates a new current page field.
	 * @param style the numeral style
	 */
	public CurrentPageField(NumeralStyle style) {
		super(style);
	}

	/**
	 * Creates a new current page field.
	 * @param style the numeral style
	 * @param textStyle the text style
	 */
	public CurrentPageField(NumeralStyle style, String textStyle) {
		super(style, textStyle);
	}

}
