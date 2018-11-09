package org.daisy.dotify.formatter.impl.core;

import java.util.List;

import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;

/**
 * Provides an interface for blocks in a dynamic sequence whose inclusion 
 * depend on the occurrence of dynamic contents.
 * 
 * @author Joel Håkansson
 */
public interface BlockGroup {

	/**
	 * Gets the list of blocks with the specified context.
	 * @param context the formatter context
	 * @param c the book context
	 * @param crh the cross reference handler
	 * @return returns a list of blocks
	 */
	public List<Block> getBlocks(FormatterContext context, DefaultContext c, CrossReferenceHandler crh);

	/**
	 * Returns true if these blocks are dynamic.
	 * @return returns true if the blocks are dynamically generated, false otherwise.
	 */
	public boolean isGenerated();
}
