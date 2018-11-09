/**
 * <p>
 * Provides PagedMediaWriter implementations.
 * </p>
 * 
 * <p>
 * Note on adding PagedMediaWriter implementations: First consider if your needs
 * could be met by converting a PEF file to your desired format using (or
 * extending) <a href="http://code.google.com/p/brailleutils/">Braille
 * Utils</a>. If not, plase add a class to this package, implementing
 * "PagedMediaWriter". If your output format requirements cannot be met by
 * implementing PagedMediaWriter, please consider submitting a <a
 * href="http://code.google.com/p/dotify/issues/list">feature request</a>.
 * </p>
 * @author Joel Håkansson
 */
package org.daisy.dotify.formatter.impl.writer;