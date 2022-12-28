package org.daisy.pipeline.braille.css.xpath.impl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cz.vutbr.web.css.Declaration;

import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.xpath.Style;

public class DeclarationStyle implements Style {

	public final static DeclarationStyle EMPTY = new DeclarationStyle(null);

	private final Declaration declaration;

	public DeclarationStyle(Declaration declaration) {
		this.declaration = declaration;
	}

	@Override
	public String toString() {
		return toString(null);
	}

	@Override
	public String toString(Style parent) {
		if (parent != null)
			throw new UnsupportedOperationException();
		if (declaration != null)
			return BrailleCssSerializer.toString(declaration);
		else
			return "";
	}

	@Override
	public void toXml(XMLStreamWriter writer) throws XMLStreamException {
		if (declaration != null)
			// <css:property>
			BrailleCssSerializer.toXml(declaration, writer);
	}
}
