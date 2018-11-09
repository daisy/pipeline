package org.daisy.braille.utils.pef;


/**
 * Provides a search index for PEFBook objects.
 * @author Joel Håkansson
 *
 */
public class PEFSearchIndex extends SearchIndex<PEFBook> {
	private static final String REGEX = "[\\s\\.,:/-]";

	/**
	 * Creates a new pef search index.
	 */
	public PEFSearchIndex() {
		super();
	}

	/**
	 * Creates a new pef search index with the specified limit.
	 * @param exclude the sub word limit, in other words
	 * 		  the shortest substrings that will match the search
	 * 		  term. For example, if the limit is 3, a search for 
	 * 		  "li" will not match entries containing the string "limit" 
	 */
	public PEFSearchIndex(int exclude) {
		super(exclude);
	}

	/**
	 * Adds a PEFBook to the index.
	 * @param p the book to add
	 */
	public void add(PEFBook p) {
		for (String key : p.getMetadataKeys()) {
			for (String val : p.getMetadata(key)) {
				if ("format".equals(key)) {
					continue;
				}
				for (String ind : val.toLowerCase().split(REGEX)) {
					if (ind!=null && ind.length()>0) {
						if ("identifier".equals(key)) {
							add(ind, p, true);
						} else {
							add(ind, p, false);
						}
					}
				}
			}
		}
	}
}
