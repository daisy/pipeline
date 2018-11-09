package org.daisy.dotify.formatter.impl.row;

import java.util.Collections;

import org.daisy.dotify.api.formatter.FormattingTypes;
import org.daisy.dotify.api.formatter.FormattingTypes.Alignment;
import org.daisy.dotify.formatter.impl.row.Margin.Type;

/**
 * Provides properties for a block. {@link RowDataProperties} are
 * immutable. 
 * @author Joel Håkansson
 *
 */
public final class RowDataProperties {
	private final int blockIndent, blockIndentParent;
	private final Margin leftMargin;
	private final Margin rightMargin;
	private final ListItem listProps;
	private final int textIndent;
	private final int firstLineIndent;
	private final Alignment align;
	private final Float rowSpacing;
	private final int outerSpaceBefore;
	private final int outerSpaceAfter;
	private final int innerSpaceBefore;
	private final int innerSpaceAfter;
	private final int orphans;
	private final int widows;
	private final SingleLineDecoration leadingDecoration;
	private final SingleLineDecoration trailingDecoration;
	private final String underlineStyle;
	
	public static class Builder {
		private int blockIndent = 0;
		private int blockIndentParent = 0;
		private int textIndent = 0;
		private int firstLineIndent = 0;
		private int outerSpaceBefore = 0;
		private int outerSpaceAfter = 0;
		private int innerSpaceBefore = 0;
		private int innerSpaceAfter = 0;
		private int orphans = 0;
		private int widows = 0;
		private Alignment align = Alignment.LEFT;
		private Float rowSpacing = null;
		private Margin leftMargin = new Margin(Type.LEFT, Collections.emptyList());
		private Margin rightMargin = new Margin(Type.RIGHT, Collections.emptyList());
		private SingleLineDecoration leadingDecoration = null;
		private SingleLineDecoration trailingDecoration = null;
		private String underlineStyle = null;

		private ListItem listProps = null;
		
		public Builder() {
		}
		
		public Builder(RowDataProperties template) {
			this.blockIndent = template.blockIndent;
			this.blockIndentParent = template.blockIndentParent;
			this.leftMargin = template.leftMargin;
			this.rightMargin = template.rightMargin;

			this.listProps = template.listProps;
			this.textIndent = template.textIndent;
			this.firstLineIndent = template.firstLineIndent;
			this.align = template.align;
			this.rowSpacing = template.rowSpacing;
			this.outerSpaceBefore = template.outerSpaceBefore;
			this.outerSpaceAfter = template.outerSpaceAfter;
			this.innerSpaceBefore = template.innerSpaceBefore;
			this.innerSpaceAfter = template.innerSpaceAfter;
			this.leadingDecoration = template.leadingDecoration;
			this.trailingDecoration = template.trailingDecoration;
			this.orphans = template.orphans;
			this.widows = template.widows;
			this.underlineStyle = template.underlineStyle;
		}
		
		public Builder blockIndent(int value) {
			blockIndent = value;
			return this;
		}
		
		public Builder blockIndentParent(int value) {
			blockIndentParent = value;
			return this;
		}
		
		public Builder textIndent(int textIndent) {
			this.textIndent = textIndent;
			return this;
		}
		
		public Builder firstLineIndent(int firstLineIndent) {
			this.firstLineIndent = firstLineIndent;
			return this;
		}
		
		public Builder align(FormattingTypes.Alignment align) {
			this.align = align;
			return this;
		}
		
		public Builder rowSpacing(Float value) {
			this.rowSpacing = value;
			return this;
		}
		
		public Builder leftMargin(Margin value) {
			leftMargin = value;
			return this;
		}
		
		public Builder rightMargin(Margin value) {
			rightMargin = value;
			return this;
		}

		public Builder listProperties(ListItem value) {
			listProps = value;
			return this;
		}
		
		public Builder outerSpaceBefore(int value) {
			this.outerSpaceBefore = value;
			return this;
		}
		
		public Builder outerSpaceAfter(int value) {
			this.outerSpaceAfter = value;
			return this;
		}

		public Builder leadingDecoration(SingleLineDecoration value) {
			this.leadingDecoration = value;
			return this;
		}

		public Builder innerSpaceBefore(int value) {
			this.innerSpaceBefore = value;
			return this;
		}

		public Builder trailingDecoration(SingleLineDecoration value) {
			this.trailingDecoration = value;
			return this;
		}

		public Builder innerSpaceAfter(int value) {
			this.innerSpaceAfter = value;
			return this;
		}
		
		public Builder orphans(int value) {
			this.orphans = value;
			return this;
		}
		
		public Builder widows(int value) {
			this.widows = value;
			return this;
		}
		
		public Builder underlineStyle(String value) {
			if (value != null && value.length() != 1) {
				throw new IllegalArgumentException("Value should be either null or a single-character string, but got: " + value);
			}
			this.underlineStyle = value;
			return this;
		}

		public RowDataProperties build() {
			return new RowDataProperties(this);
		}
	}
	
	private RowDataProperties(Builder builder) {
		this.blockIndent = builder.blockIndent;
		this.blockIndentParent = builder.blockIndentParent;
		this.leftMargin = builder.leftMargin;
		this.rightMargin = builder.rightMargin;

		this.listProps = builder.listProps;
		this.textIndent = builder.textIndent;
		this.firstLineIndent = builder.firstLineIndent;
		this.align = builder.align;
		this.rowSpacing = builder.rowSpacing;
		this.outerSpaceBefore = builder.outerSpaceBefore;
		this.outerSpaceAfter = builder.outerSpaceAfter;
		this.innerSpaceBefore = builder.innerSpaceBefore;
		this.innerSpaceAfter = builder.innerSpaceAfter;
		this.leadingDecoration = builder.leadingDecoration;
		this.trailingDecoration = builder.trailingDecoration;
		this.orphans = builder.orphans;
		this.widows = builder.widows;
		this.underlineStyle = builder.underlineStyle;
	}

	RowImpl.Builder configureNewEmptyRowBuilder(MarginProperties left, MarginProperties right) {
		return new RowImpl.Builder("").leftMargin(left).rightMargin(right)
				.alignment(getAlignment())
				.rowSpacing(getRowSpacing())
				.adjustedForMargin(true);
	}

	public int getBlockIndent() {
		return blockIndent;
	}

	public int getBlockIndentParent() {
		return blockIndentParent;
	}
	
	public int getTextIndent() {
		return textIndent;
	}
	
	public int getFirstLineIndent() {
		return firstLineIndent;
	}
	
	public FormattingTypes.Alignment getAlignment() {
		return align;
	}
	
	public Float getRowSpacing() {
		return rowSpacing;
	}

	public Margin getLeftMargin() {
		return leftMargin;
	}

	public Margin getRightMargin() {
		return rightMargin;
	}
	public boolean isList() {
		return listProps!=null;
	}
	
	ListItem getListItem() {
		return listProps;
	}

	public int getOuterSpaceBefore() {
		return outerSpaceBefore;
	}

	public int getOuterSpaceAfter() {
		return outerSpaceAfter;
	}

	public int getInnerSpaceBefore() {
		return innerSpaceBefore;
	}

	public int getInnerSpaceAfter() {
		return innerSpaceAfter;
	}

	public SingleLineDecoration getLeadingDecoration() {
		return leadingDecoration;
	}

	public SingleLineDecoration getTrailingDecoration() {
		return trailingDecoration;
	}
	
	public int getOrphans() {
		return orphans;
	}
	
	public int getWidows() {
		return widows;
	}
	
	public String getUnderlineStyle() {
		return underlineStyle;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((align == null) ? 0 : align.hashCode());
		result = prime * result + blockIndent;
		result = prime * result + blockIndentParent;
		result = prime * result + firstLineIndent;
		result = prime * result + innerSpaceAfter;
		result = prime * result + innerSpaceBefore;
		result = prime * result + ((leadingDecoration == null) ? 0 : leadingDecoration.hashCode());
		result = prime * result + ((leftMargin == null) ? 0 : leftMargin.hashCode());
		result = prime * result + ((listProps == null) ? 0 : listProps.hashCode());
		result = prime * result + orphans;
		result = prime * result + outerSpaceAfter;
		result = prime * result + outerSpaceBefore;
		result = prime * result + ((rightMargin == null) ? 0 : rightMargin.hashCode());
		result = prime * result + ((rowSpacing == null) ? 0 : rowSpacing.hashCode());
		result = prime * result + textIndent;
		result = prime * result + ((trailingDecoration == null) ? 0 : trailingDecoration.hashCode());
		result = prime * result + widows;
		result = prime * result + ((underlineStyle == null) ? 0 : underlineStyle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RowDataProperties other = (RowDataProperties) obj;
		if (align != other.align) {
			return false;
		}
		if (blockIndent != other.blockIndent) {
			return false;
		}
		if (blockIndentParent != other.blockIndentParent) {
			return false;
		}
		if (firstLineIndent != other.firstLineIndent) {
			return false;
		}
		if (innerSpaceAfter != other.innerSpaceAfter) {
			return false;
		}
		if (innerSpaceBefore != other.innerSpaceBefore) {
			return false;
		}
		if (leadingDecoration == null) {
			if (other.leadingDecoration != null) {
				return false;
			}
		} else if (!leadingDecoration.equals(other.leadingDecoration)) {
			return false;
		}
		if (leftMargin == null) {
			if (other.leftMargin != null) {
				return false;
			}
		} else if (!leftMargin.equals(other.leftMargin)) {
			return false;
		}
		if (listProps == null) {
			if (other.listProps != null) {
				return false;
			}
		} else if (!listProps.equals(other.listProps)) {
			return false;
		}
		if (orphans != other.orphans) {
			return false;
		}
		if (outerSpaceAfter != other.outerSpaceAfter) {
			return false;
		}
		if (outerSpaceBefore != other.outerSpaceBefore) {
			return false;
		}
		if (rightMargin == null) {
			if (other.rightMargin != null) {
				return false;
			}
		} else if (!rightMargin.equals(other.rightMargin)) {
			return false;
		}
		if (rowSpacing == null) {
			if (other.rowSpacing != null) {
				return false;
			}
		} else if (!rowSpacing.equals(other.rowSpacing)) {
			return false;
		}
		if (textIndent != other.textIndent) {
			return false;
		}
		if (trailingDecoration == null) {
			if (other.trailingDecoration != null) {
				return false;
			}
		} else if (!trailingDecoration.equals(other.trailingDecoration)) {
			return false;
		}
		if (widows != other.widows) {
			return false;
		}
		if (underlineStyle == null) {
			if (other.underlineStyle != null) {
				return false;
			}
		} else if (!underlineStyle.equals(other.underlineStyle)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RowDataProperties [blockIndent=" + blockIndent + ", blockIndentParent=" + blockIndentParent
				+ ", leftMargin=" + leftMargin + ", rightMargin=" + rightMargin + ", listProps=" + listProps
				+ ", textIndent=" + textIndent + ", firstLineIndent=" + firstLineIndent + ", align=" + align
				+ ", rowSpacing=" + rowSpacing + ", outerSpaceBefore=" + outerSpaceBefore + ", outerSpaceAfter="
				+ outerSpaceAfter + ", innerSpaceBefore=" + innerSpaceBefore + ", innerSpaceAfter=" + innerSpaceAfter
				+ ", orphans=" + orphans + ", widows=" + widows + ", leadingDecoration=" + leadingDecoration
				+ ", trailingDecoration=" + trailingDecoration + ", underlineStyle=" + underlineStyle + "]";
	}

}
