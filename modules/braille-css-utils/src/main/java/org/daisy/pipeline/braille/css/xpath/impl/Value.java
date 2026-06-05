package org.daisy.pipeline.braille.css.xpath.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.collect.Iterators;

import cz.vutbr.web.css.CSSProperty.CounterIncrement;
import cz.vutbr.web.css.CSSProperty.CounterSet;
import cz.vutbr.web.css.CSSProperty.CounterReset;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermString;

import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.BrailleCSSProperty.StringSet;
import org.daisy.braille.css.PropertyValue;
import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.impl.ContentList;
import org.daisy.pipeline.braille.css.impl.CounterSetList;
import org.daisy.pipeline.braille.css.impl.StringSetList;
import org.daisy.pipeline.braille.css.xpath.Style;

public class Value extends Style {

	public final List<Term<?>> value;

	public Value(cz.vutbr.web.css.Declaration value) {
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException();
		if (value instanceof PropertyValue) {
				PropertyValue pv = (PropertyValue)value;
				if (pv.getCSSProperty() == Content.content_list)
					if (pv.getValue() instanceof ContentList)
						this.value = (ContentList)pv.getValue();
					else
						throw new IllegalArgumentException();
				else if (pv.getCSSProperty() == StringSet.list_values)
					if (pv.getValue() instanceof StringSetList)
						this.value = (StringSetList)pv.getValue();
					else
						throw new IllegalArgumentException();
				else if (pv.getCSSProperty() == CounterSet.list_values ||
				         pv.getCSSProperty() == CounterReset.list_values ||
				         pv.getCSSProperty() == CounterIncrement.list_values)
					if (pv.getValue() instanceof CounterSetList)
						this.value = (CounterSetList)pv.getValue();
					else
						throw new IllegalArgumentException();
				else
					this.value = value;
			} else
				this.value = value;
	}

	public Value(ContentList value) {
		if (value == null)
			throw new IllegalArgumentException();
		this.value = value;
	}

	private Value(CounterSetList value) {
		if (value == null)
			throw new IllegalArgumentException();
		this.value = value;
	}

	@Override
	protected Iterator<Object> iterate() {
		if (value instanceof ContentList)
			return Iterators.transform(value.iterator(),
			                           v -> v instanceof TermString
			                                    ? ((TermString)v).getValue()
			                           : new Value(ContentList.of(((ContentList)value).getParser(),
			                                                      ((ContentList)value).getContext(),
			                                                      Collections.singletonList(v))));
		else if (value instanceof CounterSetList)
			return Iterators.transform(value.iterator(),
			                           v -> new Value(CounterSetList.of(Collections.singletonList(v))));
		else
			return Collections.<Object>singleton(this).iterator();
	}

	@Override
	protected String toString(Style parent) {
		if (parent != null)
			throw new UnsupportedOperationException();
		if (value instanceof PropertyValue)
			return BrailleCssSerializer.getInstance().serializePropertyValue((PropertyValue)value);
		else if (value != null)
			return BrailleCssSerializer.getInstance().serializeTermList(value);
		else
			return "";
	}

	@Override
	protected String toPrettyString(String indentation) {
		return toString();
	}

	@Override
	protected void toXml(XMLStreamWriter writer) throws XMLStreamException {
		if (value instanceof ContentList)
			BrailleCssSerializer.getInstance().toXml((ContentList)value, writer);
		else if (value instanceof CounterSetList)
			BrailleCssSerializer.getInstance().toXml((CounterSetList)value, writer);
		else if (value instanceof StringSetList)
			BrailleCssSerializer.getInstance().toXml((StringSetList)value, writer);
	}
}
