package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.common.text.StringTools;

public abstract class AbstractBlockContentManager implements Iterable<RowImpl> {
	protected boolean isVolatile;
	protected final int flowWidth;
	protected final RowDataProperties rdp;
	protected final FormatterContext fcontext;
	protected final MarginProperties leftParent;
	protected final MarginProperties rightParent;
	protected final MarginProperties leftMargin;
	protected final MarginProperties rightMargin;
	protected final ArrayList<Marker> groupMarkers;
	protected final ArrayList<String> groupAnchors;
	private final List<RowImpl> collapsiblePreContentRows;
	private final List<RowImpl> innerPreContentRows;
	private final List<RowImpl> postContentRows;
	private final List<RowImpl> skippablePostContentRows;
	protected int minWidth;
	
	AbstractBlockContentManager(int flowWidth, RowDataProperties rdp, FormatterContext fcontext) {
		this.flowWidth = flowWidth;
		this.leftParent = rdp.getLeftMargin().buildMarginParent(fcontext.getSpaceCharacter());
		this.rightParent = rdp.getRightMargin().buildMarginParent(fcontext.getSpaceCharacter());
		this.leftMargin = rdp.getLeftMargin().buildMargin(fcontext.getSpaceCharacter());
		this.rightMargin = rdp.getRightMargin().buildMargin(fcontext.getSpaceCharacter());
		this.fcontext = fcontext;
		this.rdp = rdp;
		this.groupMarkers = new ArrayList<>();
		this.groupAnchors = new ArrayList<>();
		this.collapsiblePreContentRows = makeCollapsiblePreContentRows(rdp, leftParent, rightParent);	
		this.innerPreContentRows = makeInnerPreContentRows();
		this.postContentRows = new ArrayList<>();
		this.minWidth = flowWidth-leftMargin.getContent().length()-rightMargin.getContent().length();

		this.skippablePostContentRows = new ArrayList<>();
		MarginProperties margin = new MarginProperties(leftMargin.getContent()+StringTools.fill(fcontext.getSpaceCharacter(), rdp.getTextIndent()), leftMargin.isSpaceOnly());
		if (rdp.getTrailingDecoration()==null) {
			if (leftMargin.isSpaceOnly() && rightMargin.isSpaceOnly()) {
				for (int i=0; i<rdp.getInnerSpaceAfter(); i++) {
					skippablePostContentRows.add(createAndConfigureEmptyNewRow(margin));
				}
			} else {
				for (int i=0; i<rdp.getInnerSpaceAfter(); i++) {
					postContentRows.add(createAndConfigureEmptyNewRow(margin));
				}
			}
		} else {
			for (int i=0; i<rdp.getInnerSpaceAfter(); i++) {
				postContentRows.add(createAndConfigureEmptyNewRow(margin));
			}
			postContentRows.add(makeDecorationRow(flowWidth, rdp.getTrailingDecoration(), leftParent, rightParent));
		}
		
		if (leftParent.isSpaceOnly() && rightParent.isSpaceOnly()) {
			for (int i=0; i<rdp.getOuterSpaceAfter();i++) {
				skippablePostContentRows.add(createAndConfigureNewEmptyRow(leftParent, rightParent));
			}
		} else {
			for (int i=0; i<rdp.getOuterSpaceAfter();i++) {
				postContentRows.add(createAndConfigureNewEmptyRow(leftParent, rightParent));
			}
		}
	}
	
	private static List<RowImpl> makeCollapsiblePreContentRows(RowDataProperties rdp, MarginProperties leftParent, MarginProperties rightParent) {
		List<RowImpl> ret = new ArrayList<>();
		for (int i=0; i<rdp.getOuterSpaceBefore();i++) {
			RowImpl row = new RowImpl("", leftParent, rightParent);
			row.setRowSpacing(rdp.getRowSpacing());
			ret.add(row);
		}
		return ret;
	}
	
	private List<RowImpl> makeInnerPreContentRows() {
		ArrayList<RowImpl> ret = new ArrayList<>();
		if (rdp.getLeadingDecoration()!=null) {
			ret.add(makeDecorationRow(flowWidth, rdp.getLeadingDecoration(), leftParent, rightParent));
		}
		for (int i=0; i<rdp.getInnerSpaceBefore(); i++) {
			MarginProperties margin = new MarginProperties(leftMargin.getContent()+StringTools.fill(fcontext.getSpaceCharacter(), rdp.getTextIndent()), leftMargin.isSpaceOnly());
			ret.add(createAndConfigureEmptyNewRow(margin));
		}
		return ret;
	}
	
	protected RowImpl makeDecorationRow(int flowWidth, SingleLineDecoration d, MarginProperties leftParent, MarginProperties rightParent) {
		int w = flowWidth - rightParent.getContent().length() - leftParent.getContent().length();
		int aw = w-d.getLeftCorner().length()-d.getRightCorner().length();
		RowImpl row = new RowImpl(d.getLeftCorner() + StringTools.fill(d.getLinePattern(), aw) + d.getRightCorner());
		row.setLeftMargin(leftParent);
		row.setRightMargin(rightParent);
		row.setAlignment(rdp.getAlignment());
		row.setRowSpacing(rdp.getRowSpacing());
		return row;
	}
	
	protected RowImpl createAndConfigureEmptyNewRow(MarginProperties left) {
		return createAndConfigureNewEmptyRow(left, rightMargin);
	}

	protected RowImpl createAndConfigureNewEmptyRow(MarginProperties left, MarginProperties right) {
		RowImpl r = new RowImpl("", left, right);
		r.setAlignment(rdp.getAlignment());
		r.setRowSpacing(rdp.getRowSpacing());
		return r;
	}
	
	abstract int getForceBreakCount();
	
	public abstract int getRowCount();
	/**
	 * Returns true if this RowDataManager contains objects that makes the formatting volatile,
	 * i.e. prone to change due to for example cross references.
	 * @return returns true if, and only if, the RowDataManager should be discarded if a new pass is requested,
	 * false otherwise
	 */
	public boolean isVolatile() {
		return isVolatile;
	}

	public MarginProperties getLeftMarginParent() {
		return leftParent;
	}

	public MarginProperties getRightMarginParent() {
		return rightParent;
	}

	public List<RowImpl> getCollapsiblePreContentRows() {
		return collapsiblePreContentRows;
	}

	public List<RowImpl> getInnerPreContentRows() {
		return innerPreContentRows;
	}

	/*public int countPostContentRows() {
		return postContentRows.size();
	}*/
	
	public List<RowImpl> getPostContentRows() {
		return postContentRows;
	}
	/*
	public int countSkippablePostContentRows() {
		return skippablePostContentRows.size();
	}*/
	
	public List<RowImpl> getSkippablePostContentRows() {
		return skippablePostContentRows;
	}
	
	/**
	 * Gets the minimum width available for content (excluding margins)
	 * @return returns the available width, in characters
	 */
	int getMinimumAvailableWidth() {
		return minWidth;
	}

	/**
	 * Get markers that are not attached to a row, i.e. markers that proceeds any text contents
	 * @return returns markers that proceeds this FlowGroups text contents
	 */
	public ArrayList<Marker> getGroupMarkers() {
		return groupMarkers;
	}
	
	public ArrayList<String> getGroupAnchors() {
		return groupAnchors;
	}
	
}
