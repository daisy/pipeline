package org.daisy.dotify.api.formatter;

/**
 * Provides a dynamic sequence builder.
 * @author Joel Håkansson
 */
public interface DynamicSequenceBuilder {

	/**
	 * Creates a new formatter core at the current position.
	 * @return returns a new formatter core
	 */
	public FormatterCore newStaticContext();
	
	/**
	 * Creates a new reference list builder at the current position.
	 * 
	 * @param props the properties of the reference list
	 * @return a new reference list builder
	 */
	public ReferenceListBuilder newReferencesListContext(ItemSequenceProperties props);

}