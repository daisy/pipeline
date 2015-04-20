/**
 * <p>Provides braille transformation classes. This package contains everything
 * needed to transform text into braille. Hyphenation is handled
 * internally by the translator. This design allows
 * for non-standard hyphenation as well as line break dependent braille
 * markers, such as continuation signs etc.</p>
 * 
 * <p>The entry point for translating braille is the BrailleTranslatorFactoryMaker
 * where an instance of a BrailleTranslator can be obtained.</p>
 * 
 * <p>To add a translator for another language:</p>
 * <ul>
 * <li>Implement BrailleTranslator for your locale and place it the 
 * org.daisy.dotify.impl.translator package or in a sub package thereof.</li>
 * <li>Add the name of your implementation to the org.daisy.dotify.translator.BrailleFilter
 *  file in META-INF/services</li>
 *  <li>If your implementation uses a grade not defined in {@link org.daisy.dotify.api.translator.BrailleTranslatorFactory},
 *  please add it to the API so that others may know about it.</li>
 * </ul>
 * @author Joel Håkansson
 */
package org.daisy.dotify.translator;