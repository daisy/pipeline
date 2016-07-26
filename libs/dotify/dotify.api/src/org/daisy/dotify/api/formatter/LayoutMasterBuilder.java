package org.daisy.dotify.api.formatter;

public interface LayoutMasterBuilder {

	public PageTemplateBuilder newTemplate(Condition condition);
	
	/**
	 * Sets the page area.
	 * @param properties the properties of the page area
	 * @return returns a page area builder
	 */
	public PageAreaBuilder setPageArea(PageAreaProperties properties);
}
