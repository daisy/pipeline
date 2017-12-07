package org.daisy.dotify.api.formatter;

/**
 * Provides core formatter tasks.
 * 
 * @author Joel Håkansson
 */
public interface FormatterCore extends BlockBuilder {
	
	/**
	 * Inserts a dynamic layout processor
	 * @param renderer the layout processor
	 */
	public void insertDynamicLayout(DynamicRenderer renderer);
	
	/**
	 * Starts a new table with the supplied properties.
	 * @param props the table properties
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void startTable(TableProperties props);
	
	/**
	 * <p>Marks the beginning of the table header part of a table. The table header is used
	 * for repeating cells after page breaks within a table. This call can only be made
	 * directly after start table.</p>
	 * <p>Note that this is not a hierarchical call that is matched
	 * by a corresponding end method. The end of the table header is implied in the call to
	 * <code>beginsTableBody</code>.</p>
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void beginsTableHeader();
	
	/**
	 * <p>Starts the table body. This call can only be made once per table.</p>
	 * <p>Note that this is not a hierarchical call that is matched
	 * by a corresponding end method. The end of the table body is implied in the call to
	 * <code>endTable</code>.</p>
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void beginsTableBody();
	
	/**
	 * <p>Starts a table row.</p>
	 * <p>Note that this is not a hierarchical call that is matched
	 * by a corresponding end method. The end of the table row is implied in the call to
	 * <code>beginsTableRow</code> or <code>endTable</code>.</p>
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void beginsTableRow();
	
	/**
	 * <p>Starts a table cell.</p>
	 * <p>Note that this is not a hierarchical call that is matched
	 * by a corresponding end method. The end of the table cell is implied in the call to
	 * <code>beginsTableCell</code>, <code>beginsTableRow</code> or <code>endTable</code>.</p>
	 * @param props the cell properties
	 * @return returns a formatter core for the cell
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public FormatterCore beginsTableCell(TableCellProperties props);

	/**
	 * Ends the table.
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void endTable();

}
