package org.daisy.dotify.api.formatter;

import org.daisy.dotify.api.formatter.FormattingTypes.BreakBefore;
import org.daisy.dotify.api.formatter.FormattingTypes.Keep;
/**
 * Provides a block of rows and the properties
 * associated with it.
 * @author Joel Håkansson
 */
public interface Block {

	/**
	 * Gets the number of empty rows that should precede the 
	 * rows in this block.
	 * @return returns the number of empty rows preceding the rows in this block
	 */
	public int getSpaceBefore();
	public int getSpaceAfter();
	public int getKeepWithNext();
	public String getIdentifier();
	public Keep getKeepType();
	public BreakBefore getBreakBeforeType();
	public BlockContentManager getBlockContentManager(CrossReferences refs);
	public String getBlockIdentifier();

	/**
	 * Gets the vertical position of the block on page, or null if none is
	 * specified
	 */
	public BlockPosition getVerticalPosition();

	
	public int getKeepWithPreviousSheets();
	public int getKeepWithNextSheets();
	
}