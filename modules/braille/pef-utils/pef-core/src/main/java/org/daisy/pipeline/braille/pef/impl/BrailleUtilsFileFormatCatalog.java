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

import org.daisy.braille.api.embosser.Embosser;
import org.daisy.braille.api.embosser.EmbosserProvider;
import org.daisy.braille.api.embosser.EmbosserWriter;
import org.daisy.braille.api.embosser.FileFormat;
import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.table.Table;
import org.daisy.braille.api.table.TableFilter;

import static org.daisy.pipeline.braille.common.Provider.util.dispatch;
import static org.daisy.pipeline.braille.common.Provider.util.memoize;
import org.daisy.pipeline.braille.common.Provider.util.MemoizingProvider;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.pef.FileFormatProvider;
import org.daisy.pipeline.braille.pef.TableProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

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
		final Iterable<Table> table; {
			if (q.containsKey("table")) {
				String id = q.removeOnly("table").getValue().get();
				Query tableQuery = mutableQuery().add("id", id);
				table = tableProvider.get(tableQuery); }
			else if (q.containsKey("locale")) {
				Feature locale = q.removeOnly("locale");
				MutableQuery tableQuery = mutableQuery();
				tableQuery.add(locale);
				table = tableProvider.get(tableQuery); }
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
										for (Feature f : q)
											try {
												frmt.setFeature(f.getKey(), f.getValue().orElse(f.getKey())); }
											catch (Exception e) {
												return null; }
										return frmt; }}),
							notNull());
					}
				}
			)
		);
	}
	
	private Iterable<Supplier<FileFormat>> getFileFormat(final String id) {
		List<Supplier<FileFormat>> formats = new ArrayList<Supplier<FileFormat>>();
		for (final org.daisy.braille.api.embosser.FileFormatProvider p : providers)
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
	
	private final List<org.daisy.braille.api.embosser.FileFormatProvider> providers
	= new ArrayList<org.daisy.braille.api.embosser.FileFormatProvider>();
	
	@Reference(
		name = "FileFormatProvider",
		unbind = "removeFileFormatProvider",
		service = org.daisy.braille.api.embosser.FileFormatProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addFileFormatProvider(org.daisy.braille.api.embosser.FileFormatProvider provider) {
		providers.add(provider);
	}
	
	public void removeFileFormatProvider(org.daisy.braille.api.embosser.FileFormatProvider provider) {
		providers.remove(provider);
	}
	
	private final List<EmbosserProvider> embosserProviders = new ArrayList<EmbosserProvider>();
	
	@Reference(
		name = "EmbosserProvider",
		unbind = "removeEmbosserProvider",
		service = EmbosserProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addEmbosserProvider(EmbosserProvider provider) {
		embosserProviders.add(provider);
	}
	
	public void removeEmbosserProvider(EmbosserProvider provider) {
		embosserProviders.remove(provider);
	}
		
	private List<TableProvider> tableProviders = new ArrayList<TableProvider>();
	private MemoizingProvider<Query,Table> tableProvider
	= memoize(dispatch(tableProviders));
	
	@Reference(
		name = "TableProvider",
		unbind = "removeTableProvider",
		service = TableProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void addTableProvider(TableProvider provider) {
		tableProviders.add(provider);
	}
		
	protected void removeTableProvider(TableProvider provider) {
		tableProviders.remove(provider);
		this.tableProvider.invalidateCache();
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
}
