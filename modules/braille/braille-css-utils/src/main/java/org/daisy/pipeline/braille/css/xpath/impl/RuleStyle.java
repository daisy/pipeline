package org.daisy.pipeline.braille.css.xpath.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.collect.Iterables;

import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;
import org.daisy.pipeline.braille.css.xpath.Style;

public class RuleStyle extends FullStyle {

	private final String selector;

	/**
	 * @param rule style with a single nested style
	 */
	public RuleStyle(BrailleCssStyle rule) {
		super(rule);
		if (rule == null || rule.getPropertyNames().iterator().hasNext())
			throw new IllegalArgumentException();
		this.selector = Iterables.getOnlyElement(rule.getSelectors());
	}

	@Override
	protected Iterable<String> keys() {
		return Collections.singleton(selector);
	}

	@Override
	protected Optional<String> property() {
		return Optional.empty();
	}

	@Override
	protected Optional<String> selector() {
		return Optional.of(selector);
	}

	@Override
	protected Optional<Style> get(String key) {
		if (selector.equals(key))
			return Optional.of(new FullStyle(style.getNestedStyle(key)));
		else
			return Optional.empty();
	}

	@Override
	protected Iterator<Style> iterator() {
		return Collections.<Style>singleton(this).iterator();
	}

	@Override
	public String toString() {
		return BrailleCssSerializer.toString(style);
	}

	@Override
	protected String toString(Style parent) {
		return toString();
	}

	@Override
	protected void toXml(XMLStreamWriter writer) throws XMLStreamException {
		// <css:rule>
		BrailleCssSerializer.toXml(style, writer);
	}
}
