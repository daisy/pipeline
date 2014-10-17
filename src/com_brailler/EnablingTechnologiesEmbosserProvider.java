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
import java.util.Map;

import org.daisy.braille.embosser.AbstractEmbosser;
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

    private final Map<String, Embosser> embossers;

    public EnablingTechnologiesEmbosserProvider() {
        embossers = new HashMap<String, Embosser>();

        // Single sided
        addEmbosser(new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Romeo Attache",       "", EmbosserType.ROMEO_ATTACHE));
        addEmbosser(new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Romeo Attache Pro",   "", EmbosserType.ROMEO_ATTACHE_PRO));
        addEmbosser(new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Romeo 25",            "", EmbosserType.ROMEO_25));
        addEmbosser(new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Romeo Pro 50",        "", EmbosserType.ROMEO_PRO_50));
        addEmbosser(new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Romeo Pro LE Narrow", "", EmbosserType.ROMEO_PRO_LE_NARROW));
        addEmbosser(new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Romeo Pro LE Wide",   "", EmbosserType.ROMEO_PRO_LE_WIDE));
        addEmbosser(new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Thomas",              "", EmbosserType.THOMAS));
        addEmbosser(new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Thomas Pro",          "", EmbosserType.THOMAS_PRO));
        addEmbosser(new EnablingTechnologiesSingleSidedEmbosser("Enabling Technologies - Marathon",            "", EmbosserType.MARATHON));

        // Double sided
        addEmbosser(new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - ET",                  "", EmbosserType.ET));
        addEmbosser(new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - Juliet Pro",          "", EmbosserType.JULIET_PRO));
        addEmbosser(new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - Juliet Pro 60",       "", EmbosserType.JULIET_PRO_60));
        addEmbosser(new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - Juliet Classic",      "", EmbosserType.JULIET_CLASSIC));

        // Production double sided
        addEmbosser(new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - Bookmaker",           "", EmbosserType.BOOKMAKER));
        addEmbosser(new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - Braille Express 100", "", EmbosserType.BRAILLE_EXPRESS_100));
        addEmbosser(new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - Braille Express 150", "", EmbosserType.BRAILLE_EXPRESS_150));
        addEmbosser(new EnablingTechnologiesDoubleSidedEmbosser("Enabling Technologies - BraillePlace",        "", EmbosserType.BRAILLE_PLACE));
    }

	private void addEmbosser(AbstractEmbosser e) {
		embossers.put(e.getIdentifier(), e);
	}
    
	public Embosser newFactory(String identifier) {
		return embossers.get(identifier);
	}

    //jvm1.6@Override
    public Collection<Embosser> list() {
        return embossers.values();
    }
}
