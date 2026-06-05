package org.daisy.pipeline.braille.css;

import java.io.OutputStream;
import java.util.Map;

import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermLength;
import cz.vutbr.web.css.TermNumeric;

import org.daisy.pipeline.css.Dimension;
import org.daisy.pipeline.css.Dimension.Unit;
import org.daisy.pipeline.css.Medium;

public class EmbossedMedium extends Medium {

	private final double cellWidth;
	private final double cellHeight;

	/**
	 * @param width Width of the printable area of the page (which forms the viewport)
	 * @param height Height of the printable area of the page (which forms the viewport)
	 * @param pageWidth Width of the page ({@code device-width})
	 * @param pageHeight Height of the page ({@code device-height})
	 * @param cellWidth Braille cell width in mm
	 * @param cellHeight Braille cell height in mm
	 */
	public EmbossedMedium(Dimension width, Dimension height, Dimension pageWidth, Dimension pageHeight,
	                      Map<String,Object> otherFeatures,
	                      double cellWidth, double cellHeight, MediumBuilder parser) {
		super(Type.EMBOSSED,
		      width,
		      height,
		      pageWidth,
		      pageHeight,
		      otherFeatures,
		      parser);
		if (width != null)
			switch (width.getUnit()) {
			case VW:
				throw new IllegalArgumentException("width must not be expressed in vw");
			case VH:
				if (height == null || height.getUnit() == Unit.VW)
					throw new IllegalArgumentException();
				break;
			}
		if (height != null)
			switch (height.getUnit()) {
			case VH:
				throw new IllegalArgumentException("height must not be expressed in vh");
			case VW:
				if (width == null || width.getUnit() == Unit.VH)
					throw new IllegalArgumentException();
				break;
			}
		if (width == null)
			if ((pageWidth != null && pageWidth.getUnit() == Unit.VW)
			    || (pageHeight != null && pageHeight.getUnit() == Unit.VW))
				throw new IllegalArgumentException("viewport width is not known");
		if (height == null)
			if ((pageWidth != null && pageWidth.getUnit() == Unit.VH)
			    || (pageHeight != null && pageHeight.getUnit() == Unit.VH))
				throw new IllegalArgumentException("viewport height is not known");
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
	}

	/* ///////////////////// */
	/* RelativeDimensionBase */
	/* ///////////////////// */

	/**
	 * Cell width in millimeters
	 */
	@Override
	public double getCh() {
		return cellWidth;
	}

	/**
	 * Cell height in millimeters
	 */
	@Override
	public double getEm() {
		return cellHeight;
	}

	/**
	 * The width of the viewport, in millimeters
	 *
	 * For resolving {@code vw} dimensions in media queries. Note that the meaning of {@code vw} in
	 * CSS properties may vary depending on the current page style.
	 */
	@Override
	public double getViewportWidth() {
		Dimension w = getWidth();
		if (w == null)
			super.getViewportWidth();
		return w.toUnit(Unit.MM, this).getValue().doubleValue();
	}

	/**
	 * The height of the viewport, in millimeters
	 *
	 * For resolving {@code vh} dimensions in media queries. Note that the meaning of {@code vh} in
	 * CSS properties may vary depending on the current page style.
	 */
	@Override
	public double getViewportHeight() {
		Dimension h = getHeight();
		if (h == null)
			super.getViewportHeight();
		return h.toUnit(Unit.MM, this).getValue().doubleValue();
	}

	public static class EmbossedMediumBuilder extends MediumBuilder {

		public EmbossedMediumBuilder() {
			super();
			type = Medium.Type.EMBOSSED;
		}

		@Override
		protected Dimension parseLength(Object value, String feature) throws IllegalArgumentException {
			if (value instanceof TermInteger)
				return parseLength(((TermInteger)value).getIntValue(), feature);
			if ((value instanceof Integer
			     || (value instanceof TermLength && ((TermLength)value).getUnit() == TermNumeric.Unit.none)))
				if ("width".equals(feature) || "device-width".equals(feature))
					return value instanceof Integer
						? new Dimension((Integer)value, Unit.CH)
						: new Dimension(((TermLength)value).getValue(), Unit.CH);
				else if ("height".equals(feature) || "device-height".equals(feature))
					return value instanceof Integer
						? new Dimension((Integer)value, Unit.EM)
						: new Dimension(((TermLength)value).getValue(), Unit.EM);
			return super.parseLength(value, feature);
		}

		@Override
		public EmbossedMedium build() {
			return new EmbossedMedium((Dimension)features.remove("width"),
			                          (Dimension)features.remove("height"),
			                          (Dimension)features.remove("device-width"),
			                          (Dimension)features.remove("device-height"),
			                          features,
			                          6,
			                          10,
			                          this);
		}
	}
}
