package org.daisy.dotify.common.text;

public interface SplitPointUnit {
	
	/**
	 * Returns true if this unit is allowed to be the last unit in a
	 * result.
	 * 
	 * @return returns true if the unit is breakable, false otherwise
	 */
	public boolean isBreakable();
	
	/**
	 * Returns true if this unit is skippable if it overflows the result.
	 * 
	 * @return returns true if the unit is skippable, false otherwise
	 */
	public boolean isSkippable();
	
	/**
	 * Returns true if this unit can be excluded if it is adjoined with other
	 * collapsable items with greater size.
	 * 
	 * @return true if the unit is collapsable, false otherwise
	 */
	public boolean isCollapsable();
	
	/**
	 * Gets the size of the unit.
	 * 
	 * @return returns the size of the unit
	 */
	public float getUnitSize();
	
}
