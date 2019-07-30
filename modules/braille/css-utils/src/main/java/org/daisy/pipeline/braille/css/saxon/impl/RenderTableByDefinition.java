package org.daisy.pipeline.braille.css.saxon.impl;

import java.util.ArrayList;
import java.util.Collections;

import static java.util.Collections.sort;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerException;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import com.xmlcalabash.core.XProcException;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.Combinator;
import cz.vutbr.web.css.Selector.PseudoClass;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermPair;
import cz.vutbr.web.csskit.OutputUtil;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.InlineStyle.RuleMainBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativeBlock;
import org.daisy.braille.css.SelectorImpl.PseudoClassImpl;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;

import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.stax.XMLStreamWriterHelper.ToStringWriter;
import org.daisy.common.stax.XMLStreamWriterHelper.WriterEvent;
import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;
import org.daisy.common.transform.XMLStreamToXMLStreamTransformer;
import static org.daisy.pipeline.braille.common.util.Strings.join;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "css:render-table-by",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class RenderTableByDefinition extends ExtensionFunctionDefinition {
	
	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";
	
	private static final StructuredQName funcname = new StructuredQName("css", XMLNS_CSS, "render-table-by");
	
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	@Override
	public int getMinimumNumberOfArguments() {
		return 2;
	}
	
	@Override
	public int getMaximumNumberOfArguments() {
		return 2;
	}
	
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.SINGLE_STRING,
			SequenceType.SINGLE_NODE // SINGLE_ELEMENT_NODE
		};
	}
	
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_NODE; // SINGLE_ELEMENT_NODE
	}
	
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					String axes = arguments[0].head().getStringValue();
					NodeInfo tableElement = (NodeInfo)arguments[1].head();
					
					// FIXME: why does this not work?
					// URI base = new URI(tableElement.getBaseURI());
					XdmNode result = Iterators.getOnlyElement(
						SaxonHelper.transform(
							new TableAsList(axes),
							Collections.singleton(tableElement).iterator(),
							context.getConfiguration()));
					result = (XdmNode)result.axisIterator(Axis.CHILD).next(); // because result is document-node
					return result.getUnderlyingNode(); }
				catch (TransformerException e) {
					throw new XPathException("css:render-table-by failed", XProcException.javaError(e, 0)); }
			}
		};
	}
	
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
	
	private static final QName CSS_TABLE_HEADER_POLICY = new QName(XMLNS_CSS, "table-header-policy");
	
	private static final Splitter HEADERS_SPLITTER = Splitter.on(' ').trimResults().omitEmptyStrings();
	private static final Splitter AXIS_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
	
	private static class TableAsList implements XMLStreamToXMLStreamTransformer {
		
		final List<String> axes;
		
		private TableAsList(String axes) {
			this.axes = new ArrayList<String>(AXIS_SPLITTER.splitToList(axes));
			if (this.axes.remove("auto"))
				if (!this.axes.isEmpty())
					throw new RuntimeException();
		}
		
		List<WriterEvent> writeActionsBefore;
		List<WriterEvent> writeActionsAfter;
		List<TableCell> cells;
		Set<CellCoordinates> coveredCoordinates;
		String ns;
		
		public void transform(Iterator<BaseURIAwareXMLStreamReader> input, Supplier<BaseURIAwareXMLStreamWriter> output) {
			XMLStreamReader reader = Iterators.getOnlyElement(input);
			XMLStreamWriter writer = output.get();
			
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
		  loop: while (true)
				try {
					switch (reader.next()) {
					case START_ELEMENT: {
						QName name = reader.getName();
						depth++;
						boolean isCell = false;
						if (depth == 1) {
							if (!isHTMLorDTBookElement(TABLE, name))
								throw new RuntimeException("Expected table element (html|dtb).");
							if (XMLNS_HTML.equals(name.getNamespaceURI()))
								ns = XMLNS_HTML;
							else if (XMLNS_DTB.equals(name.getNamespaceURI()))
								ns = XMLNS_DTB; }
						else if (isHTMLorDTBookElement(THEAD, name)) {
							rowType = TableCell.RowType.THEAD;
							// TODO: if style != default, warning that style on thead element is ignored
							break; }
						else if (isHTMLorDTBookElement(TFOOT, name)) {
							rowType = TableCell.RowType.TFOOT;
							// TODO: if style != default, warning that style on tfoot element is ignored
							break; }
						else if (isHTMLorDTBookElement(TBODY, name)) {
							rowType = TableCell.RowType.TBODY;
							// TODO: if style != default, warning that style on tbody element is ignored
							break; }
						else if (isHTMLorDTBookElement(TR, name)) {
							// TODO: if style != default, warning that style on tr element is ignored
							break; }
						else if (isHTMLorDTBookElement(COLGROUP, name) || isHTMLorDTBookElement(COL, name))
							throw new RuntimeException("Elements colgroup and col not supported yet.");
						else if (isHTMLorDTBookElement(TD, name) || isHTMLorDTBookElement(TH, name)) {
							isCell = true;
							withinCell = new TableCell();
							withinCell.row = row;
							withinCell.col = col;
							withinCell.rowGroup = rowGroup;
							withinCell.rowType = rowType;
							setCovered(row, col);
							cells.add(withinCell);
							if (isHTMLorDTBookElement(TH, name))
								withinCell.type = TableCell.CellType.TH;
							writeActions = withinCell.writeActions; }
						writeActions.add(w -> writeStartElement(w, name));
						for (int i = 0; i < reader.getNamespaceCount(); i++) {
							String prf = reader.getNamespacePrefix(i);
							String ns = reader.getNamespaceURI(i);
							writeActions.add(w -> w.writeNamespace(prf, ns)); }
						for (int i = 0; i < reader.getAttributeCount(); i++) {
							QName attrName = reader.getAttributeName(i);
							String attrValue = reader.getAttributeValue(i);
							if (CSS_TABLE_HEADER_POLICY.equals(attrName)) {
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
									String builder = null;
									for (RuleBlock<?> block : style) {
										if (block instanceof RuleMainBlock)
											builder = joinRuleBlocks(builder, serializeRuleBlock(style.getMainStyle()));
										else if (block instanceof RuleRelativeBlock) {
											RuleRelativeBlock ruleblock = (RuleRelativeBlock)block;
											List<Selector> selector = ruleblock.getSelector();
											if (selector.size() > 0) { // should always be true
												// note that in the cases "&::list-item", "&::list-header" and "&::table-by(...)" we
												// are ignoring any following selector parts except those that are "stacked" onto the
												// pseudo element
												if (selector.get(0).size() > 0) { // should always be true
													if (selector.get(0).get(0) instanceof PseudoElementImpl) {
														// selector.get(0).size() should always be 1
														PseudoElementImpl pseudo = (PseudoElementImpl)selector.get(0).get(0);
														// selector size should be 1
														if ("list-item".equals(pseudo.getName()))
															addListItemStyle(
																pseudo.getPseudoClasses(),
																new ListItemStyle(pseudo.getStackedPseudoElement(), ruleblock));
														else if ("list-header".equals(pseudo.getName())) {
															if (pseudo.getPseudoClasses().isEmpty())
																addListHeaderStyle(
																	new ListItemStyle(pseudo.getStackedPseudoElement(), ruleblock)); }
														else if ("table-by".equals(pseudo.getName())) {
															String axis = pseudo.getArguments()[0];
															if (pseudo.getPseudoClasses().isEmpty()) {
																if (pseudo.hasStackedPseudoElement()) {
																	pseudo = pseudo.getStackedPseudoElement();
																	if ("list-item".equals(pseudo.getName()))
																		getTableByStyle(axis).addListItemStyle(
																			pseudo.getPseudoClasses(),
																			new ListItemStyle(pseudo.getStackedPseudoElement(), ruleblock));
																	else if ("list-header".equals(pseudo.getName())) {
																		if (pseudo.getPseudoClasses().isEmpty())
																			getTableByStyle(axis).addListHeaderStyle(
																				new ListItemStyle(pseudo.getStackedPseudoElement(), ruleblock)); }
																	else
																		getTableByStyle(axis).addRuleBlock(pseudo, ruleblock); }
																else
																	getTableByStyle(axis).addRuleBlock(ruleblock); }}
														else
															builder = joinRuleBlocks(builder, serializeRuleBlock(ruleblock)); }
													else
														builder = joinRuleBlocks(builder, serializeRuleBlock(ruleblock)); }
												else
													builder = joinRuleBlocks(builder, serializeRuleBlock(ruleblock)); }}
										else
											throw new RuntimeException("Unexpected style " + block); }
									newStyle = builder; }
								if (newStyle != null)
									writeActions.add(w -> writeAttribute(w, attrName, newStyle)); }
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
							rowGroup++;
							break; }
						else if (isHTMLorDTBookElement(TR, name)) {
							row++;
							col = 1;
							while (isCovered(row, col)) col++;
							break; }
						writeActions.add(w -> w.writeEndElement());
						if (isHTMLorDTBookElement(TD, name) || isHTMLorDTBookElement(TH, name)) {
							withinCell = null;
							writeActions = writeActionsAfter;
							while (isCovered(row, col)) col++; }
						break; }
					case END_DOCUMENT:
						break loop; }}
				catch (NoSuchElementException e) {
					break; }
			
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
			sort(cells, compose(sortByRowType, sortByRow, sortByColumn));
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
			
			writer.writeStartDocument();
			write(writer);
			writer.writeEndDocument();
			
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
											writeStartElement(writer, new QName(ns, "_"));
											writeStyleAttribute(writer,
											                    groupingAxis != null ? getTableByStyle(groupingAxis).getListHeaderStyle()
											                                         : getListHeaderStyle());
											writeStartElement(writer, new QName(ns, "_"));
											writeStyleAttribute(writer, getTableByStyle(g.groupingAxis));
											promotedHeaders = new ArrayList<List<TableCell>>(); }
										else
											throw new RuntimeException("Some headers of children promoted but not all children have a promoted header."); }
									if (i == 0) {
										writeStartElement(writer, new QName(ns, "_"));
										Predicate<PseudoClass> matcher = matchesPosition(j + 1, g.children.size());
										writeStyleAttribute(writer, getTableByStyle(g.groupingAxis).getListItemStyle(matcher));
										for (TableCell h : cc.newlyPromotedHeaders())
											h.write(writer);
										writer.writeEndElement();
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
						writer.writeEndElement();
						writer.writeEndElement(); }
					i = 0;
					for (TableCellCollection c : children) {
						writeStartElement(writer, new QName(ns, "_"));
						Predicate<PseudoClass> matcher = matchesPosition(i + 1, children.size());
						writeStyleAttribute(writer,
						                    groupingAxis != null ? getTableByStyle(groupingAxis).getListItemStyle(matcher)
						                                         : getListItemStyle(matcher));
						for (TableCell h : c.newlyRenderedHeaders())
							h.write(writer);
						if (c instanceof TableCellGroup) {
							TableCellGroup g = (TableCellGroup)c;
							writeStartElement(writer, new QName(ns, "_"));
							writeStyleAttribute(writer, getTableByStyle(g.groupingAxis)); }
						c.write(writer);
						if (c instanceof TableCellGroup)
							writer.writeEndElement();
						writer.writeEndElement();
						i++; }}
				catch (XMLStreamException e) {
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
		final private Map<List<PseudoClass>,ListItemStyle> listItemStyles = new LinkedHashMap<List<PseudoClass>,ListItemStyle>();
		private ListItemStyle listHeaderStyle = new ListItemStyle();
			
		public void addListItemStyle(List<PseudoClass> pseudo, ListItemStyle style) {
			if (!listItemStyles.containsKey(pseudo))
				listItemStyles.put(pseudo, style);
			else
				listItemStyles.put(pseudo, listItemStyles.get(pseudo).mergeWith(style));
		}
		
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
		
		private static class PseudoElementStyle {
			
			final protected Map<PseudoElementImpl,List<Rule<?>>> ruleBlocks = new HashMap<PseudoElementImpl,List<Rule<?>>>();
			
			public void addRuleBlock(List<Rule<?>> ruleblock) {
				addRuleBlock(null, ruleblock);
			}
			
			public void addRuleBlock(PseudoElementImpl pseudo, List<Rule<?>> ruleblock) {
				if (!ruleblock.isEmpty())
					if (!ruleBlocks.containsKey(pseudo))
						ruleBlocks.put(pseudo, ruleblock);
					else
						ruleBlocks.put(pseudo, ImmutableList.<Rule<?>>builder().addAll(ruleBlocks.get(pseudo)).addAll(ruleblock).build());
			}
			
			public boolean isEmpty() {
				return ruleBlocks.isEmpty();
			}
			
			@Override
			public String toString() {
				String style = null;
				for (PseudoElementImpl pseudo : ruleBlocks.keySet())
					style = joinRuleBlocks(style, serializeRuleBlock(ruleBlocks.get(pseudo), pseudo));
				if (style != null)
					return style;
				else
					return "";
			}
		}
		
		private static class TableByStyle extends PseudoElementStyle {
			
			final private Map<List<PseudoClass>,ListItemStyle> listItemStyles = new LinkedHashMap<List<PseudoClass>,ListItemStyle>();
			private ListItemStyle listHeaderStyle = new ListItemStyle();
			
			public TableByStyle() {}
			
			@SuppressWarnings("unused")
			public TableByStyle(PseudoElementImpl pseudo, List<Rule<?>> ruleblock) {
				addRuleBlock(pseudo, ruleblock);
			}
			
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
			
			@SuppressWarnings("unused")
			public TableByStyle mergeWith(TableByStyle style) {
				for (Map.Entry<PseudoElementImpl,List<Rule<?>>> r: style.ruleBlocks.entrySet())
					addRuleBlock(r.getKey(), r.getValue());
				for (Map.Entry<List<PseudoClass>,ListItemStyle> s: style.listItemStyles.entrySet())
					addListItemStyle(s.getKey(), s.getValue());
				addListHeaderStyle(style.listHeaderStyle);
				return this;
			}
		}
		
		private static class ListItemStyle extends PseudoElementStyle {
			
			public ListItemStyle() {}
			
			public ListItemStyle(PseudoElementImpl pseudo, List<Rule<?>> ruleblock) {
				addRuleBlock(pseudo, ruleblock);
			}
			
			public ListItemStyle mergeWith(ListItemStyle style) {
				for (Map.Entry<PseudoElementImpl,List<Rule<?>>> r: style.ruleBlocks.entrySet())
					addRuleBlock(r.getKey(), r.getValue());
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
			sort(rowHeaders, sortByColumnAndThenRow);
			for (TableCell h : rowHeaders)
				index = recurAddHeader(headers, index, h, firstLeftThenUpward);
			sort(colHeaders, sortByRowAndThenColumn);
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
				throw new RuntimeException();
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
			
			private enum CellType {
				TD,
				TH
			}
			
			private enum RowType {
				THEAD,
				TBODY,
				TFOOT
			}
			
			private enum HeaderPolicy {
				ALWAYS,
				ONCE,
				FRONT
			}
			
			// TODO: handle colgroup and rowgroup
			private enum Scope {
				COL,
				ROW
			}
			
			private int rowGroup;
			private int row;
			private int col;
			private CellType type = CellType.TD;
			private RowType rowType = RowType.TBODY;
			private HeaderPolicy headerPolicy = HeaderPolicy.ONCE;
			private String id;
			private List<String> headers;
			private Scope scope = null;
			private List<String> axis;
			private int rowspan = 1;
			private int colspan = 1;
			
			private List<WriterEvent> writeActions = new ArrayList<WriterEvent>();
			
			public void write(XMLStreamWriter writer) throws XMLStreamException {
				for (WriterEvent action : writeActions)
					action.writeTo(writer);
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
				clone.writeActions.addAll(this.writeActions);
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
	}
	
	/*
	 * The functions below have overlapping functionality with utility functions in CSSInlineStep.
	 * TODO: move to shared component!
	 */
	
	private static String serializeDeclarations(Iterable<Declaration> declarations) {
		List<Declaration> sortedDeclarations = new ArrayList<Declaration>();
		for (Declaration d : declarations) sortedDeclarations.add(d);
		sort(sortedDeclarations);
		return join(sortedDeclarations, "; ", serializeDeclaration);
	}
	
	private static String serializeDeclaration(Declaration declaration) {
		return declaration.getProperty() + ": " + join(declaration, " ", serializeTerm);
	}
	
	private static Function<Object,String> serializeDeclaration = new Function<Object,String>() {
		public String apply(Object declaration) {
			if (declaration instanceof String) // separator
				return (String)declaration;
			if (declaration instanceof Declaration)
				return serializeDeclaration((Declaration)declaration);
			else
				throw new IllegalArgumentException("Coding error");
		}
	};
	
	private static Function<Object,String> serializeTerm = new Function<Object,String>() {
		public String apply(Object term) {
			if (term instanceof TermInteger) {
				TermInteger integer = (TermInteger)term;
				return "" + integer.getIntValue(); }
			else if (term instanceof TermPair) {
				TermPair pair = (TermPair)term;
				Term.Operator op = pair.getOperator();
				return (op != null ? op.value() : "") + pair.getKey() + " " + pair.getValue(); }
			else if (term instanceof TermFunction)
				return "" + term;
			else if (term instanceof TermList) {
				TermList list = (TermList)term;
				return join(list, " ", serializeTerm); }
			else
				return "" + term;
		}
	};
	
	private static String serializeRuleBlock(List<? extends Rule<?>> declarations, PseudoElementImpl pseudo) {
		StringBuilder b = new StringBuilder();
		if (pseudo != null)
			b.append("&").append(pseudo.toString()).append(" { ");
		b.append(serializeDeclarations(Iterables.filter(declarations, Declaration.class)));
		for (Rule<?> r : declarations)
			if (r instanceof Declaration);
			else if (r instanceof RulePage)
				b.append(new ParseStylesheetDefinition.Style().add("@page",
				                                                   ParseStylesheetDefinition.Style.of((RulePage)r)));
			else
				throw new RuntimeException("coding error");
		if (pseudo != null)
			b.append(" }");
		return b.toString();
	}

	private static String serializeRuleBlock(RuleMainBlock rule) {
		return serializeDeclarations(rule);
	}
	
	private static String serializeRuleBlock(RuleRelativeBlock rule) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (Selector s : rule.getSelector()) {
			Combinator c = s.getCombinator();
			if (first) {
				if (c == null)
					b.append("&");
				else if (c != Combinator.CHILD)
					b.append(c.value());
				first = false;
			} else if (c != null) // should always be true
				b.append(c.value());
			b = OutputUtil.appendList(b, s, OutputUtil.EMPTY_DELIM);
		}
		b.append(" { ");
		b.append(serializeRuleBlock(rule, null));
		b.append(" }");
		return b.toString();
	}
	
	private static String joinRuleBlocks(String... ruleBlocks) {
		String b = null;
		for (String r : ruleBlocks)
			if (r != null && !r.isEmpty())
				if (b == null)
					b = r;
				else {
					if (!(b.endsWith("}") || b.endsWith(";")))
						b = b + ";";
					b += " ";
					b += r; }
		return b;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(RenderTableByDefinition.class);
	
}
