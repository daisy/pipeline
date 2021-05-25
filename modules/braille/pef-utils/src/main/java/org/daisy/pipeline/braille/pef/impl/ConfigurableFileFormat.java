package org.daisy.pipeline.braille.pef.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Optional;

import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.dotify.api.embosser.LineBreaks;
import org.daisy.dotify.api.embosser.StandardLineBreaks;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.BrailleConverter;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableFilter;

import static org.daisy.pipeline.braille.common.Provider.util.dispatch;
import static org.daisy.pipeline.braille.common.Provider.util.memoize;
import org.daisy.pipeline.braille.common.Provider.util.MemoizingProvider;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.pef.FileFormatProvider;
import org.daisy.pipeline.braille.pef.impl.BRFWriter;
import org.daisy.pipeline.braille.pef.impl.BRFWriter.Padding;
import org.daisy.pipeline.braille.pef.impl.BRFWriter.PageBreaks;
import org.daisy.pipeline.braille.pef.TableProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurableFileFormat implements FileFormat {
	
	private static final String DEFAULT_TABLE = "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US";
	private static final LineBreaks DEFAULT_LINE_BREAKS = new StandardLineBreaks(StandardLineBreaks.Type.DEFAULT);
	private static final PageBreaks DEFAULT_PAGE_BREAKS = new PageBreaks() {
		public String getString() {
			return "\u000c";
		}
	};
	private static final Padding DEFAULT_PADDING = Padding.NONE;
	private static final String DEFAULT_FILE_EXTENSION = ".brf";
	
	private final org.daisy.pipeline.braille.common.Provider<Query,Table> tableProvider;
	private Table table;
	private String locale;
	private LineBreaks lineBreaks;
	private PageBreaks pageBreaks;
	private Padding padding;
	private Charset charset;
	private String fileExtension;
	
	private ConfigurableFileFormat(org.daisy.pipeline.braille.common.Provider<Query,Table> tableProvider) {
		this.tableProvider = tableProvider;
		lineBreaks = DEFAULT_LINE_BREAKS;
		pageBreaks = DEFAULT_PAGE_BREAKS;
		padding = DEFAULT_PADDING;
		charset = null;
		fileExtension = DEFAULT_FILE_EXTENSION;
	}
	
	public String getIdentifier() {
		return ConfigurableFileFormat.class.toString();
	}
	
	public String getDisplayName() {
		return getIdentifier();
	}
	
	public String getDescription() {
		return "";
	}
	
	public String getFileExtension() {
		return fileExtension;
	}
	
	public boolean supports8dot() {
		return false;
	}
	
	public boolean supportsDuplex() {
		return false;
	}
	
	private final TableFilter tableFilter = new TableFilter() {
		public boolean accept(FactoryProperties object) {
			return true;
		}
	};
	
	public TableFilter getTableFilter() {
		return tableFilter;
	}
	
	public boolean supportsTable(Table table) {
		return tableFilter.accept(table);
	}
	
	public void setFeature(String key, final Object value) {
		if (finalized)
			throw new UnsupportedOperationException("Immutable object");
		if ("table".equals(key)) {
			if (value != null) {
				if (value instanceof Table) {
					Table t = (Table)value;
					if (tableFilter.accept(t)) {
						table = (Table)value;
						return; }}
				else if (value instanceof String)
					for (Table t : tableProvider.get(mutableQuery().add("id", (String)value)))
						if (tableFilter.accept(t)) {
							table = t;
							return; }}
			throw new IllegalArgumentException("Unsupported value for table: " + value);
		} else if ("locale".equals(key)) {
			if (value != null) {
				if (value instanceof Locale) {
					locale = ((Locale)value).toLanguageTag();
					return; }
				else if (value instanceof String) {
					locale = (String)value;
					return; }}
			throw new IllegalArgumentException("Unsupported value for locale: " + value);
		} else if ("line-breaks".equals(key)) {
			if (value != null) {
				if (value instanceof LineBreaks) {
					lineBreaks = (LineBreaks)value;
					return; }
				else if (value instanceof String) {
					lineBreaks = new StandardLineBreaks(StandardLineBreaks.Type.valueOf(((String)value).toUpperCase()));
					return; }}
			throw new IllegalArgumentException("Unsupported value for line-breaks: " + value);
		} else if ("page-breaks".equals(key)) {
			if (value != null) {
				if (value instanceof PageBreaks) {
					pageBreaks = (PageBreaks)value;
					return; }
				else if (value instanceof String) {
					pageBreaks = new PageBreaks() {
						public String getString() {
							return (String)value; }};
					return; }}
			throw new IllegalArgumentException("Unsupported value for page-breaks: " + value);
		} else if ("pad".equals(key)) {
			if (value != null) {
				if (value instanceof Padding) {
					padding = (Padding)value;
					return; }
				else if (value instanceof String) {
					padding = Padding.valueOf(((String)value).toUpperCase());
					return; }}
			throw new IllegalArgumentException("Unsupported value for pad: " + value);
		} else if ("charset".equals(key)) {
			if (value != null) {
				if (value instanceof Charset) {
					charset = (Charset)value;
					return; }
				else if (value instanceof String) {
					try {
						charset = Charset.forName((String)value);
					}
					catch (UnsupportedCharsetException e) {
						logger.warn("Unsupported charset, falling back to table's preferred charset");
						charset = null;
					}
					return; }}
			throw new IllegalArgumentException("Unsupported value for charset: " + value);
		} else if ("file-extension".equals(key)) {
			if (value != null) {
				if (value instanceof String) {
					fileExtension = (String)value;
					return; }}
		} else
			throw new IllegalArgumentException("Unsupported feature: " + key);
	}
	
	public Object getFeature(String key) {
		if ("table".equals(key))
			return table;
		else if ("locale".equals(key))
			return locale;
		else if ("line-breaks".equals(key))
			return lineBreaks;
		else if ("page-breaks".equals(key))
			return pageBreaks;
		else if ("pad".equals(key))
			return padding;
		else if ("charset".equals(key))
			return charset;
		else if ("file-extension".equals(key))
			return fileExtension;
		else
			throw new IllegalArgumentException("Unsupported feature: " + key);
	}
	
	public EmbosserWriter newEmbosserWriter(final OutputStream os) {
		if (table == null)
			throw new RuntimeException("table not set");
		final BrailleConverter brailleConverter = table.newBrailleConverter();
		return new BRFWriter() {
			public LineBreaks getLinebreakStyle() {
				return lineBreaks;
			}
			public PageBreaks getPagebreakStyle() {
				return pageBreaks;
			}
			public Padding getPaddingStyle() {
				return padding;
			}
			public Charset getCharset() {
				return charset == null ? getTable().getPreferredCharset() : charset;
			}
			public BrailleConverter getTable() {
				return brailleConverter;
			}
			protected void add(byte b) throws IOException {
				os.write(b);
			}
			protected void addAll(byte[] b) throws IOException {
				os.write(b);
			}
		};
	}
	
	public Object getProperty(String key) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString() {
		ToStringHelper h = MoreObjects.toStringHelper("o.d.p.b.pef.impl.ConfigurableFileFormat");
		for (String k : new String[]{"table","locale","line-breaks","page-breaks","pad","file-extension"})
			h.add(k, getFeature(k));
		return h.toString();
	}
	
	private boolean finalized = false;
	
	private FileFormat build() {
		if (table == null) {
			if (locale == null)
				setFeature("table", DEFAULT_TABLE);
			else {
				for (Table t : tableProvider.get(mutableQuery().add("locale", locale)))
					if (tableFilter.accept(t)) {
						table = t;
						break; }
				if (table == null) {
					setFeature("table", DEFAULT_TABLE);
					logger.warn("Table " + table + " not compatible with locale " + locale); }}}
		else if (locale != null) {
			boolean match = false;
			for (Table t : tableProvider.get(mutableQuery().add("locale", locale)))
				if (t.equals(table)) {
					match = true;
					break; }
			if (!match)
				logger.warn("Table " + table + " not compatible with locale " + locale); }
		finalized = true;
		return this;
	}
	
	@Component(
		name = "org.daisy.pipeline.braille.pef.impl.ConfigurableFileFormat$Provider",
		service = { FileFormatProvider.class }
	)
	public static class Provider implements FileFormatProvider {
		
		public Iterable<FileFormat> get(Query query) {
			MutableQuery q = mutableQuery(query);
			ConfigurableFileFormat format = new ConfigurableFileFormat(tableProvider);
			for (Feature f : q)
				try {
					format.setFeature(f.getKey(), f.getValue().orElse(f.getKey())); }
				catch (Exception e) {
					return empty; }
			try {
				return Collections.singleton(format.build()); }
			catch (Exception e) {
				return empty; }
		}
		
		private List<TableProvider> tableProviders = new ArrayList<TableProvider>();
		private MemoizingProvider<Query,Table> tableProvider = memoize(dispatch(tableProviders));
	
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
	}
	
	private final static Iterable<FileFormat> empty = Optional.<FileFormat>absent().asSet();
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigurableFileFormat.class);
	
}
