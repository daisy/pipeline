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
package com_viewplus;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.table.BrailleConverter;
import org.daisy.braille.api.table.Table;
import org.daisy.braille.api.table.TableProvider;
import org.daisy.braille.impl.table.AdvancedBrailleConverter;
import org.daisy.braille.impl.table.EmbosserBrailleConverter.EightDotFallbackMethod;
import org.daisy.braille.impl.table.EmbosserTable;
import org.daisy.braille.impl.table.StringTranslator.MatchMode;

import aQute.bnd.annotation.component.Component;

/**
 *
 * @author Bert Frees
 */
@Component
public class ViewPlusTableProvider implements TableProvider {

    enum TableType implements FactoryProperties {
    	TIGER_INLINE_SUBSTITUTION_8DOT("Tiger inline substitution 8-dot", "");
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

    public ViewPlusTableProvider() {
        tables = new HashMap<String, FactoryProperties>(); 
        addTable(TableType.TIGER_INLINE_SUBSTITUTION_8DOT);
    }

	private void addTable(FactoryProperties t) {
		tables.put(t.getIdentifier(), t);
	}

    public BrailleConverter newTable(TableType t) {
    	return newFactory(t.getIdentifier()).newBrailleConverter();
    }

	public Table newFactory(String identifier) {
		FactoryProperties fp = tables.get(identifier);
		switch ((TableType)fp) {
		case TIGER_INLINE_SUBSTITUTION_8DOT:
			return new EmbosserTable(TableType.TIGER_INLINE_SUBSTITUTION_8DOT, EightDotFallbackMethod.values()[0], '\u2800'){

				/**
				 * 
				 */
				private static final long serialVersionUID = -3747633563102712142L;

				@Override
				public BrailleConverter newBrailleConverter() {
	                final String SUB = String.valueOf((char)0x1a);
	                ArrayList<String> a = new ArrayList<String>();
	                for (int i=0; i<256; i++) {
	                    a.add(SUB + (char)i);
	                }
	                return new AdvancedBrailleConverter(
	                    a.toArray(new String[a.size()]),
	                    Charset.forName("ISO-8859-1"),
	                    false,
	                    MatchMode.RELUCTANT);
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
