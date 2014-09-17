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
package com_indexbraille;

import java.util.Collection;
import java.util.HashMap;

import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserProvider;

public class IndexEmbosserProvider implements EmbosserProvider {
	public static enum EmbosserType {
                INDEX_3_7,
                INDEX_ADVANCED,
                INDEX_BASIC_BLUE_BAR,
                INDEX_CLASSIC,
                INDEX_DOMINO,
                INDEX_EVEREST_S_V1,
                INDEX_EVEREST_D_V1,
                INDEX_BASIC_S_V2,
                INDEX_BASIC_D_V2,
                INDEX_EVEREST_D_V2,
                INDEX_4X4_PRO_V2,
                INDEX_BASIC_S_V3,
                INDEX_BASIC_D_V3,
                INDEX_EVEREST_D_V3,
                INDEX_4X4_PRO_V3,
                INDEX_4WAVES_PRO_V3,
                INDEX_BASIC_D_V4,
                INDEX_EVEREST_D_V4,
                INDEX_BRAILLE_BOX_V4
	};

	private final HashMap<EmbosserType, Embosser> embossers;

	public IndexEmbosserProvider() {
                embossers = new HashMap<EmbosserType, Embosser>();
                embossers.put(EmbosserType.INDEX_BASIC_BLUE_BAR, new BlueBarEmbosser("Index Basic Blue-Bar", "Early Index Basic embosser"));
                embossers.put(EmbosserType.INDEX_BASIC_S_V2,     new IndexV2Embosser("Index Basic-S V2","", EmbosserType.INDEX_BASIC_S_V2));
                embossers.put(EmbosserType.INDEX_BASIC_D_V2,     new IndexV2Embosser("Index Basic-D V2","", EmbosserType.INDEX_BASIC_D_V2));
                embossers.put(EmbosserType.INDEX_EVEREST_D_V2,   new IndexV2Embosser("Index Everest-D V2","", EmbosserType.INDEX_EVEREST_D_V2));
                embossers.put(EmbosserType.INDEX_4X4_PRO_V2,     new IndexV2Embosser("Index 4X4 Pro V2","", EmbosserType.INDEX_4X4_PRO_V2));
                embossers.put(EmbosserType.INDEX_EVEREST_D_V3,   new IndexV3Embosser("Index Everest-D V3","", EmbosserType.INDEX_EVEREST_D_V3));
                embossers.put(EmbosserType.INDEX_BASIC_S_V3,     new IndexV3Embosser("Index Basic-S V3","", EmbosserType.INDEX_BASIC_S_V3));
                embossers.put(EmbosserType.INDEX_BASIC_D_V3,     new IndexV3Embosser("Index Basic-D V3","", EmbosserType.INDEX_BASIC_D_V3));
                embossers.put(EmbosserType.INDEX_4X4_PRO_V3,     new IndexV3Embosser("Index 4X4 Pro V3","", EmbosserType.INDEX_4X4_PRO_V3));
                embossers.put(EmbosserType.INDEX_4WAVES_PRO_V3,  new IndexV3Embosser("Index 4Waves Pro","", EmbosserType.INDEX_4WAVES_PRO_V3));
                embossers.put(EmbosserType.INDEX_BASIC_D_V4,     new IndexV4Embosser("Index Basic-D V4","", EmbosserType.INDEX_BASIC_D_V4));
                embossers.put(EmbosserType.INDEX_EVEREST_D_V4,   new IndexV4Embosser("Index Everest-D V4","", EmbosserType.INDEX_EVEREST_D_V4));
                embossers.put(EmbosserType.INDEX_BRAILLE_BOX_V4, new IndexV4Embosser("Index Braille Box","", EmbosserType.INDEX_BRAILLE_BOX_V4));
        }

	//jvm1.6@Override
	public Collection<Embosser> list() {
		return embossers.values();
	}

}
