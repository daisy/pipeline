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
	public static enum ListStyle {
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
		PL};
	/**
	 * Defines break before types.
	 */
	public static enum BreakBefore {
		/**
		 * No break
		 */
		AUTO,
		/**
		 * Start block on a new page
		 */
		PAGE}; // TODO: Implement ODD_PAGE, EVEN_PAGE 
	/**
	 * Defines keep types.
	 */
	public static enum Keep {
		/**
		 * Do not keep
		 */
		AUTO,
		/**
		 * Keep all rows in a block
		 */
		ALL}

	public static enum Alignment {
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
		RIGHT
	}
}
