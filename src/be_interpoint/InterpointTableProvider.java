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
package be_interpoint;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.table.BrailleConverter;
import org.daisy.braille.api.table.EmbosserBrailleConverter;
import org.daisy.braille.api.table.EmbosserBrailleConverter.EightDotFallbackMethod;
import org.daisy.braille.api.table.EmbosserTable;
import org.daisy.braille.api.table.Table;
import org.daisy.braille.api.table.TableProvider;

import aQute.bnd.annotation.component.Component;

/**
 *
 * @author Bert Frees
 * @author Joel Håkansson
 */
@Component
public class InterpointTableProvider implements TableProvider {
    
    enum TableType implements FactoryProperties { 
    	USA1_8("USA1_8 font", "Interpoint 8-dot table");
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

    public InterpointTableProvider() {
        tables = new HashMap<String, FactoryProperties>();
        //The implementation is intentionally excluded from listing
        //addTable(TableType.USA1_8);
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
		case USA1_8:
			return new EmbosserTable(TableType.USA1_8, EightDotFallbackMethod.values()[0], '\u2800'){

				/**
				 * 
				 */
				private static final long serialVersionUID = 5042547624591164148L;

				@Override
				public BrailleConverter newBrailleConverter() {
					String table = " a,b.k;l\"cif|msp!e:h*o+r>djg`ntq'1?2-u(v$3960x~&<5/8)z={\u007f4w7#y}%"; // TODO: add patterns U+2840 - U+28ff

	                StringBuffer sb = new StringBuffer();
	                sb.append(table);
	                return new EmbosserBrailleConverter(sb.toString(), Charset.forName("ISO-8859-1"), fallback, replacement, true);
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
