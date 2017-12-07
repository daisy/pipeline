package org.daisy.dotify.api.formatter;

import org.daisy.dotify.api.writer.PagedMediaWriter;

/**
 * <p>Provides text-only formatting.</p>
 * 
 * @author Joel Håkansson
 */
public interface Formatter {

	/**
	 * Gets the formatter configuration
	 * @return returns the formatter configuration
	 */
	public FormatterConfiguration getConfiguration();
	
	/**
	 * Sets the formatter configuration
	 * @param config the configuration
	 */
	public void setConfiguration(FormatterConfiguration config);

	/**
	 * Start a new Sequence at the current position in the flow.
	 * @param props the SequenceProperties for the new sequence
	 * @return returns a formatter core
	 */
	public FormatterSequence newSequence(SequenceProperties props);

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
	 * <p>Gets the transition builder.</p>
	 * 
	 * @return returns the transition builder
	 */
	public TransitionBuilder getTransitionBuilder();

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
	
	/**
	 * Writes the current result to a paged media
	 * @param writer the paged media writer to use
	 */
	public void write(PagedMediaWriter writer);

}