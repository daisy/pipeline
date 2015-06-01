package org.daisy.dotify.api.translator;


/**
 * Provides an interface for braille filter for a particular
 * locale. The locale is determined when the translator is instantiated
 * by the factory. 
 * @author Joel Håkansson
 */
public interface BrailleFilter {
	
	/**
	 * Translates the string into braille using the specification.
	 * The returned string should only contain:
	 * <ul>
	 * <li>braille patterns (unicode range U+2800 to U+28FF)</li>
	 * <li>Space (U+0020)</li>
	 * <li>Soft hyphen (U+00AD)</li>
	 * <li>Zero width space (U+200B)</li>
	 * </ul>
	 * Other characters may be returned as they were if they couldn't be 
	 * converted into braille. 
	 * 
	 * @param specification
	 *            the specification
	 * @return returns the translated string
	 * @throws TranslationException
	 *             if the locale is not supported by the implementation
	 * @throws IllegalArgumentException
	 *             if the sum of all attributes length is not equal to the
	 *             length of the text
	 */
	public String filter(Translatable specification) throws TranslationException;

}
