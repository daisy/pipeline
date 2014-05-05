package org.daisy.dotify.api.writer;

import java.io.Closeable;
import java.io.OutputStream;
import java.util.List;




/**
 * <p>PagedMediaWriter is an interface for writing to a paged media.</p>
 * 
 * <p>An implementation of PagedMediaWriter is responsible for writing 
 * volumes, sections, pages and rows in a specific format.</p>
 * 
 * <p>The PagedMediaWriter must not alter the input structure.
 * For example, an implementation of PagedMediaWriter must not break
 * a page unless instructed via {@link #newPage()}.</p>
 * 
 * @author Joel Håkansson
 */
public interface PagedMediaWriter extends Closeable {
	
	/**
	 * Inserts a new volume in the output format,
	 * if applicable
	 */
	public void newVolume(SectionProperties props);

	/**
	 * Insert a new section in the output format, 
	 * if applicable
	 * @param props the SectionProperties for this section
	 */
	public void newSection(SectionProperties props);

	/**
	 *  Inserts a new page in the output format,
	 *  if applicable
	 */
	public void newPage();

	/**
	 * Add a new row to the current page
	 * @param row the row
	 */
	public void newRow(Row row);
	
	/**
	 * Add a new empty row to the current page  
	 */
	public void newRow();

	/**
	 * Open the PagedMediaWriter for writing
	 * @param os The underlying OutputStream for the PagedMediaWriter
	 * @throws PagedMediaWriterException throws an PagedMediaWriterException if the PagedMediaWriter could not be opened
	 */
	public void open(OutputStream os, List<MetaDataItem> meta) throws PagedMediaWriterException;

}
