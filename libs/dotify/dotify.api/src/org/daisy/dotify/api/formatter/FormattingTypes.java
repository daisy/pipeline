package org.daisy.dotify.api.formatter;

/**
 * Provides common formatting types.
 * 
 * @author Joel Håkansson
 */
public interface FormattingTypes {
	
	/**
	 * Defines list styles.
	 */ 
	public enum ListStyle {
		/**
		 * Not a list
		 */
		NONE,
		/**
		 * Ordered list
		 */
		OL,
		/**
		 * Unordered list
		 */
		UL,
		/**
		 * Preformatted list
		 */
		PL}
	/**
	 * Defines break before types.
	 */
	public enum BreakBefore {
		/**
		 * No break
		 */
		AUTO,
		/**
		 * Start block on a new page
		 */
		PAGE,
		/**
		 * Start block on a new sheet
		 */
		SHEET
		}
	/**
	 * Defines rows in block keep types.
	 */
	public enum Keep {
		/**
		 * Do not keep
		 */
		AUTO,
		/**
		 * Keep all rows in a block on the same page
		 */
		PAGE,
		/**
		 * Keep the block on the same page and sheet
		 */
		SHEET,
		/**
		 * Keep block in the same page, sheet and volume
		 */
		VOLUME
		}

	/**
	 * Defines alignment options.
	 */
	public enum Alignment {
		/**
		 * Align content to the left
		 */
		LEFT,
		/**
		 * Center content
		 */
		CENTER,
		/**
		 * Align content to the right
		 */
		RIGHT;
		
		/**
		 * Gets the alignment offset based on available space.
		 * @param space available space
		 * @return returns the offset
		 */
		public int getOffset(int space) {
	    	switch (this) {
		    	case CENTER:
					return space / 2;
		    	case RIGHT:
					return space;
		    	case LEFT: default: return 0;
	    	}
		}
	
	}
}
