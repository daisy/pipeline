package org.daisy.pipeline.braille.css.xpath.impl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;
import org.daisy.pipeline.braille.css.xpath.Style;

public class Declaration implements Style {

	private final BrailleCssStyle declaration;

	public Declaration(BrailleCssStyle declaration) {
		this.declaration = declaration;
	}

	@Override
	public String toString() {
		return declaration.toString();
	}

	@Override
	public void toXml(XMLStreamWriter writer) throws XMLStreamException {
		// <css:property>
		BrailleCssSerializer.toXml(declaration, writer, true);
	}
}
