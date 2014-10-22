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
package es_once_cidat;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.table.AdvancedBrailleConverter;
import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;
import org.daisy.braille.table.EmbosserTable;
import org.daisy.braille.table.Table;
import org.daisy.braille.table.TableProvider;
import org.daisy.braille.tools.StringTranslator.MatchMode;
import org.daisy.factory.FactoryProperties;

import aQute.bnd.annotation.component.Component;

/**
 *
 * @author Bert Frees
 * @author Joel Håkansson
 */
@Component
public class CidatTableProvider implements TableProvider {
    
    enum TableType implements FactoryProperties {
    		IMPACTO_TRANSPARENT_6DOT("Transparent mode", "Impacto 6-dot in transparent mode"),
    		IMPACTO_TRANSPARENT_8DOT("Transparent mode (8 dot)", "Impacto 8-dot in transparent mode"),
    		PORTATHIEL_TRANSPARENT_6DOT("Transparent mode", "Portathiel 6-dot in transparent mode"),
    		//PORTATHIEL_TRANSPARENT_8DOT
                     ;
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

    public CidatTableProvider() {
        tables = new HashMap<String, FactoryProperties>(); 
        addTable(TableType.IMPACTO_TRANSPARENT_6DOT);
        addTable(TableType.IMPACTO_TRANSPARENT_8DOT);
        addTable(TableType.PORTATHIEL_TRANSPARENT_6DOT);
      //tables.add(new EmbosserTable("Transparent mode (8 dot)", "Portathiel 8-dot in transparent mode", TableType.PORTATHIEL_TRANSPARENT_8DOT, this));
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
		case IMPACTO_TRANSPARENT_6DOT:
			return new EmbosserTable(TableType.IMPACTO_TRANSPARENT_6DOT, EightDotFallbackMethod.values()[0], '\u2800'){

				/**
				 * 
				 */
				private static final long serialVersionUID = 5207685560736203905L;

				@Override
				public BrailleConverter newBrailleConverter() {
	                ArrayList<String> al = new ArrayList<String>();
	                for (int i=0; i<0x1b; i++) {
	                    al.add(String.valueOf((char)i));
	                }
	                al.add(String.copyValueOf(new char[]{(char)0x1b,(char)0x1b}));
	                for (int i=0x1c; i<64; i++) {
	                    al.add(String.valueOf((char)i));
	                }
	                return new AdvancedBrailleConverter(
	                    al.toArray(new String[al.size()]),
	                    Charset.forName("ISO-8859-1"),
	                    fallback,
	                    replacement,
	                    false,
	                    MatchMode.RELUCTANT);
				}};
		case IMPACTO_TRANSPARENT_8DOT:
			return new EmbosserTable(TableType.IMPACTO_TRANSPARENT_8DOT, EightDotFallbackMethod.values()[0], '\u2800'){

				/**
				 * 
				 */
				private static final long serialVersionUID = -4503322212435004238L;

				@Override
				public BrailleConverter newBrailleConverter() {
	                ArrayList<String> al = new ArrayList<String>();
	                for (int i=0; i<0x1b; i++) {
	                    al.add(String.valueOf((char)i));
	                }
	                al.add(String.copyValueOf(new char[]{(char)0x1b,(char)0x1b}));
	                for (int i=0x1c; i<256; i++) {
	                    al.add(String.valueOf((char)i));
	                }
	                return new AdvancedBrailleConverter(
	                    al.toArray(new String[al.size()]),
	                    Charset.forName("ISO-8859-1"),
	                    false,
	                    MatchMode.RELUCTANT);
				}};
		case PORTATHIEL_TRANSPARENT_6DOT:
			return new EmbosserTable(TableType.PORTATHIEL_TRANSPARENT_6DOT, EightDotFallbackMethod.values()[0], '\u2800'){

				/**
				 * 
				 */
				private static final long serialVersionUID = -6387852281349846246L;

				@Override
				public BrailleConverter newBrailleConverter() {
	                StringBuilder sb = new StringBuilder();
	                for (int i=0; i<0x1b; i++) {
	                    sb.append((char)i);
	                }
	                sb.append((char)0xf7);
	                for (int i=0x1c; i<64; i++) {
	                    sb.append((char)i);
	                }
	                return new EmbosserBrailleConverter(
	                        sb.toString(),
	                        Charset.forName("ISO-8859-1"),
	                        fallback,
	                        replacement,
	                        false);
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
