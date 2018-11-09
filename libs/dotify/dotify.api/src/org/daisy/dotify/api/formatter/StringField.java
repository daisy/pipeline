package org.daisy.dotify.api.formatter;

/**
 * Provides a static string field.
 * 
 * @author Joel Håkansson
 */
public class StringField implements Field {
	private final String str;
	private final String textStyle;
	
	/**
	 * Creates a new string field using the specified object.
	 * The instance's toString method will return the same value
	 * as the toString method of the supplied object.
	 * @param obj the object to use for this field
	 */
	public StringField(Object obj) {
		this(obj, null);
	}

	/**
	 * Creates a new string field using the specified string.
	 * @param str the object to use for this field
	 */
	public StringField(String str) {
		this(str, null);
	}

	/**
	 * Creates a new string field using the specified object and text
	 * style.
	 * @param obj the object to use for this field
	 * @param textStyle the text style
	 */
	public StringField(Object obj, String textStyle) {
		this.str = obj.toString();
		this.textStyle = textStyle;
	}

	/**
	 * Creates a new string field using the specified object and text
	 * style.
	 * @param str the object to use for this field
	 * @param textStyle the text style
	 */
	public StringField(String str, String textStyle) {
		this.str = str;
		this.textStyle = textStyle;
	}

	@Override
	public String toString() {
		return str;
	}

	@Override
	public String getTextStyle() {
		return textStyle;
	}

}
