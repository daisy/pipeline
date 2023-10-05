package org.daisy.pipeline.braille.dotify.calabash.impl;

import javax.xml.namespace.QName;

import org.daisy.dotify.api.table.BrailleConverter;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.Row;

/**
 * PEF writer that converts braille from specified character set to Unicode braille.
 */
class PEFWriter extends PEFMediaWriter implements PagedMediaWriter {

	private static final QName DP2_ASCII = new QName("http://www.daisy.org/ns/pipeline/", "ascii", "dp2");

	private final BrailleConverter brailleCharset;
	private final Character space;

	/**
	 * @param brailleCharset Braille character set of provided rows objects.
	 *                       <code>null</code> means the rows contain Unicode braille.
	 */
	PEFWriter(Table brailleCharset) {
		super();
		if (brailleCharset != null) {
			this.brailleCharset = brailleCharset.newBrailleConverter();
			this.space = this.brailleCharset.toText("\u2800").toCharArray()[0];
			// trick to add namespace nodes to "pef" element
			metadata.add(new MetaDataItem(new QName("http://www.w3.org/2000/xmlns/", DP2_ASCII.getPrefix(), "xmlns"),
			                              DP2_ASCII.getNamespaceURI()));
		} else {
			this.brailleCharset = null;
			this.space = '\u2800';
		}
	}

	@Override
	public void newRow(Row row) {
		state.assertOpen();
		pst.print("<row");
		if (row.getRowSpacing() != null)
			pst.print(" rowgap=\"" + (int) Math.floor((row.getRowSpacing() - 1) * 4) + "\"");
		String chars = row.getChars();
		if (chars.length() > 0) {
			// HACK: because the Liblouis based translator always uses a space ('\s') to represent a
			// blank dot pattern, no matter which braille character set was specified, we replace
			// spaces with the correct representation here.
			if (space != ' ')
				chars = chars.replace(' ', space);
			// also replace '\u2800' just to be sure
			if (space != '\u2800')
				chars = chars.replace('\u2800', space);
			if (brailleCharset != null) {
				pst.print(" " + DP2_ASCII.getPrefix() + ":" + DP2_ASCII.getLocalPart() + "=\"" + chars.replace("&", "&amp;")
				                                                                                      .replace("\"", "&quot;")
				                                                                                      .replace("<", "&lt;")
				                                                                                      .replace(">", "&gt;")
				                                                                                      + "\"");
				try {
					chars = brailleCharset.toBraille(chars);
				} catch (RuntimeException e) {
					throw new RuntimeException("Braille character set '" + brailleCharset + "' does not contain character", e);
				}
			}
			pst.println(">" + chars + "</row>");
		} else
			pst.println("/>");
	}
}
