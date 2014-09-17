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

    private final HashMap<EmbosserType, Embosser> embossers;

    public ViewPlusEmbosserProvider() {
        embossers = new HashMap<EmbosserType, Embosser>();

        // Elite Braille Printers
        embossers.put(EmbosserType.ELITE_150,       new TigerEmbosser("ViewPlus - Elite 150",       "150 CPS",                  EmbosserType.ELITE_150));
        embossers.put(EmbosserType.ELITE_200,       new TigerEmbosser("ViewPlus - Elite 200",       "200 CPS",                  EmbosserType.ELITE_200));

        // Premier Braille Printers
        embossers.put(EmbosserType.PREMIER_80,      new TigerEmbosser("ViewPlus - Premier 80",      "80 CPS",                   EmbosserType.PREMIER_80));
        embossers.put(EmbosserType.PREMIER_100,     new TigerEmbosser("ViewPlus - Premier 100",     "100 CPS",                  EmbosserType.PREMIER_100));

        // Pro Braille Printer
        embossers.put(EmbosserType.PRO_GEN_II,      new TigerEmbosser("ViewPlus - Pro Gen II",      "100 CPS",                  EmbosserType.PRO_GEN_II));

        // Desktop Braille Printers
        embossers.put(EmbosserType.CUB_JR,          new TigerEmbosser("ViewPlus - Cub Jr.",         "30 CPS",                   EmbosserType.CUB_JR));
        embossers.put(EmbosserType.CUB,             new TigerEmbosser("ViewPlus - Cub",             "50 CPS",                   EmbosserType.CUB));
        embossers.put(EmbosserType.MAX,             new TigerEmbosser("ViewPlus - Max",             "60 CPS",                   EmbosserType.MAX));

        // EmFuse Color Braille Station
        embossers.put(EmbosserType.EMFUSE,          new TigerEmbosser("ViewPlus - EmFuse",          "Large-print, Color, and Braille. " +
                                                                                                    "400 CPS",                  EmbosserType.EMFUSE));
        // Emprint Spotdot Ink & Braille Embossers
        embossers.put(EmbosserType.EMPRINT_SPOTDOT, new TigerEmbosser("ViewPlus - Emprint SpotDot", "Ink and Braille. " +
                                                                                                    "40-50 CPS",                EmbosserType.EMPRINT_SPOTDOT));
    }

    public Collection<Embosser> list() {
        return embossers.values();
    }
}
