package org.daisy.dotify.api.embosser;

import org.daisy.dotify.api.paper.PageFormat;

/**
 * Provides common embosser features to be used when configuring an Embosser factory.
 * @author Joel Håkansson
 *
 */
public class EmbosserFeatures {
	/**
	 * Embosser feature key,
	 * corresponding value should be a number, in millimeters
	 */
	public static final String CELL_WIDTH = "cellWidth";
	/**
	 * Embosser feature key,
	 * corresponding value should be a number, in millimeters
	 */
	public static final String CELL_HEIGHT = "cellHeight";
	/**
	 * Embosser feature key,
	 * corresponding value should match a value in {@link EightDotFallbackMethod}
	 */
	public static final String UNSUPPORTED_CELL_FALLBACK_METHOD = "fallback";
	/**
	 * Embosser feature key,
	 * corresponding value should be a character in the range 0x2800-0x283F
	 */
	public static final String UNSUPPORTED_CELL_REPLACEMENT = "replacement";
	/**
	 * Embosser feature key,
	 * corresponding value should match a table identifier
	 */
	public static final String TABLE = "table";
	/**
	 * Embosser feature key,
	 * corresponding value should be a {@link PageFormat} object
	 */
	public static final String PAGE_FORMAT = "pageFormat";

	/**
	 * Embosser feature key,
	 * corresponding value should be an integer
	 */
	public static final String NUMBER_OF_COPIES = "copies";

	/**
	 * Embosser feature key,
	 * corresponding value should be a boolean
	 */
	public static final String SADDLE_STITCH = "saddleStitch";

	/**
	 * Embosser feature key,
	 * corresponding value should be a boolean
	 */
	public static final String Z_FOLDING = "zFolding";

	/**
	 * Embosser feature key,
	 * corresponding value should be a boolean
	 */
	public static final String DUPLEX = "duplex";

	/**
	 * Embosser feature key,
	 * corresponding value should be an integer
	 */
	public static final String PAGES_IN_QUIRE = "pagesInQuire";
}
