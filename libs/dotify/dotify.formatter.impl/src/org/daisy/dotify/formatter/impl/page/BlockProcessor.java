package org.daisy.dotify.formatter.impl.page;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.daisy.dotify.api.formatter.FormattingTypes.BreakBefore;
import org.daisy.dotify.formatter.impl.core.Block;
import org.daisy.dotify.formatter.impl.core.BlockContext;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;
import org.daisy.dotify.formatter.impl.row.AbstractBlockContentManager;
import org.daisy.dotify.formatter.impl.row.LineProperties;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.formatter.impl.search.BlockAddress;
import org.daisy.dotify.formatter.impl.search.DefaultContext;

/**
 * Provides data about a single rendering scenario.
 * 
 * @author Joel Håkansson
 */
final class BlockProcessor {
	protected RowGroupProvider rowGroupProvider;
	
	BlockProcessor() { }

	BlockProcessor(BlockProcessor template) {
		this.rowGroupProvider = copyUnlessNull(template.rowGroupProvider);
	}
	
	protected void loadBlock(LayoutMaster master, Block g, BlockContext bc, boolean hasSequence, boolean hasResult, BiConsumer<BreakBefore, VerticalSpacing> newRowGroupSequence, Consumer<VerticalSpacing> updateVerticalSpacing) {
		AbstractBlockContentManager bcm = g.getBlockContentManager(bc);
		int keepWithNext = 0;
		if (!hasSequence || ((g.getBreakBeforeType()!=BreakBefore.AUTO || g.getVerticalPosition()!=null) && hasResult)) {
            newRowGroupSequence.accept(g.getBreakBeforeType(), 
                    g.getVerticalPosition()!=null?
                            new VerticalSpacing(g.getVerticalPosition(), new RowImpl("", bcm.getLeftMarginParent(), bcm.getRightMarginParent()))
                                    :null
            );
			keepWithNext = -1;
		} else if (g.getVerticalPosition()!=null  && !hasResult) {
			updateVerticalSpacing.accept(new VerticalSpacing(g.getVerticalPosition(), new RowImpl("", bcm.getLeftMarginParent(), bcm.getRightMarginParent())));
		} else if (rowGroupProvider!=null) {
			keepWithNext = rowGroupProvider.getKeepWithNext();
		}
		rowGroupProvider = new RowGroupProvider(master, g, bcm, bc, keepWithNext);
	}
	
	protected Optional<RowGroup> getNextRowGroup(DefaultContext context, LineProperties lineProps) {
		if (hasNextInBlock()) {
			return Optional.of(rowGroupProvider.next(context, lineProps));
		} else {
			return Optional.empty();
		}
	}
	
	protected boolean hasNextInBlock() {
		if (rowGroupProvider != null) {
			if (rowGroupProvider.hasNext()) {
				return true;
			} else {
				rowGroupProvider.close();
			}
		}
		return false;
	}
	
	private RowGroupProvider copyUnlessNull(RowGroupProvider template) {
		return template==null?null:new RowGroupProvider(template);
	}
	
	BlockAddress getBlockAddress() {
		return rowGroupProvider!=null?rowGroupProvider.getBlockAddress():null;
	}

}