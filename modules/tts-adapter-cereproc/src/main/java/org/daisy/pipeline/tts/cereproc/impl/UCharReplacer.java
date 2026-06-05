package org.daisy.pipeline.tts.cereproc.impl;

/*
 * org.daisy.util (C) 2005-2008 Daisy Consortium
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

import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Substitute unicode characters with replacement strings.
 *
 * <p>The substitution is made using different attempts in a series of preference; each successor is
 * considered a fallback to its predecessor.</p>
 * <ol>
 *   <li>Locate a replacement string in one or several user provided tables;</li>
 * </ol>
 *
 * <p>The use of this class <em>may</em> result in a change in unicode character composition between
 * input and output. If you need a certain normalization form, normalize after the use of this
 * class.</p>
 *
 * <p>Usage example:</p>
 * <code><pre>
 *   UCharReplacer ucr = new UCharReplacer();
 *   ucr.addTranslationTable(fileURL);
 *   ucr.addTranslationTable(fileURL2);
 *   String ret = ucr.replace(input);
 * </pre></code>
 *
 * <p>The translation table file is using the same xml format as that of java.util.Properties
 * [1][2], using the HEX representation (without the characteristic 0x-prefix!) of a unicode
 * character as the <tt>key</tt> attribute and the replacement string as value of the <tt>entry</tt>
 * element.</p>
 *
 * <p>If the <tt>key</tt> attribute contains exactly one unicode codepoint (one character) it will
 * be treated literally. It will not be interpreted as a HEX representation of another character,
 * even if theoretically possible. E.g. if the <tt>key</tt> is "a", it will be treated as 0x0061
 * rather than as 0x000a</p>
 *
 * <p>Note - there is a significant difference between a unicode codepoint (32 bit int) and a UTF16
 * codeunit (=char) - a codepoint consists of one or two codeunits.</p>
 * <p>To make sure an int represents a codepoint and not a codeunit, use for example <code>import
 * java.text.Normalizer</code> With NFC mode.</p>
 *
 * [1] http://java.sun.com/j2se/1.5.0/docs/api/java/util/Properties.html
 * [2] http://java.sun.com/dtd/properties.dtd
 *
 * This class has been migrated from Pipeline 1.
 *
 * @author Markus Gylling
 * @author carl walinder
 *
 */

class UCharReplacer  {

	private ArrayList<Map<Integer,String>> mSubstitutionTables; // all loaded translationtables

	private final static Logger logger = LoggerFactory.getLogger(UCharReplacer.class);

	/**
	 * Default constructor.
	 */
	public UCharReplacer() {
		mSubstitutionTables = new ArrayList<Map<Integer,String>>();
	}

	/**
	 * Add a table for use as source for character substitution strings.
	 *
	 * <p>This method can be called multiple times prior to a getReplacementString()
	 * call to add several tables. The  table preference order is determined by add
	 * order (first added, highest preference).</p>
	 *
	 * @param table URL of the table to add
	 * @throws IOException if the table was not successfully loaded.
	 */
	public void addSubstitutionTable(URL table) throws IOException {
		try {
			mSubstitutionTables.add(loadTable(table));
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Replace codepoints in a character sequence with substitute characters.
	 *
	 * @param input the character sequence to perform codepoint-to-replacementchars substitution in.
	 * @return a character sequence whose codepoints may or may not have been replaced by
	 *         substitution characters, depending on settings and substitution success.
	 */
	public CharSequence replace(CharSequence input) {
		// normalize to eliminate any ambiguities vis-a-vis the user tables
		String normalizedInput = Normalizer.normalize(input.toString(), Normalizer.Form.NFD);
		StringBuilder sb = new StringBuilder(normalizedInput.length());
		normalizedInput.codePoints().forEach(c -> {
				CharSequence substitution = getSubstitutionChars(c);
				if (substitution != null && substitution.length() > 0) {
					sb.append(substitution);
				} else {
					sb.appendCodePoint(c);
				}
			}
		);
		return sb;
	}

	/**
	 * Attempt to create a substitution char sequence for a given codepoint.
	 *
	 * <p>This is the private performer method that does the job for the public accessors.</p>
	 *
	 * @return a replacement sequence of characters if a replacement string was found in loaded
	 *         tables or null if a replacement text was not found in tables
	 */
	private CharSequence getSubstitutionChars(int codePoint) {
		String substitute = retrieveSubstituteFromUserTables(codePoint);
		if(substitute!=null) {
			return substitute;
		} else {
			return null;
		}
	}

	/**
	 * @return a substitute string if available in tables, or null if not available
	 */
	private String retrieveSubstituteFromUserTables(int codePoint) {
		for (Iterator<Map<Integer,String>> it = mSubstitutionTables.iterator(); it.hasNext(); ) {
			Map<Integer,String> table = it.next();
			if (table.containsKey(codePoint))
				return table.get(codePoint);
		}
		return null;
	}

	/**
	 * Loads a table using the Properties class.
	 */
	private HashMap<Integer,String> loadTable(URL tableURL) throws IOException {
		HashMap<Integer,String> map = new HashMap<Integer,String>();
		Properties props = new Properties();
		props.loadFromXML(tableURL.openStream());
		Set<?> keys = props.keySet();
		for (Iterator<?> it = keys.iterator(); it.hasNext(); ) {
			String key = (String)it.next();
			if (key.codePointCount(0, key.length()) == 1) {
				map.put(key.codePointAt(0), props.getProperty(key));
			} else {
				try {
					map.put(Integer.decode("0x" + key), props.getProperty(key));
				} catch (NumberFormatException e) {
					logger.error("error in translation table "
					             + tableURL.toString() + ": attribute key=\"" + key + "\" is not a hex number.");
				}
			}
		}
		return map;
	}
}
