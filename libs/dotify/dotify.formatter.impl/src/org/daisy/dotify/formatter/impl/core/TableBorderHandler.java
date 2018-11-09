package org.daisy.dotify.formatter.impl.core;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.translator.Border;
import org.daisy.dotify.api.translator.BorderSpecification.Align;
import org.daisy.dotify.api.translator.BorderSpecification.Style;
import org.daisy.dotify.api.translator.TextBorderConfigurationException;
import org.daisy.dotify.api.translator.TextBorderFactory;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.common.text.StringTools;

class TableBorderHandler {
	private static final Logger logger = Logger.getLogger(TableBorderHandler.class.getCanonicalName());
	enum Position {
		TOP,
		BOTTOM,
		LEFT,
		RIGHT,
		CENTER_HORIZONTAL,
		CENTER_VERTICAL
	}
	private final TextBorderFactoryMakerService tbf;
	private final int tableColSpacing;
	private final Map<StyleKey, TextBorderStyle> cache;
	private final String mode;

	TableBorderHandler(int tableColSpacing, TextBorderFactoryMakerService tbf, String mode) {
		this.tableColSpacing = tableColSpacing;
		this.tbf = tbf;
		this.mode = mode;
		this.cache = new HashMap<>(); //permits null values, which is relied on
	}
	
	String getSharedColumnString(Border cell1, Border cell2, BlockContext context) {
		StringBuilder sb = new StringBuilder();
		if (tableColSpacing==0) {
			sb.append(mergedColumnStyles(cell1, cell2));
		} else {
			sb.append(cell1!=null && cell1.getRight().getStyle()!=Style.NONE?getStyle(posForRight(cell1.getRight().getAlign()), cell1.getRight().getWidth()):"");
			sb.append(StringTools.fill(context.getFcontext().getSpaceCharacter(), tableColSpacing));
			sb.append(cell2!=null && cell2.getLeft().getStyle()!=Style.NONE?getStyle(posForLeft(cell2.getLeft().getAlign()), cell2.getLeft().getWidth()):"");
		}
		return sb.toString();
	}

	private String mergedColumnStyles(Border c1, Border c2) {
		if ((c1==null||c1.getRight().getStyle()==Style.NONE) && (c2==null || c2.getLeft().getStyle()==Style.NONE)) {
			return "";
		} else if (c1==null||c1.getRight().getStyle()==Style.NONE) {
			return getStyle(posForLeft(c2.getLeft().getAlign()), c2.getLeft().getWidth());
		} else if (c2==null||c2.getLeft().getStyle()==Style.NONE) {
			return getStyle(posForRight(c1.getRight().getAlign()), c1.getRight().getWidth());
		} else {  // we now know that neither c1 or c2 is null
			int w = Math.max(c1.getRight().getWidth(), c2.getLeft().getWidth());
			Position p;
			// == is fine for enum comparison
			if (c1.getRight().getAlign()==c2.getLeft().getAlign()) {
				p = Position.CENTER_HORIZONTAL;
			} else if (c1.getRight().getAlign()==Align.CENTER) {
				// choose the one that isn't center, as specified in OBFL
				p = posForLeft(c2.getLeft().getAlign());
			} else { 
				// either (c2.getLeft().getAlign()==Align.CENTER)
				// in which case the below is correct (it mirrors the above)
				// OR |<- |<- OR ->| ->|
				// in which case the same position will result regardless of the cell chosen 
				p = posForRight(c1.getRight().getAlign());
			}
			return getStyle(p, w);
		}
	}
	
	String mergedRowStyles(Border c1, Border c2) {
		if ((c1==null||c1.getBottom().getStyle()==Style.NONE) && (c2==null || c2.getTop().getStyle()==Style.NONE)) {
			return "";
		} else if (c1==null||c1.getBottom().getStyle()==Style.NONE) {
			return getStyle(posForTop(c2.getTop().getAlign()), c2.getTop().getWidth());
		} else if (c2==null||c2.getTop().getStyle()==Style.NONE) {
			return getStyle(posForBottom(c1.getBottom().getAlign()), c1.getBottom().getWidth());
		} else {  // we now know that neither c1 or c2 is null
			int w = Math.max(c1.getBottom().getWidth(), c2.getTop().getWidth());
			Position p;
			// == is fine for enum comparison
			if (c1.getBottom().getAlign()==c2.getTop().getAlign()) {
				p = Position.CENTER_HORIZONTAL;
			} else if (c1.getBottom().getAlign()==Align.CENTER) {
				// choose the one that isn't center, as specified in OBFL
				p = posForTop(c2.getTop().getAlign());
			} else { 
				// either (c2.getTop().getAlign()==Align.CENTER)
				// in which case the below is correct (it mirrors the above)
				// OR ^ ^ OR v v
				// in which case the same position will result regardless of the cell chosen 
				p = posForBottom(c1.getBottom().getAlign());
			}
			return getStyle(p, w);
		}
	}
	
	private Position posForLeft(Align a) {
		switch (a) {			
			case INNER:return Position.RIGHT;
			case OUTER:return Position.LEFT;
			case CENTER:return Position.CENTER_HORIZONTAL;
			default:
				throw new RuntimeException("Unexpected value. This is a bug.");
		}
	}
	
	private Position posForRight(Align a) {
		switch (a) {
			case INNER:return Position.LEFT;
			case OUTER:return Position.RIGHT;
			case CENTER:return Position.CENTER_HORIZONTAL;
			default:
				throw new RuntimeException("Unexpected value. This is a bug.");
		}
	}
	
	Position posForTop(Align a) {
		switch (a) {
			case INNER:return Position.BOTTOM;
			case OUTER:return Position.TOP;
			case CENTER:return Position.CENTER_VERTICAL;
			default:
				throw new RuntimeException("Unexpected value. This is a bug.");
		}
	}
	
	Position posForBottom(Align a) {
		switch (a) {
			case INNER:return Position.TOP;
			case OUTER:return Position.BOTTOM;
			case CENTER:return Position.CENTER_VERTICAL;
			default:
				throw new RuntimeException("Unexpected value. This is a bug.");
		}
	}

	String getStyle(Position align, int width) {
		boolean center = align==Position.CENTER_HORIZONTAL||align==Position.CENTER_VERTICAL;
		StyleKey k = new StyleKey(center, width);
		TextBorderStyle style = null;
		if (!cache.containsKey(k)) { //use containsKey to avoid recreating a failed border time and time again
			try {
				Map<String, Object> features = new HashMap<>();
				features.put(TextBorderFactory.FEATURE_MODE, mode);
				features.put("border", new Border.Builder().getDefault()
										.style(Style.SOLID)
										.width(width)
										.align(center?Align.CENTER:Align.OUTER)
										.build()
							);
				style = tbf.newTextBorderStyle(features);
			} catch (TextBorderConfigurationException e) {
				logger.log(Level.WARNING, "Cannot create text border", e);
			} finally {
				// put the style even if it is null, to avoid retrying the same combination
				cache.put(k, style);
			}
		} else {
			style = cache.get(k);
		}
		if (style==null) {
			return "";
		} else {
			switch (align) {
				case LEFT: return style.getLeftBorder();
				case RIGHT: return style.getRightBorder();
				case TOP: return style.getTopBorder();
				case BOTTOM: return style.getBottomBorder();
				case CENTER_HORIZONTAL: return style.getLeftBorder();
				case CENTER_VERTICAL: return style.getTopBorder();
				default:
					throw new RuntimeException("Table border is empty, but it shouldn't be. This is a bug.");
			}
		}
	}
	
	private static class StyleKey {
		final boolean center;
		final int w;
		StyleKey(boolean center, int w) {
			this.center = center;
			this.w = w;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (center ? 1231 : 1237);
			result = prime * result + w;
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
			StyleKey other = (StyleKey) obj;
			if (center != other.center) {
				return false;
			}
			if (w != other.w) {
				return false;
			}
			return true;
		}
		
	}

}
