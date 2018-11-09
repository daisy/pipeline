package org.daisy.dotify.api.formatter;

/**
 * A MarkerReferenceField is a place holder for a marker's value. 
 * Its value is resolved by the LayoutPerformer when its location 
 * in the flow is known.
 * 
 * @author Joel Håkansson
 */
public class MarkerReferenceField implements Field {
	/**
	 * Defines marker search directions.
	 */
	public enum MarkerSearchDirection {
		/**
		 * Defines a forward search direction 
		 */
		FORWARD,
		/**
		 * Defines a backward search direction
		 */
		BACKWARD
	}
	/**
	 * Defines marker search scopes.
	 */
	public enum MarkerSearchScope {
		/**
		 * Defines page content search scope
		 */
		PAGE_CONTENT,
		/**
		 * Defines page search scope 
		 */
		PAGE, 
		/**
		 * Defines spread content search scope
		 */
		SPREAD_CONTENT,
		/**
		 * Defines spread search scope
		 */
		SPREAD,
		/**
		 * Defines sheet search scope
		 */
		SHEET,
		/**
		 * Defines sequence search scope
		 */
		SEQUENCE,
		/**
		 * Defines volume search scope
		 */
		VOLUME,
		/**
		 * Defines document search scope
		 */
		DOCUMENT
	}

	private final String markerName;
	private final MarkerSearchDirection dir;
	private final MarkerSearchScope scope;
	private final String textStyle;
	private final int offset;
	
	/**
	 * Creates a new instance with the specified parameters
	 * @param markerName the name of the marker
	 * @param dir the direction
	 * @param scope the scope
	 */
	public MarkerReferenceField(String markerName, MarkerSearchDirection dir, MarkerSearchScope scope) {
		this(markerName, dir, scope, null);
	}
	
	/**
	 * Creates a new instance with the specified parameters
	 * @param markerName the name of the marker
	 * @param dir the direction
	 * @param scope the scope
	 * @param textStyle a text style name
	 */
	public MarkerReferenceField(String markerName, MarkerSearchDirection dir, MarkerSearchScope scope, String textStyle) {
		this(markerName, dir, scope, textStyle, 0);
	}

	/**
	 * Creates a new instance with the specified parameters
	 * @param markerName the name of the marker
	 * @param dir the direction
	 * @param scope the scope
	 * @param textStyle a text style name
	 * @param offset offsets the search by the specified amount, in pages
	 */
	public MarkerReferenceField(String markerName, MarkerSearchDirection dir, MarkerSearchScope scope, String textStyle, int offset) {
		super();
		this.markerName = markerName;
		this.dir = dir;
		this.scope = scope;
		this.textStyle = textStyle;
		this.offset = offset;
	}

	/**
	 * Gets the name of this marker reference field
	 * @return returns the name
	 */
	public String getName() {
		return markerName;
	}
	
	/**
	 * Gets the direction of the search
	 * @return returns the search direction
	 */
	public MarkerSearchDirection getSearchDirection() {
		return dir;
	}
	
	/**
	 * Gets the scope of the search
	 * @return returns the search scope
	 */
	public MarkerSearchScope getSearchScope() {
		return scope;
	}

	@Override
	public String getTextStyle() {
		return textStyle;
	}

	/**
	 * Gets the page offset where to start the search
	 * @return the page offset
	 */
	public int getOffset() {
		return offset;
	}

}
