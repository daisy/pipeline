/**
 * <p>Provides a braille translation API.</p>
 * <p>This package contains interfaces needed to transform text into braille.</p>
 * <p>
 * The design allows hyphenation to be handled internally by the translator,
 * which enables support for non-standard hyphenation as well as line break
 * dependent braille markers, such as continuation signs etc.
 * </p>
 * 
 * <p>The SPI entry points for using this package are:</p>
 * <ul>
 * <li>{@link org.daisy.dotify.api.translator.BrailleFilterFactoryMaker}</li>
 * <li>{@link org.daisy.dotify.api.translator.BrailleTranslatorFactoryMaker}</li>
 * <li>{@link org.daisy.dotify.api.translator.TextBorderFactoryMaker}</li>
 * </ul>
 * <p>The OSGi entry points for using this package are:</p>
 * <ul>
 * <li>{@link org.daisy.dotify.api.translator.BrailleFilterFactoryMakerService}</li>
 * <li>{@link org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService}</li>
 * <li>{@link org.daisy.dotify.api.translator.TextBorderFactoryMakerService}</li>
 * </ul>
 * 
 * <p>To add additional implementations, use the following interfaces:</p>
 * <ul>
 * <li>{@link org.daisy.dotify.api.translator.BrailleFilterFactoryService}</li>
 * <li>{@link org.daisy.dotify.api.translator.BrailleTranslatorFactoryService}</li>
 * <li>{@link org.daisy.dotify.api.translator.TextBorderFactoryService}</li>
 * </ul>
 * 
 * @author Joel Håkansson
 */
package org.daisy.dotify.api.translator;