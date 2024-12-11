package org.daisy.dotify.formatter.impl.page;

import org.daisy.dotify.api.formatter.FormattingTypes.BreakBefore;
import org.daisy.dotify.common.splitter.DefaultSplitResult;
import org.daisy.dotify.common.splitter.SplitPointDataSource;
import org.daisy.dotify.common.splitter.SplitPointHandler;
import org.daisy.dotify.common.splitter.SplitPointSpecification;
import org.daisy.dotify.common.splitter.SplitResult;
import org.daisy.dotify.common.splitter.Supplements;
import org.daisy.dotify.formatter.impl.core.Block;
import org.daisy.dotify.formatter.impl.core.BlockContext;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;
import org.daisy.dotify.formatter.impl.row.LineProperties;
import org.daisy.dotify.formatter.impl.search.BlockLineLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>Provides a {@link SplitPointDataSource data source} for {@link RowGroup row groups}.</p>
 *
 * <p>The input is</p>
 * <ul>
 *   <li>a {@link RowGroupSequence}, i.e. a sequence of {@link Block}s starting at a hard page
 *       break (<code>break-before="page"</code> or <code>break-before="sheet"</code>) or a block
 *       with absolute positioning (<code>vertical-position</code>)</li>
 *   <li>a {@link Supplements} of {@link RowGroup}s: a map containing the items in the collection
 *       corresponding to the page's <code>page-area</code> (if any). Each item is a single RowGroup.</li>
 * </ul>
 *
 * <p>Note that the implementation requires that break point searching is performed on a copy,
 * so that the items are created when a split is performed. If this assumption is not met,
 * things will break.</p>
 *
 * @author Joel Håkansson
 */
class RowGroupDataSource implements SplitPointDataSource<RowGroup, RowGroupDataSource> {
    private static final Supplements<RowGroup> EMPTY_SUPPLEMENTS = id -> null;
    private final BlockProcessor blockProcessor;
    private final LayoutMaster master;
    private final Supplements<RowGroup> supplements;
    private final RowGroupSequence data;
    private BlockContext bc;
    private Function<RowGroupSequence, Integer> reservedWidths = x -> 0;
    private int blockIndex;
    private boolean allowHyphenateLastLine;
    private int offsetInBlock;

    RowGroupDataSource(
        LayoutMaster master,
        BlockContext bc,
        List<Block> blocks,
        BreakBefore breakBefore,
        VerticalSpacing vs,
        Supplements<RowGroup> supplements
    ) {
        super();
        this.blockProcessor = new BlockProcessor();
        this.master = master;
        this.bc = bc;
        this.supplements = supplements;
        this.data = new RowGroupSequence(breakBefore, vs, blocks, null);
        this.blockIndex = 0;
        this.allowHyphenateLastLine = true;
        this.offsetInBlock = 0;
    }

    RowGroupDataSource(RowGroupDataSource template) {
        this(template, 0);
    }

    RowGroupDataSource(RowGroupDataSource template, int offset) {
        this.blockProcessor = new BlockProcessor(template.blockProcessor);
        this.master = template.master;
        this.bc = template.bc;
        this.offsetInBlock = template.offsetInBlock;
        this.supplements = template.supplements;
        this.data = new RowGroupSequence(template.data, offset);
        this.blockIndex = template.blockIndex;
        this.allowHyphenateLastLine = template.allowHyphenateLastLine;
        this.reservedWidths = template.reservedWidths;
    }

    static RowGroupDataSource copyUnlessNull(RowGroupDataSource template) {
        return template == null ? null : new RowGroupDataSource(template);
    }

    @Override
    public Supplements<RowGroup> getSupplements() {
        return supplements;
    }

    @Override
    public boolean hasElementAt(int index) {
        return ensureBuffer(index + 1);
    }

    @Override
    public boolean isEmpty() {
        return this.groupSize() == 0 && blockIndex >= data.getBlocks().size() && !blockProcessor.hasNextInBlock();
    }

    @Override
    public RowGroup get(int n) {
        if (!ensureBuffer(n + 1)) {
            throw new IndexOutOfBoundsException("" + n);
        }
        return this.data.getGroup().get(n);
    }

    @Override
    public List<RowGroup> getRemaining() {
        ensureBuffer(-1);
        if (this.data.getGroup() == null) {
            return Collections.emptyList();
        } else {
            return this.data.getGroup().subList(0, groupSize());
        }
    }

    @Override
    public int getSize(int limit) {
        if (!ensureBuffer(limit)) {
            //we have buffered all elements
            return this.groupSize();
        } else {
            return limit;
        }
    }

    VerticalSpacing getVerticalSpacing() {
        return data.getVerticalSpacing();
    }

    BreakBefore getBreakBefore() {
        return data.getBreakBefore();
    }

    BlockContext getContext() {
        return bc;
    }

    void modifyContext(Consumer<? super BlockContext.Builder> modifier) {
        BlockContext.Builder b = BlockContext.from(getContext());
        modifier.accept(b);
        bc = b.build();
    }

    void setReservedWidths(Function<RowGroupSequence, Integer> func) {
        this.reservedWidths = func;
    }

    /**
     * <p>Sets the hyphenate last line property.</p>
     *
     * <p>Note that the implementation assumes that this is only used immediately before
     * calling split on a {@link SplitPointHandler} with a {@link SplitPointSpecification}.
     * Calling this method after a call to split is not necessary.</p>
     *
     * @param value the value
     */
    void setAllowHyphenateLastLine(boolean value) {
        this.allowHyphenateLastLine = value;
    }

    /**
     * Ensures that there are at least index elements in the buffer.
     * When index is -1 this method always returns false.
     *
     * @param index the index (or -1 to get all remaining elements)
     * @return returns true if the index element was available, false otherwise
     */
    private boolean ensureBuffer(int index) {
        while (index < 0 || this.groupSize() < index) {
            if (blockIndex >= data.getBlocks().size() && !blockProcessor.hasNextInBlock()) {
                return false;
            }
            if (!blockProcessor.hasNextInBlock()) {
                //get next block
                Block b = data.getBlocks().get(blockIndex);
                blockIndex++;
                offsetInBlock = 0;
                modifyContext(c -> c.topOfPage(data.getGroup() == null ||
                                               data.getGroup().stream().allMatch(v -> v.getUnitSize() == 0)));
                blockProcessor.loadBlock(master, b, getContext(), hasSequence(), hasResult(),
                                         this::newRowGroupSequence, v -> { });
            }
            // Requesting all items implies that no special last line hyphenation processing is needed.
            // This is reasonable: The very last line in a result would never be hyphenated, so suppressing
            // hyphenation is unnecessary. Also, actively doing this would be difficult, because we do not know
            // if the line produced below is the last line or not, until after the call has already been made.
            Optional<RowGroup> added = blockProcessor.getNextRowGroup(getContext(), new LineProperties.Builder()
                    .suppressHyphenation(!allowHyphenateLastLine && index > -1 && groupSize() >= index - 1)
                    .reservedWidth(reservedWidths.apply(data))
                    .lineBlockLocation(new BlockLineLocation(blockProcessor.getBlockAddress(), offsetInBlock))
                    .build());
            added.ifPresent(rg -> data.getGroup().add(rg));
            offsetInBlock += added.map(v -> v.getRows().size()).orElse(0);
        }
        return true;
    }

    // this happens when a new page is started
    @Override
    public SplitResult<RowGroup, RowGroupDataSource> splitInRange(int atIndex) {
        // TODO: rewrite this so that rendered tail data is discarded
        if (!ensureBuffer(atIndex)) {
            throw new IndexOutOfBoundsException("" + atIndex);
        }
        RowGroupDataSource tail = new RowGroupDataSource(this, atIndex);
        tail.allowHyphenateLastLine = true;
        if (atIndex == 0) {
            return new DefaultSplitResult<RowGroup, RowGroupDataSource>(Collections.emptyList(), tail);
        } else {
            return new DefaultSplitResult<RowGroup, RowGroupDataSource>(
                this.data.getGroup().subList(0, atIndex),
                tail
            );
        }
    }

    @Override
    public RowGroupDataSource createEmpty() {
        return new RowGroupDataSource(
            master,
            getContext(),
            Collections.emptyList(),
            data.getBreakBefore(),
            data.getVerticalSpacing(),
            EMPTY_SUPPLEMENTS
        );
    }

    @Override
    public RowGroupDataSource getDataSource() {
        return this;
    }

    protected void newRowGroupSequence(BreakBefore breakBefore, VerticalSpacing vs) {
        // Vertical spacing isn't used at this stage.
        if (data.getGroup() != null) {
            // this means the return values of ScenarioData.hasSequence() and
            // ScenarioData.hasResult() did not match those of RowGroupDataSource.hasSequence() and
            // RowGroupDataSource.hasResult() for the same block
            throw new IllegalStateException();
        } else {
            data.setGroup(new ArrayList<>());
        }
    }

    private boolean hasSequence() {
        return data.getGroup() != null;
    }

    private boolean hasResult() {
        return hasSequence() && !data.getGroup().isEmpty();
    }

    private int groupSize() {
        return data.getGroup() == null ? 0 : data.getGroup().size();
    }
}
