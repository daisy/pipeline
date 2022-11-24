package org.daisy.pipeline.braille.css.xpath.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cz.vutbr.web.css.Declaration;

import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.xpath.Style;

public class DeclarationStyle extends Style {

	public final static DeclarationStyle EMPTY = new DeclarationStyle(null);

	public final Declaration declaration;

	public DeclarationStyle(Declaration declaration) {
		this.declaration = declaration;
	}

	@Override
	public Iterable<String> keys() {
		if (declaration != null)
			return Collections.singleton(declaration.getProperty());
		else
			return Collections.emptyList();
	}

	@Override
	public Optional<String> property() {
		if (declaration != null)
			return Optional.of(declaration.getProperty());
		else
			return Optional.empty();
	}

	@Override
	public Optional<String> selector() {
		return Optional.empty();
	}

	@Override
	public boolean empty() {
		return declaration == null;
	}

	@Override
	public Optional<Style> get(String key) {
		if (declaration != null && declaration.getProperty().equals(key))
			return Optional.of(new ValueStyle(declaration));
		else
			return Optional.empty();
	}

	@Override
	public Iterator<Style> iterator() {
		if (declaration != null)
			return Collections.<Style>singleton(this).iterator();
		else
			return Collections.emptyIterator();
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
