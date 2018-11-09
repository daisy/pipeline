package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.common.text.StringTools;

/**
 * Provides a way to add a border to a set of paragraphs.
 * @author Joel Håkansson
 */
class TextBorder {
	private final int topFill, bottomFill, width;
	private final String topLeftCorner, topBorder, topRightCorner, leftBorder,
			rightBorder, bottomLeftCorner, bottomBorder, bottomRightCorner,
			fillCharacter;
	private final boolean pad;

	/**
	 * The Builder is used when creating a TextBorder instance.
	 * @author Joel Håkansson
	 */
	public static class Builder {
		final int width;
		final String fillCharacter;
		TextBorderStyle style;
		int outerLeftMargin;
		boolean padToSize;
		
		/**
		 * Creates a new Builder
		 * @param width the width of the block including borders
		 * @param fillCharacter the character representing empty space
		 */
		public Builder(int width, String fillCharacter) {
			this.width = width;
			this.fillCharacter = fillCharacter;
			this.style = TextBorderStyle.NONE;
			this.outerLeftMargin = 0;
			this.padToSize = true;
		}
		
		/**
		 * Sets the text border style
		 * 
		 * @param style
		 *            the style
		 * @return returns this Builder
		 */
		public Builder style(TextBorderStyle style) {
			this.style = style;
			return this;
		}
		
		public Builder outerLeftMargin(int margin) {
			this.outerLeftMargin = margin;
			return this;
		}
		
		public Builder padToSize(boolean value) {
			this.padToSize = value;
			return this;
		}

		/**
		 * Build TextBorder using the current state of the Builder
		 * @return returns a new TextBorder instance
		 */
		public TextBorder build() {
			return new TextBorder(this);
		}
	}

	private TextBorder(Builder builder) {
		String outerLeftMargin = StringTools.fill(builder.fillCharacter, builder.outerLeftMargin);
		this.topLeftCorner = outerLeftMargin + builder.style.getTopLeftCorner();
		this.topBorder = builder.style.getTopBorder();
		this.topRightCorner = builder.style.getTopRightCorner();
		this.leftBorder = outerLeftMargin + builder.style.getLeftBorder();
		this.rightBorder = builder.style.getRightBorder();
		this.bottomLeftCorner = outerLeftMargin + builder.style.getBottomLeftCorner();
		this.bottomBorder = builder.style.getBottomBorder();
		this.bottomRightCorner = builder.style.getBottomRightCorner();

		this.topFill = builder.width - (topLeftCorner.length() + topRightCorner.length());
		this.bottomFill = builder.width - (bottomLeftCorner.length() + bottomRightCorner.length());
		this.width = builder.width;
		this.fillCharacter = builder.fillCharacter;
		this.pad = builder.padToSize;
	}

	/**
	 * Gets the rendered top border
	 * @return returns the rendered top border 
	 */
	public String getTopBorder() {
		return topLeftCorner + StringTools.fill(topBorder, topFill) + topRightCorner;
	}

	/**
	 * Gets the rendered bottom border
	 * @return returns the rendered bottom border
	 */
	public String getBottomBorder() {
		return bottomLeftCorner + StringTools.fill(bottomBorder, bottomFill) + bottomRightCorner;
	}

	public String addBorderToRow(String text, String innerRightBorder) {
    	StringBuffer sb = new StringBuffer();
		sb.append(leftBorder);
    	sb.append(text);
		if (pad) {
			//pad to size
			sb.append(StringTools.fill(fillCharacter, width - sb.length() - innerRightBorder.length() - rightBorder.length()));
			sb.append(innerRightBorder);
			sb.append(rightBorder);
    	}
    	return sb.toString();
	}
	
	public static String addBorderToRow(int width, String leftBorder, String text, String rightBorder, String fillCharacter) {
		return addBorderToRow(width, leftBorder, text, rightBorder, fillCharacter, true);
	}
	
	public static String addBorderToRow(int width, String leftBorder, String text, String rightBorder, String fillCharacter, boolean pad) {
    	StringBuffer sb = new StringBuffer();
		sb.append(leftBorder);
    	sb.append(text);
		if (pad) {
			//pad to size
			sb.append(StringTools.fill(fillCharacter, width - sb.length() - rightBorder.length()));
			sb.append(rightBorder);
    	}
    	return sb.toString();
	}

}
