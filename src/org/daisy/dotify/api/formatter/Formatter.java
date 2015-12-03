package org.daisy.dotify.api.formatter;

import java.io.Closeable;

import org.daisy.dotify.api.writer.PagedMediaWriter;

/**
 * <p>Provides text-only formatting.</p>
 * 
 * @author Joel Håkansson
 */
public interface Formatter extends Closeable {

	/**
	 * Opens the Formatter for writing.
	 */
	public void open();

	/**
	 * Start a new Sequence at the current position in the flow.
	 * @param props the SequenceProperties for the new sequence
	 * @return returns a formatter core
	 */
	public FormatterCore newSequence(SequenceProperties props);

	/**
	 * Creates a new LayoutMaster builder.
	 * @param name The name of the LayoutMaster. This is the named used in when retrieving
	 * a master for a particular sequence from the {@link SequenceProperties}.
	 * @param properties the properties
	 * @return a layout master builder
	 */
	public LayoutMasterBuilder newLayoutMaster(String name, LayoutMasterProperties properties);

	/**
	 * Creates a new empty volume template builder.
	 * @param props properties
	 * @return returns a new volume template builder
	 */
	public VolumeTemplateBuilder newVolumeTemplate(VolumeTemplateProperties props); 

	/**
	 * Creates a new table of contents with the supplied name
	 * @param name the TOC identifier
	 * @return returns a new table of contents
	 */
	public TableOfContents newToc(String name);

	/**
	 * Creates a new collection with the supplied identifier
	 * 
	 * @param collectionIdentifier the collection identifier
	 * @return returns a new collection
	 */
	public ContentCollection newCollection(String collectionIdentifier); 
	
	public void write(PagedMediaWriter writer);

}