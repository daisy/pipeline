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
	 * @param props the section properties
	 * @throws IllegalStateException if writer is not opened or if writer has been closed
	 */
	public void newVolume(SectionProperties props);

	/**
	 * Insert a new section in the output format, 
	 * if applicable
	 * @param props the SectionProperties for this section
	 * @throws IllegalStateException if writer is not opened or if writer has been closed
	 */
	public void newSection(SectionProperties props);

	/**
	 *  Inserts a new page in the output format,
	 *  if applicable
	 *  @throws IllegalStateException if writer is not opened or if writer has been closed
	 */
	public void newPage();

	/**
	 * Add a new row to the current page
	 * @param row the row
	 * @throws IllegalStateException if writer is not opened or if writer has been closed
	 */
	public void newRow(Row row);
	
	/**
	 * Add a new empty row to the current page  
	 * @throws IllegalStateException if writer is not opened or if writer has been closed
	 */
	public void newRow();
	
	/**
	 * Adds additional metadata items. Must be called before opening the writer. Multiple calls
	 * to prepare will append to the existing list of metadata.
	 * @param meta a list of metadata
	 * @throws IllegalStateException if writer has been opened
	 */
	public void prepare(List<MetaDataItem> meta);
	
	/**
	 * Open the PagedMediaWriter for writing. Must be called before writing to the writer.
	 * @param os The underlying OutputStream for the PagedMediaWriter
	 * @throws PagedMediaWriterException throws an PagedMediaWriterException if the PagedMediaWriter could not be opened
	 * @throws IllegalStateException if writer has already been opened
	 */
	public void open(OutputStream os) throws PagedMediaWriterException;

}
