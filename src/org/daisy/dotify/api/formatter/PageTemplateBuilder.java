package org.daisy.dotify.api.formatter;

public interface PageTemplateBuilder {

	/**
	 * Adds a line to the header
	 * @param obj the field list
	 */
	public void addToHeader(FieldList obj);

	/**
	 * Adds a line to the header
	 * @param obj the field list
	 */
	public void addToFooter(FieldList obj);
}
