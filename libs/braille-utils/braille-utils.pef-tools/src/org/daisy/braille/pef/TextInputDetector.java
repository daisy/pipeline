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
package org.daisy.braille.pef;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.table.BrailleConstants;
import org.daisy.braille.api.table.BrailleConverter;
import org.daisy.braille.api.table.Table;
import org.daisy.braille.api.table.TableCatalogService;

/**
 * Provides a method for detecting a table based on text input.
 * @author Joel Håkansson
 */
public class TextInputDetector {
	private final TableCatalogService factory;
	
	/**
	 * Creates a new TextInputDetector
	 */
	public TextInputDetector(TableCatalogService factory) {
		this.factory = factory;
	}

	private BitSet analyze(InputStream is) throws IOException {
		BitSet set = new BitSet(256);
		int val;
		while ((val=is.read())>-1) {
			set.set(val);
		}
		is.close();
		return set;
	}
	
	private BitSet getTableThumbprint(BrailleConverter c) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(c.toText(BrailleConstants.BRAILLE_PATTERNS_256).getBytes(c.getPreferredCharset().name()));
		return analyze(bis);
	}
	
	/**
	 * 
	 * @param input
	 * @param clearCodes set to true to clear the LF, CR, FF, and SUB fields.
	 * @return returns a thumb print for the input stream
	 * @throws InputDetectionException
	 */
	private BitSet readInput(InputStream input, boolean clearCodes) throws IOException {
		BitSet inputThumbprint;
		inputThumbprint = analyze(input);

		if (clearCodes) {
			inputThumbprint.clear(0x0a); //LF
			inputThumbprint.clear(0x0c); //FF
			inputThumbprint.clear(0x0d); //CR
			inputThumbprint.clear(0x1a); //SUB
		}
		return inputThumbprint;
	}
	
	private HashMap<BitSet, HashMap<String, Table>> analyzeTableCatalog(boolean eightDot) {
		Logger logger = Logger.getLogger(TextHandler.class.getCanonicalName());
		HashMap<BitSet, HashMap<String, Table>> tables = new HashMap<BitSet, HashMap<String, Table>>();
		BitSet tableThumbprint;
		for (FactoryProperties fp : factory.list()) {
			try {
				Table type = factory.newTable(fp.getIdentifier());
				BrailleConverter c = type.newBrailleConverter();
				if (!eightDot && c.supportsEightDot()) {
					continue;
				}
				tableThumbprint = getTableThumbprint(c);
				HashMap<String, Table> t = tables.get(tableThumbprint);
				String sample = c.toText(BrailleConstants.BRAILLE_PATTERNS_256);
				if (t==null) {
					t = new HashMap<String, Table>();
					t.put(sample, type);
					tables.put(tableThumbprint, t);
				} else {
					Table x = t.get(sample);
					if (x!=null) {
						logger.fine("Ignoring " + type.getDisplayName() + " since it is identical to " + x.getDisplayName());
					} else {
						logger.fine(type.getDisplayName() + " has the same bit set as another table.");
						t.put(sample, type);
					}
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Could not read table thumbprint", e);
			}
		}
		return tables;
	}
	
	/**
	 * Detects tables matching the supplied text input. Only tables that create
	 * non-identical output are included. Therefore, more than one match
	 * indicates that the output would look different depending on the
	 * table chosen.
	 * @param is text input stream
	 * @return returns a list of matching tables
	 * @throws IOException if input cannot be read
	 */
	public List<Table> detect(InputStream is) throws IOException {
		BitSet inputThumbprint = readInput(is, true);
		ArrayList<Table> res = new ArrayList<Table>();
		//BitSet tableThumbprint;
		int size = 0;
		for (int i = inputThumbprint.nextSetBit(0); i >= 0; i = inputThumbprint.nextSetBit(i+1)) {
		    size++;
		}
		Logger logger = Logger.getLogger(TextHandler.class.getCanonicalName());
		HashMap<BitSet, HashMap<String, Table>> tables = analyzeTableCatalog(size>(64-4)); // 6-dot minus cleared codes
		for (BitSet key : tables.keySet()) {
			BitSet tableThumbprint = (BitSet)key.clone();
			tableThumbprint.and(inputThumbprint);
			if (tableThumbprint.equals(inputThumbprint)) {
				HashMap<String, Table> yy = tables.get(key);
				if (yy!=null) {
					Collection<Table> coll = yy.values();
					StringBuilder sb = new StringBuilder();
					for (Table t : coll) {
						sb.append(" '" + t.getDisplayName() + "'");
					}
					if (coll.size()>1) {
						// there are more than one table with the same bit set producing different text results
						logger.warning("These (matching) tables uses the same bytes for different braille characters:" + sb.toString());
					}
					logger.fine("Input matches table(s):" + sb.toString());
					res.addAll(coll);
				}
			}
		}
		return res;
	}
}
