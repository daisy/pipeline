package org.daisy.pipeline.braille.pef.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.Table;

import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import org.daisy.pipeline.braille.pef.AbstractTableProvider;
import org.daisy.pipeline.braille.pef.TableProvider;
import org.daisy.pipeline.common.NormalizeLang;

import org.osgi.service.component.annotations.Activate;
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
	private static Map<Locale,TableProxy> tableFromLocale = new HashMap<>();
	
	@Activate
	protected void init() {
		putTable("org_daisy.EmbosserTableProvider.TableType.CS_CZ",                   parseLocale("cs"));
		putTable("org_daisy.EmbosserTableProvider.TableType.DA_DK",                   parseLocale("da"));
		putTable("org_daisy.EmbosserTableProvider.TableType.DE_DE",                   parseLocale("de"));
		putTable("org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US", parseLocale("en"));
		putTable("org_daisy.EmbosserTableProvider.TableType.EN_GB",                   parseLocale("en-GB"));
		putTable("org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US", parseLocale("en-US"));
		putTable("org_daisy.EmbosserTableProvider.TableType.ES_ES_TABLE_2",           parseLocale("es"));
		putTable("org_daisy.EmbosserTableProvider.TableType.IT_IT_FIRENZE",           parseLocale("it"));
		putTable("com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_047_01",    parseLocale("nb"));
		putTable("com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_031_01",    parseLocale("nl"));
		putTable("com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_047_01",    parseLocale("no"),
		                                                                              parseLocale("nn"),
		                                                                              parseLocale("nb"));
		putTable("com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_046_01",    parseLocale("sv"));
	}

	private void putTable(String id, Locale... locales) {
		for (org.daisy.dotify.api.table.TableProvider p : providers)
			for (FactoryProperties fp : p.list())
				if (fp.getIdentifier().equals(id)) {
					TableProxy table = null;
					for (Locale locale : locales) {
						if (table == null)
							table = new TableProxy(fp, p, locale);
						tableFromLocale.put(locale, table);
					}
					break;
				}
	}

	private Collection<FactoryProperties> properties = null;

	@Override
	public Collection<FactoryProperties> list() {
		if (properties == null)
			properties = Collections.unmodifiableCollection(new HashSet<>(tableFromLocale.values()));
		return properties;
	}

	/**
	 * Recognized features:
	 *
	 * - locale: A locale that is mapped to a specific table
	 *     that is a sane default for that locale.
	 */
	@Override
	protected Iterable<Table> _get(Query query) {
		for (Feature feature : query)
			if (!supportedFeatures.contains(feature.getKey())) {
				logger.debug("Unsupported feature: " + feature);
				return empty; }
		Iterable<Table> table = empty;
		MutableQuery q = mutableQuery(query);
		Locale documentLocale; {
			try {
				documentLocale = q.containsKey("document-locale")
					? NormalizeLang.normalize(parseLocale(q.removeOnly("document-locale").getValue().get()))
					: null; }
			catch (IllegalArgumentException e) {
				logger.error("Invalid locale", e);
				documentLocale = null; }}
		if (q.containsKey("locale")) {
			Locale locale; {
				try {
					locale = NormalizeLang.normalize(parseLocale(q.removeOnly("locale").getValue().get())); }
				catch (IllegalArgumentException e) {
					logger.error("Invalid locale", e);
					return empty; }}
			if (q.isEmpty()) {
				TableProxy t = tableFromLocale.get(locale);
				if (t == null)
					t = tableFromLocale.get(new Locale(locale.getLanguage()));
				if (t != null)
					table = Collections.singleton(t.getTable()); }}
		else if (documentLocale != null && q.isEmpty()) {
			TableProxy t = tableFromLocale.get(documentLocale);
			if (t == null)
				t = tableFromLocale.get(new Locale(documentLocale.getLanguage()));
			if (t != null)
				table = Collections.singleton(t.getTable()); }
		return table;
	}
	
	private final static Iterable<Table> empty = Collections.<Table>emptyList();
	
	private final List<org.daisy.dotify.api.table.TableProvider> providers
	= new ArrayList<org.daisy.dotify.api.table.TableProvider>();
	
	@Reference(
		name = "TableProvider",
		unbind = "-",
		service = org.daisy.dotify.api.table.TableProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addTableProvider(org.daisy.dotify.api.table.TableProvider provider) {
		providers.add(provider);
	}
	
	public void removeTableProvider(org.daisy.dotify.api.table.TableProvider provider) {
		providers.remove(provider);
	}
	
	static class TableProxy implements FactoryProperties {

		private final org.daisy.dotify.api.table.TableProvider provider;
		private final FactoryProperties properties;
		private final Locale locale;
		private Table table = null;

		public TableProxy(FactoryProperties properties, org.daisy.dotify.api.table.TableProvider provider, Locale locale) {
			this.properties = properties;
			this.provider = provider;
			this.locale = locale;
		}

		public Table getTable() {
			if (table == null)
				table = provider.newFactory(properties.getIdentifier());
			return table;
		}

		public Locale getLocale() {
			return locale;
		}

		@Override
		public String getIdentifier() {
			return properties.getIdentifier();
		}

		@Override
		public String getDisplayName() {
			return "Default table for " + locale.getDisplayName();
		}

		@Override
		public String getDescription() {
			if (getIdentifier().startsWith("org_daisy") ||
			    getIdentifier().startsWith("org.daisy"))
				return null;
			else
				return properties.getDescription();
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(LocaleBasedTableProvider.class);
}
