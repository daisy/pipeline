package org.daisy.dotify.api.formatter;

import java.util.ArrayList;


/**
 * Provides a compound field object.
 * 
 * @author Joel Håkansson
 */
public class CompoundField extends ArrayList<Field> implements Field {

	private final String textStyle;
	public CompoundField() {
		this(null);
	}
	
	public CompoundField(String textStyle) {
		this.textStyle = textStyle;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 6117663405561381287L;

	@Override
	public String getTextStyle() {
		return textStyle;
	}

}
