package org.daisy.pipeline.braille.css.xpath.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.xpath.Style;

public class Declaration extends Style {

	public final cz.vutbr.web.css.Declaration declaration;

	public Declaration(cz.vutbr.web.css.Declaration declaration) {
		if (declaration == null)
			throw new IllegalArgumentException();
		this.declaration = declaration;
	}

	@Override
	protected Iterable<String> keys() {
		return Collections.singleton(declaration.getProperty());
	}

	@Override
	protected Optional<String> property() {
		return Optional.of(declaration.getProperty());
	}

	@Override
	protected Optional<String> selector() {
		return Optional.empty();
	}

	@Override
	protected Optional<Style> get(String key) {
		if (declaration.getProperty().equals(key))
			return Optional.of(new Value(declaration));
		else
			return Optional.empty();
	}

	@Override
	protected Optional<Style> remove(Iterator<String> keys) {
		while (keys.hasNext())
			if (declaration.getProperty().equals(keys.next()))
				return Optional.empty();
		return Optional.of(this);
	}

	@Override
	protected Iterator<Object> iterate() {
		return Collections.<Object>singleton(this).iterator();
	}

	@Override
	protected String toString(Style parent) {
		if (parent != null)
			throw new UnsupportedOperationException();
		return BrailleCssSerializer.getInstance().toString(declaration);
	}

	@Override
	protected String toPrettyString(String indentation) {
		return toString();
	}

	@Override
	protected void toXml(XMLStreamWriter writer) throws XMLStreamException {
		// <css:property>
		BrailleCssSerializer.getInstance().toXml(declaration, writer);
	}
}
