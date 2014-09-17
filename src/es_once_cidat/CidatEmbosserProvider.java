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

import java.util.Collection;
import java.util.HashMap;

import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserProvider;

/**
 *
 * @author Bert Frees
 */
public class CidatEmbosserProvider implements EmbosserProvider {

    public static enum EmbosserType {
        IMPACTO_600,
        IMPACTO_TEXTO,
        PORTATHIEL_BLUE
    };

    private final HashMap<EmbosserType, Embosser> embossers;

    public CidatEmbosserProvider() {
        embossers = new HashMap<EmbosserType, Embosser>();
        embossers.put(EmbosserType.IMPACTO_600, new ImpactoEmbosser("Cidat - Impacto 600", "High-quality, high-speed (600 pages per hour) double-sided embosser", EmbosserType.IMPACTO_600));
        embossers.put(EmbosserType.IMPACTO_TEXTO, new ImpactoEmbosser("Cidat - Impacto Texto","High-quality, high-speed (800 pages per hour) double-sided embosser", EmbosserType.IMPACTO_TEXTO));
        embossers.put(EmbosserType.PORTATHIEL_BLUE, new PortathielBlueEmbosser("Cidat - Portathiel Blue", "Small, lightweight, portable double-sided embosser", EmbosserType.PORTATHIEL_BLUE));
    }

    //jvm1.6@Override
    public Collection<Embosser> list() {
        return embossers.values();
    }
}
