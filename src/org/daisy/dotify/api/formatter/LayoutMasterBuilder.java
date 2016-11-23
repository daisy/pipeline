package org.daisy.dotify.api.formatter;

/**
 * Provides a builder for a layout master.
 * @author Joel Håkansson
 *
 */
public interface LayoutMasterBuilder {

	/**
	 * Adds a new template to the builder.
	 * @param condition a condition
	 * @return returns a new page template builder
	 */
	public PageTemplateBuilder newTemplate(Condition condition);
	
	/**
	 * Sets the page area.
	 * @param properties the properties of the page area
	 * @return returns a page area builder
	 */
	public PageAreaBuilder setPageArea(PageAreaProperties properties);
}
