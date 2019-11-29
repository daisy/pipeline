package org.daisy.dotify.formatter.impl.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.daisy.dotify.api.formatter.FieldList;
import org.daisy.dotify.api.formatter.MarginRegion;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.formatter.MarkerIndicatorRegion;
import org.daisy.dotify.api.formatter.NoField;
import org.daisy.dotify.api.formatter.PageAreaProperties;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.writer.Row;
import org.daisy.dotify.formatter.impl.common.Page;
import org.daisy.dotify.formatter.impl.core.BorderManager;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.core.HeightCalculator;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;
import org.daisy.dotify.formatter.impl.core.PageTemplate;
import org.daisy.dotify.formatter.impl.core.PaginatorException;
import org.daisy.dotify.formatter.impl.row.MarginProperties;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.formatter.impl.search.PageDetails;
import org.daisy.dotify.formatter.impl.search.VolumeKeepPriority;


//FIXME: scope spread is currently implemented using document wide scope, i.e. across volume boundaries. This is wrong, but is better than the previous sequence scope.

/**
 * <p>Provides a {@link Page} object.</p>
 *
 * @author Joel Håkansson
 */
public class PageImpl implements Page {
	private final FieldResolver fieldResolver;
	private final PageDetails details;
	private final LayoutMaster master;
	private final FormatterContext fcontext;
	private final PageAreaContent pageAreaTemplate;
    private final ArrayList<RowImpl> pageArea;
    private final ArrayList<String> anchors;
    private final ArrayList<String> identifiers;
	private final int flowHeight;
	private final PageTemplate template;
	private final BorderManager finalRows;

	private boolean hasRows;
	private boolean isVolBreakAllowed;
	private int keepPreviousSheets;
	private VolumeKeepPriority volumeBreakAfterPriority;
	private final BrailleTranslator filter;
	
	private int renderedHeaderRows;
	private int renderedFooterRows;
	private boolean topPageAreaProcessed;
	private boolean bottomPageAreaProcessed;
	
	public PageImpl(FieldResolver fieldResolver, PageDetails details, LayoutMaster master, FormatterContext fcontext, PageAreaContent pageAreaTemplate) {
		this.fieldResolver = fieldResolver;
		this.details = details;
		this.master = master;
		this.fcontext = fcontext;
		this.pageAreaTemplate = pageAreaTemplate;

		this.pageArea = new ArrayList<>();
		this.anchors = new ArrayList<>();
		this.identifiers = new ArrayList<>();
		this.template = master.getTemplate(details.getPageNumber());
        this.flowHeight = master.getFlowHeight(template);
		this.isVolBreakAllowed = true;
		this.keepPreviousSheets = 0;
		this.volumeBreakAfterPriority = VolumeKeepPriority.empty();
		if (master.duplex() && details.getPageId().getOrdinal() % 2 == 1) {
			this.finalRows = new BorderManager(master, fcontext, master.getOuterMargin(), master.getInnerMargin());
		} else {
			this.finalRows = new BorderManager(master, fcontext, master.getInnerMargin(), master.getOuterMargin());
		}
		this.hasRows = false;
		this.filter = fcontext.getDefaultTranslator();
		this.renderedHeaderRows = 0;
		this.renderedFooterRows = 0;
		this.topPageAreaProcessed = false;
		this.bottomPageAreaProcessed = false;
	}

	void addToPageArea(List<RowImpl> block) {
		if (hasRows) {
			throw new IllegalStateException("Page area must be added before adding rows.");
		}
		pageArea.addAll(block);
	}
	
	void newRow(RowImpl r) {
		hasRows = true;
		while (renderedHeaderRows<template.getHeader().size()) {
			FieldList fields = template.getHeader().get(renderedHeaderRows);
			renderedHeaderRows++;
			if (fields.getFields().stream().anyMatch(v->v instanceof NoField)) {
				//
				RowImpl r2 = fieldResolver.renderField(getDetails(), fields, filter, Optional.of(r));
				finalRows.addRow(r2.shouldAdjustForMargin()?addMarginRegion(r2):r2);
				addRowDetails(r);
				return;
			} else {
				finalRows.addRow(fieldResolver.renderField(getDetails(), fields, filter, Optional.empty()));
			}
		}
		if (renderedHeaderRows>=template.getHeader().size()) {
			if (!topPageAreaProcessed) {
				addTopPageArea();
				getDetails().startsContentMarkers();
				topPageAreaProcessed = true;
			}
			if (hasBodyRowsLeft()) {
				//
				finalRows.addRow(r.shouldAdjustForMargin()?addMarginRegion(r):r);
			} else {
				if (!bottomPageAreaProcessed) {
					addBottomPageArea();
					bottomPageAreaProcessed = true;
				}
				while (renderedFooterRows<template.getFooter().size()) {
					FieldList fields = template.getFooter().get(renderedFooterRows);
					renderedFooterRows++;
					if (fields.getFields().stream().anyMatch(v->v instanceof NoField)) {
						//
						RowImpl r2 = fieldResolver.renderField(getDetails(), fields, filter, Optional.of(r));
						finalRows.addRow(r2.shouldAdjustForMargin()?addMarginRegion(r2):r2);
						addRowDetails(r);
						return;
					} else {
						finalRows.addRow(fieldResolver.renderField(getDetails(), fields, filter, Optional.empty()));
					}
				}
			}
			addRowDetails(r);
		}
	}
	
	@FunctionalInterface
	interface MarkerRef {
		boolean hasMarkerWithName(String name);
	}
	
	private RowImpl addMarginRegion(RowImpl r) {
		RowImpl.Builder b = new RowImpl.Builder(r);
		MarkerRef rf = r::hasMarkerWithName;
		MarginProperties margin = r.getLeftMargin();
		for (MarginRegion mr : getPageTemplate().getLeftMarginRegion()) {
			margin = getMarginRegionValue(mr, rf, false).append(margin);
		}
		b.leftMargin(margin);
		margin = r.getRightMargin();
		for (MarginRegion mr : getPageTemplate().getRightMarginRegion()) {
			margin = margin.append(getMarginRegionValue(mr, rf, true));
		}
		b.rightMargin(margin);
		return b.build();
	}
	
	private MarginProperties getMarginRegionValue(MarginRegion mr, MarkerRef r, boolean rightSide) throws PaginatorException {
		String ret = "";
		int w = mr.getWidth();
		if (mr instanceof MarkerIndicatorRegion) {
			ret = firstMarkerForRow(r, (MarkerIndicatorRegion)mr);
			if (ret.length()>0) {
				try {
					ret = fcontext.getDefaultTranslator().translate(Translatable.text(fcontext.getConfiguration().isMarkingCapitalLetters()?ret:ret.toLowerCase()).build()).getTranslatedRemainder();
				} catch (TranslationException e) {
					throw new PaginatorException("Failed to translate: " + ret, e);
				}
			}
			boolean spaceOnly = ret.length()==0;
			if (ret.length()<w) {
				StringBuilder sb = new StringBuilder();
				if (rightSide) {
					while (sb.length()<w-ret.length()) { sb.append(fcontext.getSpaceCharacter()); }
					sb.append(ret);
				} else {
					sb.append(ret);				
					while (sb.length()<w) { sb.append(fcontext.getSpaceCharacter()); }
				}
				ret = sb.toString();
			} else if (ret.length()>w) {
				throw new PaginatorException("Cannot fit " + ret + " into a margin-region of size "+ mr.getWidth());
			}
			return new MarginProperties(ret, spaceOnly);
		} else {
			throw new PaginatorException("Unsupported margin-region type: " + mr.getClass().getName());
		}
	}
	
	private String firstMarkerForRow(MarkerRef r, MarkerIndicatorRegion mrr) {
		return mrr.getIndicators().stream()
				.filter(mi -> r.hasMarkerWithName(mi.getName()))
				.map(mi -> mi.getIndicator())
				.findFirst().orElse("");
	}
	
	private void addRowDetails(RowImpl r) {
		getDetails().getMarkers().addAll(r.getMarkers());
		anchors.addAll(r.getAnchors());
		identifiers.addAll(r.getIdentifiers());
	}
	
	void addMarkers(List<Marker> m) {
		getDetails().getMarkers().addAll(m);
	}
	
	public List<String> getAnchors() {
		return anchors;
	}
	
	void addIdentifiers(List<String> ids) {
		identifiers.addAll(ids);
	}
	
	public List<String> getIdentifiers() {
		return identifiers;
	}
	
	/**
	 * Gets the page space needed to render the rows. 
	 * @param rows the rows to render
	 * @param defSpacing a value &gt;= 1.0
	 * @return returns the space, in rows
	 */
	static float rowsNeeded(Collection<? extends Row> rows, float defSpacing) {
		HeightCalculator c = new HeightCalculator(defSpacing);
		c.addRows(rows);
		return c.getCurrentHeight();
	}

	// vertical position of the next body row, measured from the top edge of the page body area
	float currentPosition() {
		float pos = finalRows.getOffsetHeight();
		float headerHeight = template.getHeaderHeight();
		if (pos < headerHeight) {
			if (hasTopPageArea()) {
				return pageAreaSpaceNeeded();
			} else {
				return 0;
			}
		} else {
			return pos - headerHeight;
		}
	}
	
	// space available for body rows
	// - this excludes space used for page-area
	float spaceAvailableInFlow() {
		return getFlowHeight() + template.getHeaderHeight() - pageAreaSpaceNeeded() - finalRows.getOffsetHeight();
	}
	
	private float staticAreaSpaceNeeded() {
		return rowsNeeded(pageAreaTemplate.getBefore(), master.getRowSpacing()) + rowsNeeded(pageAreaTemplate.getAfter(), master.getRowSpacing());
	}
	
	float pageAreaSpaceNeeded() {
		return (!pageArea.isEmpty() ? staticAreaSpaceNeeded() + rowsNeeded(pageArea, master.getRowSpacing()) : 0);
	}
	
	private boolean hasTopPageArea() {
		return master.getPageArea() != null
			&& master.getPageArea().getAlignment() == PageAreaProperties.Alignment.TOP
			&& !pageArea.isEmpty();
	}
	
	private boolean hasBottomPageArea() {
		return master.getPageArea()!=null
			&& master.getPageArea().getAlignment() == PageAreaProperties.Alignment.BOTTOM
			&& !pageArea.isEmpty();
	}
	
	private void addTopPageArea() {
		if (hasTopPageArea()) {
			finalRows.addAll(pageAreaTemplate.getBefore());
			finalRows.addAll(pageArea);
			finalRows.addAll(pageAreaTemplate.getAfter());
		}
	}
	
	private void addBottomPageArea() {
		if (hasBottomPageArea()) {
			finalRows.addAll(pageAreaTemplate.getBefore());
			finalRows.addAll(pageArea);
			finalRows.addAll(pageAreaTemplate.getAfter());
		}
	}
	
	private boolean addHeaderIfNotAdded() {
		if (!hasRows) { // the header hasn't been added yet 
			//add the header
			for (FieldList fields : template.getHeader()) {
				finalRows.addRow(fieldResolver.renderField(getDetails(), fields, filter, Optional.empty()));
			}
			//add top page area
			addTopPageArea();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Adds the footer area if not already added. Note that this area also contains the page area if aligned to be the bottom.
	 */
	private void addBottomPageAreaAndFooterIfNotAdded() {
		if (!template.getFooter().isEmpty() || finalRows.hasBorder() || (master.getPageArea()!=null && master.getPageArea().getAlignment()==PageAreaProperties.Alignment.BOTTOM && !pageArea.isEmpty())) {
			while (hasBodyRowsLeft()) {
				finalRows.addRow(new RowImpl());
			}
			if (!bottomPageAreaProcessed) {
				addBottomPageArea();
				bottomPageAreaProcessed = true;
			}
			while (renderedFooterRows<template.getFooter().size()) {
				FieldList fields = template.getFooter().get(renderedFooterRows);
				renderedFooterRows++;
				finalRows.addRow(fieldResolver.renderField(getDetails(), fields, filter, Optional.empty()));
			}
		}
	}
	
	private boolean hasBodyRowsLeft() {
		float headerHeight = template.getHeaderHeight();
		float areaSize = (master.getPageArea()!=null && master.getPageArea().getAlignment()==PageAreaProperties.Alignment.BOTTOM ? pageAreaSpaceNeeded() : 0);
		return Math.ceil(finalRows.getOffsetHeight() + areaSize) < getFlowHeight() + headerHeight - template.validateAndAnalyzeHeader() - template.validateAndAnalyzeFooter();
	}

	/*
	 * The assumption is made that by now all pages have been added to the parent sequence and volume scopes
	 * have been set on the page struct.
	 */
	@Override
	public List<Row> getRows() {
		try {
			if (!finalRows.isClosed()) {
				addHeaderIfNotAdded();
				addBottomPageAreaAndFooterIfNotAdded();
			}
			return finalRows.getRows();
		} catch (PaginatorException e) {
			throw new RuntimeException("Pagination failed.", e);
		}
	}

	
	/**
	 * Get the page number, one based.
	 * 
	 * @return returns the page number
	 */
	public int getPageNumber() {
		return details.getPageNumber();
	}

	/**
	 * Gets the flow height for this page, i.e. the number of rows available for text flow
	 * @return returns the flow height
	 */
	int getFlowHeight() {
		return flowHeight;
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
	
	public VolumeKeepPriority getAvoidVolumeBreakAfter() {
		return volumeBreakAfterPriority;
	}
	
	void setAvoidVolumeBreakAfter(VolumeKeepPriority value) {
		this.volumeBreakAfterPriority = value;
	}

	public PageDetails getDetails() {
		return details;
	}

	boolean hasRows() {
		return hasRows;
	}
}
