package org.daisy.pipeline.braille.css.xpath.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.collect.Iterators;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Term;

import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.PropertyValue;
import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.impl.ContentList;
import org.daisy.pipeline.braille.css.xpath.Style;

public class ValueStyle extends Style {

	public final List<Term<?>> value;

	public ValueStyle(Declaration value) {
		if (value == null)
			throw new IllegalArgumentException();
		ContentList content = toContentList(value);
		if (content != null)
			this.value = content;
		else
			this.value = value;
	}

	public ValueStyle(ContentList value) {
		if (value == null)
			throw new IllegalArgumentException();
		this.value = value;
	}

	@Override
	public boolean empty() {
		return value == null || value.isEmpty();
	}

	@Override
	public Iterator<Style> iterator() {
		if (value instanceof ContentList)
			return Iterators.transform(value.iterator(),
			                           v -> new ValueStyle(ContentList.of(Collections.singletonList(v),
			                                                              ((ContentList)value).getSupportedBrailleCSS())));
		return Collections.emptyIterator();
	}

	@Override
	public String toString(Style parent) {
		if (parent != null)
			throw new UnsupportedOperationException();
		if (value instanceof PropertyValue)
			return BrailleCssSerializer.serializePropertyValue((PropertyValue)value);
		else if (value != null)
			return BrailleCssSerializer.serializeTermList(value);
		else
			return "";
	}

	@Override
	public void toXml(XMLStreamWriter writer) throws XMLStreamException {
		if (value instanceof ContentList)
			BrailleCssSerializer.toXml((ContentList)value, writer);
	}

	private static ContentList toContentList(List<Term<?>> value) {
		if (value instanceof ContentList)
			return (ContentList)value;
		else if (value instanceof PropertyValue) {
			PropertyValue pv = (PropertyValue)value;
			if (pv.getCSSProperty() == Content.content_list)
				if (pv.getValue() instanceof ContentList)
					return (ContentList)pv.getValue();
				else
					throw new IllegalArgumentException();
		}
		return null;
	}
}
