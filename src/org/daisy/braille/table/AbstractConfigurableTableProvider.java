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
package org.daisy.braille.table;

import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;

/**
 * Provides an abstract base for TableProviders, implementing basic features
 * such as the ability to set page fallback action and replacement character.
 * @author Joel Håkansson
 *
 * @param <T>
 */
public abstract class AbstractConfigurableTableProvider<T> implements ConfigurableTableProvider<T> {
	
	protected EightDotFallbackMethod fallback;
	protected char replacement;
	
	public AbstractConfigurableTableProvider(EightDotFallbackMethod fallback, char replacement) {
		this.fallback = fallback;
		this.replacement = replacement;
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
