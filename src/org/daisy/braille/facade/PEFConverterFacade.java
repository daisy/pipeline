/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.braille.facade;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.daisy.braille.api.paper.PageFormat;
import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserCatalogService;
import org.daisy.braille.embosser.EmbosserFactoryException;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.UnsupportedWidthException;
import org.daisy.braille.pef.PEFHandler;
import org.daisy.braille.pef.PEFHandler.Alignment;
import org.daisy.braille.pef.Range;
import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;
import org.xml.sax.SAXException;

/**
 * Provides a facade for both PEFHandler and TextHandler
 * @author Joel Håkansson
 */
public class PEFConverterFacade {

	/**
	 * Key for parsePefFile setting,
	 * corresponding settings value should match an embosser identifier
	 */
	public final static String KEY_EMBOSSER = "embosser";
	/**
	 * Key for parsePefFile setting,
	 * corresponding settings value should match a table identifier
	 */
	public final static String KEY_TABLE = "table";
	/**
	 * Key for parsePefFile setting,
	 * corresponding settings value should match a value in StandardLineBreaks.Type
	 */
	public final static String KEY_BREAKS = "breaks";
	/**
	 * Key for parsePefFile setting,
	 * corresponding settings value should match a range
	 */
	public final static String KEY_RANGE = "range";
	/**
	 * Key for parsePefFile setting,
	 * corresponding settings value should match a value in {@link EightDotFallbackMethod}
	 */
	public final static String KEY_FALLBACK = "fallback";
	/**
	 * Key for parsePefFile setting,
	 * corresponding settings value should be a character in the range 0x2800-0x283F
	 */
	public final static String KEY_REPLACEMENT = "replacement";
	/**
	 * Key for parsePefFile setting,
	 * corresponding settings value should match a padding style
	 */
	public final static String KEY_PADDING = "pad";
	/**
	 * Key for parsePefFile setting,
	 * corresponding settings value should be the number of characters to offset alignment by  
	 */
	public final static String KEY_ALIGNMENT_OFFSET = "alignmentOffset";
	/**
	 * Key for parsePefFile setting,
	 * corresponding settings value should match a value in {@link Alignment}
	 */
	public final static String KEY_ALIGN = "align";
	/**
	 * Key for parsePefFile setting,
	 * corresponding settings value should be a number, in millimeters
	 */
	public final static String KEY_CELL_WIDTH = "cellWidth";
	/**
	 * Key for parsePefFile setting,
	 * corresponding settings value should be a number, in millimeters 
	 */
	public final static String KEY_CELL_HEIGHT = "cellHeight";
	
	private final EmbosserCatalogService ef;
	public PEFConverterFacade(EmbosserCatalogService embosserFactory) {
		this.ef = embosserFactory;
	}

	/**
	 * Parses the given PEF-file input using the supplied output stream and settings.
	 * @param input
	 * @param os
	 * @param settings
	 * @throws NumberFormatException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws EmbosserFactoryException
	 * @throws UnsupportedWidthException
	 */
	public void parsePefFile(File input, OutputStream os, PageFormat pf, Map<String, String> settings) throws NumberFormatException, ParserConfigurationException, SAXException, IOException, EmbosserFactoryException, UnsupportedWidthException {
		Range range = null;
		Alignment align = Alignment.CENTER_OUTER;
		int offset = 0;
		Embosser emb = null;
		emb = ef.newEmbosser(settings.remove(KEY_EMBOSSER));
		if (emb==null) {
			emb = ef.newEmbosser("org_daisy.GenericEmbosserProvider.EmbosserType.NONE");
		}
		if (pf!=null) {
			emb.setFeature(EmbosserFeatures.PAGE_FORMAT, pf);
		}
		for (String key : settings.keySet()) {
			String value = settings.get(key);
			if (KEY_TABLE.equals(key)) {
				emb.setFeature(EmbosserFeatures.TABLE, value);
			} else if (KEY_BREAKS.equals(key)) {
				emb.setFeature("breaks", value);
			} else if (KEY_RANGE.equals(key)) {
				range = Range.parseRange(value);
			} else if (KEY_FALLBACK.equals(key)) {
				emb.setFeature(EmbosserFeatures.UNSUPPORTED_CELL_FALLBACK_METHOD, value);
			} else if (KEY_REPLACEMENT.equals(key)) {
				emb.setFeature(EmbosserFeatures.UNSUPPORTED_CELL_REPLACEMENT, value);
			} else if (KEY_PADDING.equals(key)) {
				emb.setFeature("padNewline", value);
			} else if (KEY_ALIGNMENT_OFFSET.equals(key)) {
				offset = Integer.parseInt(value);
			} else if (KEY_ALIGN.equals(key)) {
				if (value.equalsIgnoreCase("center")) {
					value = "CENTER_OUTER";
				}
				try {
					align = Alignment.valueOf(value.toUpperCase());
				} catch (IllegalArgumentException e) {
					System.out.println("Unknown value: " + value);
				}
			} else if (KEY_CELL_WIDTH.equals(key)) {
				emb.setFeature(EmbosserFeatures.CELL_WIDTH, value);
			} else if (KEY_CELL_HEIGHT.equals(key)) {
				emb.setFeature(EmbosserFeatures.CELL_HEIGHT, value);
			} else {
				throw new IllegalArgumentException("Unknown option \"" + key + "\"");
			}
		}
		
		EmbosserWriter embosser = emb.newEmbosserWriter(os);
		PEFHandler.Builder builder = 
			new PEFHandler.Builder(embosser).
				range(range).
				align(align).
				offset(offset);
		PEFHandler ph = builder.build();
		parsePefFile(input, ph);
	}

	/**
	 * Parses the given input using the supplied PEFHandler.
	 * @param input the input PEF file
	 * @param ph the PEFHandler to use
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws UnsupportedWidthException
	 */
	public void parsePefFile(File input, PEFHandler ph) throws ParserConfigurationException, SAXException, IOException, UnsupportedWidthException {
		if (!input.exists()) {
			throw new IllegalArgumentException("Input does not exist");
		}
		FileInputStream is = new FileInputStream(input);
		parsePefFile(is, ph);
	}
	
	/**
	 * Parses the given input stream using the supplied PEFHandler.
	 * @param is the input stream
	 * @param ph the PEFHandler
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws UnsupportedWidthException
	 */
	public void parsePefFile(InputStream is, PEFHandler ph) throws ParserConfigurationException, SAXException, IOException, UnsupportedWidthException {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		SAXParser sp = spf.newSAXParser();
		try {
			sp.parse(is, ph);
		} catch (SAXException e) {
			if (ph.hasWidthError()) {
				throw new UnsupportedWidthException(e);
			} else {
				throw e;
			}
		}		
	}

}
