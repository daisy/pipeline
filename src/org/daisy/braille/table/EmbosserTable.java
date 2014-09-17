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

import java.util.HashMap;

/**
 * Provides an embosser table implementation. This implementation
 * assumes that each character matches a single braille pattern,
 * and vice versa. 
 * @author Joel Håkansson
 *
 * @param <T>
 */
public class EmbosserTable<T> extends AbstractTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3902130832797155793L;
	private final HashMap<String, Object> props;
	private final T type;
	private final ConfigurableTableProvider<T> provider;
	
	/**
	 * Creates a new EmbosserTable with the supplied settings
	 * @param name the name for the table
	 * @param desc the description for the table
	 * @param type the type of table
	 * @param provider the provider
	 */
	public EmbosserTable(String name, String desc, T type, ConfigurableTableProvider<T> provider) {
		super(name, desc, type.getClass().getCanonicalName() + "." + type.toString());
		this.type = type;
		this.provider = provider;
		props = new HashMap<String, Object>();
		props.put(TableProperties.IS_ONE_TO_ONE, true);
		props.put(TableProperties.IS_DISPLAY_FORMAT, true);
	}
	
	EmbosserTable<T> putProperty(String key, Object value) {
		props.put(key, value);
		return this;
	}

	//jvm1.6@Override
	public BrailleConverter newBrailleConverter() {
		return provider.newTable(type);
	}

	//jvm1.6@Override
	public void setFeature(String key, Object value) {
		provider.setFeature(key, value);
	}

	//jvm1.6@Override
	public Object getProperty(String key) {
		return props.get(key);
	}

	//jvm1.6@Override
	public Object getFeature(String key) {
		return provider.getFeature(key);
	}

}
