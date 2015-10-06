package org.daisy.dotify.api.translator;


/**
 * Provides an interface for braille translation and hyphenation for a particular
 * locale. The locale is determined when the translator is instantiated
 * by the factory. 
 * @author Joel Håkansson
 */
public interface BrailleTranslator {
	
	/**
	 * Translates the string in the specified language and the specified text
	 * attributes.
	 * 
	 * @param text
	 *            the text to translate
	 * @param locale
	 *            the language/region of the text
	 * @param attributes
	 *            the attributes of the text, or null if none applies. The
	 *            length of all attributes must be equal to the length of the
	 *            text.
	 * @return returns the translator result
	 * @throws TranslationException
	 *             if the locale is not supported by the implementation
	 * @throws IllegalArgumentException
	 *             if the sum of all attributes length is not equal to the
	 *             length of the text
	 * @deprecated use translate(Translatable)
	 */
	public BrailleTranslatorResult translate(String text, String locale, TextAttribute attributes) throws TranslationException;

	/**
	 * Translates the string in the specified language and the specified text
	 * attributes.
	 * 
	 * @param text
	 *            the text to translate
	 * @param attributes
	 *            the attributes of the text. The
	 *            length of all attributes must be equal to the length of the
	 *            text.
	 * @return returns the translator result
	 * @throws IllegalArgumentException
	 *             if the sum of all attributes length is not equal to the
	 *             length of the text
	 * @deprecated use translate(Translatable)
	 */
	public BrailleTranslatorResult translate(String text, TextAttribute attributes);

	/**
	 * Translates the string in the specified language.
	 * 
	 * @param text
	 *            the text to translate
	 * @param locale
	 *            the language/region of the text
	 * @return returns the translator result
	 * @throws TranslationException
	 *             if the locale is not supported by the implementation
	 * @deprecated use translate(Translatable)
	 */
	public BrailleTranslatorResult translate(String text, String locale) throws TranslationException;

	/**
	 * Translate the string using the translator's default language.
	 * @param text
	 * @return returns the translator result
	 * @deprecated use translate(Translatable)
	 */
	public BrailleTranslatorResult translate(String text);
	
	/**
	 * Translates a text into braille using the supplied specification. 
	 * Note that the global hyphenation value is ignored, as the 
	 * hyphenation policy is defined in the Translatable.
	 * 
	 * @param specification the specification
	 * @return returns a translator result
	 * @throws TranslationException
	 *             if the locale is not supported by the implementation
	 * @throws IllegalArgumentException
	 *             if the sum of all attributes length is not equal to the
	 *             length of the text
	 */
	public BrailleTranslatorResult translate(Translatable specification) throws TranslationException;
	
	/**
	 * Sets hyphenating to the specified value.  Setting hyphenating to false indicates 
	 * that hyphenation should not be performed.
	 * @deprecated use translate(Translatable)
	 */
	public void setHyphenating(boolean value);
	
	/**
	 * Returns true if the translator is hyphenating.
	 * @return returns true if the translator is hyphenating, false otherwise.
	 * @deprecated the global hyphenation policy will be removed in future versions
	 */
	public boolean isHyphenating();
	
	public String getTranslatorMode();

}
