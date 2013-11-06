package org.daisy.dotify.api.formatter;

import java.util.List;


/**
 * Provides a manager for a number of rows.
 * 
 * @author Joel Håkansson
 */
public interface BlockContentManager extends Iterable<Row> {

	public List<Marker> getGroupMarkers(); 
	public int getRowCount();
	/**
	 * Returns true if this RowDataManager contains objects that makes the formatting volatile,
	 * i.e. prone to change due to for example cross references.
	 * @return returns true if, and only if, the RowDataManager should be discarded if a new pass is requested,
	 * false otherwise
	 */
	public boolean isVolatile();
	
}
