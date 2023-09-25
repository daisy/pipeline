package org.daisy.pipeline.braille.pef.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.Table;

import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.pef.AbstractTableProvider;
import org.daisy.pipeline.braille.pef.TableProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see <a href="../../../../../../../README.md">Documentation</a>
 */
@Component(
	name = "org.daisy.pipeline.braille.pef.impl.LocaleTableProvider",
	service = {
		TableProvider.class
		// org.daisy.dotify.api.table.TableProvider.class
	}
)
public class LocaleBasedTableProvider extends AbstractTableProvider {
	
	private static Set<String> supportedFeatures = ImmutableSet.of("locale", "document-locale");
	private static Map<String,String> tableFromLocale = new HashMap<String,String>();
	
	public LocaleBasedTableProvider() {
		tableFromLocale.put("en", "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US");
		tableFromLocale.put("de", "org_daisy.EmbosserTableProvider.TableType.DE_DE");
		tableFromLocale.put("nl", "com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_031_01");
	}

	/**
	 * Recognized features:
	 *
	 * - locale: A locale that is mapped to a specific table
	 *     that is a sane default for that locale.
	 */
	protected Iterable<Table> _get(Query query) {
		for (Feature feature : query)
			if (!supportedFeatures.contains(feature.getKey())) {
				logger.debug("Unsupported feature: " + feature);
				return empty; }
		Iterable<Table> table = empty;
		MutableQuery q = mutableQuery(query);
		final String documentLocale = q.containsKey("document-locale")
			? q.removeOnly("document-locale").getValue().get()
			: null;
		if (q.containsKey("locale")) {
			String id = tableFromLocale.get(q.removeOnly("locale").getValue().get());
			if (id != null && q.isEmpty())
				table = get(id); }
		else if (documentLocale != null && q.isEmpty()) {
			String id = tableFromLocale.get(documentLocale);
			if (id != null)
				table = get(id); }
		return table;
	}
	
	private final static Iterable<Table> empty = Optional.<Table>absent().asSet();
	
	private final List<org.daisy.dotify.api.table.TableProvider> providers
	= new ArrayList<org.daisy.dotify.api.table.TableProvider>();
	
	@Reference(
		name = "TableProvider",
		unbind = "removeTableProvider",
		service = org.daisy.dotify.api.table.TableProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addTableProvider(org.daisy.dotify.api.table.TableProvider provider) {
		providers.add(provider);
	}
	
	public void removeTableProvider(org.daisy.dotify.api.table.TableProvider provider) {
		providers.remove(provider);
	}
	
	private Iterable<Table> get(String id) {
		for (org.daisy.dotify.api.table.TableProvider p : providers)
			for (FactoryProperties fp : p.list())
				if (fp.getIdentifier().equals(id))
					return Optional.fromNullable(p.newFactory(id)).asSet();
		return empty;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(LocaleBasedTableProvider.class);
	
}
