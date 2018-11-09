package org.daisy.dotify.formatter.impl.row;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.segment.Segment;

/**
 * BlockHandler is responsible for breaking blocks of text into rows. BlockProperties
 * such as list numbers, leaders and margins are resolved in the process. The input
 * text is filtered using the supplied StringFilter before breaking into rows, since
 * the length of the text could change.
 * 
 * @author Joel Håkansson
 */
public class BlockContentManager extends AbstractBlockContentManager {
	private final List<RowImpl> rows;
	private final SegmentProcessor sp;
	private int rowIndex;
	
	public BlockContentManager(String blockId, int flowWidth, List<Segment> segments, RowDataProperties rdp, CrossReferenceHandler refs, Context context, FormatterCoreContext fcontext) {
		super(flowWidth, rdp, fcontext);
		this.rows = new ArrayList<>();
		this.sp = new SegmentProcessor(blockId, segments, flowWidth, refs, context, flowWidth - margins.getRightMargin().getContent().length(), margins, fcontext, rdp);
		initFields();
	}
	
	private BlockContentManager(BlockContentManager template) {
		super(template);
		this.rows = new ArrayList<>(template.rows);
		this.sp = new SegmentProcessor(template.sp);
		this.rowIndex = template.rowIndex;
	}
	
    private void initFields() {
		rowIndex = 0;
    }
	
    @Override
	public void setContext(DefaultContext context) {
		this.sp.setContext(context);
	}

	@Override
	public AbstractBlockContentManager copy() {
		return new BlockContentManager(this);
	}
	
	/**
	 * Ensures that the specified result index is available in the result list.
	 * Note that this function is modeled after {@link RowGroupDataSource}, but that it
	 * isn't used in the same way (yet).
	 * @param index the index to ensure
	 * @return returns true if the specified index is available in the result list, false
	 * if the specified index cannot be made available (because the input doesn't contain
	 * the required amount of data).
	 */
	private boolean ensureBuffer(int index, boolean wholeWordsOnly) {
		while (index<0 || rows.size()<index) {
			if (!sp.hasMoreData()) {
				return false;
			}
			sp.getNext(wholeWordsOnly).ifPresent(v->rows.add(v));
		}
		return rows.size()>=index;
	}
	
	@Override
	public int getRowCount() {
		if (hasNext()) {
			throw new IllegalStateException();
		}
		return rows.size();
	}
	
	@Override
	public boolean supportsVariableWidth() {
		return true;
	}
	
    @Override
	public void reset() {
    	sp.reset();
    	rows.clear();
    	initFields();
    }

	@Override
	public boolean hasNext() {
		int diff = rows.size()-rowIndex;
		if (diff==0) {
			if (!sp.hasMoreData()) {
				return false;
			} else {
				return new SegmentProcessor(sp).getNext(false).isPresent();
			}
		} else if (diff<0) {
			// The next value should always follow the size of the last produced result.
			// If it doesn't, something has gone wrong elsewhere in this class.
			throw new RuntimeException("Error in code");
		} else {
			return true;
		}
	}

	@Override
	public Optional<RowImpl> getNext(boolean wholeWordsOnly) {
		if (ensureBuffer(rowIndex+1, wholeWordsOnly)) {
			RowImpl ret = rows.get(rowIndex);
			rowIndex++;
			return Optional.of(ret);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public int getForceBreakCount() {
		if (hasNext()) {
			throw new IllegalStateException();
		}
		return sp.getForceCount();
	}

	@Override
	public List<Marker> getGroupMarkers() {
		return sp.getGroupMarkers();
	}
	
	@Override
	public List<String> getGroupAnchors() {
		return sp.getGroupAnchors();
	}

	@Override
	public List<String> getGroupIdentifiers() {
		return sp.getGroupIdentifiers();
	}

	@Override
	public boolean hasSignificantContent() {
		return sp.hasSignificantContent();
	}

}
