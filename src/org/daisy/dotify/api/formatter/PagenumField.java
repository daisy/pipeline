package org.daisy.dotify.api.formatter;


/**
 * A PagenumField is a reference to some property of the physical pages in
 * the final document. Its value is resolved by the LayoutPerformer when its 
 * location in the flow is known.
 * 
 * @author Joel Håkansson
 *
 */
public class PagenumField extends NumeralField {
	
	public PagenumField(NumeralStyle style) {
		super(style);
	}

	public PagenumField(NumeralStyle style, String textStyle) {
		super(style, textStyle);
	}

}
