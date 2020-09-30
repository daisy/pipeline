package org.daisy.pipeline.braille.common;

/**
 * Hyphenating means breaking within words.
 */
public interface Hyphenator extends Transform {
	
	/**
	 * Hyphenate by inserting soft hyphens and zero width spaces in order to indicate all
	 * the break opportunities within words. Don't add new break opportunities to words
	 * that already have a soft hyphen or zero width space it them. Apart from the
	 * insertion of these special characters no other transformations are allowed. This
	 * means that non-standard hyphenation can not be supported through this interface.
	 */
	public FullHyphenator asFullHyphenator() throws UnsupportedOperationException;
	
	/**
	 * Hyphenate by breaking an input into lines of a preferred and maximal
	 * length. Transformations such as non-standard hyphenation are
	 * allowed.
	 */
	public LineBreaker asLineBreaker() throws UnsupportedOperationException;
	
	/* ------------------ */
	/* asFullHyphenator() */
	/* ------------------ */
	
	public interface FullHyphenator {
		
		public String transform(String text);
		public String[] transform(String[] text);
		
	}
	
	/* --------------- */
	/* asLineBreaker() */
	/* --------------- */
	
	public interface LineBreaker {
		
		public LineIterator transform(String input);
		
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
