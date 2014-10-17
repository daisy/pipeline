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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.embosser.AbstractEmbosser;
import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserProvider;

/**
 *
 * @author Bert Frees
 */
public class InterpointEmbosserProvider implements EmbosserProvider {

    public static enum EmbosserType { INTERPOINT_55 }

    private final Map<String, Embosser> embossers;
    
    public InterpointEmbosserProvider() {
        embossers = new HashMap<String, Embosser>();
        addEmbosser(
                new Interpoint55Embosser("Interpoint 55",
                                         "Robust, high-quality, high-speed (2000 pages per hour) double-sided embosser with paper supply from rolls"));
    }
    
	private void addEmbosser(AbstractEmbosser e) {
		embossers.put(e.getIdentifier(), e);
	}
	
	public Embosser newFactory(String identifier) {
		return embossers.get(identifier);
	}

    public Collection<Embosser> list() {
        return embossers.values();
    }
}
