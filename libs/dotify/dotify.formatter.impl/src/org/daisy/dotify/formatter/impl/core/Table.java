package org.daisy.dotify.formatter.impl.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.daisy.dotify.api.formatter.FormatterCore;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.formatter.RenderingScenario;
import org.daisy.dotify.api.formatter.TableCellProperties;
import org.daisy.dotify.api.formatter.TableProperties;
import org.daisy.dotify.api.translator.Border;
import org.daisy.dotify.api.translator.BorderSpecification;
import org.daisy.dotify.api.translator.BorderSpecification.Style;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.common.text.StringTools;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.row.AbstractBlockContentManager;
import org.daisy.dotify.formatter.impl.row.MarginProperties;
import org.daisy.dotify.formatter.impl.row.RowDataProperties;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.segment.Segment;
import org.daisy.dotify.formatter.impl.segment.TextSegment;

class Table extends Block {
	private static final Logger logger = Logger.getLogger(Table.class.getCanonicalName());
	private int headerRows;
	private final TableData td;
	private final TableProperties tableProps;
	private Map<String, Result> resultCache;
	private final TableBorderHandler tbh;

	Table(FormatterCoreContext fc, TableProperties tableProps, RowDataProperties rdp, TextBorderFactoryMakerService tbf, String mode, RenderingScenario rs) {
		super(null, rdp, rs);
		this.tableProps = tableProps;
		if (tableProps.getTableRowSpacing()>0) {
			throw new UnsupportedOperationException("Table row spacing > 0 is not implemented.");
		}
		this.headerRows = 0;
		this.td = new TableData(fc);
		this.tbh = new TableBorderHandler(tableProps.getTableColSpacing(), tbf, mode);
	}
	
	Table(Table template) {
		super(template);
		this.headerRows = template.headerRows;
		this.td = template.td;
		this.tableProps = template.tableProps;
		this.resultCache = template.resultCache;
		this.tbh = template.tbh;
	}
	
	public Table copy() {
		return new Table(this);
	}

	public void beginsTableBody() {
		//FIXME: add check here that the table header has complete rows, i.e. no open rowspans 
		headerRows = td.getRowCount();
	}

	public void beginsTableRow() {
		td.beginsTableRow();
	}
	
	void closeTable() {
		td.closeTable();
	}
	
	@Override
	boolean isEmpty() {
		return td.isEmpty();
	}

	public FormatterCore beginsTableCell(TableCellProperties props) {
		return td.beginsTableCell(props);
	}

	@Override
	public void addSegment(TextSegment s) {
		super.addSegment(s);
		((FormatterCoreImpl)td.getCurrentCell()).getCurrentBlock().addSegment(s);
	}
	
	@Override
	public void addSegment(Segment s) {
		super.addSegment(s);
		((FormatterCoreImpl)td.getCurrentCell()).getCurrentBlock().addSegment(s);
	}

	@Override
	protected AbstractBlockContentManager newBlockContentManager(BlockContext context) {
		int columnCount = countColumns();
		int rowCount = countRows();
		int[] colSpace = calcSpacings(new ColumnSpaceCalculator(rowCount, columnCount));
		//int[] rowSpace = calcSpacings(new RowSpaceCalculator(rowCount, columnCount));
		MarginProperties leftMargin = rdp.getLeftMargin().buildMargin(context.getFcontext().getSpaceCharacter());
		MarginProperties rightMargin = rdp.getRightMargin().buildMargin(context.getFcontext().getSpaceCharacter());
		int maxWidth = (context.getFlowWidth() 
				- leftMargin.getContent().length() 
				- rightMargin.getContent().length()
				);
		int columnWidth = maxWidth;
		for (int i : colSpace) {
			columnWidth -= i; 
		}
		//columnWidth = columnWidth / columnCount;
		int[] currentColumnWidth = new int[columnCount];
		Arrays.fill(currentColumnWidth, columnWidth);
		DefaultContext dc = DefaultContext.from(context).metaVolume(metaVolume).metaPage(metaPage).build();
		resultCache = new HashMap<>();
		Result r = minimizeCost(maxWidth, currentColumnWidth, colSpace, tableProps.getPreferredEmtpySpace(), context, dc, leftMargin, rightMargin);
		return new TableBlockContentManager(context.getFlowWidth(), r.minWidth, r.forceCount, r.rows, rdp, context.getFcontext());
	}
	
	private Result minimizeCost(int maxTableWidth, int[] columnWidth, int[] colSpacing, int spacePreferred, BlockContext context, DefaultContext dc, MarginProperties leftMargin, MarginProperties rightMargin) {
		int columnCount = columnWidth.length;
		int[] currentColumnWidth = Arrays.copyOf(columnWidth, columnWidth.length);
		Result[] results = new Result[columnCount];
		//base result
		Result currentResult = renderTableWithCache(tableProps.getPreferredEmtpySpace(), currentColumnWidth, colSpacing, context, dc, leftMargin, rightMargin);
		int x = 0;
		while (true) {
			// render all possibilities
			for (int i=0; i<columnCount; i++) {
				if (currentColumnWidth[i]>=1) {
					// change value
					currentColumnWidth[i] = currentColumnWidth[i] - 1;
					try {
						results[i] = renderTableWithCache(spacePreferred, currentColumnWidth, colSpacing, context, dc, leftMargin, rightMargin);
					} catch (Exception e) {
						// if rendering fails, invalidate this solution but continue
						results[i] = null;
					}
					// restore value
					currentColumnWidth[i] = currentColumnWidth[i] + 1;
				} else {
					results[i] = null;
				}
			}
			// select
			int tableWidth = 0;
			for (int j=0; j<td.getGridWidth(); j++) {
				if (j>0) {
					tableWidth += colSpacing[j-1];
				}
				tableWidth += currentColumnWidth[j];
			}
			Result min;
			if (tableWidth<=maxTableWidth) {
				min = min(currentResult, results);
			} else {
				//since reduction is required at this point, choose the best new solution (even if it is worse than the current)
				int start = x;
				while (results[x]==null) {
					x = (x+1) % columnCount;
					if (x==start) {
						throw new RuntimeException("Failed to solve table.");
					}
				}
				min = min(results[x], results);
				x = (x+1) % columnCount;
			}
			if (min==currentResult && tableWidth<=maxTableWidth) {
				break;
			} else {
				currentResult = min;
				currentColumnWidth = min.widths;
			} 
		}
		return currentResult;
	}
	
	private static class Result {
		List<RowImpl> rows;
		TableCost cost;
		int[] widths;
		int minWidth;
		int forceCount;
	}
	
	private static Result min(Result v, Result ... values) {
		if (values.length<1) {
			throw new IllegalArgumentException("No values");
		}
		Result ret = v;
		for (int i=0; i<values.length; i++) {
			//System.out.println("COST: " + values[i].cost.getCost());
			//if new value is less than existing value, replace it
			Result x = values[i];
			if (x!=null) {
				ret = x.cost.getCost()<ret.cost.getCost()?x:ret;
			}
		}
		return ret;
	}
	
	private Result renderTableWithCache(int spacePreferred, int[] columnWidth, int[] colSpacing, BlockContext context, DefaultContext dc, MarginProperties leftMargin, MarginProperties rightMargin) {
		String key = toKey(columnWidth);
		Result r = null;
		if (resultCache.containsKey(key)) {
			// r may be null afterwards
			r = resultCache.get(key);
			logger.finest("Using cached result with key: " + key);
		} else {
			logger.finest("Calculating new result for key: " + key);
			try {
				r = renderTable(columnWidth, colSpacing, context, dc, leftMargin, rightMargin, spacePreferred); 
				logger.finest("Cost for solution: " + r.cost.getCost());
			} finally {
				// Also put failing results in the resultCache to prevent them from being attempted again (this is why finally is used)
				resultCache.put(toKey(columnWidth), r);
			}			
		}
		return r;
	}
	
	private String toKey(int...values) {
		StringBuilder ret = new StringBuilder();
		for (int v : values) {
			ret.append(v).append(",");
		}
		return ret.toString();
	}
	
	private Result renderTable(int[] columnWidth, int[] colSpacing, BlockContext context, DefaultContext dc, MarginProperties leftMargin, MarginProperties rightMargin, int spacePreferred) {
		List<RowImpl> result = new ArrayList<RowImpl>();
		Result ret = updateRendering(columnWidth, colSpacing, new TableCostImpl(spacePreferred), context, dc);
		for (int r=0; r<td.getGridHeight(); r++) {
			// render into rows
			boolean tableRowHasData = false;
			while (hasMoreContent(r)) { //while content
				RowImpl row = getResultRow(r, context, columnWidth, colSpacing, leftMargin, rightMargin, FillStyle.EMPTY, false);
				//TODO: this will keep the whole table row together (if possible), but it could be more advanced
				result.add(row);
				tableRowHasData = true;
			}
			boolean addBorder = hasBorderItems(r);
			if (addBorder && r<td.getGridHeight()-1) {
				// row borders
				if (tableProps.getTableRowSpacing()>0) {
					{
						// separate, do this border
						RowImpl row = getResultRow(r, context, columnWidth, colSpacing, leftMargin, rightMargin, FillStyle.THIS_BORDER, true);
						if (row!=null) { result.add(row); }
					}{
						// space
						RowImpl row = getResultRow(r, context, columnWidth, colSpacing, leftMargin, rightMargin, FillStyle.EMPTY, true);
						if (row!=null) { result.add(row); }
					}
				} else {
					// merged
					RowImpl row = getResultRow(r, context, columnWidth, colSpacing, leftMargin, rightMargin, FillStyle.MERGE, true);
					//row.setAllowsBreakAfter(false);
					if (row!=null) { result.add(row); }
				}
			}
			if (tableRowHasData) {
				// modify last row
				result.add(new RowImpl.Builder(result.remove(result.size()-1)).allowsBreakAfter(true).build());
			}
			if (addBorder && r<td.getGridHeight()-1 && tableProps.getTableRowSpacing()>0) {
				// separate, do next border
				RowImpl row = getResultRow(r, context, columnWidth, colSpacing, leftMargin, rightMargin, FillStyle.NEXT_BORDER, true);
				if (row!=null) { result.add(row); }
			}
		}
		ret.cost.completeTable(result, td.getGridWidth());
		ret.rows = result;
		ret.widths = Arrays.copyOf(columnWidth, columnWidth.length);
		return ret;
	}
	
	private Result updateRendering(int[] columnWidth, int[] colSpacing, TableCost costFunc, BlockContext context, DefaultContext dc) {
		Result ret = new Result();
		ret.cost = costFunc;
		int minWidth = context.getFlowWidth();
		int forceCount = 0;
		for (TableRow row : td) {
			for (TableCell cell : row) {
				int flowWidth = 0;
				int ci = cell.getInfo().getStartingPoint().getCol();
				for (int j=0; j<cell.getInfo().getColSpan(); j++) {
					if (j>0) {
						flowWidth += colSpacing[ci-1+j];
					}
					flowWidth += columnWidth[ci+j];
				}
				CellData cd = cell.render(context.getFcontext(), dc, context.getRefs(), flowWidth);
				minWidth = Math.min(cd.getMinWidth(), minWidth);
				forceCount += cd.getForceCount();
				costFunc.addCell(cd.getRows(), flowWidth, cd.getForceCount());
			}
		}
		ret.forceCount = forceCount;
		ret.minWidth = minWidth;
		return ret;
	}
	
	private boolean hasMoreContent(int r) {
		CellData cr;
		//This seems redundant, but the row iterator is different each time we're here
		//and we need to know beforehand if there is any content left
		for (int x=0; x<td.getGridWidth(); x++) {
			cr = td.cellForGrid(r, x).getRendered();
			// allow row change if the cell ends in another grid row
			if (cr.getInfo().getEndPoint().getRow()<=r && cr.getRowIterator().hasNext()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasBorderItems(int r) {
		TableCell cx, cy;
		for (int x=0; x<td.getGridWidth(); x++) {
			cx = td.cellForGrid(r, x);
			cy = td.cellForGrid(r+1, x);
			if ((	cx!=null &&
					cx.getInfo().getEndPoint().getRow()==r && 
					cx.getInfo().getBorder()!=null &&
					cx.getInfo().getBorder().getBottom().getStyle()!=Style.NONE) ||
				(	cy!=null &&
					cy.getInfo().getStartingPoint().getRow()==r+1 &&
					cy.getInfo().getBorder()!=null &&
					cy.getInfo().getBorder().getTop().getStyle()!=Style.NONE)
				) {
					return true;
			}
		}
		return false;
	}
	
	enum FillStyle {
		EMPTY,
		THIS_BORDER,
		NEXT_BORDER,
		MERGE
	}
	
	private RowImpl getResultRow(int r, BlockContext context, int[] columnWidth, int[] colSpacing, MarginProperties leftMargin, MarginProperties rightMargin, FillStyle f, boolean allowsBreakAfter) {
		CellData cr;
		StringBuilder tableRow = new StringBuilder();
		List<Marker> markers = new ArrayList<>();
		List<String> anchors = new ArrayList<>();
		List<String> identifiers = new ArrayList<>();
		for (int j=0; j<td.getGridWidth(); j++) {
			cr = td.cellForGrid(r, j).getRendered();
			String data = "";
			if (cr.getRowIterator().hasNext()) {
				RowImpl row = cr.getRowIterator().next();
				// Align
				data = BorderManager.padLeft(cr.getCellWidth(), row, context.getFcontext().getSpaceCharacter());
				markers.addAll(row.getMarkers());
				anchors.addAll(row.getAnchors());
				identifiers.addAll(row.getIdentifiers());
			} else {
				StringBuilder d = new StringBuilder();
				Border cx = null, cy = null;
				for (int x=j; x<cr.getInfo().getColSpan()+j; x++) {
					char fill = context.getFcontext().getSpaceCharacter();
					if (f==FillStyle.EMPTY) {
						cx = null;
						cy = null;
					} else {
						if (f==FillStyle.THIS_BORDER) {
							cx = td.cellForGrid(r, x).getInfo().getBorder();
							cy = null;
						} else if (f==FillStyle.NEXT_BORDER) {
							cx = null;
							cy = td.cellForGrid(r+1, x).getInfo().getBorder();
						} else if (f==FillStyle.MERGE) {
							cx = td.cellForGrid(r, x).getInfo().getBorder();
							cy = td.cellForGrid(r+1, x).getInfo().getBorder();
						}
						String t = tbh.mergedRowStyles(cx, cy);
						if (t.length()>1) {
							logger.warning("Expected a single border character, the result will be incorrect.");
						}
						if (t.length()>=1) {
							fill = t.charAt(0);
						}
						d.append(StringTools.fill(fill, columnWidth[x]));
					}
				}
				data = d.toString();
			}
			tableRow.append(data);
			// Fill (only after intermediary columns)
			if (cr.getInfo().getEndPoint().getCol()+1<td.getGridWidth()) {
				TableCell c = td.cellForGrid(cr.getInfo().getStartingPoint().getRow(), cr.getInfo().getEndPoint().getCol()+1);
				String border;
				if (c==null) {
					border = "";
				} else {
					border = tbh.getSharedColumnString(cr.getInfo().getBorder(), c.getInfo().getBorder(), context);
				}
				//FIXME: length calculation incorrect if border is separated
				int length = cr.getCellWidth()+colSpacing[cr.getInfo().getEndPoint().getCol()] - data.length() - border.length();
				if (length>0) {
					tableRow.append(StringTools.fill(context.getFcontext().getSpaceCharacter(), length));
				}
				tableRow.append(border);
			}
			j += cr.getInfo().getColSpan()-1;
		}
		RowImpl row = new RowImpl.Builder(tableRow.toString()).leftMargin(leftMargin).rightMargin(rightMargin)
				.rowSpacing(tableProps.getRowSpacing())
				.adjustedForMargin(true)
				.allowsBreakAfter(allowsBreakAfter)
				.addMarkers(markers)
				.addAnchors(anchors)
				.addIdentifiers(identifiers)
				.build();
		return row;
	}

	private int[] calcSpacings(GridSpaceCalculator comp) {
		Iterators it = comp.getIterator();
		int[] ret = new int[it.inner-1];
		Arrays.fill(ret, 0);
		TableCell cell1;
		for (TableRow r : td) {
			int index = -1;
			cell1 = null;
			for (TableCell c : r) {
				if (cell1!=null) {
					index += Math.max(comp.getSpan(cell1), 1);
					ret[index] = Math.max(ret[index], comp.getSpacingValue(cell1, c));
				}
				cell1 = c;
			}
		}
		return ret;
	}
	
	private static class Iterators {
		@SuppressWarnings("unused")
		private final int inner, outer;
		Iterators(int outer, int inner) {
			this.outer = outer;
			this.inner = inner;
		}
	}
	
	private static interface GridSpaceCalculator {
		Iterators getIterator();
		TableCell getFirstCell(int outer, int inner);
		TableCell getSecondCell(int outer, int inner);
		int getSpan(TableCell cell);
		int getSpacingValue(TableCell cell1, TableCell cell2);
	}
	
	private class ColumnSpaceCalculator implements GridSpaceCalculator {
		private final int rowCount, colCount;
		
		public ColumnSpaceCalculator(int rowCount, int colCount) {
			this.rowCount = rowCount;
			this.colCount = colCount;
		}
		
		@Override
		public Iterators getIterator() {
			return new Iterators(rowCount, colCount);
		}

		@Override
		public TableCell getFirstCell(int outer, int inner) {
			return td.getRow(outer).getCell(inner);
		}
		
		public BorderSpecification getBorderBefore(Border border) {
			return border.getLeft();
		}

		public BorderSpecification getBorderAfter(Border border) {
			return border.getRight();
		}

		@Override
		public TableCell getSecondCell(int outer, int inner) {
			return td.getRow(outer).getCell(inner+1);
		}

		@Override
		public int getSpan(TableCell cell) {
			return cell.getInfo().getColSpan();
		}

		public int getTableSpacing() {
			return tableProps.getTableColSpacing();
		}
		
		@Override
		public int getSpacingValue(TableCell cell1, TableCell cell2) {
			int b1 = cell1.getInfo().getBorder()!=null && getBorderAfter(cell1.getInfo().getBorder()).getStyle()!=Style.NONE?1:0;
			int b2 = cell2.getInfo().getBorder()!=null && getBorderBefore(cell2.getInfo().getBorder()).getStyle()!=Style.NONE?1:0;
			if (getTableSpacing()>0) {
				return b1 + b2 + getTableSpacing();					
			} else {
				return Math.max(b1, b2);
			}
		}
	}

	@SuppressWarnings("unused")
	private class RowSpaceCalculator implements GridSpaceCalculator {
		private final int rowCount, colCount;
		public RowSpaceCalculator(int rowCount, int colCount) {
			this.rowCount = rowCount;
			this.colCount = colCount;
		}
		@Override
		public Iterators getIterator() {
			return new Iterators(colCount, rowCount);
		}

		@Override
		public TableCell getFirstCell(int outer, int inner) {
			return td.getRow(inner).getCell(outer);
		}

		public BorderSpecification getBorderBefore(Border border) {
			return border.getTop();
		}

		public BorderSpecification getBorderAfter(Border border) {
			return border.getBottom();
		}

		@Override
		public TableCell getSecondCell(int outer, int inner) {
			return td.getRow(inner+1).getCell(outer);
		}

		@Override
		public int getSpan(TableCell cell) {
			return cell.getInfo().getRowSpan();
		}

		public int getTableSpacing() {
			return tableProps.getTableRowSpacing();
		}
		
		@Override
		public int getSpacingValue(TableCell cell1, TableCell cell2) {
			int b1 = cell1.getInfo().getBorder()!=null && getBorderAfter(cell1.getInfo().getBorder()).getStyle()!=Style.NONE?1:0;
			int b2 = cell2.getInfo().getBorder()!=null && getBorderBefore(cell2.getInfo().getBorder()).getStyle()!=Style.NONE?1:0;
			if (getTableSpacing()>0) {
				return b1 + b2 + getTableSpacing();					
			} else {
				return Math.max(b1, b2);
			}
		}
		
	}

	private int countColumns() {
		int cc = 0;
		// calculate the number of columns based on the first row
		// if subsequent rows differ, report it as an error
		for (TableCell c : td.getRow(0)) {
			cc += Math.max(c.getInfo().getColSpan(), 1);
		}
		return cc;
	}
	
	private int countRows() {
		int cc = 0;
		// calculate the number of rows based on the first colum
		// if subsequent columns differ, report it as an error
		for (TableRow r : td) {
			cc += Math.max(r.getCell(0).getInfo().getRowSpan(), 1);
		}
		return cc;
	}

}
