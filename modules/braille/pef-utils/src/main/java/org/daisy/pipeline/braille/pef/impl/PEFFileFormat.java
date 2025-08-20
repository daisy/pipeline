package org.daisy.pipeline.braille.pef.impl;

import java.io.OutputStream;

import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableFilter;

public class PEFFileFormat implements FileFormat {

	private boolean duplexEnabled = true;
	private boolean saddleStitchEnabled = false;

	public PEFFileFormat() {}

	public Object getProperty(String key) {
		throw new UnsupportedOperationException();
	}

	public Object getFeature(String key) {
		if (EmbosserFeatures.DUPLEX.equals(key))
			return duplexEnabled;
		else if (EmbosserFeatures.SADDLE_STITCH.equals(key))
			return saddleStitchEnabled;
		return null;
	}

	public void setFeature(String key, Object value) {
		if (EmbosserFeatures.DUPLEX.equals(key))
			try {
				duplexEnabled = (Boolean)value;
				return;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Unsupported value for duplex: " + value, e);
			}
		else if (EmbosserFeatures.SADDLE_STITCH.equals(key))
			try {
				saddleStitchEnabled = (Boolean)value;
				return;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Unsupported value for saddle stitch: " + value, e);
			}
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
}
