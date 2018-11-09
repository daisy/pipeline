package org.daisy.dotify.api.formatter;

/**
 * NoField represents the absence of a field in a header or
 * footer. Text from the page body "flows into" the header or footer
 * at this position.
 */
public class NoField implements Field {
	
	private NoField() {}
	
	private static final NoField instance = new NoField();
	
	/**
	 * Gets the singleton instance.
	 * @return returns the singleton instance
	 */
	public static NoField getInstance() {
		return instance;
	}
	
	@Override
	public String getTextStyle() {
		return null;
	}
}
