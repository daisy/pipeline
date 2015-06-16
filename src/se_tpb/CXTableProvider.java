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
package se_tpb;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.api.embosser.EightDotFallbackMethod;
import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.table.BrailleConverter;
import org.daisy.braille.api.table.Table;
import org.daisy.braille.api.table.TableProvider;
import org.daisy.braille.impl.table.EmbosserBrailleConverter;
import org.daisy.braille.impl.table.EmbosserTable;

import aQute.bnd.annotation.component.Component;

@Component
public class CXTableProvider implements TableProvider {
//	public final static String IS_ONE_TO_ONE = "is one-to-one";
//	public final static String IS_DISPLAY_FORMAT = "is display format";
	
	enum TableType implements FactoryProperties {
		SV_SE_CX("Swedish CX", "Matches the Swedish representation in CX");
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

	public CXTableProvider() {
		tables = new HashMap<String, FactoryProperties>(); 
		addTable(TableType.SV_SE_CX);
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
		case SV_SE_CX:
			return new EmbosserTable(TableType.SV_SE_CX, EightDotFallbackMethod.values()[0], '\u2800'){

				/**
				 * 
				 */
				private static final long serialVersionUID = 1834860594148104502L;

				@Override
				public BrailleConverter newBrailleConverter() {
					return new EmbosserBrailleConverter(
							new String(
									" a,b.k;l^cif/msp'e:h*o!r~djgäntq_å?ê-u(v@îöë§xèç\"û+ü)z=à|ôwï#yùé"),
							Charset.forName("UTF-8"), fallback, replacement, true);
				}};
		default:
			return null;
		}
	}

	@Override
	public Collection<FactoryProperties> list() {
		return Collections.unmodifiableCollection(tables.values());
	}

}
