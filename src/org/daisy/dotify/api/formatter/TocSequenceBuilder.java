package org.daisy.dotify.api.formatter;

/**
 * Provides methods needed to build a TOC sequence.
 * 
 * @author Joel Håkansson
 *
 */
public interface TocSequenceBuilder {

	/**
	 * Creates a new on toc start block
	 * @param useWhen a condition, or null
	 * @return returns the formatter for the events
	 */
	public FormatterCore newOnTocStart(String useWhen);

	/**
	 * Creates a new on toc start block that always apply
	 * @return returns the formatter for the events
	 */
	public FormatterCore newOnTocStart();

	/**
	 * Creates a new on volume start block
	 * @param useWhen a condition, or null
	 * @return returns the formatter for the events
	 */
	public FormatterCore newOnVolumeStart(String useWhen);

	/**
	 * Creates a new on volume start block that always apply
	 * @return returns the formatter for the events
	 */
	public FormatterCore newOnVolumeStart();

	/**
	 * Creates a new on volume end block
	 * @param useWhen a condition, or null
	 * @return returns the formatter for the events
	 */
	public FormatterCore newOnVolumeEnd(String useWhen);

	/**
	 * Creates a new on volume end block that always apply
	 * @return returns the formatter for the events
	 */
	public FormatterCore newOnVolumeEnd();

	/**
	 * Creates a new on toc end block
	 * @param useWhen a condition, or null
	 * @return returns the formatter for the events
	 */
	public FormatterCore newOnTocEnd(String useWhen);

	/**
	 * Creates a new on toc end block that always apply
	 * @return returns the formatter for the events
	 */
	public FormatterCore newOnTocEnd();

}
