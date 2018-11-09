package org.daisy.dotify.formatter.impl.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

/**
 * <p>Provides a data source for row groups.</p>
 * 
 * <p>Note that the implementation requires that break point searching is performed on a copy,
 * so that the items are created when a split is performed. If this assumption is not met,
 * things will break.</p>
 * @author Joel Håkansson
 */
class RowGroupDataSource extends BlockProcessor implements SplitPointDataSource<RowGroup, RowGroupDataSource> {
	private static final Supplements<RowGroup> EMPTY_SUPPLEMENTS = new Supplements<RowGroup>() {
		@Override
		public RowGroup get(String id) {
			return null;
		}
	};
	private final LayoutMaster master;
	private final Supplements<RowGroup> supplements;
	private final BreakBefore breakBefore;
	private final VerticalSpacing vs;
	private final List<Block> blocks;
	private List<RowGroup> groups;
	private BlockContext bc;
	private int blockIndex;
	private boolean allowHyphenateLastLine;

	RowGroupDataSource(LayoutMaster master, BlockContext bc, List<Block> blocks, BreakBefore breakBefore, VerticalSpacing vs, Supplements<RowGroup> supplements) {
		super();
		this.master = master;
		this.bc = bc;
		this.groups = null;
		this.blocks = blocks;
		this.supplements = supplements;
		this.breakBefore = breakBefore;
		this.vs = vs;
		this.blockIndex = 0;
		this.allowHyphenateLastLine = true;
	}

	RowGroupDataSource(RowGroupDataSource template) {
		this(template, 0);
	}
	
	RowGroupDataSource(RowGroupDataSource template, int offset) {
		super(template);
		this.master = template.master;
		this.bc = template.bc;
		if (template.groups==null) {
			this.groups = null;
		} else if (template.groups.size()>offset) {
			this.groups = new ArrayList<>(
					offset>0?template.groups.subList(offset, template.groups.size()):template.groups);
		} else {
			this.groups = new ArrayList<>();
		}
		this.blocks = template.blocks;
		this.supplements = template.supplements;
		this.breakBefore = template.breakBefore;
		this.vs = template.vs;
		this.blockIndex = template.blockIndex;
		this.allowHyphenateLastLine = template.allowHyphenateLastLine;
	}
	
	static RowGroupDataSource copyUnlessNull(RowGroupDataSource template) {
		return template==null?null:new RowGroupDataSource(template);
	}
	
	@Override
	public Supplements<RowGroup> getSupplements() {
		return supplements;
	}

	@Override
	public boolean hasElementAt(int index) {
		return ensureBuffer(index+1);
	}

	@Override
	public boolean isEmpty() {
		return this.currentRowCount()==0 && blockIndex>=blocks.size() && !hasNextInBlock();
	}

	@Override
	public RowGroup get(int n) {
		if (!ensureBuffer(n+1)) {
			throw new IndexOutOfBoundsException("" + n);
		}
		return this.groups.get(n);
	}

	@Override
	public List<RowGroup> getRemaining() {
		ensureBuffer(-1);
		if (this.groups==null) {
			return Collections.emptyList();
		} else {
			return this.groups.subList(0, currentRowCount());
		}
	}

	@Override
	public int getSize(int limit) {
		if (!ensureBuffer(limit))  {
			//we have buffered all elements
			return this.currentRowCount();
		} else {
			return limit;
		}
	}

	VerticalSpacing getVerticalSpacing() {
		return vs;
	}
	
	BreakBefore getBreakBefore() {
		return breakBefore;
	}
	
	BlockContext getContext() {
		return bc;
	}
	
	void setContext(BlockContext c) {
		this.bc = c;
	}
	
	/**
	 * <p>Sets the hyphenate last line property.</p>
	 * 
	 * <p>Note that the implementation assumes that this is only used immediately before
	 * calling split on a {@link SplitPointHandler} with a {@link SplitPointSpecification}.
	 * Calling this method after a call to split is not necessary.</p>
	 * @param value the value
	 */
	void setAllowHyphenateLastLine(boolean value) {
		this.allowHyphenateLastLine = value;
	}
	
	/**
	 * Ensures that there are at least index elements in the buffer.
	 * When index is -1 this method always returns false.
	 * @param index the index (or -1 to get all remaining elements)
	 * @return returns true if the index element was available, false otherwise
	 */
	private boolean ensureBuffer(int index) {
		while (index<0 || this.currentRowCount()<index) {
			if (blockIndex>=blocks.size() && !hasNextInBlock()) {
				return false;
			}
			if (!hasNextInBlock()) {
				//get next block
				Block b = blocks.get(blockIndex);
				blockIndex++;
				loadBlock(master, b, bc);
			}
			// Requesting all items implies that no special last line hyphenation processing is needed.
			// This is reasonable: The very last line in a result would never be hyphenated, so suppressing
			// hyphenation is unnecessary. Also, actively doing this would be difficult, because we do not know
			// if the line produced below is the last line or not, until after the call has already been made.
			processNextRowGroup(bc, !allowHyphenateLastLine && index>-1 && currentRowCount()>=index-1);
		}
		return true;
	}

	@Override
	public SplitResult<RowGroup, RowGroupDataSource> splitInRange(int atIndex) {
		// TODO: rewrite this so that rendered tail data is discarded
		if (!ensureBuffer(atIndex)) {
			throw new IndexOutOfBoundsException("" + atIndex);
		}
		RowGroupDataSource tail = new RowGroupDataSource(this, atIndex);
		tail.allowHyphenateLastLine = true;
		if (atIndex==0) {
			return new DefaultSplitResult<RowGroup, RowGroupDataSource>(Collections.emptyList(), tail);
		} else {
			return new DefaultSplitResult<RowGroup, RowGroupDataSource>(this.groups.subList(0, atIndex), tail);
		}
	}

	@Override
	public RowGroupDataSource createEmpty() {
		return new RowGroupDataSource(master, bc, Collections.emptyList(), breakBefore, vs, EMPTY_SUPPLEMENTS);
	}

	@Override
	public RowGroupDataSource getDataSource() {
		return this;
	}

	@Override
	protected void newRowGroupSequence(BreakBefore breakBefore, VerticalSpacing vs) {
		if (groups!=null) {
			throw new IllegalStateException();
		} else {
			groups = new ArrayList<>();
		}
	}

	@Override
	protected boolean hasSequence() {
		return groups!=null;
	}

	@Override
	protected boolean hasResult() {
		return hasSequence() && !groups.isEmpty();
	}

	@Override
	protected void addRowGroup(RowGroup rg) {
		groups.add(rg);
	}
	
	int currentRowCount() {
		return groups==null?0:groups.size();
	}
}
