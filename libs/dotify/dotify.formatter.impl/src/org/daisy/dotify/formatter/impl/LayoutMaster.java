package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;

import org.daisy.dotify.api.formatter.Condition;
import org.daisy.dotify.api.formatter.LayoutMasterBuilder;
import org.daisy.dotify.api.formatter.LayoutMasterProperties;
import org.daisy.dotify.api.formatter.PageAreaBuilder;
import org.daisy.dotify.api.formatter.PageAreaProperties;
import org.daisy.dotify.api.formatter.PageTemplateBuilder;
import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.api.writer.SectionProperties;

/**
 * Specifies the layout of a paged media.
 * @author Joel Håkansson
 */
class LayoutMaster implements LayoutMasterBuilder, SectionProperties {
	private final LayoutMasterProperties props; 
	private final ArrayList<PageTemplate> templates;
	private final PageTemplate defaultPageTemplate;
	private PageAreaBuilderImpl pageArea;
	private final FormatterCoreContext fc;

	public LayoutMaster(FormatterCoreContext fc, LayoutMasterProperties props) {
		this.fc = fc;
		this.templates = new ArrayList<>();
		this.props = props;
		this.defaultPageTemplate = new PageTemplate();
		this.pageArea = null;
	}
	
	@Override
	public PageTemplateBuilder newTemplate(Condition c) {
		PageTemplate p = new PageTemplate(c);
		templates.add(p);
		return p;
	}

	/**
	 * Gets the template for the specified page number
	 * @param pagenum the page number to get the template for
	 * @return returns the template
	 */
	public PageTemplate getTemplate(int pagenum) {
		for (PageTemplate t : templates) {
			if (t.appliesTo(pagenum)) { return t; }
		}
		return defaultPageTemplate;
	}

	/**
	 * Gets the page area for all pages using this master.
	 * @return returns the PageArea, or null if no page area is used.
	 */
	public PageAreaProperties getPageArea() {
		return (pageArea!=null?pageArea.getProperties():null);
	}
	
	PageAreaBuilderImpl getPageAreaBuilder() {
		return pageArea;
	}

	/**
	 * Gets the page width.
	 * An implementation must ensure that getPageWidth()=getFlowWidth()+getInnerMargin()+getOuterMargin()
	 * @return returns the page width
	 */
	public int getPageWidth() {
		return props.getPageWidth();
	}

	/**
	 * Gets the page height.
	 * An implementation must ensure that getPageHeight()=getHeaderHeight()+getFlowHeight()+getFooterHeight()
	 * @return returns the page height
	 */
	public int getPageHeight() {
		return props.getPageHeight();
	}

	/**
	 * Gets row spacing, in row heights. For example, use 2.0 for double row spacing and 1.0 for normal row spacing.
	 * @return returns row spacing
	 */
	public float getRowSpacing() {
		return props.getRowSpacing();
	}

	/**
	 * Returns true if output is intended on both sides of the sheets
	 * @return returns true if output is intended on both sides of the sheets
	 */
	public boolean duplex() {
		return props.duplex();
	}

	/**
	 * Gets the flow width
	 * @return returns the flow width
	 */
	public int getFlowWidth() {
		return props.getFlowWidth();
	}

	/**
	 * Gets the border.
	 * @return the border
	 */
	public TextBorderStyle getBorder() {
		return props.getBorder();
	}

	/**
	 * Gets inner margin
	 * @return returns the inner margin
	 */
	public int getInnerMargin() {
		return props.getInnerMargin();
	}

	/**
	 * Gets outer margin
	 * @return returns the outer margin
	 */
	public int getOuterMargin() {
		return props.getOuterMargin();
	}

	@Override
	public PageAreaBuilder setPageArea(PageAreaProperties properties) {
		pageArea = new PageAreaBuilderImpl(fc, properties);
		return pageArea;
	}
}
