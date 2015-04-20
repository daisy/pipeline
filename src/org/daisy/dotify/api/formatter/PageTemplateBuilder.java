package org.daisy.dotify.api.formatter;

public interface PageTemplateBuilder {

	/**
	 * Adds a line to the header
	 * @param obj
	 */
	public void addToHeader(FieldList obj);

	/**
	 * Adds a line to the header
	 * @param obj
	 */
	public void addToFooter(FieldList obj);
}
