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

	public final static FullStyle EMPTY = new FullStyle(null);

	public final BrailleCssStyle style;

	public FullStyle(BrailleCssStyle style) {
		this.style = style;
	}

	@Override
	public Iterable<String> keys() {
		if (style != null)
			return Iterables.concat(style.getPropertyNames(), style.getSelectors());
		else
			return Collections.emptyList();
	}

	@Override
	public Optional<String> property() {
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
	public Optional<String> selector() {
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
	public boolean empty() {
		return style == null || style.isEmpty();
	}

	@Override
	public Optional<Style> get(String key) {
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
	public Optional<Style> getOrDefault(String propertyName) {
		if (style != null) {
			Declaration d = style.getDeclaration(propertyName, true);
			if (d != null)
				return Optional.of(new ValueStyle(d));
		}
		return Optional.empty();
	}

	@Override
	public Style remove(String key) {
		if (style != null) {
			BrailleCssStyle s = style.remove(key);
			if (s.isEmpty())
				return FullStyle.EMPTY;
			else if (s != style)
				return new FullStyle(s);
		}
		return this;
	}

	@Override
	public Iterator<Style> iterator() {
		if (style != null)
			return Iterators.concat(
				Iterators.transform(style.getDeclarations().iterator(), DeclarationStyle::new),
				Iterators.transform(style.getRules().iterator(), RuleStyle::new));
		else
			return Collections.emptyIterator();
	}

	@Override
	public Style add(String key, Style style) {
		if (style.empty())
			return this;
		else if (style instanceof FullStyle)
			return new FullStyle((this.style != null ? this.style : BrailleCssStyle.of("", Context.ELEMENT))
			                     .add(key, ((FullStyle)style).style));
		else if (style instanceof ValueStyle) {
			ValueStyle v = (ValueStyle)style;
			if (v.value instanceof ContentList) {
				Declaration d = new PropertyValue(key, Content.content_list, (ContentList)v.value, null,
				                                  ((ContentList)v.value).getSupportedBrailleCSS());
				return new FullStyle((this.style != null ? this.style : BrailleCssStyle.of("", Context.ELEMENT)).add(d));
			}
		}
		throw new IllegalArgumentException();
	}

	@Override
	public String toString(Style parent) {
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
