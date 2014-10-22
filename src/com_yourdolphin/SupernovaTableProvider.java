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
package com_yourdolphin;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;
import org.daisy.braille.table.EmbosserTable;
import org.daisy.braille.table.Table;
import org.daisy.braille.table.TableProvider;
import org.daisy.factory.FactoryProperties;

import aQute.bnd.annotation.component.Component;

@Component
public class SupernovaTableProvider implements TableProvider {
	enum TableType implements FactoryProperties {
		SV_SE_6DOT("Swedish - Supernova 6 dot", "Table for Supernova, using 6 dot");
		private final String name;
		private final String desc;
		private final String identifier;
		TableType(String name, String desc) {
			this.name = name;
			this.desc = desc;
			this.identifier = this.getClass().getCanonicalName() + "." + this.toString();
		}
		@Override
		public String getIdentifier() {
			return identifier;
		}
		@Override
		public String getDisplayName() {
			return name;
		}
		@Override
		public String getDescription() {
			return desc;
		}
	};

	private final Map<String, FactoryProperties> tables;
	
	public SupernovaTableProvider() {
		tables = new HashMap<String, FactoryProperties>(); 
		addTable(TableType.SV_SE_6DOT);
	}

	private void addTable(FactoryProperties t) {
		tables.put(t.getIdentifier(), t);
	}

	/**
	 * Get a new table instance based on the factory's current settings.
	 * 
	 * @param t
	 *            the type of table to return, this will override the factory's
	 *            default table type.
	 * @return returns a new table instance.
	 */
	public BrailleConverter newTable(TableType t) {
		return newFactory(t.getIdentifier()).newBrailleConverter();
	}

	public Table newFactory(String identifier) {
		FactoryProperties fp = tables.get(identifier);
		switch ((TableType)fp) {
		case SV_SE_6DOT:
			return new EmbosserTable(TableType.SV_SE_6DOT, EightDotFallbackMethod.values()[0], '\u2800'){

				/**
				 * 
				 */
				private static final long serialVersionUID = 1946091643211394782L;

				@Override
				public BrailleConverter newBrailleConverter() {
					return new EmbosserBrailleConverter(
							new String(" a,b.k;l@cif/msp'e:h*o!rødjgäntq_å?ê-u(v`îöë§xèç\"û+ü)z=àœôwï#yùé"), 
							Charset.forName("UTF-8"), fallback, replacement, true);
				}};
		default:
			return null;
		}
	}

	//jvm1.6@Override
	public Collection<FactoryProperties> list() {
		return Collections.unmodifiableCollection(tables.values());
	}

}
