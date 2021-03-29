package org.daisy.pipeline.braille.css.saxon.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.PseudoClass;
import cz.vutbr.web.css.Selector.SelectorPart;
import cz.vutbr.web.csskit.AbstractRuleBlock;

import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.InlineStyle.RuleMainBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativeBlock;
import org.daisy.braille.css.SelectorImpl;
import org.daisy.braille.css.SelectorImpl.PseudoClassImpl;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;
import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.common.stax.XMLStreamWriterHelper.ToStringWriter;
import org.daisy.common.stax.XMLStreamWriterHelper.WriterEvent;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;
import org.daisy.pipeline.braille.common.util.Strings;
import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;

public class TableAsList extends SingleInSingleOutXMLTransformer {

	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";

	private static final QName _STYLE = new QName("style");
	private static final QName _ID = new QName("id");

	private static final String XMLNS_HTML = "http://www.w3.org/1999/xhtml";
	private static final String XMLNS_DTB = "http://www.daisy.org/z3986/2005/dtbook/";

	private static final String TABLE = "table";
	private static final String THEAD = "thead";
	private static final String TFOOT = "tfoot";
	private static final String TBODY = "tbody";
	private static final String TR = "tr";
	private static final String TD = "td";
	private static final String TH = "th";
	private static final String COLGROUP = "colgroup";
	private static final String COL = "col";

	private static final QName _HEADERS = new QName("headers");
	private static final QName _SCOPE = new QName("scope");
	private static final QName _AXIS = new QName("axis");
	private static final QName _ROWSPAN = new QName("rowspan");
	private static final QName _COLSPAN = new QName("colspan");

	private static final QName CSS_TABLE_HEADER_POLICY = new QName(XMLNS_CSS, "table-header-policy", "css");
	private static final QName CSS_TABLE_BY = new QName(XMLNS_CSS, "table-by", "css");
	private static final QName CSS_LIST_ITEM = new QName(XMLNS_CSS, "list-item", "css");
	private static final QName CSS_LIST_HEADER = new QName(XMLNS_CSS, "list-header", "css");
	private static final QName CSS_ID = new QName(XMLNS_CSS, "id", "css");
	private static final QName CSS_FLOW = new QName(XMLNS_CSS, "flow", "css");

	private static final Splitter HEADERS_SPLITTER = Splitter.on(' ').trimResults().omitEmptyStrings();
	private static final Splitter AXIS_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

	final List<String> axes;

	TableAsList(String axes) {
		this.axes = new ArrayList<String>(AXIS_SPLITTER.splitToList(axes));
		if (this.axes.remove("auto"))
			if (!this.axes.isEmpty())
				throw new RuntimeException();
	}

	private List<WriterEvent> writeActionsBefore;
	private List<WriterEvent> writeActionsAfter;
	private List<TableCell> cells;
	private Set<CellCoordinates> coveredCoordinates;

	@Override
	public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params)
			throws IllegalArgumentException {

		if (source == null || result == null)
			throw new IllegalArgumentException();
		return () -> transform(source.ensureSingleItem().asXMLStreamReader(), result.asXMLStreamWriter());
	}

	private void transform(BaseURIAwareXMLStreamReader reader, BaseURIAwareXMLStreamWriter writer) {
		try {
			writeActionsBefore = new ArrayList<WriterEvent>();
			writeActionsAfter = new ArrayList<WriterEvent>();
			cells = new ArrayList<TableCell>();
			coveredCoordinates = new HashSet<CellCoordinates>();
			List<WriterEvent> writeActions = writeActionsBefore;
			int depth = 0;
			TableCell withinCell = null;
			TableCell.RowType rowType = TableCell.RowType.TBODY;
			int rowGroup = 1;
			int row = 1;
			int col = 1;
			String namespace = null;
			Deque<SimpleInlineStyle> inheritedStyle = new LinkedList<>();
			while (reader.hasNext()) {
				switch (reader.next()) {
					case START_ELEMENT: {
						QName name = reader.getName();
						depth++;
						boolean isCell = false;
						if (depth == 1) {
							if (!isHTMLorDTBookElement(TABLE, name))
								throw new RuntimeException("Expected table element (html|dtb).");
							if (XMLNS_HTML.equals(name.getNamespaceURI()))
								namespace = XMLNS_HTML;
							else if (XMLNS_DTB.equals(name.getNamespaceURI()))
								namespace = XMLNS_DTB; }
						else if (isHTMLorDTBookElement(THEAD, name) ||
						         isHTMLorDTBookElement(TFOOT, name) ||
						         isHTMLorDTBookElement(TBODY, name) ||
						         isHTMLorDTBookElement(TR, name)) {
							rowType = isHTMLorDTBookElement(THEAD, name)
								? TableCell.RowType.THEAD
								: isHTMLorDTBookElement(TFOOT, name)
									? TableCell.RowType.TFOOT
									: isHTMLorDTBookElement(TBODY, name)
										? TableCell.RowType.TBODY
										: rowType;
										;
							String style = null; {
								for (int i = 0; i < reader.getAttributeCount(); i++) {
									if (_STYLE.equals(reader.getAttributeName(i))) {
										style = reader.getAttributeValue(i);
										break; }}}
							// FIXME: if (non-default) block-level style present, warn that style is ignored
							// FIXME: We assume that not both a tbody/thead/tfoot and a child tr have a text-transform property,
							// because two text-transform properties can not always simply be replaced with a single one.
							inheritedStyle.push(new SimpleInlineStyle(style, inheritedStyle.isEmpty() ? null : inheritedStyle.peek()));
							break; }
						else if (isHTMLorDTBookElement(COLGROUP, name) || isHTMLorDTBookElement(COL, name))
							throw new RuntimeException("Elements colgroup and col not supported yet.");
						if (isHTMLorDTBookElement(TD, name) || isHTMLorDTBookElement(TH, name)) {
							isCell = true;
							withinCell = new TableCell();
							withinCell.row = row;
							withinCell.col = col;
							withinCell.rowGroup = rowGroup;
							withinCell.rowType = rowType;
							withinCell.ns = namespace;
							setCovered(row, col);
							cells.add(withinCell);
							if (isHTMLorDTBookElement(TH, name))
								withinCell.type = TableCell.CellType.TH;
							String style = null; {
								for (int i = 0; i < reader.getAttributeCount(); i++) {
									if (_STYLE.equals(reader.getAttributeName(i))) {
										style = reader.getAttributeValue(i);
										break; }}}
							withinCell.style = new SimpleInlineStyle(style, inheritedStyle.isEmpty() ? null : inheritedStyle.peek());
							writeActions = withinCell.content; }
						else {
							if (withinCell != null) {
								String flow = "normal";
								for (int i = 0; i < reader.getAttributeCount(); i++)
									if (CSS_FLOW.equals(reader.getAttributeName(i))) {
										flow = reader.getAttributeValue(i);
										break; }
								if (!"normal".equals(flow)) {
									writeActions.add(writeElementOnce(reader));
									break; }}
							writeActions.add(w -> writeStartElement(w, name)); }
						for (int i = 0; i < reader.getNamespaceCount(); i++) {
							String prf = reader.getNamespacePrefix(i);
							String ns = reader.getNamespaceURI(i);
							writeActions.add(w -> w.writeNamespace(prf, ns)); }
						for (int i = 0; i < reader.getAttributeCount(); i++) {
							QName attrName = reader.getAttributeName(i);
							String attrValue = reader.getAttributeValue(i);
							if (CSS_ID.equals(attrName)) {
								WriteOnlyOnce writeOnlyOnce = new WriteOnlyOnce();
								writeOnlyOnce.add(w -> writeAttribute(w, attrName, attrValue));
								writeActions.add(writeOnlyOnce); }
							else if (CSS_TABLE_HEADER_POLICY.equals(attrName)) {
								if (isCell)
									if ("once".equals(attrValue))
										withinCell.headerPolicy = TableCell.HeaderPolicy.ONCE;
									else if ("always".equals(attrValue))
										withinCell.headerPolicy = TableCell.HeaderPolicy.ALWAYS;
									else if ("front".equals(attrValue))
										withinCell.headerPolicy = TableCell.HeaderPolicy.FRONT;
									else
										throw new RuntimeException(
											"Expected value once|always for table-header-policy property but got " + attrValue); }
							else if (isCell && _HEADERS.equals(attrName))
								withinCell.headers = HEADERS_SPLITTER.splitToList(attrValue);
							else if (isCell && _SCOPE.equals(attrName)) {
								if ("row".equals(attrValue))
									withinCell.scope = TableCell.Scope.ROW;
								else if ("col".equals(attrValue))
									withinCell.scope = TableCell.Scope.COL;
								else if ("colgroup".equals(attrValue) || "rowgroup".equals(attrValue))
									throw new RuntimeException(
											"Value " + attrValue + " for scope attribute not supported yet.");
								else
									throw new RuntimeException(
											"Expected value col|row|colgroup|rowgroup for scope attribute but got " + attrValue); }
							else if (isCell && _AXIS.equals(attrName))
								withinCell.axis = AXIS_SPLITTER.splitToList(attrValue);
							else if (isCell && _ROWSPAN.equals(attrName)) {
								int rowspan = nonNegativeInteger(attrValue);
								if (rowspan == 0)
									throw new RuntimeException("rowspan 0 not supported yet.");
								withinCell.rowspan = rowspan;
								for (int m = 1; m < rowspan; m++)
									for (int n = 0; n < withinCell.colspan; n++)
										setCovered(row + m, col + n); }
							else if (isCell && _COLSPAN.equals(attrName)) {
								int colspan = nonNegativeInteger(attrValue);
								if (colspan == 0)
									throw new RuntimeException("colspan 0 not supported yet.");
								withinCell.colspan = colspan;
								for (int m = 0; m < withinCell.rowspan; m++)
									for (int n = 1; n < withinCell.colspan; n++)
										setCovered(row + m, col + n); }

							// TODO: check that there are no duplicate IDs?
							else if (isCell && _ID.equals(attrName))
								withinCell.id = attrValue;
							else if (depth == 1 && _STYLE.equals(attrName)) {
								String newStyle; {
									InlineStyle style = new InlineStyle(attrValue);
									List<RuleBlock<? extends Rule<?>>> builder = new ArrayList<>();
									for (RuleBlock<?> block : style) {
										if (block instanceof RuleMainBlock)
											builder.add(style.getMainStyle());
										else if (block instanceof RuleRelativeBlock) {
											RuleRelativeBlock ruleblock = (RuleRelativeBlock)block;
											List<Selector> selector = ruleblock.getSelector();
											if (selector.size() > 0) { // should always be true
												if (selector.get(0).size() > 0) { // should always be true
													// the first part is normally a PseudoElementImpl, i.e. a pseudo element or a custom
													// pseudo class like :-obfl-alternate-scenario
													// other parts are possible too, but will in practice already have been processed
													// by css:inline
													if (selector.get(0).get(0) instanceof PseudoElementImpl) {
														// selector.get(0).size() should normally always be 1
														// pseudo classes and pseudo elements are stacked onto the first pseudo element,
														// and for other parts (classes or attributes) it does not makes sense to come after
														// a pseudo element
														PseudoElementImpl pseudo = (PseudoElementImpl)selector.get(0).get(0);
														if ("list-header".equals(pseudo.getName())) {
															if (pseudo.getPseudoClasses().isEmpty())
																addListHeaderStyle(
																	new ListItemStyle(rest(ruleblock))); }
														else if ("table-by".equals(pseudo.getName())) {
															String axis = pseudo.getArguments()[0];
															if (pseudo.getPseudoClasses().isEmpty()) {
																if (pseudo.hasStackedPseudoElement()) {
																	pseudo = pseudo.getStackedPseudoElement();
																	ruleblock = (RuleRelativeBlock)rest(ruleblock);
																	if ("list-item".equals(pseudo.getName()))
																		getTableByStyle(axis).addListItemStyle(
																			pseudo.getPseudoClasses(),
																			new ListItemStyle(rest(ruleblock)));
																	else if ("list-header".equals(pseudo.getName())) {
																		if (pseudo.getPseudoClasses().isEmpty())
																			getTableByStyle(axis).addListHeaderStyle(
																				new ListItemStyle(rest(ruleblock))); }
																	else
																		getTableByStyle(axis).addRuleBlock(ruleblock); }
																else
																	getTableByStyle(axis).addRuleBlock(rest(ruleblock)); }}
														else
															// could be some other pseudo element or a custom pseudo class like
															// :-obfl-alternate-scenario (yes, it is implemented as a PseudoElementImpl even
															// though it is a class)
															// in the case a ::list-item, ::list-header or ::table-by follows later in the
															// selector, they will be handled later in a subsequent call to css:render-table-by,
															// after the pseudo elements/classes have been handled elsewhere
															builder.add(ruleblock); }
													else
														builder.add(ruleblock); }
												else
													builder.add(ruleblock); }}
										else
											throw new RuntimeException("Unexpected style " + block); }
									newStyle = serializeRuleBlockList(builder); }
								if (!newStyle.isEmpty())
									writeActions.add(w -> writeAttribute(w, attrName, newStyle)); }
							else if (isCell && _STYLE.equals(attrName))
								; // handled above
							else
								writeActions.add(w -> writeAttribute(w, attrName, attrValue)); }
						break; }
					case CHARACTERS:
						String chars = reader.getText();
						writeActions.add(w -> w.writeCharacters(chars));
						break;
					case END_ELEMENT: {
						QName name = reader.getName();
						depth--;
						if (isHTMLorDTBookElement(THEAD, name)
						    || isHTMLorDTBookElement(TFOOT, name)
						    || isHTMLorDTBookElement(TBODY, name)) {
							inheritedStyle.pop();
							rowGroup++;
							break; }
						else if (isHTMLorDTBookElement(TR, name)) {
							inheritedStyle.pop();
							row++;
							col = 1;
							while (isCovered(row, col)) col++;
							break; }
						if (isHTMLorDTBookElement(TD, name) || isHTMLorDTBookElement(TH, name)) {
							withinCell = null;
							writeActions = writeActionsAfter;
							while (isCovered(row, col)) col++; }
						else
							writeActions.add(w -> w.writeEndElement());
						break; }}}

			// handle colspan and rowspan on data cells by simply splitting them into several identical ones for now
			List<TableCell> moreCells = new ArrayList<TableCell>();
			for (TableCell c : cells)
				if (!isHeader(c)) {
					if (c.rowspan > 1) {
						int span = c.rowspan;
						c.rowspan = 1;
						for (int i = 1; i < span; i++) {
							TableCell dup = c.clone();
							dup.row = c.row + i;
							moreCells.add(dup); }}
					if (c.colspan > 1) {
						int span = c.colspan;
						c.colspan = 1;
						for (int i = 1; i < span; i++) {
							TableCell dup = c.clone();
							dup.col = c.col + i;
							moreCells.add(dup); }}}
			cells.addAll(moreCells);

			// rearrange row groups and order cells by row
			Collections.sort(cells, compose(sortByRowType, sortByRow, sortByColumn));
			rowGroup = 0;
			row = 0;
			int newRowGroup = rowGroup = 0;
			int newRow = row = 0;
			for (TableCell c : cells) {
				if (c.rowGroup != rowGroup) {
					rowGroup = c.rowGroup;
					newRowGroup++; }
				if (c.row != row) {
					row = c.row;
					newRow++; }
				c.rowGroup = newRowGroup;
				c.row = newRow; }

			write(writer);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isHTMLorDTBookElement(String element, QName name) {
		return ((XMLNS_HTML.equals(name.getNamespaceURI())
		        || XMLNS_DTB.equals(name.getNamespaceURI()))
		        && name.getLocalPart().equalsIgnoreCase(element));
	}

	private void setCovered(int row, int col) {
		CellCoordinates coords = new CellCoordinates(row, col);
		if (coveredCoordinates.contains(coords))
			throw new RuntimeException("Table structure broken: cells overlap");
		coveredCoordinates.add(coords);
	}

	private boolean isCovered(int row, int col) {
		return coveredCoordinates.contains(new CellCoordinates(row, col));
	}

	private void write(XMLStreamWriter writer) throws XMLStreamException {
		for (WriterEvent action : writeActionsBefore)
			action.writeTo(writer);
		List<TableCell> dataCells = new ArrayList<TableCell>();
		for (TableCell c : cells)
			if (!isHeader(c))
				dataCells.add(c);
		new TableCellGroup(dataCells, axes.iterator()).write(writer);
		for (WriterEvent action : writeActionsAfter)
			action.writeTo(writer);
	}

	private static final List<TableCell> emptyList = new ArrayList<TableCell>();

	private abstract class TableCellCollection {
		public abstract List<TableCell> newlyRenderedHeaders();
		public abstract List<TableCell> newlyPromotedHeaders();
		public abstract void write(XMLStreamWriter writer) throws XMLStreamException;
	}

	private class SingleTableCell extends TableCellCollection {

		private final TableCell cell;
		private final TableCellGroup parent;
		private final SingleTableCell precedingSibling;
		private final boolean columnAppliedBeforeRow;

		private SingleTableCell(TableCell cell, TableCellGroup parent, SingleTableCell precedingSibling, boolean columnAppliedBeforeRow) {
			this.cell = cell;
			this.parent = parent;
			this.precedingSibling = precedingSibling;
			this.columnAppliedBeforeRow = columnAppliedBeforeRow;
		}

		private List<TableCell> newlyAppliedHeaders;
		public List<TableCell> newlyAppliedHeaders() {
			if (newlyAppliedHeaders == null) {
				newlyAppliedHeaders = new ArrayList<TableCell>();
				for (TableCell h : findHeaders(cell, !columnAppliedBeforeRow))
					if (!parent.appliedHeaders().contains(h))
						newlyAppliedHeaders.add(h); }
			return newlyAppliedHeaders;
		}

		private List<TableCell> newlyRenderedOrPromotedHeaders;
		private List<TableCell> newlyRenderedOrPromotedHeaders() {
			if (newlyRenderedOrPromotedHeaders == null) {
				newlyRenderedOrPromotedHeaders = new ArrayList<TableCell>();
				Iterator<TableCell> lastAppliedHeaders = (
					precedingSibling != null ? precedingSibling.newlyAppliedHeaders() : emptyList
				).iterator();
				boolean canOmit = true;
				for (TableCell h : parent.deferredHeaders()) {
					newlyRenderedOrPromotedHeaders.add(h);
					canOmit = false; }
				for (TableCell h : newlyAppliedHeaders()) {
					if (canOmit
					    && h.headerPolicy != TableCell.HeaderPolicy.ALWAYS
					    && lastAppliedHeaders.hasNext() && lastAppliedHeaders.next().equals(h))
						continue;
					newlyRenderedOrPromotedHeaders.add(h);
					canOmit = false; }}
			return newlyRenderedOrPromotedHeaders;
		}

		private List<TableCell> newlyPromotedHeaders;
		public List<TableCell> newlyPromotedHeaders() {
			if (newlyPromotedHeaders == null) {
				newlyPromotedHeaders = new ArrayList<TableCell>();
				int i = newlyRenderedOrPromotedHeaders().size() - 1;
				while (i >= 0 && newlyRenderedOrPromotedHeaders().get(i).headerPolicy != TableCell.HeaderPolicy.FRONT)
					i--;
				while (i >= 0)
					newlyPromotedHeaders.add(0, newlyRenderedOrPromotedHeaders().get(i--)); }
			return newlyPromotedHeaders;
		}

		private List<TableCell> newlyRenderedHeaders;
		public  List<TableCell> newlyRenderedHeaders() {
			if (newlyRenderedHeaders == null) {
				newlyRenderedHeaders = new ArrayList<TableCell>();
				int i = newlyRenderedOrPromotedHeaders().size() - 1;
				while (i >= 0 && newlyRenderedOrPromotedHeaders().get(i).headerPolicy != TableCell.HeaderPolicy.FRONT)
					newlyRenderedHeaders.add(0, newlyRenderedOrPromotedHeaders().get(i--)); }
			return newlyRenderedHeaders;
		}

		public void write(XMLStreamWriter writer) throws XMLStreamException {
			cell.write(writer);
		}

		@Override
		public String toString() {
			ToStringWriter xml = new ToStringWriter();
			try {
				write(xml); }
			catch (Exception e) {
				throw new RuntimeException("coding error", e); }
			StringBuilder s = new StringBuilder();
			s.append("SingleTableCell[header: ").append(newlyRenderedHeaders());
			s.append(", cell: ").append(cell);
			s.append(", xml: ").append(xml).append("]");
			return s.toString();
		}
	}

	private class TableCellGroup extends TableCellCollection {

		private final TableCellGroup parent;
		private final TableCell groupingHeader;
		private final String groupingAxis;
		private final TableCellGroup precedingSibling;
		private final boolean hasSubGroups;
		private final String subGroupingAxis;
		private final List<TableCellCollection> children;
		private final boolean rowApplied;
		private final boolean columnAppliedBeforeRow;

		private TableCellGroup(List<TableCell> cells, Iterator<String> nextAxes) {
			this(cells, nextAxes, null, null, null, null, false, false);
		}

		private TableCellGroup(List<TableCell> cells, Iterator<String> nextAxes,
		                       TableCellGroup parent, TableCell groupingHeader, String groupingAxis,
		                       TableCellGroup precedingSibling,
		                       boolean rowApplied, boolean columnAppliedBeforeRow) {
			this.parent = parent;
			this.groupingHeader = groupingHeader;
			this.groupingAxis = groupingAxis;
			this.precedingSibling = precedingSibling;
			this.rowApplied = rowApplied;
			this.columnAppliedBeforeRow = columnAppliedBeforeRow;
			children = groupCellsBy(cells, nextAxes);
			hasSubGroups = !children.isEmpty() && children.get(0) instanceof TableCellGroup; // all children of same type
			subGroupingAxis = hasSubGroups ? ((TableCellGroup)children.get(0)).groupingAxis : null; // all children has same groupingAxis
		}

		private List<TableCellCollection> groupCellsBy(List<TableCell> cells, Iterator<String> axes) {
			String firstAxis = axes.hasNext() ? axes.next() : null;
			List<String> nextAxes = ImmutableList.copyOf(axes);
			if (firstAxis != null) {
				Map<TableCell,List<TableCell>> categories = new LinkedHashMap<TableCell,List<TableCell>>();
				List<TableCell> uncategorized = null;
				for (TableCell c : cells) {
					boolean categorized = false;
					for (TableCell h : findHeaders(c, !columnAppliedBeforeRow))
						if (h.axis != null && h.axis.contains(firstAxis)) {
							List<TableCell> category = categories.get(h);
							if (category == null) {
								category = new ArrayList<TableCell>();
								categories.put(h, category); }
							category.add(c);
							categorized = true; }
					if (!categorized) {
						if (uncategorized == null)
							uncategorized = new ArrayList<TableCell>();
						uncategorized.add(c); }}
				if (!categories.isEmpty()) {
					List<TableCellCollection> children = new ArrayList<TableCellCollection>();
					TableCellGroup child = null;
					for (TableCell h : categories.keySet()) {
						child = new TableCellGroup(categories.get(h), nextAxes.iterator(), this, h, firstAxis, child,
						                           rowApplied, columnAppliedBeforeRow);
						children.add(child); }
					if (uncategorized != null) {
						child = new TableCellGroup(uncategorized, nextAxes.iterator(), this, null, firstAxis, child,
						                           rowApplied, columnAppliedBeforeRow);
						children.add(child); }
					return children; }
				else if ("row".equals(firstAxis)) {
					List<TableCellCollection> children = new ArrayList<TableCellCollection>();
					TableCellGroup child = null;
					Map<Integer,List<TableCell>> rows = new LinkedHashMap<Integer,List<TableCell>>();
					for (TableCell c : cells) {
						List<TableCell> row = rows.get(c.row);
						if (row == null) {
							row = new ArrayList<TableCell>();
							rows.put(c.row, row); }
						row.add(c); }
					for (List<TableCell> row : rows.values()) {
						child = new TableCellGroup(row, nextAxes.iterator(), this, null, firstAxis, child, true, columnAppliedBeforeRow);
						children.add(child); }
					return children; }
				else if ("column".equals(firstAxis) || "col".equals(firstAxis)) {
					List<TableCellCollection> children = new ArrayList<TableCellCollection>();
					TableCellGroup child = null;
					Map<Integer,List<TableCell>> columns = new LinkedHashMap<Integer,List<TableCell>>();
					for (TableCell c : cells) {
						List<TableCell> column = columns.get(c.col);
						if (column == null) {
							column = new ArrayList<TableCell>();
							columns.put(c.col, column); }
						column.add(c); }
					for (List<TableCell> column : columns.values()) {
						child = new TableCellGroup(column, nextAxes.iterator(), this, null, firstAxis, child, rowApplied, !rowApplied);
						children.add(child); }
					return children; }
				else if ("row-group".equals(firstAxis)) {
					List<TableCellCollection> children = new ArrayList<TableCellCollection>();
					TableCellGroup child = null;
					Map<Integer,List<TableCell>> rowGroups = new LinkedHashMap<Integer,List<TableCell>>();
					for (TableCell c : cells) {
						List<TableCell> rowGroup = rowGroups.get(c.rowGroup);
						if (rowGroup == null) {
							rowGroup = new ArrayList<TableCell>();
							rowGroups.put(c.rowGroup, rowGroup); }
						rowGroup.add(c); }
					for (List<TableCell> rowGroup : rowGroups.values()) {
						child = new TableCellGroup(rowGroup, nextAxes.iterator(), this, null, firstAxis, child,
						                           rowApplied, columnAppliedBeforeRow);
						children.add(child); }
					return children; }
				else
					return groupCellsBy(cells, nextAxes.iterator()); }
			else {
				List<TableCellCollection> children = new ArrayList<TableCellCollection>();
				SingleTableCell child = null;
				for (TableCell c : cells) {
					child = new SingleTableCell(c, this, child, columnAppliedBeforeRow);
					children.add(child); }
				return children; }
		}

		private List<TableCell> newlyAppliedHeaders;
		public List<TableCell> newlyAppliedHeaders() {
			if (newlyAppliedHeaders == null) {
				newlyAppliedHeaders = new ArrayList<TableCell>();
				if (groupingHeader != null)
					for (TableCell h : findHeaders(groupingHeader, !columnAppliedBeforeRow))
						if (!previouslyAppliedHeaders().contains(h))
							newlyAppliedHeaders.add(h); }
			return newlyAppliedHeaders;
		}

		private List<TableCell> previouslyAppliedHeaders() {
			if (parent != null)
				return parent.appliedHeaders();
			else
				return emptyList;
		}

		private List<TableCell> appliedHeaders;
		public List<TableCell> appliedHeaders() {
			if (appliedHeaders == null) {
				appliedHeaders = new ArrayList<TableCell>();
				appliedHeaders.addAll(previouslyAppliedHeaders());
				appliedHeaders.addAll(newlyAppliedHeaders()); }
			return appliedHeaders;
		}

		private List<TableCell> newlyDeferredHeaders;
		private List<TableCell> newlyDeferredHeaders() {
			if (newlyDeferredHeaders == null) {
				newlyDeferredHeaders = new ArrayList<TableCell>();
				int i = newlyAppliedHeaders().size() - 1;
				while (i >= 0 && newlyAppliedHeaders().get(i).headerPolicy == TableCell.HeaderPolicy.ALWAYS)
					newlyDeferredHeaders.add(0, newlyAppliedHeaders().get(i--)); }
			return newlyDeferredHeaders;
		}

		private List<TableCell> deferredHeaders;
		private List<TableCell> deferredHeaders() {
			if (deferredHeaders == null) {
				deferredHeaders = new ArrayList<TableCell>();
				if (newlyRenderedOrPromotedHeaders().isEmpty())
					deferredHeaders.addAll(previouslyDeferredHeaders());
				deferredHeaders.addAll(newlyDeferredHeaders()); }
			return deferredHeaders;
		}

		private List<TableCell> previouslyDeferredHeaders() {
			if (parent != null)
				return parent.deferredHeaders();
			else
				return emptyList;
		}

		private List<TableCell> newlyRenderedOrPromotedHeaders;
		private List<TableCell> newlyRenderedOrPromotedHeaders() {
			if (newlyRenderedOrPromotedHeaders == null) {
				newlyRenderedOrPromotedHeaders = new ArrayList<TableCell>();
				int i = newlyAppliedHeaders().size() - 1;
				while (i >= 0 && newlyAppliedHeaders().get(i).headerPolicy == TableCell.HeaderPolicy.ALWAYS)
					i--;
				if (i >= 0) {
					Iterator<TableCell> lastAppliedHeaders = (
						precedingSibling != null ? precedingSibling.newlyAppliedHeaders() : emptyList
					).iterator();
					boolean canOmit = true;
					for (TableCell h : previouslyDeferredHeaders()) {
						newlyRenderedOrPromotedHeaders.add(h);
						canOmit = false; }
					for (int j = 0; j <= i; j++) {
						TableCell h = newlyAppliedHeaders().get(j);
						if (canOmit
						    && h.headerPolicy != TableCell.HeaderPolicy.ALWAYS
						    && lastAppliedHeaders.hasNext() && lastAppliedHeaders.next().equals(h))
							continue;
						newlyRenderedOrPromotedHeaders.add(h);
						canOmit = false; }}}
			return newlyRenderedOrPromotedHeaders;
		}

		private List<TableCell> newlyPromotedHeaders;
		public List<TableCell> newlyPromotedHeaders() {
			if (newlyPromotedHeaders == null) {
				newlyPromotedHeaders = new ArrayList<TableCell>();
				int i = newlyRenderedOrPromotedHeaders().size() - 1;
				while (i >= 0 && newlyRenderedOrPromotedHeaders().get(i).headerPolicy != TableCell.HeaderPolicy.FRONT)
					i--;
				while (i >= 0)
					newlyPromotedHeaders.add(0, newlyRenderedOrPromotedHeaders().get(i--)); }
			return newlyPromotedHeaders;
		}

		private List<TableCell> newlyRenderedHeaders;
		public List<TableCell> newlyRenderedHeaders() {
			if (newlyRenderedHeaders == null) {
				newlyRenderedHeaders = new ArrayList<TableCell>();
				int i = newlyRenderedOrPromotedHeaders().size() - 1;
				while (i >= 0 && newlyRenderedOrPromotedHeaders().get(i).headerPolicy != TableCell.HeaderPolicy.FRONT)
					newlyRenderedHeaders.add(0, newlyRenderedOrPromotedHeaders().get(i--)); }
			return newlyRenderedHeaders;
		}

		public void write(XMLStreamWriter writer) {
			try {
				if (hasSubGroups) {
					writeStartElement(writer, CSS_TABLE_BY);
					writeAttribute(writer, _AXIS, subGroupingAxis);
					writeStyleAttribute(writer, getTableByStyle(subGroupingAxis)); }
				List<List<TableCell>> promotedHeaders = null;
				int i = 0;
				for (TableCellCollection c : children) {
					if (c instanceof TableCellGroup) {
						TableCellGroup g = (TableCellGroup)c;
						int j = 0;
						for (TableCellCollection cc : g.children) {
							if (!cc.newlyPromotedHeaders().isEmpty()) {
								if (promotedHeaders == null) {
									if (i == 0 && j == 0) {
										writeStartElement(writer, CSS_LIST_HEADER);
										writeStyleAttribute(writer, getTableByStyle(g.groupingAxis).getListHeaderStyle());
										if (g.hasSubGroups) {
											writeStartElement(writer, CSS_TABLE_BY);
											writeAttribute(writer, _AXIS, g.subGroupingAxis);
											writeStyleAttribute(writer, getTableByStyle(g.subGroupingAxis)); }
										promotedHeaders = new ArrayList<List<TableCell>>(); }
									else
										throw new RuntimeException("Some headers of children promoted but not all children have a promoted header."); }
								if (i == 0) {
									if (g.hasSubGroups) {
										writeStartElement(writer, CSS_LIST_ITEM);
										Predicate<PseudoClass> matcher = matchesPosition(j + 1, g.children.size());
										writeStyleAttribute(writer, getTableByStyle(g.subGroupingAxis).getListItemStyle(matcher)); }
									for (TableCell h : cc.newlyPromotedHeaders())
										h.write(writer);
									if (g.hasSubGroups)
										writer.writeEndElement(); // css:list-item
									promotedHeaders.add(cc.newlyPromotedHeaders()); }
								else if (!promotedHeaders.get(j).equals(cc.newlyPromotedHeaders()))
									throw new RuntimeException("Headers of children promoted but not the same as promoted headers of sibling groups."); }
							else if (promotedHeaders != null)
								throw new RuntimeException("Some headers of children promoted but not all children have a promoted header.");
							j++; }
						if (promotedHeaders != null && promotedHeaders.size() != j) {
							throw new RuntimeException("Headers of children promoted but not the same as promoted headers of sibling groups."); }}
					else if (promotedHeaders != null)
						throw new RuntimeException("Coding error");
					i++; }
				if (promotedHeaders != null) {
					if (((TableCellGroup)children.get(0)).hasSubGroups)
						writer.writeEndElement(); // css:table-by
					writer.writeEndElement(); } // css:list-header
				i = 0;
				for (TableCellCollection c : children) {
					if (hasSubGroups) {
						writeStartElement(writer, CSS_LIST_ITEM);
						Predicate<PseudoClass> matcher = matchesPosition(i + 1, children.size());
						writeStyleAttribute(writer, getTableByStyle(subGroupingAxis).getListItemStyle(matcher)); }
					for (TableCell h : c.newlyRenderedHeaders())
						h.write(writer);
					c.write(writer);
					if (hasSubGroups)
						writer.writeEndElement(); // css:list-item
					i++; }
				if (hasSubGroups)
					writer.writeEndElement(); // css:table-by
			} catch (XMLStreamException e) {
				throw new RuntimeException(e); }
		}

		@Override
		public String toString() {
			ToStringWriter xml = new ToStringWriter();
			write(xml);
			StringBuilder s = new StringBuilder();
			s.append("TableCellGroup[header: ").append(newlyRenderedHeaders());
			s.append(", children: ").append(children);
			s.append(", xml: ").append(xml).append("]");
			return s.toString();
		}
	}

	private static void writeStyleAttribute(XMLStreamWriter writer, PseudoElementStyle style) throws XMLStreamException {
		if (!style.isEmpty())
			writeAttribute(writer, _STYLE, style.toString());
	}

	final private Map<String,TableByStyle> tableByStyles = new HashMap<String,TableByStyle>();
	private ListItemStyle listHeaderStyle = new ListItemStyle();

	public void addListHeaderStyle(ListItemStyle style) {
		listHeaderStyle = listHeaderStyle.mergeWith(style);
	}

	public TableByStyle getTableByStyle(String axis) {
		TableByStyle style = tableByStyles.get(axis);
		if (style == null) {
			style = new TableByStyle();
			tableByStyles.put(axis, style); }
		return style;
	}

	public ListItemStyle getListHeaderStyle() {
		return listHeaderStyle;
	}

	private static class PseudoElementStyle {

		final protected Map<List<Selector>,RuleBlock<Rule<?>>> ruleBlocks = new HashMap<>();

		public void addRuleBlock(RuleBlock<Rule<?>> ruleblock) {
			if (!ruleblock.isEmpty()) {
				List<Selector> selector = null;
				if (ruleblock instanceof RuleRelativeBlock)
					selector = ((RuleRelativeBlock)ruleblock).getSelector();
				RuleBlock<Rule<?>> r;
				if (ruleBlocks.containsKey(selector))
					r = ruleBlocks.get(selector);
				else {
					// we make a copy so that we can modify the rule later without affecting the
					// original which might be used in other places (it should be considered
					// immutable)
					r = selector != null
						? new RuleRelativeBlock(selector)
						: new AbstractRuleBlock<Rule<?>>();
					r.unlock();
				}
				// we can modify the existing rule because it is mutable and dedicated to this class
				r.addAll(ruleblock);
				ruleBlocks.put(selector, r);
			}
		}

		public boolean isEmpty() {
			return ruleBlocks.isEmpty();
		}

		@Override
		public String toString() {
			return serializeRuleBlockList(ruleBlocks.values());
		}
	}

	private static class TableByStyle extends PseudoElementStyle {

		final private Map<List<PseudoClass>,ListItemStyle> listItemStyles = new LinkedHashMap<List<PseudoClass>,ListItemStyle>();
		private ListItemStyle listHeaderStyle = new ListItemStyle();

		public TableByStyle() {}

		public void addListItemStyle(List<PseudoClass> pseudo, ListItemStyle style) {
			if (!listItemStyles.containsKey(pseudo))
				listItemStyles.put(pseudo, style);
			else
				listItemStyles.put(pseudo, listItemStyles.get(pseudo).mergeWith(style));
		}

		public void addListHeaderStyle(ListItemStyle style) {
			listHeaderStyle = listHeaderStyle.mergeWith(style);
		}

		public ListItemStyle getListItemStyle(Predicate<PseudoClass> matcher) {
			ListItemStyle style = new ListItemStyle();
		  outer: for (List<PseudoClass> pseudoClasses : listItemStyles.keySet()) {
				for (PseudoClass pseudoClass : pseudoClasses)
					if (!matcher.apply(pseudoClass))
						continue outer;
				style = style.mergeWith(listItemStyles.get(pseudoClasses)); }
			return style;
		}

		public ListItemStyle getListHeaderStyle() {
			return listHeaderStyle;
		}
	}

	private static class ListItemStyle extends PseudoElementStyle {

		public ListItemStyle() {}

		public ListItemStyle(RuleBlock<Rule<?>> ruleblock) {
			addRuleBlock(ruleblock);
		}

		public ListItemStyle mergeWith(ListItemStyle style) {
			for (RuleBlock<Rule<?>> r: style.ruleBlocks.values())
				addRuleBlock(r);
			return this;
		}
	}

	private final static Predicate<PseudoClass> matchesPosition(final int position, final int elementCount) {
		return new Predicate<PseudoClass>() {
			public boolean apply(PseudoClass pseudo) {
				if (pseudo instanceof PseudoClassImpl)
					return ((PseudoClassImpl)pseudo).matchesPosition(position, elementCount);
				return false;
			}
		};
	}

	// see https://www.w3.org/TR/REC-html40/struct/tables.html#h-11.4.3
	private List<TableCell> findHeaders(TableCell cell, boolean firstLeftThenUpward) {
		List<TableCell> headers = new ArrayList<TableCell>();
		if (isHeader(cell))
			headers.add(cell);
		findHeaders(headers, 0, cell, firstLeftThenUpward);
		return headers;
	}

	private int findHeaders(List<TableCell> headers, int index, TableCell cell, boolean firstLeftThenUpward) {

		// headers attribute
		if (cell.headers != null) {
			for (String id : cell.headers)
				index = recurAddHeader(headers, index, getById(id), firstLeftThenUpward);
			return index; }

		// scope attribute can be used instead of headers (they should not be used in same table)
		List<TableCell> rowHeaders = new ArrayList<TableCell>();
		List<TableCell> colHeaders = new ArrayList<TableCell>();
		for (TableCell h : cells)
			if (h != cell && h.scope != null) {
				switch (h.scope) {
				case ROW:
					if (h.row <= (cell.row + cell.rowspan - 1) && cell.row <= (h.row + h.rowspan - 1))
						rowHeaders.add(h);
					break;
				case COL:
					if (h.col <= (cell.col + cell.colspan - 1) && cell.col <= (h.col + h.colspan - 1))
						colHeaders.add(h);
					break; }}
		Collections.sort(rowHeaders, sortByColumnAndThenRow);
		for (TableCell h : rowHeaders)
			index = recurAddHeader(headers, index, h, firstLeftThenUpward);
		Collections.sort(colHeaders, sortByRowAndThenColumn);
		for (TableCell h : colHeaders)
			index = recurAddHeader(headers, index, h, firstLeftThenUpward);

		if (!isHeader(cell)) {
			int direction = (firstLeftThenUpward ? 0 : 1);
		  outer: while (true) {
				switch (direction) {
				case 0: { // search left from the cell's position to find row header cells
					int k = 0;
					for (int i = 0; i < cell.rowspan; i++)
						for (int j = cell.col - 1; j > 0;) {
							boolean foundHeader = false;
							TableCell c = getByCoordinates(cell.row + i, j);
							if (c != null && isHeader(c)) {
								foundHeader = true;
								if (c.scope == null) {
									int l = recurAddHeader(headers, index, c, firstLeftThenUpward) - index;
									k += l;
									if (l > 1)
										break; }}
							else if (foundHeader)
								break;
							if (c == null)
								j--;
							else
								j = j - c.colspan; }
					index += k;
					if (!firstLeftThenUpward)
						break outer; }
				case 1: { // search upwards from the cell's position to find column header cells
					int k = 0;
					for (int i = 0; i < cell.colspan; i++)
						for (int j = cell.row - 1; j > 0;) {
							boolean foundHeader = false;
							TableCell c = getByCoordinates(j, cell.col + i);
							if (c != null && isHeader(c)) {
								foundHeader = true;
								if (c.scope == null) {
									int l = recurAddHeader(headers, index, c, firstLeftThenUpward) - index;
									k += l;
									if (l > 1)
										break; }}
							else if (foundHeader)
								break;
							if (c == null)
								j--;
							else
								j = j - c.rowspan; }
					index += k;
					if (!firstLeftThenUpward) {
						direction--;
						continue outer; }
					break outer; }}}}

		return index;
	}

	private int recurAddHeader(List<TableCell> headers, int index, TableCell header, boolean firstLeftThenUpward) {
		if (headers.contains(header))
			; // if a cell spans multiple cols/rows we could find the same header via multiple paths
		else {
			headers.add(index, header);
			index = findHeaders(headers, index, header, firstLeftThenUpward);
			index ++; }
		return index;
	}

	private TableCell getById(String id) {
		for (TableCell c : cells)
			if (id.equals(c.id))
				return c;
		throw new RuntimeException("No element found with id " + id);
	}

	private TableCell getByCoordinates(int row, int col) {
		for (TableCell c : cells)
			if (c.row <= row && (c.row + c.rowspan - 1) >= row &&
			    c.col <= col && (c.col + c.colspan - 1) >= col)
				return c;
		return null;
	}

	private static boolean isHeader(TableCell cell) {
		return (cell.type == TableCell.CellType.TH || (cell.axis != null) || (cell.scope != null));
	}

	@SafeVarargs
	private static final <T> Comparator<T> compose(final Comparator<T>... comparators) {
		return new Comparator<T>() {
			public int compare(T x1, T x2) {
				for (Comparator<T> comparator : comparators) {
					int result = comparator.compare(x1, x2);
					if (result != 0)
						return result; }
				return 0;
			}
		};
	}

	private static final Comparator<TableCell> sortByRow = new Comparator<TableCell>() {
		public int compare(TableCell c1, TableCell c2) {
			return new Integer(c1.row).compareTo(c2.row);
		}
	};

	private static final Comparator<TableCell> sortByColumn = new Comparator<TableCell>() {
		public int compare(TableCell c1, TableCell c2) {
			return new Integer(c1.col).compareTo(c2.col);
		}
	};

	private static final Comparator<TableCell> sortByRowType = new Comparator<TableCell>() {
		public int compare(TableCell c1, TableCell c2) {
			return c1.rowType.compareTo(c2.rowType);
		}
	};

	private static final Comparator<TableCell> sortByRowAndThenColumn = compose(sortByRow, sortByColumn);

	private static final Comparator<TableCell> sortByColumnAndThenRow = compose(sortByColumn, sortByRow);

	private static class TableCell {

		enum CellType {
			TD,
			TH
		}

		enum RowType {
			THEAD,
			TBODY,
			TFOOT
		}

		enum HeaderPolicy {
			ALWAYS,
			ONCE,
			FRONT
		}

		// TODO: handle colgroup and rowgroup
		enum Scope {
			COL,
			ROW
		}

		int rowGroup;
		int row;
		int col;
		CellType type = CellType.TD;
		RowType rowType = RowType.TBODY;
		HeaderPolicy headerPolicy = HeaderPolicy.ONCE;
		String id;
		List<String> headers;
		Scope scope = null;
		List<String> axis;
		int rowspan = 1;
		int colspan = 1;
		String ns;
		SimpleInlineStyle style = null;
		List<WriterEvent> content = new ArrayList<WriterEvent>();

		private AtomicReference<Boolean> written = new AtomicReference<Boolean>(false);

		public void write(XMLStreamWriter writer) throws XMLStreamException {
			writer.writeStartElement(ns, type == CellType.TD ? "td" : "th");
			if (axis != null)
				writeAttribute(writer, _AXIS, Strings.join(axis, ","));
			if (id != null && !written.get()) {
				writeAttribute(writer, _ID, id);
				written.set(true); }
			if (headers != null)
				writeAttribute(writer, _HEADERS, Strings.join(headers, " "));
			if (style != null) {
				String styleAttr = BrailleCssSerializer.toString(style);
				if (styleAttr != null && !"".equals(styleAttr))
					writeAttribute(writer, _STYLE, styleAttr); }
			for (WriterEvent action : content)
				action.writeTo(writer);
			writer.writeEndElement();
		}

		public TableCell clone() {
			TableCell clone = new TableCell();
			clone.rowGroup = this.rowGroup;
			clone.row = this.row;
			clone.col = this.col;
			clone.type = this.type;
			clone.rowType = this.rowType;
			clone.headerPolicy = this.headerPolicy;
			clone.id = this.id;
			clone.headers = this.headers;
			clone.scope = this.scope;
			clone.axis = this.axis;
			clone.rowspan = this.rowspan;
			clone.colspan = this.colspan;
			clone.ns = this.ns;
			clone.style = (SimpleInlineStyle)this.style.clone();
			clone.content.addAll(this.content);
			clone.written = this.written;
			return clone;
		}

		@Override
		public String toString() {
			ToStringWriter xml = new ToStringWriter();
			try {
				write(xml); }
			catch (Exception e) {
				throw new RuntimeException("coding error", e); }
			StringBuilder s = new StringBuilder();
			s.append("TableCell{" + row + "," + col + "}[").append(xml).append("]");
			return s.toString();
		}
	}

	private static class CellCoordinates {

		private final int row;
		private final int col;

		private CellCoordinates(int row, int col) {
			this.row = row;
			this.col = col;
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + col;
			result = prime * result + row;
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CellCoordinates other = (CellCoordinates) obj;
			if (col != other.col)
				return false;
			if (row != other.row)
				return false;
			return true;
		}
	}

	private static int nonNegativeInteger(String s) {
		try {
			int i = Integer.parseInt(s);
			if (i >= 0)
				return i; }
		catch(NumberFormatException e) {}
		throw new RuntimeException("Expected positive integer but got "+ s);
	}

	private static class WriteOnlyOnce extends ArrayList<WriterEvent> implements WriterEvent {
		public void writeTo(XMLStreamWriter writer) throws XMLStreamException {
			Iterator<WriterEvent> i = iterator();
			while (i.hasNext()) {
				i.next().writeTo(writer);
				i.remove();
			}
		}
	}

	public static WriterEvent writeElementOnce(XMLStreamReader reader) throws XMLStreamException {
		WriteOnlyOnce list = new WriteOnlyOnce();
		int depth = 0;
		element: while (true)
			try {
				switch (reader.getEventType()) {
				case START_ELEMENT: {
					QName name = reader.getName();
					depth++;
					list.add(w -> writeStartElement(w, name));
					for (int i = 0; i < reader.getNamespaceCount(); i++) {
						String prf = reader.getNamespacePrefix(i);
						String ns = reader.getNamespaceURI(i);
						list.add(w -> w.writeNamespace(prf, ns)); }
					for (int i = 0; i < reader.getAttributeCount(); i++) {
						QName attrName = reader.getAttributeName(i);
						String attrValue = reader.getAttributeValue(i);
						list.add(w -> writeAttribute(w, attrName, attrValue)); }
					break; }
				case CHARACTERS:
					String chars = reader.getText();
					list.add(w -> w.writeCharacters(chars));
					break;
				case END_ELEMENT: {
					QName name = reader.getName();
					list.add(w -> w.writeEndElement());
					depth--;
					if (depth == 0)
						break element; }}
				reader.next(); }
			catch (NoSuchElementException e) {
				throw new RuntimeException("coding error"); }
		return list;
	}

	/* Remove the first part of the selector, or the whole selector if it exists of only one part */
	private static RuleBlock<Rule<?>> rest(RuleRelativeBlock rule) {
		List<Selector> combinedSelector = rule.getSelector();
		if (combinedSelector.size() == 0 || combinedSelector.get(0).size() == 0)
			throw new RuntimeException("coding error");
		SelectorPart first = combinedSelector.get(0).get(0);
		if (first instanceof PseudoElementImpl) {
			// combinedSelector.get(0).size() should normally always be 1
			List<Selector> rest = combinedSelector.subList(1, combinedSelector.size());
			RuleBlock<Rule<?>> newRule;
			PseudoElementImpl pseudo = (PseudoElementImpl)first;
			if (pseudo.hasStackedPseudoElement()) {
				combinedSelector = new ArrayList<>();
				Selector selector = (Selector)new SelectorImpl().unlock();
				selector.add(pseudo.getStackedPseudoElement());
				combinedSelector.add(selector);
				combinedSelector.addAll(rest);
				newRule = new RuleRelativeBlock(combinedSelector);
			} else {
				combinedSelector = pseudo.getCombinedSelectors();
				newRule = combinedSelector.isEmpty()
					? new AbstractRuleBlock<Rule<?>>()
					: new RuleRelativeBlock(combinedSelector);
			}
			newRule.replaceAll(rule); // we can do this because rule is considered immutable
			return newRule;
		} else {
			throw new RuntimeException("not implemented");
		}
	}
	
	private static String serializeRuleBlockList(Iterable<? extends RuleBlock<? extends Rule<?>>> ruleBlocks) {
		String b = null;
		for (RuleBlock<? extends Rule<?>> r : ruleBlocks) {
			String s;
			if (r instanceof RuleMainBlock)
				s = BrailleCssSerializer.toString((RuleMainBlock)r);
			else if (r instanceof RuleRelativeBlock)
				s = BrailleCssSerializer.toString((RuleRelativeBlock)r);
			else
				s = BrailleCssSerializer.toString(r);
			if (!s.isEmpty())
				if (b == null)
					b = s;
				else {
					if (!(b.endsWith("}") || b.endsWith(";")))
						b = b + ";";
					b += " ";
					b += s; }}
		if (b == null) b = "";
		return b;
	}
}
