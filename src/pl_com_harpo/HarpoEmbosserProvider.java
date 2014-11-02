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
package pl_com_harpo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserProvider;
import org.daisy.factory.FactoryProperties;

import aQute.bnd.annotation.component.Component;

@Component
public class HarpoEmbosserProvider implements EmbosserProvider {

    public static enum EmbosserType implements FactoryProperties {
        MOUNTBATTEN_LS("Mountbatten LS", ""),
        MOUNTBATTEN_PRO("Mountbatten Pro", ""),
        MOUNTBATTEN_WRITER_PLUS("Mountbatten Writer+", "");
		private final String name;
		private final String desc;
		private final String identifier;
    	EmbosserType (String name, String desc) {
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

    private final Map<String, FactoryProperties> embossers;

    public HarpoEmbosserProvider() {
        embossers = new HashMap<String, FactoryProperties>();
        addEmbosser(EmbosserType.MOUNTBATTEN_LS);
        addEmbosser(EmbosserType.MOUNTBATTEN_PRO);
        addEmbosser(EmbosserType.MOUNTBATTEN_WRITER_PLUS);
    }

	private void addEmbosser(FactoryProperties e) {
		embossers.put(e.getIdentifier(), e);
	}

	public Embosser newFactory(String identifier) {
		FactoryProperties fp = embossers.get(identifier);
		switch ((EmbosserType)fp) {
		case MOUNTBATTEN_LS:
			return new MountbattenEmbosser(EmbosserType.MOUNTBATTEN_LS);
		case MOUNTBATTEN_PRO:
			return new MountbattenEmbosser(EmbosserType.MOUNTBATTEN_PRO);
		case MOUNTBATTEN_WRITER_PLUS:
			return new MountbattenEmbosser(EmbosserType.MOUNTBATTEN_WRITER_PLUS);
		default:
			return null;
		}
	}

    public Collection<FactoryProperties> list() {
        return Collections.unmodifiableCollection(embossers.values());
    }
}
