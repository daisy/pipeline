package org.daisy.pipeline.braille.css.xpath.impl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;
import org.daisy.pipeline.braille.css.xpath.Style;

public class Stylesheet implements Style {

	public final static Stylesheet EMPTY = new Stylesheet(null);

	private final BrailleCssStyle style;

	public Stylesheet(BrailleCssStyle style) {
		this.style = style;
	}

	@Override
	public String toString() {
		if (style != null)
			return BrailleCssSerializer.toString(style);
		else
			return "";
	}

	@Override
	public void toXml(XMLStreamWriter writer) throws XMLStreamException {
		if (style != null)
			// <css:rule>
			BrailleCssSerializer.toXml(style, writer);
	}
}
