package org.daisy.dotify.common.text;

/**
 * A supplementary unit is a unit that should be included with the 
 * accompanying unit and within the boundaries of the SplitPoint.
 * The difference between a supplementary unit and a regular unit
 * is that their widths are only counted once within a SplitPoint,
 * should they occur as supplement to more than one unit within
 * the SplitPoint (as determined by equals).
 * 
 * @author Joel Håkansson
 *
 */
public interface SupplementaryUnit {
	
	/**
	 * Gets the size of the unit.
	 * 
	 * @return returns the size of the unit
	 */
	public float getUnitSize();

}
