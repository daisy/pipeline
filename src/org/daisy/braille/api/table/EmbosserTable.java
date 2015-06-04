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
package org.daisy.braille.api.table;

import java.util.HashMap;

import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.table.EmbosserBrailleConverter.EightDotFallbackMethod;

/**
 * Provides an embosser table implementation. This implementation
 * assumes that each character matches a single braille pattern,
 * and vice versa. 
 * @author Joel Håkansson
 *
 * @param <T>
 */
public abstract class EmbosserTable extends AbstractTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3902130832797155793L;
	private final HashMap<String, Object> props;
	
	protected EightDotFallbackMethod fallback;
	protected char replacement;
	
	/**
	 * Creates a new EmbosserTable with the supplied settings
	 * @param name the name for the table
	 * @param desc the description for the table
	 * @param type the type of table
	 * @param provider the provider
	 */
	public EmbosserTable(FactoryProperties fp, EightDotFallbackMethod fallback, char replacement) {
		super(fp.getDisplayName(), fp.getDescription(), fp.getIdentifier());
		props = new HashMap<String, Object>();
		props.put(TableProperties.IS_ONE_TO_ONE, true);
		props.put(TableProperties.IS_DISPLAY_FORMAT, true);
		
		this.fallback = fallback;
		this.replacement = replacement;
	}
	
	EmbosserTable putProperty(String key, Object value) {
		props.put(key, value);
		return this;
	}

	@Override
	public abstract BrailleConverter newBrailleConverter();
	
	@Override
	public Object getProperty(String key) {
		return props.get(key);
	}

	private void setFallback(String value) {
		if (value != null && !"".equals(value)) {
			setFallback(EightDotFallbackMethod.valueOf(value.toUpperCase()));
		}
	}

	private void setFallback(EightDotFallbackMethod value) {
		fallback = value;
	}

	/**
	 * hex value between 2800-283F
	 * 
	 * @param value
	 * @return
	 */
	private void setReplacement(String value) {
		if (value != null && !"".equals(value)) {
			setReplacement((char) Integer.parseInt(value, 16));
		}
	}

	private void setReplacement(char value) {
		int val = (value + "").codePointAt(0);
		if (val >= 0x2800 && val <= 0x283F) {
			replacement = value;
		} else {
			throw new IllegalArgumentException("Replacement value out of range");
		}
	}
	
	public void setFeature(String key, Object value) {
		if ("replacement".equals(key)) {
			if (value!=null) {
				setReplacement((String)value);
			}
		} else if ("fallback".equals(key)) {
			if (value!=null) {
				setFallback(value.toString());
			}
		} else {
			throw new IllegalArgumentException("Unknown feature: " + key);
		}
	}
	
	public Object getFeature(String key) {
		if ("replacement".equals(key)) {
			return replacement;
		} else if ("fallback".equals(key)) {
			return fallback;
		} else {
			throw new IllegalArgumentException("Unknown feature: " + key);
		}
	}

}
