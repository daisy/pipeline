package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import org.daisy.dotify.common.text.StringTools;

class PaginatorTools {
	/**
	 * Distribution modes 
	 */
	public enum DistributeMode {
		/**
		 * Distribute so that the spaces between strings are kept equal
		 */
		EQUAL_SPACING,
		/**
		 * Distribute so that each cell is equally wide
		 */
		UNISIZE_TABLE_CELL
	};
	
	private PaginatorTools() { }
	
	private static String distributeEqualSpacing(ArrayList<String> units, int width, String padding) {
		if (units.size()==1) {
			return units.get(0);
		}
		int chunksLength = 0;
		for (String s : units) {
			chunksLength += s.codePointCount(0, s.length());
		}
		int totalSpace = width-chunksLength;
		int parts = units.size()-1;
		double target = totalSpace/(double)parts;
		int used = 0;
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<units.size(); i++) {
			if (i>0) {
				int spacing = (int)Math.round(i * target) - used;
				used += spacing;
				sb.append(StringTools.fill(padding, spacing));
			}
			sb.append(units.get(i));
		}
		assert sb.length()==width;
		return sb.toString();
	}
	
	private static String distributeTable(ArrayList<String> units, int width, String padding) throws PaginatorToolsException {
		double target = width/(double)units.size();
		StringBuffer sb = new StringBuffer();
		int used = 0;
		for (int i=0; i<units.size(); i++) {
			int spacing = (int)Math.round((i+1) * target) - used;
			String cell = units.get(i);
			used += spacing;
			spacing -= cell.codePointCount(0, cell.length());
			if (spacing<0) {
				throw new PaginatorToolsException("Text does not fit within cell: " + cell);
			}
			sb.append(cell);
			if (i<units.size()-1) {
				sb.append(StringTools.fill(padding, spacing));
			}
		}
		return sb.toString();
	}
	
	/**
	 * Distribute <tt>units</tt> of text over <tt>width</tt> chars, separated by <tt>padding</tt> pattern
	 * using distribution mode <tt>mode</tt>.
	 * @param units the units of text to distribute
	 * @param width the width of the resulting string
	 * @param padding the padding pattern to use as separator
	 * @param mode the distribution mode to use
	 * @return returns a string of <tt>width</tt> chars 
	 */
	public static String distribute(ArrayList<String> units, int width, String padding, DistributeMode mode) throws PaginatorToolsException {
		switch (mode) {
			case EQUAL_SPACING:
				return distributeEqualSpacing(units, width, padding);
			case UNISIZE_TABLE_CELL:
				return distributeTable(units, width, padding);
		}
		// Cannot happen
		return null;
	}

	public static String distribute(Collection<TabStopString> units) {
		TreeSet<TabStopString> sortedUnits = new TreeSet<>();
		sortedUnits.addAll(units);
		StringBuffer sb = new StringBuffer();
		int used = 0;
		for (TabStopString t : sortedUnits) {
			used = sb.codePointCount(0, sb.length());
			if (used > t.getPosition()) {
				throw new RuntimeException("Cannot layout cell.");
			}
			int amount = t.getPosition()-used;
			switch (t.getAlignment()) {
				case LEFT:
					//ok
					break;
				case CENTER:
					amount -= t.length() / 2;
					break;
				case RIGHT:
					amount -= t.length();
					break;
			}
			sb.append(StringTools.fill(t.getPattern(), amount));
			sb.append(t.getText());
		}
		return sb.toString();
	}
}
