package org.daisy.dotify.impl.system.common;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Provides parameters needed when running a Task system.
 * NOTE: some default properties are set in this class.
 * This behavior is misplaced and will be removed in future
 * versions. Users of this class are advised not to rely 
 * on the existence of these properties.
 * 
 * @author Joel Håkansson
 */
public class RunParameters {
	private static final Logger logger = Logger.getLogger(RunParameters.class.getCanonicalName());
	private static final String DEPRECATED_PROPERTY_MSG = "Property %s is deprecated, use %s instead.";
	private static final int ROWGAP_DEFAULT = 0;
	private static final int ROWS_DEFAULT = 29;
	private static final int COLS_DEFAULT = 28;
	private static final int INNER_MARGIN_DEFAULT = 2;
	private static final int OUTER_MARGIN_DEFAULT = 2;
	public static String COLS = "cols";
	public static String PAGE_WIDTH = "page-width";
	public static String INNER_MARGIN = "inner-margin";
	public static String OUTER_MARGIN = "outer-margin";
	public static String ROWS = "rows";
	public static String PAGE_HEIGHT = "page-height";
	public static String ROWGAP = "rowgap";
	public static String ROW_SPACING = "row-spacing";

	public static Map<String, Object> fromMap(Map<String, Object> map) {
		Map<String, Object> p1 = new HashMap<>();
		p1.putAll(map);
		verifyAndSetWidth(p1);
		verifyAndSetHeight(p1);
		verifyAndSetRowSpacing(p1);
		return p1;
	}
	
	static void verifyAndSetWidth(Map<String, Object> p1) {
		Integer cols = asInteger(p1.get(COLS), null);
		Integer pageWidth = asInteger(p1.get(PAGE_WIDTH), null);
		int innerMargin = asInteger(p1.get(INNER_MARGIN), INNER_MARGIN_DEFAULT);
		int outerMargin = asInteger(p1.get(OUTER_MARGIN), OUTER_MARGIN_DEFAULT);

		if (cols!=null) {
			logger.warning(String.format(DEPRECATED_PROPERTY_MSG, COLS, PAGE_WIDTH));
		}
		if (cols==null && pageWidth==null) {
			//use default
			cols = COLS_DEFAULT;
		}
		if (cols==null) {
			cols = pageWidth - innerMargin - outerMargin;
		} else if (pageWidth==null) {
			pageWidth = innerMargin + outerMargin + cols;
		} else if (pageWidth!=innerMargin + outerMargin + cols) {
			throw new IllegalArgumentException("Conflicting definitions: " + PAGE_WIDTH + "/" + COLS + "/" + INNER_MARGIN + "/" + OUTER_MARGIN);
		}

		p1.put(COLS, cols);
		p1.put(PAGE_WIDTH, pageWidth);
		p1.put(INNER_MARGIN, innerMargin);
		p1.put(OUTER_MARGIN, outerMargin);
	}
	
	static void verifyAndSetHeight(Map<String, Object> p1) {
		Integer rows = asInteger(p1.get(ROWS), null);
		Integer pageHeight = asInteger(p1.get(PAGE_HEIGHT), null);
		if (rows!=null) {
			logger.warning(String.format(DEPRECATED_PROPERTY_MSG, ROWS, PAGE_HEIGHT));
		}
		if (rows==null && pageHeight==null) {
			//use default
			rows = ROWS_DEFAULT;
		}
		if (rows==null) {
			rows = pageHeight;
		} else if (pageHeight==null) {
			pageHeight = rows;
		} else if (pageHeight!=rows) {
			throw new IllegalArgumentException("Conflicting definitions: " + PAGE_HEIGHT + "/" + ROWS);
		}
		p1.put(ROWS, rows);
		p1.put(PAGE_HEIGHT, pageHeight);
	}
	
	static void verifyAndSetRowSpacing(Map<String, Object> p1) {
		Integer rowgap = asInteger(p1.get(ROWGAP), null);
		Float rowSpacing = asFloat(p1.get(ROW_SPACING), null);
		if (rowgap!=null) {
			logger.warning(String.format(DEPRECATED_PROPERTY_MSG, ROWGAP, ROW_SPACING));
		}
		if (rowgap==null && rowSpacing==null) {
			//use default
			rowgap = ROWGAP_DEFAULT;
		}
		if (rowgap==null) {
			float t = (rowSpacing-1)*4;
			if (t<0) {
				throw new IllegalArgumentException("Negative " + ROWGAP + " caused by the value of " + ROW_SPACING + ":" + rowSpacing);
			}
			rowgap = Math.round(t);
		} else if (rowSpacing==null) {
			rowSpacing = (rowgap/4f)+1;
		} else if (!rowSpacing.equals((rowgap/4f)+1)) {
			throw new IllegalArgumentException("Conflicting definitions: " + ROWGAP + "/" + ROW_SPACING);
		}
		p1.put(ROWGAP, rowgap);
		// There's a toString here because at least one stylesheet expects xs:decimal and if a float is passed in, it will result in xs:float
		p1.put(ROW_SPACING, rowSpacing.toString());
	}
	
	private static Integer asInteger(Object o, Integer def) {
		if (o==null) {
			return def;
		} else if (o instanceof Integer) {
			return (Integer)o;
		} else {
			try {
				return Integer.parseInt(o.toString());
			} catch (NumberFormatException e) {
				return def;
			}
		}
	}
	
	private static Float asFloat(Object o, Float def) {
		if (o==null) {
			return def;
		} else if (o instanceof Float) {
			return (Float)o;
		} else {
			try {
				return Float.parseFloat(o.toString());
			} catch (NumberFormatException e) {
				return def;
			}
		}
	}
}
