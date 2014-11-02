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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserProvider;
import org.daisy.factory.FactoryProperties;

import aQute.bnd.annotation.component.Component;

@Component
public class ViewPlusEmbosserProvider implements EmbosserProvider {
    
    public static enum EmbosserType implements FactoryProperties {
            PREMIER_80("ViewPlus - Premier 80","80 CPS"),
            PREMIER_100("ViewPlus - Premier 100", "100 CPS"),
            ELITE_150("ViewPlus - Elite 150", "150 CPS"),
            ELITE_200("ViewPlus - Elite 200", "200 CPS"),
            PRO_GEN_II("ViewPlus - Pro Gen II", "100 CPS"),
            CUB_JR("ViewPlus - Cub Jr.", "30 CPS"),
            CUB("ViewPlus - Cub", "50 CPS"),
            MAX("ViewPlus - Max", "60 CPS"),
            EMFUSE("ViewPlus - EmFuse", "Large-print, Color, and Braille. 400 CPS"),
            EMPRINT_SPOTDOT("ViewPlus - Emprint SpotDot", "Ink and Braille. 40-50 CPS");
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

    public ViewPlusEmbosserProvider() {
        embossers = new HashMap<String, FactoryProperties>();

        // Elite Braille Printers
        addEmbosser(EmbosserType.ELITE_150);
        addEmbosser(EmbosserType.ELITE_200);

        // Premier Braille Printers
        addEmbosser(EmbosserType.PREMIER_80);
        addEmbosser(EmbosserType.PREMIER_100);

        // Pro Braille Printer
        addEmbosser(EmbosserType.PRO_GEN_II);

        // Desktop Braille Printers
        addEmbosser(EmbosserType.CUB_JR);
        addEmbosser(EmbosserType.CUB);
        addEmbosser(EmbosserType.MAX);

        // EmFuse Color Braille Station
        addEmbosser(EmbosserType.EMFUSE);
        // Emprint Spotdot Ink & Braille Embossers
        addEmbosser(EmbosserType.EMPRINT_SPOTDOT);
    }

	private void addEmbosser(FactoryProperties e) {
		embossers.put(e.getIdentifier(), e);
	}

	public Embosser newFactory(String identifier) {
		FactoryProperties fp = embossers.get(identifier);
		switch ((EmbosserType)fp) {
		case ELITE_150:
			return new TigerEmbosser(EmbosserType.ELITE_150);
		case ELITE_200:
			return new TigerEmbosser(EmbosserType.ELITE_200);
		case PREMIER_80:
			return new TigerEmbosser(EmbosserType.PREMIER_80);
		case PREMIER_100:
			return new TigerEmbosser(EmbosserType.PREMIER_100);
		case PRO_GEN_II:
			return new TigerEmbosser(EmbosserType.PRO_GEN_II);
		case CUB_JR:
			return new TigerEmbosser(EmbosserType.CUB_JR);
		case CUB:
			return new TigerEmbosser(EmbosserType.CUB);
		case MAX:
			return new TigerEmbosser(EmbosserType.MAX);
		case EMFUSE:
			return new TigerEmbosser(EmbosserType.EMFUSE);
		case EMPRINT_SPOTDOT:
			return new TigerEmbosser(EmbosserType.EMPRINT_SPOTDOT);
		default:
			return null;
		}
	}

    public Collection<FactoryProperties> list() {
        return Collections.unmodifiableCollection(embossers.values());
    }
}
