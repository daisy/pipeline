package org.daisy.dotify.formatter.impl.page;

import org.daisy.dotify.api.formatter.FormattingTypes.BreakBefore;
import org.daisy.dotify.formatter.impl.core.Block;
import org.daisy.dotify.formatter.impl.core.BlockContext;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;
import org.daisy.dotify.formatter.impl.row.AbstractBlockContentManager;
import org.daisy.dotify.formatter.impl.row.LineProperties;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.formatter.impl.search.BlockAddress;
import org.daisy.dotify.formatter.impl.search.DefaultContext;

import java.util.Optional;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Provides data about a single rendering scenario.
 *
 * @author Joel Håkansson
 */
final class BlockProcessor {
    protected RowGroupProvider rowGroupProvider;
    private final Stack<OrphanWidowControl> owcStack = new Stack<>();

    BlockProcessor() {
    }

    BlockProcessor(BlockProcessor template) {
        OrphanWidowControl lastOwc = null;
        for (OrphanWidowControl owc : template.owcStack) {
            owcStack.add((lastOwc = new OrphanWidowControl(owc, lastOwc)));
        }
        this.rowGroupProvider = template.rowGroupProvider != null
            ? new RowGroupProvider(template.rowGroupProvider, lastOwc)
            : null;
    }

    protected void loadBlock(
        LayoutMaster master,
        Block g,
        BlockContext bc,
        boolean hasSequence,
        boolean hasResult,
        BiConsumer<BreakBefore, VerticalSpacing> newRowGroupSequence,
        Consumer<VerticalSpacing> updateVerticalSpacing
    ) {
        AbstractBlockContentManager bcm = g.getBlockContentManager(bc);
        int keepWithNext = 0;
        if (
            !hasSequence ||
            (
                (g.getBreakBeforeType() != BreakBefore.AUTO || g.getVerticalPosition() != null) &&
                hasResult
            )
        ) {
            newRowGroupSequence.accept(g.getBreakBeforeType(),
                g.getVerticalPosition() != null ?
                new VerticalSpacing(
                    g.getVerticalPosition(),
                    new RowImpl("", bcm.getLeftMarginParent(), bcm.getRightMarginParent())
                ) :
                null
            );
            keepWithNext = -1;
        } else if (g.getVerticalPosition() != null && !hasResult) {
            updateVerticalSpacing.accept(
                new VerticalSpacing(
                    g.getVerticalPosition(),
                    new RowImpl("", bcm.getLeftMarginParent(), bcm.getRightMarginParent())
                )
            );
        } else if (rowGroupProvider != null) {
            keepWithNext = rowGroupProvider.getKeepWithNext();
        }
        if (rowGroupProvider != null) {
            // might have ancestors in common with previous block
            Block prev = rowGroupProvider.g;
            int common = g.getCommonAncestors(prev);
            while (owcStack.size() > common) {
                owcStack.pop();
            }
        } else {
            owcStack.clear();
        }
        OrphanWidowControl owc = !owcStack.empty() ? owcStack.peek() : null;
        int depth = g.getAncestors();
        while (owcStack.size() < depth - 1) {
            owcStack.push((owc = new OrphanWidowControl(0, 0, 0, g.getBlockAddress(), owc)));
        }
        owc = new OrphanWidowControl(
            g.getRowDataProperties().getOrphans(),
            g.getRowDataProperties().getWidows(),
            bc.getRefs().getRowCount(g.getBlockAddress()),
            g.getBlockAddress(),
            owc);
        rowGroupProvider = new RowGroupProvider(master, g, bcm, bc, keepWithNext, owc);
        owcStack.push(owc);
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

    BlockAddress getBlockAddress() {
        return rowGroupProvider != null ? rowGroupProvider.getBlockAddress() : null;
    }

}
