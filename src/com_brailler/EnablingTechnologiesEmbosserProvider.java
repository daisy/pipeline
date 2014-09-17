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
package com_brailler;

import java.util.Collection;
import java.util.HashMap;

import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserProvider;

/**
 *
 * @author Bert Frees
 */
public class EnablingTechnologiesEmbosserProvider implements EmbosserProvider {

    public static enum EmbosserType {
        ROMEO_ATTACHE,
        ROMEO_ATTACHE_PRO,
        ROMEO_25,
        ROMEO_PRO_50,
        ROMEO_PRO_LE_NARROW,
        ROMEO_PRO_LE_WIDE,
        THOMAS,
        THOMAS_PRO,
        MARATHON,
        ET,
        JULIET_PRO,
        JULIET_PRO_60,
        JULIET_CLASSIC,
        BOOKMAKER,
        BRAILLE_EXPRESS_100,
        BRAILLE_EXPRESS_150,
        BRAILLE_PLACE
    };

    private final HashMap<EmbosserType, Embosser> embossers;

    public EnablingTechnologiesEmbosserProvider() {
        embossers = new HashMap<EmbosserType, Embosser>();

        // Single sided
        embossers.put(EmbosserType.ROMEO_ATTACHE,       new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Romeo Attache",       "", EmbosserType.ROMEO_ATTACHE));
        embossers.put(EmbosserType.ROMEO_ATTACHE_PRO,   new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Romeo Attache Pro",   "", EmbosserType.ROMEO_ATTACHE_PRO));
        embossers.put(EmbosserType.ROMEO_25,            new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Romeo 25",            "", EmbosserType.ROMEO_25));
        embossers.put(EmbosserType.ROMEO_PRO_50,        new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Romeo Pro 50",        "", EmbosserType.ROMEO_PRO_50));
        embossers.put(EmbosserType.ROMEO_PRO_LE_NARROW, new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Romeo Pro LE Narrow", "", EmbosserType.ROMEO_PRO_LE_NARROW));
        embossers.put(EmbosserType.ROMEO_PRO_LE_WIDE,   new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Romeo Pro LE Wide",   "", EmbosserType.ROMEO_PRO_LE_WIDE));
        embossers.put(EmbosserType.THOMAS,              new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Thomas",              "", EmbosserType.THOMAS));
        embossers.put(EmbosserType.THOMAS_PRO,          new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Thomas Pro",          "", EmbosserType.THOMAS_PRO));
        embossers.put(EmbosserType.MARATHON,            new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Marathon",            "", EmbosserType.MARATHON));

        // Double sided
        embossers.put(EmbosserType.ET,                  new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - ET",                  "", EmbosserType.ET));
        embossers.put(EmbosserType.JULIET_PRO,          new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - Juliet Pro",          "", EmbosserType.JULIET_PRO));
        embossers.put(EmbosserType.JULIET_PRO_60,       new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - Juliet Pro 60",       "", EmbosserType.JULIET_PRO_60));
        embossers.put(EmbosserType.JULIET_CLASSIC,      new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - Juliet Classic",      "", EmbosserType.JULIET_CLASSIC));

        // Production double sided
        embossers.put(EmbosserType.BOOKMAKER,           new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - Bookmaker",           "", EmbosserType.BOOKMAKER));
        embossers.put(EmbosserType.BRAILLE_EXPRESS_100, new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - Braille Express 100", "", EmbosserType.BRAILLE_EXPRESS_100));
        embossers.put(EmbosserType.BRAILLE_EXPRESS_150, new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - Braille Express 150", "", EmbosserType.BRAILLE_EXPRESS_150));
        embossers.put(EmbosserType.BRAILLE_PLACE,       new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - BraillePlace",        "", EmbosserType.BRAILLE_PLACE));
    }

    //jvm1.6@Override
    public Collection<Embosser> list() {
        return embossers.values();
    }
}
