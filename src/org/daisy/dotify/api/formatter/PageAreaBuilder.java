package org.daisy.dotify.api.formatter;

//FIXME: rename to PageArea
/**
 * Provides a page area builder.
 * @author Joel Håkansson
 *
 */
public interface PageAreaBuilder {

	/**
	 * Gets a formatter core for the area before the page area.
	 * @return returns a formatter core
	 */
	public FormatterCore getBeforeArea();
	
	/**
	 * Gets a formatter core for the area after the page area.
	 * @return returns a formatter core
	 */
	public FormatterCore getAfterArea();
}
