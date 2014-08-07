package org.daisy.dotify.api.formatter;

public interface DynamicSequenceBuilder {

	public FormatterCore newStaticContext();
	
	/**
	 * Creates a new item sequence at the current position.
	 * 
	 * @param props the properties of the item sequence
	 */
	public ReferenceListBuilder newReferencesListContext(ItemSequenceProperties props);
	

}
