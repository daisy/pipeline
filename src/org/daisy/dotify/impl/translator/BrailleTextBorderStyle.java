package org.daisy.dotify.impl.translator;

import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.api.translator.TextBorderStyle.Builder;


/**
 * Provides 6-dot braille border styles.
 * 
 * @author Joel Håkansson
 */
public interface BrailleTextBorderStyle {

	/**
	 * Solid outer braille border
	 */
	public final static TextBorderStyle SOLID_THIN_OUTER = new Builder().
			topLeftCorner("\u280F").
			topBorder("\u2809").
			topRightCorner("\u2839").
			leftBorder("\u2807").
			rightBorder("\u2838").
			bottomLeftCorner("\u2827").
			bottomBorder("\u2824").
			bottomRightCorner("\u283c").build();
	public final static TextBorderStyle SOLID_THIN_INNER = new Builder().
			topLeftCorner("\u2820").
			topBorder("\u2824").
			topRightCorner("\u2804").
			leftBorder("\u2838").
			rightBorder("\u2807").
			bottomLeftCorner("\u2808").
			bottomBorder("\u2809").
			bottomRightCorner("\u2801").build();
	public final static TextBorderStyle SOLID_WIDE_OUTER = new Builder().
			topLeftCorner("\u283F").
			topBorder("\u281B").
			topRightCorner("\u283F").
			leftBorder("\u283F").
			rightBorder("\u283F").
			bottomLeftCorner("\u283F").
			bottomBorder("\u2836").
			bottomRightCorner("\u283F").build();
	public final static TextBorderStyle SOLID_WIDE_INNER = new Builder().
			topLeftCorner("\u2836").
			topBorder("\u2836").
			topRightCorner("\u2836").
			leftBorder("\u283F").
			rightBorder("\u283F").
			bottomLeftCorner("\u281B").
			bottomBorder("\u281B").
			bottomRightCorner("\u281B").build();

}
