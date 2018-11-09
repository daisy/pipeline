package org.daisy.dotify.formatter.impl.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.formatter.PageAreaProperties;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.writer.Row;
import org.daisy.dotify.formatter.impl.core.BorderManager;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.core.HeightCalculator;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;
import org.daisy.dotify.formatter.impl.core.PageTemplate;
import org.daisy.dotify.formatter.impl.core.PaginatorException;
import org.daisy.dotify.formatter.impl.datatype.VolumeKeepPriority;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.formatter.impl.search.PageDetails;
import org.daisy.dotify.formatter.impl.writer.Page;


//FIXME: scope spread is currently implemented using document wide scope, i.e. across volume boundaries. This is wrong, but is better than the previous sequence scope.
/**
 * Provides a page object.
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
	private final int pageMargin;
	private final BorderManager finalRows;

	private boolean hasRows;
	private boolean isVolBreakAllowed;
	private int keepPreviousSheets;
	private VolumeKeepPriority volumeBreakAfterPriority;
	private final BrailleTranslator filter;
	
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
		this.pageMargin = ((details.getPageId().getOrdinal() % 2 == 0) ? master.getInnerMargin() : master.getOuterMargin());
		this.finalRows = new BorderManager(master, fcontext, pageMargin);
		this.hasRows = false;
		this.filter = fcontext.getDefaultTranslator();
	}
	
	public PageImpl(PageImpl template) {
		this.fieldResolver = template.fieldResolver;
		this.details = template.details;
		this.master = template.master;
		this.fcontext = template.fcontext;
		this.pageAreaTemplate = template.pageAreaTemplate;
	    this.pageArea = new ArrayList<>(template.pageArea);
	    this.anchors = new ArrayList<>(template.anchors);
	    this.identifiers = new ArrayList<>(template.identifiers);
		this.flowHeight = template.flowHeight;
		this.template = template.template;
		this.pageMargin = template.pageMargin;
		this.finalRows = new BorderManager(template.finalRows);

		this.hasRows = template.hasRows;
		this.isVolBreakAllowed = template.isVolBreakAllowed;
		this.keepPreviousSheets = template.keepPreviousSheets;
		this.volumeBreakAfterPriority = template.volumeBreakAfterPriority;
		this.filter = template.filter;
	}
	
	public static PageImpl copyUnlessNull(PageImpl page) {
		return page==null?null:new PageImpl(page);
	}

	void addToPageArea(List<RowImpl> block) {
		if (hasRows) {
			throw new IllegalStateException("Page area must be added before adding rows.");
		}
		pageArea.addAll(block);
	}
	
	void newRow(RowImpl r) {
		if (!hasRows) {
			//add the header
	        finalRows.addAll(fieldResolver.renderFields(getDetails(), template.getHeader(), filter));
	        //add the top page area
			addTopPageArea();
			getDetails().startsContentMarkers();
			hasRows = true;
		}
		finalRows.addRow(r);
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
	 * @param rows
	 * @param defSpacing a value >= 1.0
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
	
	private void addTopPageArea() {
		if (hasTopPageArea()) {
			finalRows.addAll(pageAreaTemplate.getBefore());
			finalRows.addAll(pageArea);
			finalRows.addAll(pageAreaTemplate.getAfter());
		}
	}

	/*
	 * The assumption is made that by now all pages have been added to the parent sequence and volume scopes
	 * have been set on the page struct.
	 */
	@Override
	public List<Row> getRows() {
		try {
			if (!finalRows.isClosed()) {
				if (!hasRows) { // the header hasn't been added yet 
					//add the header
			        finalRows.addAll(fieldResolver.renderFields(getDetails(), template.getHeader(), filter));
			      //add top page area
					addTopPageArea();
				}
		        float headerHeight = template.getHeaderHeight();
		        if (!template.getFooter().isEmpty() || finalRows.hasBorder() || (master.getPageArea()!=null && master.getPageArea().getAlignment()==PageAreaProperties.Alignment.BOTTOM && !pageArea.isEmpty())) {
		            float areaSize = (master.getPageArea()!=null && master.getPageArea().getAlignment()==PageAreaProperties.Alignment.BOTTOM ? pageAreaSpaceNeeded() : 0);
		            while (Math.ceil(finalRows.getOffsetHeight() + areaSize) < getFlowHeight() + headerHeight) {
						finalRows.addRow(new RowImpl());
					}
					if (master.getPageArea()!=null && master.getPageArea().getAlignment()==PageAreaProperties.Alignment.BOTTOM && !pageArea.isEmpty()) {
						finalRows.addAll(pageAreaTemplate.getBefore());
						finalRows.addAll(pageArea);
						finalRows.addAll(pageAreaTemplate.getAfter());
					}
		            finalRows.addAll(fieldResolver.renderFields(getDetails(), template.getFooter(), filter));
				}
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

}
