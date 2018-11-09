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
package org.daisy.braille.utils.impl.provider.indexbraille;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.daisy.dotify.api.embosser.Embosser;
import org.daisy.dotify.api.embosser.EmbosserFactoryProperties;
import org.daisy.dotify.api.embosser.EmbosserProvider;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.paper.Length;
import org.daisy.dotify.api.table.TableCatalog;
import org.daisy.dotify.api.table.TableCatalogService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

@Component
public class IndexEmbosserProvider implements EmbosserProvider {
	private static final Margin EMPTY_MARGIN = new Margin.Builder().build();
	private static final Margin V4_MARGIN = new Margin.Builder()
			.right(Length.newMillimeterValue(3))
			.top(Length.newMillimeterValue(5))
			.bottom(Length.newMillimeterValue(5))
			.build();

	public static enum EmbosserType implements EmbosserFactoryProperties {
//		INDEX_3_7("", ""), //Not implemented
//		INDEX_ADVANCED("", ""), //Not implemented
//		INDEX_CLASSIC("", ""), //Not implemented
//		INDEX_DOMINO("", ""), //Not implemented
//		INDEX_EVEREST_S_V1("", ""), //Not implemented
//		INDEX_EVEREST_D_V1("", ""), //Not implemented
		INDEX_BASIC_BLUE_BAR("Basic Blue-Bar", "Early Index Basic embosser", (t, u)->new BlueBarEmbosser(t, u), EMPTY_MARGIN),
		INDEX_BASIC_S_V2("Basic-S V2", "", (t, u)->new IndexV2Embosser(t, u), EMPTY_MARGIN),
		INDEX_BASIC_D_V2("Basic-D V2", "", (t, u)->new IndexV2Embosser(t, u), EMPTY_MARGIN),
		INDEX_EVEREST_D_V2("Everest-D V2", "", (t, u)->new IndexV2Embosser(t, u), EMPTY_MARGIN),
		INDEX_4X4_PRO_V2("4X4 Pro V2", "", (t, u)->new IndexV2Embosser(t, u), EMPTY_MARGIN),
		INDEX_BASIC_S_V3("Basic-S V3", "", (t, u)->new IndexV3Embosser(t, u), EMPTY_MARGIN),
		INDEX_BASIC_D_V3("Basic-D V3", "", (t, u)->new IndexV3Embosser(t, u), EMPTY_MARGIN),
		INDEX_EVEREST_D_V3("Everest-D V3", "", (t, u)->new IndexV3Embosser(t, u), EMPTY_MARGIN),
		INDEX_4X4_PRO_V3("4X4 Pro V3", "", (t, u)->new IndexV3Embosser(t, u), EMPTY_MARGIN),
		INDEX_4WAVES_PRO_V3("4Waves Pro", "", (t, u)->new IndexV3Embosser(t, u), EMPTY_MARGIN),
		INDEX_BASIC_D_V4("Basic-D V4", "", (t, u)->new IndexV4Embosser(t, u), V4_MARGIN),
		INDEX_EVEREST_D_V4("Everest-D V4", "", (t, u)->new IndexV4Embosser(t, u), V4_MARGIN),
		INDEX_BRAILLE_BOX_V4("Braille Box V4", "", (t, u)->new IndexV4Embosser(t, u), V4_MARGIN),
		INDEX_BASIC_D_V5("Basic-D V5", "", (t, u)->new IndexV4Embosser(t, u), V4_MARGIN),
		INDEX_EVEREST_D_V5("Everest-D V5", "", (t, u)->new IndexV4Embosser(t, u), V4_MARGIN),
		INDEX_BRAILLE_BOX_V5("Braille Box V5", "", (t, u)->new IndexV4Embosser(t, u), V4_MARGIN),
		INDEX_FANFOLD_V5("Fanfold", "", (t, u)->new IndexV4Embosser(t, u), V4_MARGIN);

		private static final String MAKE = "Index";
		private final String name;
		private final String model;
		private final String desc;
		private final String identifier;
		private final BiFunction<TableCatalogService, EmbosserType, Embosser> instanceCreator;
		private final Margin unprintable;

        EmbosserType (String model, String desc, BiFunction<TableCatalogService, EmbosserType, Embosser> instanceCreator, Margin unprintable) {
			this.name = MAKE + " " + model;
			// Make is included in the model name here
			this.model = name;
			this.desc = desc;
			this.identifier = "com_indexbraille.IndexEmbosserProvider.EmbosserType." + this.toString();
			this.instanceCreator = instanceCreator;
			this.unprintable = unprintable;
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
		
		private Embosser newInstance(TableCatalogService tableCatalogService) {
			return instanceCreator.apply(tableCatalogService, this);
		}
		
		Margin getUnprintable() {
			return unprintable;
		}

		@Override
		public String getMake() {
			return MAKE;
		}

		@Override
		public String getModel() {
			return model;
		}
	};

	private final Map<String, EmbosserFactoryProperties> embossers;
	private TableCatalogService tableCatalogService = null;

	public IndexEmbosserProvider() {
		embossers = new HashMap<>();
		for (EmbosserType e : EmbosserType.values()) {
			embossers.put(e.getIdentifier(), e);			
		}
	}

	@Override
	public Embosser newFactory(String identifier) {
		FactoryProperties fp = embossers.get(identifier);
		return fp==null?null:((EmbosserType)fp).newInstance(tableCatalogService);
	}

	@Override
	public Collection<EmbosserFactoryProperties> list() {
		return Collections.unmodifiableCollection(embossers.values());
	}

	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setTableCatalog(TableCatalogService service) {
		this.tableCatalogService = service;
	}

	public void unsetTableCatalog(TableCatalogService service) {
		this.tableCatalogService = null;
	}

	@Override
	public void setCreatedWithSPI() {
		if (tableCatalogService==null) {
			tableCatalogService = TableCatalog.newInstance();
		}
	}

}
