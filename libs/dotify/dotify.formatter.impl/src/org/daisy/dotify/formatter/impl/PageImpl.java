package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.daisy.dotify.api.formatter.CompoundField;
import org.daisy.dotify.api.formatter.CurrentPageField;
import org.daisy.dotify.api.formatter.Field;
import org.daisy.dotify.api.formatter.FieldList;
import org.daisy.dotify.api.formatter.FormattingTypes;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.formatter.MarkerReferenceField;
import org.daisy.dotify.api.formatter.MarkerReferenceField.MarkerSearchDirection;
import org.daisy.dotify.api.formatter.MarkerReferenceField.MarkerSearchScope;
import org.daisy.dotify.api.formatter.PageAreaProperties;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.writer.Row;
import org.daisy.dotify.common.text.StringTools;


//FIXME: scope spread is currently implemented using document wide scope, i.e. across volume boundaries. This is wrong, but is better than the previous sequence scope.
/**
 * Provides a page object.
 * 
 * @author Joel Håkansson
 */
class PageImpl implements Page {
	private final static Pattern trailingWs = Pattern.compile("\\s*\\z");
	private final static Pattern softHyphen = Pattern.compile("\u00ad");
	private PageSequence parent;
	private final LayoutMaster master;
	private final FormatterContext fcontext;
	private final List<RowImpl> before;
	private final List<RowImpl> after;
	private final ArrayList<RowImpl> rows;
	private final ArrayList<RowImpl> pageArea;
	private final ArrayList<Marker> markers;
	private final ArrayList<String> anchors;
	private final ArrayList<String> identifiers;
	private final int pageIndex;
	private final int flowHeight;
	private final PageTemplate template;
	private int contentMarkersBegin;
	private boolean isVolBreak;
	private boolean isVolBreakAllowed;
	private int keepPreviousSheets;
	private Integer volumeBreakAfterPriority;
	private int volumeNumber;
	
	
	public PageImpl(LayoutMaster master, FormatterContext fcontext, int pageIndex, List<RowImpl> before, List<RowImpl> after) {
		this.master = master;
		this.fcontext = fcontext;
		this.rows = new ArrayList<>();
		this.before = before;
		this.after = after; 

		this.pageArea = new ArrayList<>();
		this.markers = new ArrayList<>();
		this.anchors = new ArrayList<>();
		this.identifiers = new ArrayList<>();
		this.pageIndex = pageIndex;
		contentMarkersBegin = 0;
		this.parent = null;
		this.template = master.getTemplate(pageIndex+1);
		this.flowHeight = master.getPageHeight() - 
				(int)Math.ceil(getHeight(template.getHeader(), master.getRowSpacing())) -
				(int)Math.ceil(getHeight(template.getFooter(), master.getRowSpacing())) -
				(master.getBorder() != null ? (int)Math.ceil(distributeRowSpacing(null, false).spacing*2) : 0);
		this.isVolBreak = false;
		this.isVolBreakAllowed = true;
		this.keepPreviousSheets = 0;
		this.volumeBreakAfterPriority = null;
		this.volumeNumber = 0;
	}
	
	static float getHeight(List<FieldList> list, float def) {
		float ret = 0;
		for (FieldList f : list) {
			if (f.getRowSpacing()!=null) {
				ret += f.getRowSpacing();
			} else {
				ret += def;
			}
		}
		return ret;
	}

	void addToPageArea(List<RowImpl> block) {
		pageArea.addAll(block);
	}
	
	public void newRow(RowImpl r) {
		if (rowsOnPage()==0) {
			contentMarkersBegin = markers.size();
		}
		rows.add(r);
		markers.addAll(r.getMarkers());
		anchors.addAll(r.getAnchors());
	}
	
	/**
	 * Gets the number of rows on this page
	 * @return returns the number of rows on this page
	 */
	public int rowsOnPage() {
		return rows.size();
	}
	
	public void addMarkers(List<Marker> m) {
		markers.addAll(m);
	}
	
	/**
	 * Get all markers for this page
	 * @return returns a list of all markers on a page
	 */
	public List<Marker> getMarkers() {
		return markers;
	}
	
	/**
	 * Get markers for this page excluding markers before text content
	 * @return returns a list of markers on a page
	 */
	public List<Marker> getContentMarkers() {
		return markers.subList(contentMarkersBegin, markers.size());
	}
	
	public List<String> getAnchors() {
		return anchors;
	}
	
	public void addIdentifier(String id) {
		identifiers.add(id);
	}
	
	public List<String> getIdentifiers() {
		return identifiers;
	}
	
	/**
	 * Gets the page space needed to render the rows. 
	 * @param rows
	 * @param defSpacing a value >= 1.0
	 * @return returns the space, in rows
	 */
	static float rowsNeeded(Iterable<? extends Row> rows, float defSpacing) {
		float ret = 0;
		if (defSpacing < 1) {
			defSpacing = 1;
		}
		for (Row r : rows) {
			if (r.getRowSpacing()!=null && r.getRowSpacing()>=1) {
				ret += r.getRowSpacing();
			} else {
				ret += defSpacing;
			}
		}
		return ret;
	}
	
	float spaceNeeded() {
		return 	pageAreaSpaceNeeded() +
				rowsNeeded(rows, master.getRowSpacing());
	}
	
	float staticAreaSpaceNeeded() {
		return rowsNeeded(before, master.getRowSpacing()) + rowsNeeded(after, master.getRowSpacing());
	}
	
	float pageAreaSpaceNeeded() {
		return (!pageArea.isEmpty() ? staticAreaSpaceNeeded() + rowsNeeded(pageArea, master.getRowSpacing()) : 0);
	}
	
	/**
	 * Space needed if adding the supplied floating rows.
	 * @param rs
	 * @return
	 */
	float spaceNeeded(Iterable<? extends Row> rs) {
		return rowsNeeded(rs, master.getRowSpacing()) + (pageArea.isEmpty() ? staticAreaSpaceNeeded() : 0);
	}
	
	int spaceUsedOnPage(int offs) {
		return (int)Math.ceil(spaceNeeded()) + offs;
	}
	
	private List<RowImpl> buildPageRows(TextBorderStyle border) throws PaginatorException {
		ArrayList<RowImpl> ret = new ArrayList<>();
		{
			LayoutMaster lm = master;
			int pagenum = getPageIndex() + 1;
			PageTemplate t = lm.getTemplate(pagenum);
			BrailleTranslator filter = fcontext.getDefaultTranslator();
			ret.addAll(renderFields(lm, t.getHeader(), filter));
			if (lm.getPageArea()!=null && lm.getPageArea().getAlignment()==PageAreaProperties.Alignment.TOP && !pageArea.isEmpty()) {
				ret.addAll(before);
				ret.addAll(pageArea);
				ret.addAll(after);
			}
			ret.addAll(rows);
			float headerHeight = getHeight(t.getHeader(), lm.getRowSpacing());
			if (!t.getFooter().isEmpty() || border != TextBorderStyle.NONE || (lm.getPageArea()!=null && lm.getPageArea().getAlignment()==PageAreaProperties.Alignment.BOTTOM && !pageArea.isEmpty())) {
				float areaSize = (lm.getPageArea()!=null && lm.getPageArea().getAlignment()==PageAreaProperties.Alignment.BOTTOM ? pageAreaSpaceNeeded() : 0);
				while (Math.ceil(rowsNeeded(ret, lm.getRowSpacing()) + areaSize) < getFlowHeight() + headerHeight) {
					ret.add(new RowImpl());
				}
				if (lm.getPageArea()!=null && lm.getPageArea().getAlignment()==PageAreaProperties.Alignment.BOTTOM && !pageArea.isEmpty()) {
					ret.addAll(before);
					ret.addAll(pageArea);
					ret.addAll(after);
				}
				ret.addAll(renderFields(lm, t.getFooter(), filter));
			}
		}
		return ret;
	}

	@Override
	public List<Row> getRows() {

		try {
			TextBorderStyle border = master.getBorder();
			if (border == null) {
				border = TextBorderStyle.NONE;
			}
			List<RowImpl> ret = buildPageRows(border);
			
			LayoutMaster lm = master;
			ArrayList<Row> ret2 = new ArrayList<>();
			{
				final int pagenum = getPageIndex() + 1;
				TextBorder tb = null;

				int fsize = border.getLeftBorder().length() + border.getRightBorder().length();
				final int pageMargin = ((pagenum % 2 == 0) ? lm.getOuterMargin() : lm.getInnerMargin());
				int w = master.getFlowWidth() + fsize + pageMargin;

				tb = new TextBorder.Builder(w, fcontext.getSpaceCharacter()+"")
						.style(border)
						.outerLeftMargin(pageMargin)
						.padToSize(!TextBorderStyle.NONE.equals(border))
						.build();
				if (!TextBorderStyle.NONE.equals(border)) {
					RowImpl r = new RowImpl(tb.getTopBorder());
					DistributedRowSpacing rs = distributeRowSpacing(lm.getRowSpacing(), true);
					r.setRowSpacing(rs.spacing);
					ret2.add(r);
				}
				String res;

				for (RowImpl row : ret) {
					res = "";
					if (row.getChars().length() > 0) {
						// remove trailing whitespace
						String chars = trailingWs.matcher(row.getChars()).replaceAll("");
						//if (!TextBorderStyle.NONE.equals(frame)) {
							res = tb.addBorderToRow(
									padLeft(master.getFlowWidth(), chars, row.getLeftMargin(), row.getRightMargin(), row.getAlignment(), fcontext.getSpaceCharacter()), 
									"");
						//} else {
						//	res = StringTools.fill(getMarginCharacter(), pageMargin + row.getLeftMargin()) + chars;
						//}
					} else {
						if (!TextBorderStyle.NONE.equals(border)) {
							res = tb.addBorderToRow(row.getLeftMargin().getContent(), row.getRightMargin().getContent());
						} else {
							if (!row.getLeftMargin().isSpaceOnly() || !row.getRightMargin().isSpaceOnly()) {
								res = TextBorder.addBorderToRow(
									lm.getFlowWidth(), row.getLeftMargin().getContent(), "", row.getRightMargin().getContent(), fcontext.getSpaceCharacter()+"");
							} else {
								res = "";
							}
						}
					}
					int rowWidth = StringTools.length(res) + pageMargin;
					String r = res;
					if (rowWidth > master.getPageWidth()) {
						throw new PaginatorException("Row is too long (" + rowWidth + "/" + master.getPageWidth() + ") '" + res + "'");
					}
					RowImpl r2 = new RowImpl(r);
					ret2.add(r2);
					Float rs2 = row.getRowSpacing();
					if (!TextBorderStyle.NONE.equals(border)) {
						DistributedRowSpacing rs = distributeRowSpacing(rs2, true);
						r2.setRowSpacing(rs.spacing);
						//don't add space to the last line
						if (row!=ret.get(ret.size()-1)) {
							RowImpl s = null;
							for (int i = 0; i < rs.lines-1; i++) {
								s = new RowImpl(tb.addBorderToRow(row.getLeftMargin().getContent(), row.getRightMargin().getContent()));
								s.setRowSpacing(rs.spacing);
								ret2.add(s);
							}
						}
					} else {
						r2.setRowSpacing(rs2);
					}
					
				}
				if (!TextBorderStyle.NONE.equals(border)) {
					ret2.add(new RowImpl(tb.getBottomBorder()));
				}
			}
			if (ret2.size()>0) {
				RowImpl last = ((RowImpl)ret2.get(ret2.size()-1));
				if (lm.getRowSpacing()!=1) {
					//set row spacing on the last row to 1.0
					last.setRowSpacing(1f);
				} else if (last.getRowSpacing()!=null) {
					//ignore row spacing on the last row if overall row spacing is 1.0
					last.setRowSpacing(null);
				}
			}
			return ret2;
		} catch (PaginatorException e) {
			throw new RuntimeException("Pagination failed.", e);
		}
	}
	
	static String padLeft(int w, RowImpl row, char space) {
		String chars = trailingWs.matcher(row.getChars()).replaceAll("");
		return padLeft(w, chars, row.getLeftMargin(), row.getRightMargin(), row.getAlignment(), space);
	}
	
	static String padLeft(int w, String text, MarginProperties leftMargin, MarginProperties rightMargin, FormattingTypes.Alignment align, char space) {
		if ("".equals(text) && leftMargin.isSpaceOnly() && rightMargin.isSpaceOnly()) {
			return "";
		} else {
			String r = leftMargin.getContent() + StringTools.fill(space, align.getOffset(w - (leftMargin.getContent().length() + rightMargin.getContent().length() + text.length()))) + text;
			if (rightMargin.isSpaceOnly()) {
				return r;
			} else {
				return r + StringTools.fill(space, w - r.length() - rightMargin.getContent().length()) + rightMargin.getContent();
			}
		}
	}

	/**
	 * Get the page index, offset included, zero based. Don't assume
	 * getPageIndex() % 2 == getPageOrdinal() % 2
	 * 
	 * @return returns the page index in the sequence (zero based)
	 */
	public int getPageIndex() {
		return pageIndex;
	}
	
	/**
	 * Gets the external page number
	 * @return the external page number
	 */
	public int getPageNumber() {
		return pageIndex + 1;
	}
	
	/**
	 * Gets the ordinal number for the page in the page sequence list
	 * @return returns the ordinal number for the page
	 */
	public int getPageOrdinal() {
		return pageIndex-getSequenceParent().getPageNumberOffset();
	}
	
	int getPageId() {
		return getSequenceParent().getGlobalStartIndex()+getPageOrdinal();
	}

	public PageSequence getSequenceParent() {
		return parent;
	}
	
	public void setSequenceParent(PageSequence seq) {
		this.parent = seq;
	}
	
	/**
	 * Gets the flow height for this page, i.e. the number of rows available for text flow
	 * @return returns the flow height
	 */
	public int getFlowHeight() {
		return flowHeight;
	}

	public boolean isVolumeBreak() {
		return isVolBreak;
	}

	public void setVolumeBreak(boolean value) {
		isVolBreak = value;
	}
	
	
	private List<RowImpl> renderFields(LayoutMaster lm, List<FieldList> fields, BrailleTranslator translator) throws PaginatorException {
		ArrayList<RowImpl> ret = new ArrayList<>();
		for (FieldList row : fields) {
			try {
				RowImpl r = new RowImpl(distribute(row, lm.getFlowWidth(), fcontext.getSpaceCharacter()+"", translator));
				r.setRowSpacing(row.getRowSpacing());
				ret.add(r);
			} catch (PaginatorToolsException e) {
				throw new PaginatorException("Error while rendering header", e);
			}
		}
		return ret;
	}
	
	private String distribute(FieldList chunks, int width, String padding, BrailleTranslator translator) throws PaginatorToolsException {
		ArrayList<String> chunkF = new ArrayList<>();
		for (Field f : chunks.getFields()) {
			DefaultTextAttribute.Builder b = new DefaultTextAttribute.Builder(null);
			String resolved = softHyphen.matcher(resolveField(f, this, b)).replaceAll("");
			Translatable.Builder tr = Translatable.text(fcontext.getConfiguration().isMarkingCapitalLetters()?resolved:resolved.toLowerCase()).
										hyphenate(false);
			if (resolved.length()>0) {
				tr.attributes(b.build(resolved.length()));
			}
			try {
				chunkF.add(translator.translate(tr.build()).getTranslatedRemainder());
			} catch (TranslationException e) {
				throw new PaginatorToolsException(e);
			}
		}
		return PaginatorTools.distribute(chunkF, width, padding, PaginatorTools.DistributeMode.EQUAL_SPACING_TRUNCATE);
	}
	
	private static String resolveField(Field field, PageImpl p, DefaultTextAttribute.Builder b) {
		String ret;
		DefaultTextAttribute.Builder b2 = new DefaultTextAttribute.Builder(field.getTextStyle());
		if (field instanceof CompoundField) {
			ret = resolveCompoundField((CompoundField)field, p, b2);
		} else if (field instanceof MarkerReferenceField) {
			MarkerReferenceField f2 = (MarkerReferenceField)field;
			PageImpl start;
			if (f2.getSearchScope()==MarkerSearchScope.SPREAD) {
				start = p.getPageInVolumeWithOffset(f2.getOffset(), p.shouldAdjustOutOfBounds(f2));
			} else {
				start = p.getPageInSequenceWithOffset(f2.getOffset(), p.shouldAdjustOutOfBounds(f2));
			}
			ret = findMarker(start, f2);
		} else if (field instanceof CurrentPageField) {
			ret = resolveCurrentPageField((CurrentPageField)field, p);
		} else {
			ret = field.toString();
		}
		if (ret.length()>0) {
			b.add(b2.build(ret.length()));
		}
		return ret;
	}

	private static String resolveCompoundField(CompoundField f, PageImpl p, DefaultTextAttribute.Builder b) {
		StringBuffer sb = new StringBuffer();
		for (Field f2 : f) {
			String res = resolveField(f2, p, b);
			sb.append(res);
		}
		return sb.toString();
	}

	private static String findMarker(PageImpl page, MarkerReferenceField markerRef) {
		if (page==null) {
			return "";
		}
		if (markerRef.getSearchScope()==MarkerSearchScope.VOLUME || markerRef.getSearchScope()==MarkerSearchScope.DOCUMENT) {
			throw new RuntimeException("Marker reference scope not implemented: " + markerRef.getSearchScope());
		}
		int dir = 1;
		int index = 0;
		int count = 0;
		List<Marker> m;
		if (markerRef.getSearchScope() == MarkerReferenceField.MarkerSearchScope.PAGE_CONTENT) {
			m = page.getContentMarkers();
		} else {
			m = page.getMarkers();
		}
		if (markerRef.getSearchDirection() == MarkerReferenceField.MarkerSearchDirection.BACKWARD) {
			dir = -1;
			index = m.size()-1;
		}
		while (count < m.size()) {
			Marker m2 = m.get(index);
			if (m2.getName().equals(markerRef.getName())) {
				return m2.getValue();
			}
			index += dir; 
			count++;
		}
		PageImpl next = null;
		if (markerRef.getSearchScope() == MarkerReferenceField.MarkerSearchScope.SEQUENCE ||
			markerRef.getSearchScope() == MarkerSearchScope.SHEET && page.isWithinSheetScope(dir) //||
			//markerRef.getSearchScope() == MarkerSearchScope.SPREAD && page.isWithinSequenceSpreadScope(dir)
			) {
			next = page.getPageInSequenceWithOffset(dir, false);
		} //else if (markerRef.getSearchScope() == MarkerSearchScope.SPREAD && page.isWithinDocumentSpreadScope(dir)) {
			else if (markerRef.getSearchScope() == MarkerSearchScope.SPREAD && page.isWithinVolumeSpreadScope(dir)) {
			next = page.getPageInVolumeWithOffset(dir, false);
		}
		if (next!=null) {
			return findMarker(next, markerRef);
		} else {
			return "";
		}
	}
	
	private boolean shouldAdjustOutOfBounds(MarkerReferenceField markerRef) {
		if (markerRef.getSearchDirection()==MarkerSearchDirection.FORWARD && markerRef.getOffset()>=0 ||
			markerRef.getSearchDirection()==MarkerSearchDirection.BACKWARD && markerRef.getOffset()<=0) {
			return false;
		} else {
			switch(markerRef.getSearchScope()) {
			case PAGE_CONTENT: case PAGE:
				return false;
			case SEQUENCE: case VOLUME: case DOCUMENT:
				return true;
			case SPREAD:
				//return  isWithinSequenceSpreadScope(markerRef.getOffset());				
				//return  isWithinDocumentSpreadScope(markerRef.getOffset());
				return isWithinVolumeSpreadScope(markerRef.getOffset());
			case SHEET:
				return isWithinSheetScope(markerRef.getOffset()) && 
						markerRef.getSearchDirection()==MarkerSearchDirection.BACKWARD;
			default:
				throw new RuntimeException("Error in code. Missing implementation for value: " + markerRef.getSearchScope());
			}
		}
	}

	/*
	 * This method is unused at the moment, but could be activated once additional scopes are added to the API,
	 * namely SPREAD_WITHIN_SEQUENCE
	 */
	@SuppressWarnings("unused") 
	private boolean isWithinSequenceSpreadScope(int offset) {
		return 	offset==0 ||
				(
					getSequenceParent().getLayoutMaster().duplex() && 
					(
						(offset == 1 && getPageOrdinal() % 2 == 1) ||
						(offset == -1 && getPageOrdinal() % 2 == 0)
					)
				);
	}
	
	/*
	 * This method is unused at the moment, but could be activated if additional scopes are added to the API,
	 * namely SPREAD_WITHIN_DOCUMENT
	 */
	@SuppressWarnings("unused")
	private boolean isWithinDocumentSpreadScope(int offset) {
		if (offset==0) {
			return true;
		} else {
			PageImpl n = getPageInDocumentWithOffset(offset, false);
			return isWithinSpreadScope(offset, n);
		}
	}
	
	private boolean isWithinVolumeSpreadScope(int offset) {
		if (offset==0) {
			return true;
		} else {
			PageImpl n = getPageInVolumeWithOffset(offset, false);
			return isWithinSpreadScope(offset, n);
		}
	}
	
	private boolean isWithinSpreadScope(int offset, PageImpl n) {
		if (n==null) { 
			return ((offset == 1 && getPageOrdinal() % 2 == 1) ||
					(offset == -1 && getPageOrdinal() % 2 == 0));
		} else {
			return (
					(offset == 1 && getPageOrdinal() % 2 == 1 && getSequenceParent().getLayoutMaster().duplex()==true) ||
					(offset == -1 && getPageOrdinal() % 2 == 0 && n.getSequenceParent().getLayoutMaster().duplex()==true && n.getPageOrdinal() % 2 == 1)
				);
		}
	}
	
	private boolean isWithinSheetScope(int offset) {
		return 	offset==0 || 
				(
					getSequenceParent().getLayoutMaster().duplex() &&
					(
						(offset == 1 && getPageOrdinal() % 2 == 0) ||
						(offset == -1 && getPageOrdinal() % 2 == 1)
					)
				);
	}
	
	private PageImpl getPageInSequenceWithOffset(int offset, boolean adjustOutOfBounds) {
		if (offset==0) {
			return this;
		} else {
			PageSequence parent = getSequenceParent();
			int next = getPageIndex() - parent.getPageNumberOffset() + offset;
			if (adjustOutOfBounds) {
				next = Math.min(parent.getPageCount()-1, Math.max(0, next));
			}
			if (next < parent.getPageCount() && next >= 0) {
				return parent.getPage(next);
			}
			return null;
		}
	}
	
	private PageImpl getPageInVolumeWithOffset(int offset, boolean adjustOutOfBounds) {
		if (offset==0) {
			return this;
		} else {
			return getPageInScope(getSequenceParent().getParent().getContentsInVolume(getVolumeNumber()), offset, adjustOutOfBounds);
		}
	}

	private PageImpl getPageInDocumentWithOffset(int offset, boolean adjustOutOfBounds) {
		if (offset==0) {
			return this;
		} else {
			return getPageInScope(getSequenceParent().getParent().getPageView(), offset, adjustOutOfBounds);
		}
	}
	
	private PageImpl getPageInScope(PageView pageView, int offset, boolean adjustOutOfBounds) {
		if (offset==0) {
			return this;
		} else {
			if (pageView!=null) {
				List<PageImpl> scope = pageView.getPages();
				int next = pageView.toLocalIndex(getPageId())+offset;
				int size = scope.size();
				if (adjustOutOfBounds) {
					next = Math.min(size-1, Math.max(0, next));
				}
				if (next < size && next >= 0) {
					return scope.get(next);
				}
			}
			return null;
		}
	}
	
	private static String resolveCurrentPageField(CurrentPageField f, PageImpl p) {
		int pagenum = p.getPageIndex() + 1;
		return f.getNumeralStyle().format(pagenum);
	}
	
	/**
	 * Divide a row-spacing value into several rows with a row-spacing < 2.
	 * <p>E.g. A row spacing of 2.5 will return:</p>
	 * <dl>
	 * 	<dt>RowSpacing.spacing</dt><dd>1.25</dd> 
	 *  <dt>RowSpacing.lines</dt><dd>2</dd>
	 * </dl>
	 * @param rs
	 * @return
	 */
	private DistributedRowSpacing distributeRowSpacing(Float rs, boolean nullIfEqualToDefault) {
		if (rs == null) {
			//use default
			rs = this.master.getRowSpacing();
		}
		int ins = Math.max((int)Math.floor(rs), 1);
		Float spacing = rs / ins;
		if (nullIfEqualToDefault && spacing.equals(this.master.getRowSpacing())) {
			return new DistributedRowSpacing(null, ins);
		} else {
			return new DistributedRowSpacing(spacing, ins);
		}
	}
	
	private class DistributedRowSpacing {
		private final Float spacing;
		private final int lines;
		DistributedRowSpacing(Float s, int l) {
			this.spacing = s;
			this.lines = l;
		}
	}
	
	void setKeepWithPreviousSheets(int value) {
		keepPreviousSheets = Math.max(value, keepPreviousSheets);
	}
	
	void setAllowsVolumeBreak(boolean value) {
		this.isVolBreakAllowed = value;
	}

	public boolean allowsVolumeBreak() {
		return isVolBreakAllowed;
	}

	public int keepPreviousSheets() {
		return keepPreviousSheets;
	}

	PageTemplate getPageTemplate() {
		return template;
	}

	int getVolumeNumber() {
		return volumeNumber;
	}

	void setVolumeNumber(int volumeNumber) {
		this.volumeNumber = volumeNumber;
	}
	
	Integer getAvoidVolumeBreakAfter() {
		return volumeBreakAfterPriority;
	}
	
	void setAvoidVolumeBreakAfter(Integer value) {
		this.volumeBreakAfterPriority = value;
	}

}
