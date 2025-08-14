package org.daisy.pipeline.braille.pef.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.dotify.api.embosser.LineBreaks;
import org.daisy.dotify.api.embosser.StandardLineBreaks;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.BrailleConverter;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableFilter;

import org.daisy.pipeline.braille.pef.impl.BRFWriter;
import org.daisy.pipeline.braille.pef.impl.BRFWriter.Padding;
import org.daisy.pipeline.braille.pef.impl.BRFWriter.PageBreaks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurableFileFormat implements FileFormat {

	static final String DEFAULT_TABLE = "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US";
	private static final LineBreaks DEFAULT_LINE_BREAKS = new StandardLineBreaks(StandardLineBreaks.Type.DEFAULT);
	private static final PageBreaks DEFAULT_PAGE_BREAKS = new PageBreaks() {
		public String getString() {
			return "\u000c";
		}
	};
	private static final Padding DEFAULT_PADDING = Padding.NONE;
	private static final String DEFAULT_FILE_EXTENSION = ".brf";

	private Table table = null;
	private boolean duplex = true;
	private boolean saddleStitch = false;
	private LineBreaks lineBreaks = null;
	private PageBreaks pageBreaks = null;
	private Padding padding = null;
	private Charset charset = null;
	private String fileExtension = null;

	ConfigurableFileFormat() {
		lineBreaks = DEFAULT_LINE_BREAKS;
		pageBreaks = DEFAULT_PAGE_BREAKS;
		padding = DEFAULT_PADDING;
		charset = null;
		fileExtension = DEFAULT_FILE_EXTENSION;
	}

	@Override
	public String getIdentifier() {
		return ConfigurableFileFormat.class.toString();
	}

	@Override
	public String getDisplayName() {
		return getIdentifier();
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getFileExtension() {
		return fileExtension;
	}

	@Override
	public boolean supports8dot() {
		return false;
	}

	@Override
	public boolean supportsDuplex() {
		return true;
	}

	@Override
	public boolean supportsVolumes() {
		return false;
	}

	private final TableFilter tableFilter = new TableFilter() {
		public boolean accept(FactoryProperties object) {
			return true;
		}
	};

	@Override
	public TableFilter getTableFilter() {
		return tableFilter;
	}

	@Override
	public boolean supportsTable(Table table) {
		return tableFilter.accept(table);
	}

	@Override
	public void setFeature(String key, final Object value) {
		if (EmbosserFeatures.TABLE.equals(key)) {
			if (value instanceof Table) {
				Table t = (Table)value;
				if (tableFilter.accept(t)) {
					table = (Table)value;
					return; }}
			throw new IllegalArgumentException("Unsupported value for table: " + value);
		} else if (EmbosserFeatures.DUPLEX.equals(key)) {
			if (value instanceof Boolean) {
				duplex = (Boolean)value;
				return; }
			throw new IllegalArgumentException("Unsupported value for duplex: " + value);
		} else if (EmbosserFeatures.SADDLE_STITCH.equals(key)) {
			if (value instanceof Boolean) {
				saddleStitch = (Boolean)value;
				return;  }
			throw new IllegalArgumentException("Unsupported value for saddle stitch: " + value);
		} else if ("lineBreaks".equals(key)) {
			if (value != null) {
				if (value instanceof LineBreaks) {
					lineBreaks = (LineBreaks)value;
					return; }
				else if (value instanceof String) {
					lineBreaks = new StandardLineBreaks(StandardLineBreaks.Type.valueOf(((String)value).toUpperCase()));
					return; }}
			throw new IllegalArgumentException("Unsupported value for line-breaks: " + value);
		} else if ("pageBreaks".equals(key)) {
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
		} else if ("fileExtension".equals(key)) {
			if (value != null) {
				if (value instanceof String) {
					fileExtension = (String)value;
					return; }}
		} else
			throw new IllegalArgumentException("Unsupported feature: " + key);
	}

	@Override
	public Object getFeature(String key) {
		if (EmbosserFeatures.TABLE.equals(key))
			return table;
		else if (EmbosserFeatures.DUPLEX.equals(key))
			return duplex;
		else if (EmbosserFeatures.SADDLE_STITCH.equals(key))
			return saddleStitch;
		else if ("lineBreaks".equals(key))
			return lineBreaks;
		else if ("pageBreaks".equals(key))
			return pageBreaks;
		else if ("pad".equals(key))
			return padding;
		else if ("charset".equals(key))
			return charset;
		else if ("fileExtension".equals(key))
			return fileExtension;
		else
			return null;
	}

	@Override
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

	@Override
	public Object getProperty(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		ToStringHelper h = MoreObjects.toStringHelper("o.d.p.b.pef.impl.ConfigurableFileFormat");
		for (String k : new String[]{"table", "line-breaks", "page-breaks", "pad", "file-extension"})
			h.add(k, getFeature(k));
		return h.toString();
	}

	private static final Logger logger = LoggerFactory.getLogger(ConfigurableFileFormat.class);

}
