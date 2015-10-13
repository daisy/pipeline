package org.daisy.dotify.api.formatter;


/**
 * A MarkerReferenceField is a place holder for a marker's value. 
 * Its value is resolved by the LayoutPerformer when its location 
 * in the flow is known.
 * @author joha
 *
 */
public class MarkerReferenceField implements Field {
	/**
	 * Defines marker search directions.
	 */
	public static enum MarkerSearchDirection {FORWARD, BACKWARD}
	/**
	 * Defines marker search scopes.
	 */
	public static enum MarkerSearchScope {PAGE_CONTENT, PAGE, SEQUENCE}

	private final String markerName;
	private final MarkerSearchDirection dir;
	private final MarkerSearchScope scope;
	private final String textStyle;
	
	public MarkerReferenceField(String markerName, MarkerSearchDirection dir, MarkerSearchScope scope) {
		this(markerName, dir, scope, null);
	}

	public MarkerReferenceField(String markerName, MarkerSearchDirection dir, MarkerSearchScope scope, String textStyle) {
		super();
		this.markerName = markerName;
		this.dir = dir;
		this.scope = scope;
		this.textStyle = textStyle;
	}

	public String getName() {
		return markerName;
	}
	
	public MarkerSearchDirection getSearchDirection() {
		return dir;
	}
	
	public MarkerSearchScope getSearchScope() {
		return scope;
	}

	@Override
	public String getTextStyle() {
		return textStyle;
	}

}
