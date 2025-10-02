package org.daisy.pipeline.braille.css.xpath.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.PropertyValue;
import org.daisy.pipeline.braille.css.impl.BrailleCssParser;
import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;
import org.daisy.pipeline.braille.css.impl.ContentList;
import org.daisy.pipeline.braille.css.xpath.Style;

public class Stylesheet extends Style {

	// for use in Style.toString() and Style.put() only
	public final static Stylesheet EMPTY = new Stylesheet();

	public final BrailleCssStyle style;

	public static Stylesheet of(BrailleCssStyle style) {
		if (style != null && style.underlyingObject instanceof org.daisy.pipeline.css.CounterStyle)
			return new CounterStyle(style);
		else if (style == null || style.isEmpty())
			return EMPTY;
		else
			return new Stylesheet(style);
	}

	protected Stylesheet(BrailleCssStyle style) {
		if (style == null || style.isEmpty())
			throw new IllegalArgumentException();
		this.style = style;
	}

	// for Stylesheet.EMPTY
	private Stylesheet() {
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
			cz.vutbr.web.css.Declaration d = style.getDeclaration(key);
			if (d != null)
				return Optional.of(new Value(d));
			BrailleCssStyle s = style.getNestedStyle(key);
			if (s != null)
				return Optional.of(Stylesheet.of(s));
		}
		return Optional.empty();
	}

	@Override
	protected Optional<Style> getOrDefault(String propertyName) {
		if (style != null) {
			cz.vutbr.web.css.Declaration d = style.getDeclaration(propertyName, true);
			if (d != null)
				return Optional.of(new Value(d));
		}
		return Optional.empty();
	}

	@Override
	protected Optional<Style> remove(Iterator<String> keys) {
		if (style == null)
			return Optional.empty();
		BrailleCssStyle s = style.remove(keys);
		if (s.isEmpty())
			return Optional.empty();
		else if (s != style)
			return Optional.of(Stylesheet.of(s));
		else
			return Optional.of(this);
	}

	@Override
	protected Iterator<Object> iterate() {
		if (style != null)
			return Iterators.concat(
				Iterators.transform(style.getDeclarations().iterator(), Declaration::new),
				Iterators.transform(style.getRules().iterator(), Rule::new));
		else
			return Collections.emptyIterator();
	}

	@Override
	protected Optional<Style> put(String key, Optional<Style> style) {
		if (!style.isPresent())
			return this.style != null ? Optional.of(this) : Optional.empty();
		Style s = style.get();
		if (s instanceof Stylesheet)
			return Optional.of(Stylesheet.of((this.style != null ? this.style : BrailleCssStyle.EMPTY)
			                                 .add(key, ((Stylesheet)s).style)));
		else if (s instanceof Value) {
			Value v = (Value)s;
			if (v.value instanceof ContentList)
				return Optional.of(Stylesheet.of((this.style != null ? this.style : BrailleCssStyle.EMPTY)
				                                 .add(key, (ContentList)v.value)));
		}
		throw new IllegalArgumentException();
	}

	@Override
	protected String toString(Style parent) {
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
			style = BrailleCssStyle.EMPTY;
		else if (relativeTo == null && parent != null)
			// we may want to remove unneeded declarations
			relativeTo = BrailleCssStyle.EMPTY;
		return BrailleCssSerializer.getInstance().toString(style, relativeTo);
	}

	@Override
	protected String toPrettyString(String indentation) {
		if (style != null)
			return BrailleCssSerializer.getInstance().toString(style, indentation);
		else
			return "";
	}

	@Override
	protected void toAttributes(Style parent, XMLStreamWriter writer) throws XMLStreamException {
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
			return;
		else if (style == null)
			// we may need to add declarations to cancel parent declarations
			style = BrailleCssStyle.EMPTY;
		else if (relativeTo == null && parent != null)
			// we may want to remove unneeded declarations
			relativeTo = BrailleCssStyle.EMPTY;
		BrailleCssSerializer.getInstance().toAttributes(style, relativeTo, writer);
	}

	@Override
	protected void toXml(XMLStreamWriter writer) throws XMLStreamException {
		if (style != null)
			// <css:rule>
			BrailleCssSerializer.getInstance().toXml(style, writer);
	}
}
