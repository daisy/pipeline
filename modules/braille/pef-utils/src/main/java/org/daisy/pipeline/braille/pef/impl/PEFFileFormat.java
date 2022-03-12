package org.daisy.pipeline.braille.pef.impl;

import java.io.OutputStream;
import java.util.Collections;

import com.google.common.base.Optional;

import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableFilter;

import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.pef.FileFormatProvider;

import org.osgi.service.component.annotations.Component;

public class PEFFileFormat implements FileFormat {

	private PEFFileFormat() {}

	public final static PEFFileFormat INSTANCE = new PEFFileFormat();

	public Object getProperty(String key) {
		throw new UnsupportedOperationException();
	}

	public Object getFeature(String key) {
		throw new IllegalArgumentException("Unsupported feature: " + key);
	}

	public void setFeature(String key, Object value) {
		throw new IllegalArgumentException("Unsupported feature: " + key);
	}

	public String getIdentifier() {
		return "pef";
	}

	public String getDisplayName() {
		return "PEF";
	}

	public String getDescription() {
		return "Portable Embosser Format";
	}

	public boolean supports8dot() {
		return true;
	}

	public boolean supportsDuplex() {
		return true;
	}

	public boolean supportsVolumes() {
		return true;
	}

	public String getFileExtension() {
		return ".pef";
	}

	public boolean supportsTable(Table table) {
		return false;
	}

	public TableFilter getTableFilter() {
		return x -> false;
	}

	public EmbosserWriter newEmbosserWriter(OutputStream os) {
		// For now this is just a dummy FileFormat that can not be used to write a file. We just
		// copy the input PEF file (stream) instead of parsing and reserializing it. Note that
		// before we can implement this method, the EmbosserWriter API needs to be enhanced if we
		// want to retain the PEF metadata.
		throw new UnsupportedOperationException();
	}

	@Component(
		name = "org.daisy.pipeline.braille.pef.impl.PEFFileFormat$Provider",
		service = { FileFormatProvider.class }
	)
	public static class Provider implements FileFormatProvider {

		public Iterable<FileFormat> get(Query query) {
			final MutableQuery q = mutableQuery(query);
			q.removeAll("document-locale");
			if (q.containsKey("format") && "pef".equalsIgnoreCase(q.removeOnly("format").getValue().get()) && q.isEmpty())
				return Collections.singleton(PEFFileFormat.INSTANCE);
			return empty;
		}

		private final static Iterable<FileFormat> empty = Optional.<FileFormat>absent().asSet();
	}
}
