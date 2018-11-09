package org.daisy.dotify.formatter.impl.core;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.formatter.impl.row.AbstractBlockContentManager;
import org.daisy.dotify.formatter.impl.row.RowDataProperties;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.formatter.impl.search.DefaultContext;

class TableBlockContentManager extends AbstractBlockContentManager {
	private final List<RowImpl> rows;
	private final int forceCount;
    private int rowIndex;

	TableBlockContentManager(int flowWidth, int minWidth, int forceCount, List<RowImpl> rows, RowDataProperties rdp, FormatterContext fcontext) {
		super(flowWidth, rdp, fcontext, minWidth);
		this.rows = Collections.unmodifiableList(rows);
		this.forceCount = forceCount;
        initFields();
	}
	
	TableBlockContentManager(TableBlockContentManager template) {
		super(template);
		this.rows = template.rows;
		this.forceCount = template.forceCount;
		this.rowIndex = template.rowIndex;
	}
	
    private void initFields() {
    	rowIndex = 0;
    }
	
	@Override
	public AbstractBlockContentManager copy() {
		return new TableBlockContentManager(this);
	}
	
	@Override
	public boolean supportsVariableWidth() {
		return false;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

    @Override
    public void reset() {
    	initFields();
    }
	
	@Override
	public Optional<RowImpl> getNext(boolean wholeWordsOnly) {
		if (rowIndex<rows.size()) {
			RowImpl ret = rows.get(rowIndex);
			rowIndex++;
			return Optional.of(ret);
		} else {
			return Optional.empty();
		}
	}

    @Override
    public boolean hasNext() {
        return rowIndex<rows.size();
    }

	@Override
	public int getForceBreakCount() {
		return forceCount;
	}

	@Override
	public void setContext(DefaultContext context) {
		// FIXME: Support setting context of tables. At the moment, there is no point in setting a context here, because the contents has already been rendered.
	}
	
	@Override
	public List<Marker> getGroupMarkers() {
		return Collections.emptyList();
	}
	
	@Override
	public List<String> getGroupAnchors() {
		return Collections.emptyList();
	}

	@Override
	public List<String> getGroupIdentifiers() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasSignificantContent() {
		return hasNext();
	}

}
