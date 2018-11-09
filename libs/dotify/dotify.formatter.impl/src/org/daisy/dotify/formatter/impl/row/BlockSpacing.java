package org.daisy.dotify.formatter.impl.row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.daisy.dotify.common.text.StringTools;

class BlockSpacing {
	private final List<RowImpl> collapsiblePreContentRows;
	private final List<RowImpl> innerPreContentRows;
	private final List<RowImpl> postContentRows;
	private final List<RowImpl> skippablePostContentRows;
	
	BlockSpacing(BlockMargin margins, RowDataProperties rdp, int flowWidth, char spaceCharacter) {
		this.collapsiblePreContentRows = makeCollapsiblePreContentRows(rdp, margins.getLeftParent(), margins.getRightParent());	
		this.innerPreContentRows = makeInnerPreContentRows(spaceCharacter, rdp, flowWidth, margins);

		List<RowImpl> postContentRowsBuilder = new ArrayList<>();
		List<RowImpl> skippablePostContentRowsBuilder = new ArrayList<>();
		MarginProperties margin = new MarginProperties(margins.getLeftMargin().getContent()+StringTools.fill(spaceCharacter, rdp.getTextIndent()), margins.getLeftMargin().isSpaceOnly());
		if (rdp.getTrailingDecoration()==null) {
			if (margins.getLeftMargin().isSpaceOnly() && margins.getRightMargin().isSpaceOnly()) {
				for (int i=0; i<rdp.getInnerSpaceAfter(); i++) {
					skippablePostContentRowsBuilder.add(rdp.configureNewEmptyRowBuilder(margin, margins.getRightMargin()).build());
				}
			} else {
				for (int i=0; i<rdp.getInnerSpaceAfter(); i++) {
					postContentRowsBuilder.add(rdp.configureNewEmptyRowBuilder(margin, margins.getRightMargin()).build());
				}
			}
		} else {
			for (int i=0; i<rdp.getInnerSpaceAfter(); i++) {
				postContentRowsBuilder.add(rdp.configureNewEmptyRowBuilder(margin, margins.getRightMargin()).build());
			}
			postContentRowsBuilder.add(makeDecorationRow(rdp, flowWidth, rdp.getTrailingDecoration(), margins.getLeftParent(), margins.getRightParent()));
		}
		
		if (margins.getLeftParent().isSpaceOnly() && margins.getRightParent().isSpaceOnly()) {
			for (int i=0; i<rdp.getOuterSpaceAfter();i++) {
				skippablePostContentRowsBuilder.add(rdp.configureNewEmptyRowBuilder(margins.getLeftParent(), margins.getRightParent()).build());
			}
		} else {
			for (int i=0; i<rdp.getOuterSpaceAfter();i++) {
				postContentRowsBuilder.add(rdp.configureNewEmptyRowBuilder(margins.getLeftParent(), margins.getRightParent()).build());
			}
		}
		this.postContentRows = Collections.unmodifiableList(postContentRowsBuilder);
		this.skippablePostContentRows = Collections.unmodifiableList(skippablePostContentRowsBuilder);
	}
	
	private static List<RowImpl> makeCollapsiblePreContentRows(RowDataProperties rdp, MarginProperties leftParent, MarginProperties rightParent) {
		List<RowImpl> ret = new ArrayList<>();
		for (int i=0; i<rdp.getOuterSpaceBefore();i++) {
			RowImpl row = new RowImpl.Builder("").leftMargin(leftParent).rightMargin(rightParent)
					.rowSpacing(rdp.getRowSpacing())
					.adjustedForMargin(true)
					.build();
			ret.add(row);
		}
		return Collections.unmodifiableList(ret);
	}
	
	private static List<RowImpl> makeInnerPreContentRows(char spaceCharacter, RowDataProperties rdp, int flowWidth, BlockMargin margins) {
		ArrayList<RowImpl> ret = new ArrayList<>();
		if (rdp.getLeadingDecoration()!=null) {
			ret.add(makeDecorationRow(rdp, flowWidth, rdp.getLeadingDecoration(), margins.getLeftParent(), margins.getRightParent()));
		}
		for (int i=0; i<rdp.getInnerSpaceBefore(); i++) {
			MarginProperties margin = new MarginProperties(margins.getLeftMargin().getContent()+StringTools.fill(spaceCharacter, rdp.getTextIndent()), margins.getLeftMargin().isSpaceOnly());
			ret.add(rdp.configureNewEmptyRowBuilder(margin, margins.getRightMargin()).build());
		}
		return Collections.unmodifiableList(ret);
	}
	
	private static RowImpl makeDecorationRow(RowDataProperties rdp, int flowWidth, SingleLineDecoration d, MarginProperties leftParent, MarginProperties rightParent) {
		int w = flowWidth - rightParent.getContent().length() - leftParent.getContent().length();
		int aw = w-d.getLeftCorner().length()-d.getRightCorner().length();
		RowImpl row = new RowImpl.Builder(d.getLeftCorner() + StringTools.fill(d.getLinePattern(), aw) + d.getRightCorner())
				.leftMargin(leftParent)
				.rightMargin(rightParent)
				.alignment(rdp.getAlignment())
				.rowSpacing(rdp.getRowSpacing())
				.adjustedForMargin(true)
				.build();
		return row;
	}

	public List<RowImpl> getCollapsiblePreContentRows() {
		return collapsiblePreContentRows;
	}

	public List<RowImpl> getInnerPreContentRows() {
		return innerPreContentRows;
	}

	public List<RowImpl> getPostContentRows() {
		return postContentRows;
	}

	public List<RowImpl> getSkippablePostContentRows() {
		return skippablePostContentRows;
	}

}
