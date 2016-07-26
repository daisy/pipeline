package org.daisy.dotify.api.formatter;

public interface ReferenceListBuilder {

	
	/**
	 * <p>Creates a new on collection start block.</p> 
	 * <p>Calling this method is only valid within an item sequence.</p>
	 * @throws IllegalStateException if not in an item sequence
	 * @return a formatter core
	 */
	public FormatterCore newOnCollectionStart();
	
	/**
	 * <p>Creates a new on collection end block.</p> 
	 * <p>Calling this method is only valid within an item sequence.</p>
	 * @return a formatter core
	 */
	public FormatterCore newOnCollectionEnd();

	/**
	 * <p>Creates a new on page start block.</p> 
	 * <p>Calling this method is only valid within an item sequence.</p>
	 * @return a formatter core
	 */
	public FormatterCore newOnPageStart();
	
	/**
	 * <p>Creates a new on page end block.</p> 
	 * <p>Calling this method is only valid within an item sequence.</p>
	 * @return a formatter core
	 */
	public FormatterCore newOnPageEnd();
	
}
