package org.daisy.dotify.formatter.impl.page;

import org.daisy.dotify.api.formatter.FormattingTypes.BreakBefore;
import org.daisy.dotify.formatter.impl.core.Block;
import org.daisy.dotify.formatter.impl.core.BlockContext;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;
import org.daisy.dotify.formatter.impl.row.BlockStatistics;
import org.daisy.dotify.formatter.impl.row.LineProperties;

import java.util.List;
import java.util.Stack;

/**
 * Provides data about a single rendering scenario.
 *
 * @author Joel Håkansson
 */
class ScenarioData {
    private final BlockProcessor blockProcessor;
    private Stack<RowGroupSequence> dataGroups = new Stack<>();

    ScenarioData() {
        blockProcessor = new BlockProcessor();
        dataGroups = new Stack<>();
    }

    /**
     * Creates a deep copy of the supplied instance.
     *
     * @param template the instance to copy
     */
    ScenarioData(ScenarioData template) {
        this.blockProcessor = new BlockProcessor(template.blockProcessor);
        dataGroups = new Stack<>();
        for (RowGroupSequence rgs : template.dataGroups) {
            dataGroups.add(new RowGroupSequence(rgs));
        }
    }

    float calcSize() {
        float size = 0;
        for (RowGroupSequence rgs : dataGroups) {
            for (RowGroup rg : rgs.getGroup()) {
                size += rg.getUnitSize();
            }
        }
        return size;
    }

    private boolean hasSequence() {
        return !dataGroups.isEmpty();
    }

    private boolean hasResult() {
        return !dataGroups.isEmpty();
    }

    private void newRowGroupSequence(BreakBefore breakBefore, VerticalSpacing vs) {
        RowGroupSequence rgs = new RowGroupSequence(breakBefore, vs);
        dataGroups.add(rgs);
    }

    private void setVerticalSpacing(VerticalSpacing vs) {
        dataGroups.peek().setVerticalSpacing(vs);
    }

    List<RowGroupSequence> getDataGroups() {
        return dataGroups;
    }

    void processBlock(LayoutMaster master, Block g, BlockContext bc) {
        blockProcessor.loadBlock(
            master,
            g,
            bc,
            hasSequence(),
            hasResult(),
            this::newRowGroupSequence,
            this::setVerticalSpacing
        );
        while (blockProcessor.hasNextInBlock()) {
            blockProcessor.getNextRowGroup(bc, LineProperties.DEFAULT)
                    .ifPresent(rg -> dataGroups.peek().getGroup().add(rg));
        }
        dataGroups.peek().getBlocks().add(g);
    }


    /**
     * Gets the current block's statistics, or null if no block has been loaded.
     *
     * @return returns the block statistics, or null
     */
    BlockStatistics getBlockStatistics() {
        if (blockProcessor.rowGroupProvider != null) {
            return blockProcessor.rowGroupProvider.getBlockStatistics();
        } else {
            return null;
        }
    }

}
