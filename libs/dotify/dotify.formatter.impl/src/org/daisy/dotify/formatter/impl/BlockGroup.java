package org.daisy.dotify.formatter.impl;

import java.util.List;

public interface BlockGroup {

	public List<Block> getBlocks(FormatterContext context, DefaultContext c, CrossReferenceHandler crh);
	public boolean isGenerated();
}
