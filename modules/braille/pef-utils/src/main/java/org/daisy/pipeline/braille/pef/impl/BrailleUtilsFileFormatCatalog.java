package org.daisy.pipeline.braille.pef.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import static com.google.common.base.Predicates.notNull;
import com.google.common.base.Supplier;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

import org.daisy.dotify.api.embosser.Embosser;
import org.daisy.dotify.api.embosser.EmbosserProvider;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableFilter;

import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.pef.FileFormatProvider;
import org.daisy.pipeline.braille.pef.TableRegistry;

import org.osgi.framework.FrameworkUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "org.daisy.pipeline.braille.pef.impl.BrailleUtilsFileFormatCatalog",
	service = { FileFormatProvider.class }
)
public class BrailleUtilsFileFormatCatalog implements FileFormatProvider {
	
	public Iterable<FileFormat> get(Query query) {
		final MutableQuery q = mutableQuery(query);
		Iterable<Supplier<FileFormat>> format; {
			if (q.containsKey("format")) {
				String id = q.removeOnly("format").getValue().get();
				format = getFileFormat(id); }
			else if (q.containsKey("embosser")) {
				String id = q.removeOnly("embosser").getValue().get();
				format = getEmbosserAsFileFormat(id); }
			else
				return empty; }
		final String documentLocale = q.containsKey("document-locale")
			? q.removeOnly("document-locale").getValue().get()
			: null;
		final Iterable<Table> table; {
			if (q.containsKey("table")) {
				String id = q.removeOnly("table").getValue().get();
				// table could be a locale
				String locale; {
					try {
						locale = parseLocale(id).toLanguageTag(); }
					catch (IllegalArgumentException e) {
						locale = null; }}
				table = locale != null
					? concat(tableRegistry.get(mutableQuery().add("locale", locale)),
					         tableRegistry.get(mutableQuery().add("id", id)))
					: tableRegistry.get(mutableQuery().add("id", id)); }
			else if (q.containsKey("locale")) {
				Feature locale = q.removeOnly("locale");
				MutableQuery tableQuery = mutableQuery();
				tableQuery.add(locale);
				table = tableRegistry.get(tableQuery); }
			else if (documentLocale != null) {
				Query tableQuery = mutableQuery().add("locale", documentLocale);
				table = tableRegistry.get(tableQuery); }
			else
				table = Collections.singleton(null); }
		return concat(
			transform(
				format,
				new Function<Supplier<FileFormat>,Iterable<FileFormat>>() {
					public Iterable<FileFormat> apply(final Supplier<FileFormat> format) {
						return filter(
							transform(
								table,
								new Function<Table,FileFormat>() {
									public FileFormat apply(Table table) {
										FileFormat frmt = format.get();
										if (table != null)
											frmt.setFeature("table", table);
										for (Feature f : q) {
											if (table != null && "locale".equals(f.getKey())) {
												String locale = f.getValue().get();
												boolean match = false;
												for (Table t : tableRegistry.get(mutableQuery().add("locale", locale)))
													if (t.equals(table)) {
														match = true;
														break; }
												if (!match) {
													logger.warn("Table " + table + " not compatible with locale " + locale);
													return null; }}
											try {
												frmt.setFeature(f.getKey(), f.getValue().orElse(f.getKey())); }
											catch (Exception e) {
												return null; }}
										return frmt; }}),
							notNull());
					}
				}
			)
		);
	}
	
	private Iterable<Supplier<FileFormat>> getFileFormat(final String id) {
		List<Supplier<FileFormat>> formats = new ArrayList<Supplier<FileFormat>>();
		for (final org.daisy.dotify.api.embosser.FileFormatProvider p : providers)
			for (FactoryProperties fp : p.list())
				if (fp.getIdentifier().equals(id))
					formats.add(new Supplier<FileFormat>() {
						public FileFormat get() {
							return p.newFactory(id); }});
		return formats;
	}
	
	private Iterable<Supplier<FileFormat>> getEmbosserAsFileFormat(final String id) {
		List<Supplier<FileFormat>> formats = new ArrayList<Supplier<FileFormat>>();
		for (final EmbosserProvider p : embosserProviders)
			for (FactoryProperties fp : p.list())
				if (fp.getIdentifier().equals(id))
					formats.add(new Supplier<FileFormat>() {
						public FileFormat get() {
							return new EmbosserAsFileFormat(p.newFactory(id)); }});
		return formats;
	}
	
	private final static Iterable<FileFormat> empty = Optional.<FileFormat>absent().asSet();
	
	private final List<org.daisy.dotify.api.embosser.FileFormatProvider> providers
	= new ArrayList<org.daisy.dotify.api.embosser.FileFormatProvider>();
	
	@Reference(
		name = "FileFormatProvider",
		unbind = "-",
		service = org.daisy.dotify.api.embosser.FileFormatProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addFileFormatProvider(org.daisy.dotify.api.embosser.FileFormatProvider provider) {
		if (!OSGiHelper.inOSGiContext())
			provider.setCreatedWithSPI();
		providers.add(provider);
	}
	
	public void removeFileFormatProvider(org.daisy.dotify.api.embosser.FileFormatProvider provider) {
		providers.remove(provider);
	}
	
	private final List<EmbosserProvider> embosserProviders = new ArrayList<EmbosserProvider>();
	
	@Reference(
		name = "EmbosserProvider",
		unbind = "-",
		service = EmbosserProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addEmbosserProvider(EmbosserProvider provider) {
		if (!OSGiHelper.inOSGiContext())
			provider.setCreatedWithSPI();
		embosserProviders.add(provider);
	}
	
	public void removeEmbosserProvider(EmbosserProvider provider) {
		embosserProviders.remove(provider);
	}
		
	private TableRegistry tableRegistry;
	
	@Reference(
		name = "TableRegistry",
		unbind = "-",
		service = TableRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindTableRegistry(TableRegistry registry) {
		tableRegistry = registry;
	}
	
	private static class EmbosserAsFileFormat implements FileFormat {
		
		private final Embosser embosser;
		
		public EmbosserAsFileFormat(Embosser embosser) {
			this.embosser = embosser;
		}
		
		public Object getProperty(String key) {
			return embosser.getProperty(key);
		}

		public Object getFeature(String key) {
			return embosser.getFeature(key);
		}

		public void setFeature(String key, Object value) {
			embosser.setFeature(key, value);
		}

		public String getIdentifier() {
			return embosser.getIdentifier();
		}

		public String getDisplayName() {
			return embosser.getDisplayName();
		}

		public String getDescription() {
			return embosser.getDescription();
		}

		public boolean supports8dot() {
			return embosser.supports8dot();
		}

		public boolean supportsDuplex() {
			return embosser.supportsDuplex();
		}

		public boolean supportsVolumes() {
			return false;
		}

		public String getFileExtension() {
			return ".brf";
		}

		public boolean supportsTable(Table table) {
			return embosser.supportsTable(table);
		}

		public TableFilter getTableFilter() {
			return embosser.getTableFilter();
		}

		public EmbosserWriter newEmbosserWriter(OutputStream os) {
			return embosser.newEmbosserWriter(os);
		}
	}
	
	private static abstract class OSGiHelper {
		static boolean inOSGiContext() {
			try {
				return FrameworkUtil.getBundle(OSGiHelper.class) != null;
			} catch (NoClassDefFoundError e) {
				return false;
			}
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(BrailleUtilsFileFormatCatalog.class);
}
