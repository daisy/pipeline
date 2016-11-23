package org.daisy.dotify.api.formatter;

import java.util.ArrayList;


/**
 * Provides a compound field object.
 * 
 * @author Joel Håkansson
 */
public class CompoundField extends ArrayList<Field> implements Field {
	private static final long serialVersionUID = 6117663405561381287L;

	private final String textStyle;

	/**
	 * Creates a new compound field.
	 */
	public CompoundField() {
		this(null);
	}
	
	/**
	 * Creates a new compound field with the specified text style.
	 * @param textStyle the text style for this compound field.
	 */
	public CompoundField(String textStyle) {
		this.textStyle = textStyle;
	}

	@Override
	public String getTextStyle() {
		return textStyle;
	}

}
