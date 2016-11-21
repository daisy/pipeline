package org.daisy.dotify.common.text;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;


/**
 * Breaks a paragraph of text into rows. It is assumed that all 
 * preferred break points are supplied with the input string.
 * 
 * Soft hyphen (0x00ad) and zero width space (0x200b) characters
 * can also be used for non-standard hyphenation.
 * 
 * Soft hyphen (0x00ad), zero width space (0x200b), dash (0x002d)
 * and space are used to determine an appropriate break point. Soft
 * hyphens are removed in the result.
 * @author Joel Håkansson
 *
 */
public class BreakPointHandler {
	private static final char SOFT_HYPHEN = '\u00ad';
	private static final char ZERO_WIDTH_SPACE = '\u200b';
	private static final char DASH = '-';
	private static final char SPACE = ' ';
	private static final Pattern LEADING_WHITESPACE = Pattern.compile("\\A[\\s\u200b]+");
	private static final Pattern TRAILING_WHITESPACE = Pattern.compile("[\\s\u200b]+\\z");
	private String charsStr;
	private int offset;
	private TreeMap<Integer, NonStandardHyphenationInfo> meta;
	
	/**
	 * Provides a builder for break point handlers
	 * @author Joel Håkansson
	 *
	 */
	public static class Builder {
		private final String str;
		private final TreeMap<Integer, NonStandardHyphenationInfo> meta;
		
		/**
		 * Creates a new builder with the string to break.
		 * All regular break points must be in supplied with the input string,
		 * represented by hyphen 0x2d, soft hyphen 0xad or space 0x20.
		 * @param str the string
		 */
		public Builder(String str) {
			this.str = str;
			this.meta = new TreeMap<>();
		}

		/**
		 * Adds a non-standard hyphenation rule to apply if the hyphenation point
		 * within the specified range is chosen for hyphenation.
		 * 
		 * @param offset the offset where the rule applies
		 * @param length the length of segment that should be replaced
		 * @param replacement the replacement string, must contain exactly 
		 * 		one soft hyphen OR exactly one zero width space. Furthermore, the
		 * 		replacement string is expected to push the hyphenation point 
		 * 		towards the end of the text.
		 * @return returns the builder
		 */
		public Builder addHyphenationInfo(int offset, int length, String replacement) {
			if (str.length()<offset+length) {
				throw new IndexOutOfBoundsException();
			}
			//TODO: Verify that range includes at least one hyphenation point 
			NonStandardHyphenationInfo info = new NonStandardHyphenationInfo(replacement, length);
			meta.put(offset, info);
			return this;
		}

		/**
		 * Creates a new break point handler with the specified configuration.
		 * @return returns a new BreakPointHandler
		 */
		public BreakPointHandler build() {
			if (meta.isEmpty()) {
				return new BreakPointHandler(str, null, 0);
			} else {
				return new BreakPointHandler(str, meta, 0);
			}
		}
	}
	
	/**
	 * Create a new BreakPointHandler. All preferred break points 
	 * must be in supplied with the input String, represented by 
	 * hyphen 0x2d, soft hyphen 0xad or space 0x20.
	 * @param str the paragraph to break into rows. 
	 */
	public BreakPointHandler(String str) {
		this(str, null, 0);
	}

	@SuppressWarnings("unchecked")
	private BreakPointHandler(String str, TreeMap<Integer, NonStandardHyphenationInfo> meta, int offset) {
		if (str==null) {
			throw new NullPointerException("Input string cannot be null.");
		}
		this.charsStr = str;
		this.offset = offset;
		if (meta!=null) {
			this.meta = (TreeMap<Integer, NonStandardHyphenationInfo>)meta.clone();
		} else {
			this.meta = null;
		}
	}
	
	/**
	 * Creates a new copy of this object in its current state.
	 * @return returns a new instance
	 */
	public BreakPointHandler copy() {
		return new BreakPointHandler(charsStr, meta, offset);
	}

	/**
	 * Gets the next row from this BreakPointHandler
	 * @param breakPoint the desired breakpoint for this row
	 * @param force if force is allowed if no breakpoint is found
	 * @return returns the next BreakPoint
	 */
	public BreakPoint nextRow(int breakPoint, boolean force) {
		if (charsStr.length()==0) {
			// pretty simple...
			return new BreakPoint("", "", false);
		}

		assert charsStr.length()==charsStr.codePointCount(0, charsStr.length());
		if (charsStr.length()<=breakPoint) {
			return finalizeBreakpointTrimTail(charsStr, "", false);
		} else if (breakPoint<=0) {
			return finalizeBreakpointTrimTail("", charsStr, false);
		} else {
			return findBreakpoint(breakPoint, force);
		}
	}
	
	private BreakPoint findBreakpoint(int breakPoint, boolean force) {
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
			return finalizeBreakpointFull(head, tailStart, false);
		} else if (charsStr.charAt(strPos + 1) == SPACE || charsStr.charAt(strPos + 1) == ZERO_WIDTH_SPACE) {
			String head = charsStr.substring(0, strPos+2); // strPos+1
			int tailStart = strPos+2;
			return finalizeBreakpointFull(head, tailStart, false);
		} else {
			return newBreakpointFromPosition(strPos, breakPoint, force);
		}
	}
	
	private BreakPoint newBreakpointFromPosition(int strPos, int breakPoint, boolean force) {
		// back up
		int i=findBreakpointBefore(strPos);
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
		return finalizeBreakpointFull(head, tailStart, hard);
	}
	
	private BreakPoint finalizeBreakpointFull(String head, int tailStart, boolean hard) {
		String tail = getTail(tailStart);

		head = TRAILING_WHITESPACE.matcher(head).replaceAll("");
		
		return finalizeBreakpointTrimTail(head, tail, hard);
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
	
	private BreakPoint finalizeBreakpointTrimTail(String head, String tail, boolean hard) {
		//trim leading whitespace in tail
		tail = LEADING_WHITESPACE.matcher(tail).replaceAll("");
		head = finalizeResult(head);
		offset = charsStr.length() - tail.length();
		charsStr = tail;
		return new BreakPoint(head, tail, hard);
	}

	/**
	 * Counts the remaining characters, excluding unused breakpoints.
	 * @return returns the number of remaining characters
	 */
	public int countRemaining() {
		if (charsStr==null) {
			return 0;
		}
		return getRemaining().length();
	}
	
	/**
	 * Gets the remaining characters, removing unused breakpoint characters.
	 * @return returns the remaining characters
	 */
	public String getRemaining() {
		return finalizeResult(charsStr);
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
	private int findBreakpointBefore(int strPos) {
		int i = strPos;
whileLoop: while (i>=0) {
			switch (charsStr.charAt(i)) {
				case SOFT_HYPHEN: case ZERO_WIDTH_SPACE:
					boolean done = true;
					if (meta!=null) {
						Entry<Integer, NonStandardHyphenationInfo> entry = meta.floorEntry(i+offset);
						if (entry!=null) {
							int head = NonStandardHyphenationInfo.getHeadLength(charsStr, entry.getKey()-offset);
							if ((entry.getKey()+head)>i+offset) { // the closest entry is applicable
								if (i+head<=strPos) { // the closest entry fits
									NonStandardHyphenationInfo rule = entry.getValue();
									//patch string
									charsStr = rule.apply(charsStr, entry.getKey()-offset);
									i = entry.getKey() - offset + head;
								} else { //find another breakpoint
									done = false;
								}
							}
						}
					}
					if (done) {
						break whileLoop;
					}
					break;
				case SPACE: case DASH:
					//non-standard hyphenation does not apply
					break whileLoop;
			}
			i--;
		}
		return i;
	}
	
	private String finalizeResult(String str) {
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
