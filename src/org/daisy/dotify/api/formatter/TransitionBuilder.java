package org.daisy.dotify.api.formatter;

/**
 * <p>Provides a definition for volume transitions. A volume transition 
 * indicates that the rows on the last page or sheet of the volume
 * may be moved in order to avoid breaking the volume inside a sentence or
 * a paragraph.</p>
 * 
 * <p>When volume transition processing is activated, an implementation 
 * should break between paragraphs and, only if that's not possible,
 * between sentences.</p>
 * 
 * <p>Content added to the {@link BlockContentBuilder}s
 * returned by the methods of this interface should be inserted 
 * when the text transitions from one volume to the next <em>if and 
 * only if</em> the page layout is affected by a particular
 * volume transition. If the page layout is not affected, template
 * contents should not be inserted. For example, if the volume 
 * transition takes place between two sequences.</p>
 * 
 * <p>Volume transition processing is deactivated by default. To activate,
 * set the {@link TransitionBuilderProperties.ApplicationRange} using the method 
 * {@link #setProperties(TransitionBuilderProperties)}.</p>
 *  
 * @author Joel Håkansson
 */
public interface TransitionBuilder {
	
	/**
	 * Gets the properties for this transition builder.
	 * @return returns the properties
	 */
	public TransitionBuilderProperties getProperties();
	
	/**
	 * Sets the properties for this transition builder.
	 * @param props the properties
	 */
	public void setProperties(TransitionBuilderProperties props);

	/**
	 * Gets the builder for contents to add when a block is resumed after
	 * a volume break.
	 * 
	 * @return returns a builder for content.
	 */
	public BlockContentBuilder getBlockResumedBuilder();

	/**
	 * Gets the builder for contents to add when a block is interrupted
	 * before a volume break.
	 * 
	 * @return returns a builder for content.
	 */
	public BlockContentBuilder getBlockInterruptedBuilder();

	/**
	 * Gets the builder for contents to add when a sequence is resumed
	 * after a volume break.
	 * @return returns a builder for content.
	 */
	public BlockBuilder getSequenceResumedBuilder();

	/**
	 * Gets the builder for contents to add when a sequence is
	 * interrupted before a volume break.
	 * @return returns a builder for content.
	 */
	public BlockBuilder getSequenceInterruptedBuilder();
	
}
