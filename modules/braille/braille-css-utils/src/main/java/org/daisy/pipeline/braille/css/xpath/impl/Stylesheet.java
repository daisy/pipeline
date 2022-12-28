package org.daisy.pipeline.braille.css.xpath.impl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;
import org.daisy.pipeline.braille.css.xpath.Style;

public class Stylesheet implements Style {

	public final static Stylesheet EMPTY = new Stylesheet(null);

	public final BrailleCssStyle style;

	public Stylesheet(BrailleCssStyle style) {
		this.style = style;
	}

	@Override
	public String toString() {
		return toString(null);
	}

	@Override
	public String toString(Style parent) {
		BrailleCssStyle style = this.style;
		BrailleCssStyle relativeTo; {
			if (parent != null) {
				if (!(parent instanceof Stylesheet))
					throw new IllegalArgumentException("Unexpected type for second argument: " + parent);
				relativeTo = ((Stylesheet)parent).style;
			} else
				relativeTo = null;
		}
		if (style == null && relativeTo == null)
			return "";
		else if (style == null)
			// we may need to add declarations to cancel parent declarations
			style = BrailleCssStyle.of("", Context.ELEMENT);
		else if (relativeTo == null && parent != null)
			// we may want to remove unneeded declarations
			relativeTo = BrailleCssStyle.of("", Context.ELEMENT);
		return style.toString(relativeTo);
	}

	@Override
	public void toXml(XMLStreamWriter writer) throws XMLStreamException {
		if (style != null)
			// <css:rule>
			BrailleCssSerializer.toXml(style, writer);
	}
}
