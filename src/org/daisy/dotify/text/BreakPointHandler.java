package org.daisy.dotify.text;

import java.util.regex.Pattern;


/**
 * Breaks a paragraph of text into rows. It is assumed that all 
 * preferred break points are supplied with the input String.
 * As a consequence, non standard hyphenation is not supported.
 * 
 * Soft hyphen (0x00ad), dash (0x002d) and space are used to
 * determine an appropriate break point. Soft hyphens are
 * removed in the result.
 * @author Joel Håkansson, TPB
 *
 */
public class BreakPointHandler {
	private final static char SOFT_HYPHEN = '\u00ad';
	private final static char ZERO_WIDTH_SPACE = '\u200b';
	private final static char DASH = '-';
	private final static char SPACE = ' ';
	private final static Pattern leadingWhitespace = Pattern.compile("\\A[\\s\u200b]+");
	private final static Pattern trailingWhitespace = Pattern.compile("[\\s\u200b]+\\z");
	private String charsStr;
	
	/**
	 * Create a new BreakPointHandler. All preferred break points 
	 * must be in supplied with the input String, represented by 
	 * hyphen 0x2d, soft hyphen 0xad or space 0x20.
	 * @param str the paragraph to break into rows. 
	 */
	public BreakPointHandler(String str) {
		if (str==null) {
			throw new NullPointerException("Input string cannot be null.");
		}
		this.charsStr = str;
	}

	/**
	 * Gets the next row from this BreakPointHandler
	 * @param breakPoint the desired breakpoint for this row
	 * @return returns the next BreakPoint
	 */
	public BreakPoint nextRow(int breakPoint, boolean force) {
		return doNextRow(breakPoint, force, false);
	}
	
	/**
	 * Tries to break the row at the breakpoint but does not modify the buffer.
	 * @param breakPoint the row length limit
	 * @return returns the break point
	 */
	public BreakPoint tryNextRow(int breakPoint) {
		return doNextRow(breakPoint, false, true);
	}
	
	private BreakPoint doNextRow(int breakPoint, boolean force, boolean test) {
		if (charsStr.length()==0) {
			// pretty simple...
			return new BreakPoint("", "", false);
		}

		assert charsStr.length()==charsStr.codePointCount(0, charsStr.length());
		if (charsStr.length()<=breakPoint) {
			return finalizeBreakpointTrimTail(charsStr, "", test, false);
		} else if (breakPoint<=0) {
			return finalizeBreakpointTrimTail("", charsStr, test, false);
		} else {
			return findBreakpoint(breakPoint, force, test);
		}
	}
	
	private BreakPoint findBreakpoint(int breakPoint, boolean force, boolean test) {
		int strPos = findBreakpointPosition(charsStr, breakPoint);
		assert strPos<charsStr.length();

		/*if (strPos>=charsStr.length()-1) {
			head = charsStr.substring(0, strPos);
			System.out.println(head);
			tailStart = strPos;
		} else */
		// check next character to see if it can be removed.
		if (strPos==charsStr.length()-1) {
			String head = charsStr.substring(0, strPos+1);
			int tailStart = strPos+1;
			return finalizeBreakpointFull(head, tailStart, test, false);
		} else if (charsStr.charAt(strPos + 1) == SPACE || charsStr.charAt(strPos + 1) == ZERO_WIDTH_SPACE) {
			String head = charsStr.substring(0, strPos+2); // strPos+1
			int tailStart = strPos+2;
			return finalizeBreakpointFull(head, tailStart, test, false);
		} else {
			return newBreakpointFromPosition(strPos, breakPoint, force, test);
		}
	}
	
	private BreakPoint newBreakpointFromPosition(int strPos, int breakPoint, boolean force, boolean test) {
		// back up
		int i=findBreakpointBefore(charsStr, strPos);
		String head;
		boolean hard = false;
		int tailStart;
		if (i<0) { // no breakpoint found, break hard 
			if (force) {
				hard = true;
				head = charsStr.substring(0, strPos+1);
				tailStart = strPos+1;
			} else {
				head = "";
				tailStart = 0;
			}
		} else if (charsStr.charAt(i)==SPACE) { // don't ignore space at breakpoint
			head = charsStr.substring(0, i+1); //i
			tailStart = i+1;
		} else if (charsStr.charAt(i)==SOFT_HYPHEN) { // convert soft hyphen to hard hyphen 
			head = charsStr.substring(0, i) + DASH;
			tailStart = i+1;
		}  else if (charsStr.charAt(i)==ZERO_WIDTH_SPACE) { // ignore zero width space 
			head = charsStr.substring(0, i);
			tailStart = i+1;
		} else if (charsStr.charAt(i)==DASH && charsStr.length()>1 && charsStr.charAt(i-1)==SPACE) {
			// if hyphen is preceded by space, back up one more
			head = charsStr.substring(0, i);
			tailStart = i;
		} else {
			head = charsStr.substring(0, i+1);
			tailStart = i+1;
		}
		return finalizeBreakpointFull(head, tailStart, test, hard);
	}
	
	private BreakPoint finalizeBreakpointFull(String head, int tailStart, boolean test, boolean hard) {
		String tail = getTail(tailStart);

		head = trailingWhitespace.matcher(head).replaceAll("");
		
		return finalizeBreakpointTrimTail(head, tail, test, hard);
	}
	
	private String getTail(int tailStart) {
		if (charsStr.length()>tailStart) {
			String tail = charsStr.substring(tailStart);
			assert (tail.length()<=charsStr.length());
			return tail;
		} else {
			return "";
		}
	}
	
	private BreakPoint finalizeBreakpointTrimTail(String head, String tail, boolean test, boolean hard) {
		//trim leading whitespace in tail
		tail = leadingWhitespace.matcher(tail).replaceAll("");
		head = finalize(head);
		if (!test) {
			charsStr = tail;
		}
		return new BreakPoint(head, tail, hard);
	}

	public int countRemaining() {
		if (charsStr==null) {
			return 0;
		}
		return getRemaining().length();
	}
	
	public String getRemaining() {
		return finalize(charsStr);
	}
	
	/**
	 * Finds the breakpoint position in the input string by counting
	 * all characters, excluding soft hyphen and zero width space.
	 *  
	 * @param charsStr
	 * @param breakPoint
	 * @return returns the breakpoint poisition
	 */
	private static int findBreakpointPosition(String charsStr, int breakPoint) {
		int strPos = -1;
		int len = 0;
		for (char c : charsStr.toCharArray()) {
			strPos++;
			switch (c) {
				case SOFT_HYPHEN: case ZERO_WIDTH_SPACE:
					break;
				default:
					len++;
			}
			if (len>=breakPoint) {
				break;
			}
		}
		return strPos;
	}
	
	/**
	 * Finds the break point closest before the starting position.
	 * @param charsStr
	 * @param strPos
	 * @return returns the break point, or -1 if none is found
	 */
	private static int findBreakpointBefore(String charsStr, int strPos) {
		int i = strPos;
whileLoop: while (i>=0) {
			switch (charsStr.charAt(i)) {
				case SPACE: case DASH: case SOFT_HYPHEN: case ZERO_WIDTH_SPACE:
					break whileLoop;
			}
			i--;
		}
		return i;
	}
	
	private String finalize(String str) {
		StringBuilder sb = new StringBuilder();
		for (char c : str.toCharArray()) {
			switch (c) {
				case SOFT_HYPHEN: case ZERO_WIDTH_SPACE:
					// remove from output
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
		/*
		return str.replaceAll(""+SOFT_HYPHEN, "").replaceAll(""+ZERO_WIDTH_SPACE, "");*/
	}
	/**
	 * Does this BreakPointHandler has any text left to break into rows 
	 * @return returns true if this BreakPointHandler has any text left to break into rows
	 */
	public boolean hasNext() {
		return (charsStr!=null && charsStr.length()>0);
	}

}
