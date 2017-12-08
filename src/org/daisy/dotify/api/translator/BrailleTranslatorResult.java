package org.daisy.dotify.api.translator;

/**
 * Provides a braille translator result.
 * @author Joel Håkansson
 *
 */
public interface BrailleTranslatorResult {

	/**
	 * Metric identifier representing the total number of forced breaks applied
	 * on the instance up to this point. Note that this only counts actually forced
	 * breaks, not breaks where forced was <i>allowed but not used</i> .
	 */
	public static final String METRIC_FORCED_BREAK = "forced-break-count";

	/**
	 * Metric identifier representing the total number of breaks applied at break
	 * points <i>inside words</i> on the instance up to this point.
	 */
	public static final String METRIC_HYPHEN_COUNT = "word-break-count";

	/**
	 * Gets the translated string preceding the row break, including a translated 
	 * hyphen at the end, if needed. The length of the translated text must not exceed the
	 * specified <tt>limit</tt>. If <tt>force</tt> is not used, the result could be empty and no 
	 * characters removed from the buffer. A caller would typically set <tt>force</tt> to 
	 * true when <tt>limit</tt> is equal to the maximum number of available characters 
	 * on a row.
	 * 
	 * @param limit specifies the maximum number of characters allowed in the result
	 * @param force specifies if the translator should force a break at the limit
	 * 				 if no natural break point is found 
	 * @return returns the translated string
	 */
	public default String nextTranslatedRow(int limit, boolean force) {
		return nextTranslatedRow(limit, force, false);
	}

	/**
	 * <p>
	 * Gets the translated string preceding the row break, including a translated 
	 * hyphen at the end, if needed. The length of the translated text must not exceed the
	 * specified <tt>limit</tt>. If <tt>force</tt> is not used, the result could be empty and no 
	 * characters removed from the buffer. A caller would typically set <tt>force</tt> to 
	 * true when <tt>limit</tt> is equal to the maximum number of available characters 
	 * on a row.</p>
	 * 
	 * <p>
	 * When <code>wholeWordsOnly</code> is set to true, the row may not end on a break point
	 * inside a word. However, when combined with <code>force</code>, a row may end on such a break point
	 * if the distance to the first word boundary is longer than the row <em>and</em>
	 * there is a suitable break point inside the word.</p>
	 * 
	 * @param limit specifies the maximum number of characters allowed in the result
	 * @param force specifies if the translator should force a break at the limit
	 * 				 if no natural break point is found
	 * @param wholeWordsOnly specifies that the row may not end on a break point inside a word.
	 * @return returns the translated string
	 */
	public String nextTranslatedRow(int limit, boolean force, boolean wholeWordsOnly);
	
	/**
	 * Gets the translated remainder, in other words the characters not
	 * yet extracted with <tt>nextTranslatedRow</tt>.
	 * @return returns the translated remainder
	 */
	public String getTranslatedRemainder();
	
	/**
	 * Returns the number of characters remaining in the result. This
	 * number equals the number of characters in <tt>getTranslatedRemainder</tt>.
	 * @return returns the number of characters remaining
	 */
	public int countRemaining();

	/**
	 * Returns true if there are characters remaining in the result, in other
	 * words of there are characters not yet extracted with <tt>nextTranslatedRow</tt>.
	 * @return returns true if there are characters remaining, false otherwise
	 */
	public boolean hasNext();
	
	/**
	 * <p>Returns true if this result supports the given metric,
	 * false otherwise.</p>
	 * <p>Metrics specified by the API are included as static strings in
	 * this class, but additional ones may be offered by an implementation. </p>
	 * @param metric the metric identifier 
	 * @return returns true if the given metric is supported, false otherwise
	 */
	public boolean supportsMetric(String metric);
	
	/**
	 * <p>Gets the value of a given metric.</p>
	 * <p>Metrics specified by the API are included as static strings in
	 * this interface, but additional ones may be offered by an implementation.
	 * Note that an implementation is not required to support any metrics.
	 * Metric support can be tested with <code>supportsMetric</code>.</p>
	 * @param metric the metric identifier
	 * @return the metric value
	 * @throws UnsupportedMetricException if the metric is not supported
	 */
	public double getMetric(String metric);
	
	/**
	 * Returns a copy of this result in the current state.
	 * @return returns a copy of the result
	 * @throws UnsupportedOperationException if the operation is not supported
	 */
	public BrailleTranslatorResult copy();

}
