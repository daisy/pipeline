package org.daisy.dotify.formatter.impl.core;

import java.util.ArrayList;

import org.daisy.dotify.api.formatter.Condition;
import org.daisy.dotify.api.formatter.LayoutMasterBuilder;
import org.daisy.dotify.api.formatter.LayoutMasterProperties;
import org.daisy.dotify.api.formatter.PageAreaBuilder;
import org.daisy.dotify.api.formatter.PageAreaProperties;
import org.daisy.dotify.api.formatter.PageTemplateBuilder;
import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.api.writer.SectionProperties;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;

/**
 * Specifies the layout of a paged media.
 * @author Joel Håkansson
 */
public class LayoutMaster implements LayoutMasterBuilder, SectionProperties {
	private final LayoutMasterProperties props; 
	private final ArrayList<PageTemplate> templates;
	private final PageTemplate defaultPageTemplate;
	private PageAreaBuilderImpl pageArea;
	private final FormatterCoreContext fc;

	public LayoutMaster(FormatterCoreContext fc, LayoutMasterProperties props) {
		this.fc = fc;
		this.templates = new ArrayList<>();
		this.props = props;
		this.defaultPageTemplate = new PageTemplate(props.getRowSpacing());
		this.pageArea = null;
	}
	
	/**
	 * Creates a new object with the same section properties
	 * as this object.
	 * @return returns a new object
	 */
	public SectionProperties newSectionProperties() {
		return new SectionProperties() {

			@Override
			public int getPageWidth() {
				return props.getPageWidth();
			}

			@Override
			public int getPageHeight() {
				return props.getPageHeight();
			}

			@Override
			public float getRowSpacing() {
				return props.getRowSpacing();
			}

			@Override
			public boolean duplex() {
				return props.duplex();
			}
		};
	}
	
	@Override
	public PageTemplateBuilder newTemplate(Condition c) {
		PageTemplate p = new PageTemplate(c, getRowSpacing());
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
	
	public PageAreaBuilderImpl getPageAreaBuilder() {
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
	DistributedRowSpacing distributeRowSpacing(Float rs, boolean nullIfEqualToDefault) {
		if (rs == null) {
			//use default
			rs = getRowSpacing();
		}
		int ins = Math.max((int)Math.floor(rs), 1);
		Float spacing = rs / ins;
		if (nullIfEqualToDefault && spacing.equals(getRowSpacing())) {
			return new DistributedRowSpacing(null, ins);
		} else {
			return new DistributedRowSpacing(spacing, ins);
		}
	}
	
	public int getFlowHeight(PageTemplate template) {
		return getPageHeight() - 
				(int)Math.ceil(template.getHeaderHeight()) + template.validateAndAnalyzeHeader() -
				(int)Math.ceil(template.getFooterHeight()) + template.validateAndAnalyzeFooter() -
				(getBorder() != null ? (int)Math.ceil(distributeRowSpacing(null, false).spacing*2) : 0);
	}
}
