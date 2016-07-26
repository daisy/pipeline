package org.daisy.dotify.api.formatter;





/**
 * A Marker is a reference data inserted at some point in the flow. It can be used to create 
 * running headers/footers.
 * @author Joel Håkansson
 *
 */
public class Marker {
	private final String name;
	private final String value;
	
	/**
	 * <p>Create a new Marker with the given name and value.</p><p>Marker names are used
	 * when finding references for e.g. headers and footers and are generally
	 * not unique. Instead, think of marker names as "type" or "class".</p>
	 * @param name the name of the Marker
	 * @param value the Marker value
	 */
	public Marker(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Get the name of this Marker. Note that marker names are not at all
	 * unique.
	 * @return returns the name of this Marker
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the value of this Marker
	 * @return returns this Marker's value
	 */
	public String getValue() {
		return value;
	}

}