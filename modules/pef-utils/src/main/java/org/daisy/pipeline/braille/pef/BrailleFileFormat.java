package org.daisy.pipeline.braille.pef;

import java.io.OutputStream;

import com.google.common.base.CaseFormat;

import cz.vutbr.web.css.Term;

import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.dotify.api.paper.Area;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableFilter;

import org.daisy.pipeline.braille.css.EmbossedMedium;
import org.daisy.pipeline.css.Dimension;
import org.daisy.pipeline.css.Dimension.Unit;
import org.daisy.pipeline.css.Medium.MediumBuilder;

public class BrailleFileFormat extends EmbossedMedium implements FileFormat {

	private final FileFormat underlyingFormat;
	private final Area printableArea;
	private final boolean duplex;
	private final boolean blankLastPage;
	private final boolean overflowAllowed;

	/**
	 * {@code printableArea}, {@code pageWidth} and {@code pageHeight} determine the dimensions of
	 * the medium as observed in media queries, but may or may not be linked to hard constraints
	 * such as a physical sheet of paper and embosser.
	 *
	 * @param printableArea The dimensions and position of the printable area of the page
	 * @param blankLastPage whether the last page of each volume should be left blank
	 */
	public BrailleFileFormat(FileFormat format, Area printableArea,
	                         Dimension pageWidth, Dimension pageHeight,
	                         double cellWidth, double cellHeight,
	                         boolean duplex, boolean blankLastPage, boolean overflowAllowed,
	                         MediumBuilder parser) {
		super(new Dimension(printableArea.getWidth(), Unit.MM),
		      new Dimension(printableArea.getHeight(), Unit.MM),
		      pageWidth,
		      pageHeight,
		      null,
		      cellWidth,
		      cellHeight,
		      parser);
		this.underlyingFormat = format;
		this.printableArea = printableArea;
		this.duplex = duplex;
		this.blankLastPage = blankLastPage;
		this.overflowAllowed = overflowAllowed;
	}

	/**
	 * The printable area of the braille page
	 */
	public Area getPrintableArea() {
		return printableArea;
	}

	/**
	 * Whether content is allowed to overflow the viewport.
	 *
	 * An example of when this could be true is when the file is not bound to a specific embosser
	 * and paper.
	 */
	public boolean isOverflowAllowed() {
		return overflowAllowed;
	}

	/* ////// */
	/* Medium */
	/* ////// */

	@Override
	protected Object getNonStandardFeature(String feature) {
		// sheets-multiple-of-two and blank-last-page are handled in px:pef-store (and are not
		// expected to be features of the underlying FileFormat)
		if ("-daisy-duplex".equals(feature))
			return duplex;
		else if ("-daisy-blank-last-page".equals(feature))
			return blankLastPage;
		else {
			if (feature.startsWith("-daisy-"))
				feature = feature.substring("-daisy-".length());
			try {
				return getFeature(CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, feature));
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

	/* ////////// */
	/* FileFormat */
	/* ////////// */

	@Override
	public Object getFeature(String key) {
		return underlyingFormat.getFeature(key);
	}

	@Override
	public Object getProperty(String key) {
		return underlyingFormat.getProperty(key);
	}

	public String getIdentifier() {
		return underlyingFormat.getIdentifier();
	}

	public String getDisplayName() {
		return underlyingFormat.getDisplayName();
	}

	public String getDescription() {
		return underlyingFormat.getDescription();
	}

	public boolean supports8dot() {
		return underlyingFormat.supports8dot();
	}

	public boolean supportsDuplex() {
		return underlyingFormat.supportsDuplex();
	}

	public boolean supportsVolumes() {
		return underlyingFormat.supportsVolumes();
	}

	public String getFileExtension() {
		return underlyingFormat.getFileExtension();
	}

	public boolean supportsTable(Table table) {
		return underlyingFormat.supportsTable(table);
	}

	public TableFilter getTableFilter() {
		return underlyingFormat.getTableFilter();
	}

	public EmbosserWriter newEmbosserWriter(OutputStream os) {
		return underlyingFormat.newEmbosserWriter(os);
	}

	@Override
	public void setFeature(String key, Object value) {
		throw new UnsupportedOperationException();
	}
}
