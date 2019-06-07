/**
 * <p>
 * Provides {@link org.daisy.dotify.api.writer.PagedMediaWriterFactoryService} implementations.
 * </p>
 * 
 * <p>
 * Note on adding PagedMediaWriter implementations: First consider if your needs
 * can be met by converting a PEF file to your desired format using (or
 * extending) <a href="https://github.com/brailleapps/braille-utils.impl">Braille
 * Utils</a>. If not, please add a class to this package, implementing
 * "PagedMediaWriter". If your output format requirements cannot be met by
 * implementing PagedMediaWriter, please consider submitting a <a
 * href="https://github.com/brailleapps/dotify.api/issues">feature request</a>.
 * </p>
 * 
 * <p><b>IMPORTANT: This package contains implementations that should only be 
 * accessed using the Java SPI or OSGi. Additional classes in this package 
 * should only be used by the implementations herein.</b>
 * </p>
 * @author Joel Håkansson
 */
package org.daisy.dotify.formatter.impl.writer;