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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.embosser.AbstractEmbosser;
import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserProvider;

public class ViewPlusEmbosserProvider implements EmbosserProvider {
    
    public static enum EmbosserType {
                                        PREMIER_80,
                                        PREMIER_100,
                                        ELITE_150,
                                        ELITE_200,
                                        PRO_GEN_II,
                                        CUB_JR,
                                        CUB,
                                        MAX,
                                        EMFUSE,
                                        EMPRINT_SPOTDOT
    };

    private final Map<String, Embosser> embossers;

    public ViewPlusEmbosserProvider() {
        embossers = new HashMap<String, Embosser>();

        // Elite Braille Printers
        addEmbosser(new TigerEmbosser("ViewPlus - Elite 150",       "150 CPS",                  EmbosserType.ELITE_150));
        addEmbosser(new TigerEmbosser("ViewPlus - Elite 200",       "200 CPS",                  EmbosserType.ELITE_200));

        // Premier Braille Printers
        addEmbosser(new TigerEmbosser("ViewPlus - Premier 80",      "80 CPS",                   EmbosserType.PREMIER_80));
        addEmbosser(new TigerEmbosser("ViewPlus - Premier 100",     "100 CPS",                  EmbosserType.PREMIER_100));

        // Pro Braille Printer
        addEmbosser(new TigerEmbosser("ViewPlus - Pro Gen II",      "100 CPS",                  EmbosserType.PRO_GEN_II));

        // Desktop Braille Printers
        addEmbosser(new TigerEmbosser("ViewPlus - Cub Jr.",         "30 CPS",                   EmbosserType.CUB_JR));
        addEmbosser(new TigerEmbosser("ViewPlus - Cub",             "50 CPS",                   EmbosserType.CUB));
        addEmbosser(new TigerEmbosser("ViewPlus - Max",             "60 CPS",                   EmbosserType.MAX));

        // EmFuse Color Braille Station
        addEmbosser(new TigerEmbosser("ViewPlus - EmFuse",          "Large-print, Color, and Braille. " +
                                                                                                    "400 CPS",                  EmbosserType.EMFUSE));
        // Emprint Spotdot Ink & Braille Embossers
        addEmbosser(new TigerEmbosser("ViewPlus - Emprint SpotDot", "Ink and Braille. " +
                                                                                                    "40-50 CPS",                EmbosserType.EMPRINT_SPOTDOT));
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
