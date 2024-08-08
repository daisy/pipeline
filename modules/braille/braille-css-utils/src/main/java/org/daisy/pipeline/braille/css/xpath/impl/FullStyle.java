package org.daisy.pipeline.braille.css.xpath.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import cz.vutbr.web.css.Declaration;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.PropertyValue;
import org.daisy.pipeline.braille.css.impl.BrailleCssParser;
import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;
import org.daisy.pipeline.braille.css.impl.ContentList;
import org.daisy.pipeline.braille.css.xpath.Style;

public class FullStyle extends Style {

	// for use in Style.toString() and Style.add() only
	public final static Style EMPTY = new FullStyle();

	public final BrailleCssStyle style;

	public FullStyle(BrailleCssStyle style) {
		if (style == null || style.isEmpty())
			throw new IllegalArgumentException();
		this.style = style;
	}

	// for FullStyle.EMPTY
	private FullStyle() {
		this.style = null;
	}

	@Override
	protected Iterable<String> keys() {
		if (style != null)
			return Iterables.concat(style.getPropertyNames(), style.getSelectors());
		else
			return Collections.emptyList();
	}

	@Override
	protected Optional<String> property() {
		if (style != null) {
			Iterator<String> properties = style.getPropertyNames().iterator();
			if (properties.hasNext()) {
				String property = properties.next();
				if (properties.hasNext() || style.getSelectors().iterator().hasNext())
					throw new UnsupportedOperationException();
				return Optional.of(property);
			}
		}
		return Optional.empty();
	}

	@Override
	protected Optional<String> selector() {
		if (style != null) {
			Iterator<String> selectors = style.getSelectors().iterator();
			if (selectors.hasNext()) {
				String selector = selectors.next();
				if (selectors.hasNext() || style.getPropertyNames().iterator().hasNext())
					throw new UnsupportedOperationException();
				return Optional.of(selector);
			}
		}
		return Optional.empty();
	}

	@Override
	protected Optional<Style> get(String key) {
		if (style != null) {
			Declaration d = style.getDeclaration(key);
			if (d != null)
				return Optional.of(new ValueStyle(d));
			BrailleCssStyle s = style.getNestedStyle(key);
			if (s != null)
				return Optional.of(new FullStyle(s));
		}
		return Optional.empty();
	}

	@Override
	protected Optional<Style> getOrDefault(String propertyName) {
		if (style != null) {
			Declaration d = style.getDeclaration(propertyName, true);
			if (d != null)
				return Optional.of(new ValueStyle(d));
		}
		return Optional.empty();
	}

	@Override
	protected Optional<Style> remove(String key) {
		if (style == null)
			return Optional.empty();
		BrailleCssStyle s = style.remove(key);
		if (s.isEmpty())
			return Optional.empty();
		else if (s != style)
			return Optional.of(new FullStyle(s));
		else
			return Optional.of(this);
	}

	@Override
	protected Iterator<Style> iterator() {
		if (style != null)
			return Iterators.concat(
				Iterators.transform(style.getDeclarations().iterator(), DeclarationStyle::new),
				Iterators.transform(style.getRules().iterator(), RuleStyle::new));
		else
			return Collections.emptyIterator();
	}

	@Override
	protected Optional<Style> add(String key, Optional<Style> style) {
		if (!style.isPresent())
			return this.style != null ? Optional.of(this) : Optional.empty();
		Style s = style.get();
		if (s instanceof FullStyle)
			return Optional.of(new FullStyle((this.style != null ? this.style : BrailleCssStyle.EMPTY)
			                                 .add(key, ((FullStyle)s).style)));
		else if (s instanceof ValueStyle) {
			ValueStyle v = (ValueStyle)s;
			if (v.value instanceof ContentList)
				return Optional.of(new FullStyle((this.style != null ? this.style : BrailleCssStyle.EMPTY)
				                                 .add(key, (ContentList)v.value)));
		}
		throw new IllegalArgumentException();
	}

	@Override
	protected String toString(Style parent) {
		BrailleCssStyle style = this.style;
		BrailleCssStyle relativeTo; {
			if (parent != null) {
				if (!(parent instanceof FullStyle))
					throw new IllegalArgumentException("Unexpected type for second argument: " + parent);
				relativeTo = ((FullStyle)parent).style;
			} else
				relativeTo = null;
		}
		if (style == null && relativeTo == null)
			return "";
		else if (style == null)
			// we may need to add declarations to cancel parent declarations
			style = BrailleCssStyle.EMPTY;
		else if (relativeTo == null && parent != null)
			// we may want to remove unneeded declarations
			relativeTo = BrailleCssStyle.EMPTY;
		return BrailleCssSerializer.toString(style, relativeTo);
	}

	@Override
	protected void toXml(XMLStreamWriter writer) throws XMLStreamException {
		if (style != null)
			// <css:rule>
			BrailleCssSerializer.toXml(style, writer);
	}
}
