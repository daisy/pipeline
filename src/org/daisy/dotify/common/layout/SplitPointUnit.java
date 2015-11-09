package org.daisy.dotify.common.layout;

import java.util.List;

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
	 * Returns true if this unit can be excluded if an adjoining object 
	 * <tt>collapsesWith</tt> this unit, but has a greater size.
	 * 
	 * @return true if the unit is collapsible, false otherwise
	 */
	public boolean isCollapsible();
	
	/**
	 * Returns true if this unit can collapse with the other object.
	 * 
	 * @param obj the other object
	 * @return true if the objects can collapse, false otherwise
	 */
	public boolean collapsesWith(Object obj);
	
	/**
	 * Gets the size of the unit.
	 * 
	 * @return returns the size of the unit
	 */
	public float getUnitSize();
	
	/**
	 * Gets the size of the unit if it is the last unit.
	 * 
	 * @return returns the size of the unit when placed last
	 * in the result.
	 */
	public float getLastUnitSize();
	
	/**
	 * Gets the supplementary IDs for this unit.
	 * 
	 * @return returns a list of the supplementary IDs
	 */
	public List<String> getSupplementaryIDs();

}
