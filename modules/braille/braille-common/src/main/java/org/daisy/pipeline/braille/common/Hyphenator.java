package org.daisy.pipeline.braille.common;

import java.util.Locale;

import org.daisy.pipeline.braille.css.CSSStyledText;

/**
 * Hyphenation means morphological breaking within <em>words</em>. However this class is
 * not only responsible for providing hyphenation opportunities, but all soft wrap
 * opportunities (including but not restricted to those defined by the Unicode line
 * breaking rules [UAX14]).
 */
public interface Hyphenator extends Transform {
	
	/**
	 * Indicate all soft wrap opportunities by inserting soft hyphens and zero width
	 * spaces. Don't add new soft wrap opportunities to words that already have a soft
	 * hyphen or zero width space in them. Soft wrap opportunities at white space may also
	 * be omitted. Apart from the insertion of these special characters no other
	 * transformations are allowed. This means that non-standard hyphenation can not be
	 * supported through this interface.
	 */
	public FullHyphenator asFullHyphenator() throws UnsupportedOperationException;
	
	/**
	 * Break the input into lines of a preferred and maximal length. Transformations such
	 * as non-standard hyphenation are allowed.
	 */
	public LineBreaker asLineBreaker() throws UnsupportedOperationException;
	
	/* ------------------ */
	/* asFullHyphenator() */
	/* ------------------ */
	
	public interface FullHyphenator {
		
		public Iterable<CSSStyledText> transform(Iterable<CSSStyledText> text) throws NonStandardHyphenationException;
		
	}
	
	/* --------------- */
	/* asLineBreaker() */
	/* --------------- */
	
	public interface LineBreaker {
		
		public LineIterator transform(String input, Locale language);
		
	}
	
	/**
	 * Thrown by {@link FullHyphenator} if non-standard breaks are present.
	 */
	@SuppressWarnings("serial")
	public class NonStandardHyphenationException extends RuntimeException {

		public NonStandardHyphenationException() {
			super();
		}

		public NonStandardHyphenationException(Throwable cause) {
			super(cause);
		}
	}

	/* ------------ */
	/* LineIterator */
	/* ------------ */
	
	public interface LineIterator {
		
		/**
		 * Returns true if there is remaining text to make a new line of.
		 */
		public boolean hasNext();
		
		/**
		 * Get the next line of text, with a length as close as possible to, but not
		 * exceding `limit` characters, breaking a word if needed. `limit` must be greater
		 * than 0. If `force` is true, may not return an empty string. If `allowHyphens`
		 * is false, breaking words is not permitted unless `force` is true and an empty
		 * string would otherwise be returned. Must throw an exception if there is no
		 * remaining text to make a new line of.
		 */
		public String nextLine(int limit, boolean force, boolean allowHyphens);
		public String nextLine(int limit, boolean force);
		
		/**
		 * Whether or not a hyphen character should be inserted at the end of
		 * the current line to indicate a word break. Must throw an exception
		 * if `nextLine` hasn't been called yet.
		 */
		public boolean lineHasHyphen();
		
		public String remainder();
		
		/**
		 * Set a checkpoint to which the iterator can be `reset` later.
		 */
		public void mark();
		
		/**
		 * Roll back to a checkpoint previously set with `mark`, or to the
		 * beginning if `mark` hasn't been called yet.
		 */
		public void reset();
		
	}
}
