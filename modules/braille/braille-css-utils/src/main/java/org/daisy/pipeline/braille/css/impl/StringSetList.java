package org.daisy.pipeline.braille.css.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermPair;
import cz.vutbr.web.css.TermString;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.pipeline.braille.css.impl.ContentList.AttrFunction;
import org.daisy.pipeline.braille.css.impl.ContentList.ContentFunction;

import org.w3c.dom.Element;

/**
 * This class is immutable
 */
public class StringSetList extends AbstractList<Term<?>> implements Term<StringSetList> {

	/**
	 * This class is immutable
	 */
	public static class StringSet extends UnmodifiableTerm<ContentList> implements TermPair<String,ContentList> {
		private final String name;
		private final ContentList value;
		private final Operator operator;
		private StringSet(String name, ContentList value, Operator operator) throws IllegalArgumentException {
			this(name, value, operator, false);
		}
		private StringSet(String name, ContentList value, Operator operator, boolean noCheck) throws IllegalArgumentException {
			super();
			if (!noCheck) {
				for (Term<?> v : value)
					if (!(v instanceof TermString
					      || v instanceof AttrFunction
					      || (v instanceof ContentFunction && !((ContentFunction)v).target.isPresent())))
						throw new IllegalArgumentException("unexpected term in string-set list: " + v);
			}
			this.name = name;
			this.value = value;
			this.operator = operator;
		}
		@Override
		public String getKey() {
			return name;
		}
		@Override
		public ContentList getValue() {
			return value;
		}
		@Override
		public Operator getOperator() {
			return operator;
		}
		@Override
		public TermPair<String,ContentList> setKey(String key) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		@Override
		public String toString() {
			return BrailleCssSerializer.toString(this);
		}
		/**
		 * Evaluate <code>attr()</code> and <code>content()</code> values.
		 */
		private StringSet evaluate(Element context) {
			ContentList evaluatedValue = value.clone();
			evaluatedValue.evaluate(context);
			return new StringSet(name, evaluatedValue, operator, true);
		}
	}

	private List<Term<?>> list;

	private StringSetList(List<Term<?>> list) {
		super();
		this.list = list;
	}

	/**
	 * @param list assumed to not change
	 */
	public static StringSetList of(BrailleCssParser parser, Context context, TermList list) throws IllegalArgumentException {
		List<Term<?>> pairs = new ArrayList<>();
		for (Term<?> t : list) {
			if (t instanceof TermPair) {
				TermPair pair = (TermPair)t;
				Object k = pair.getKey();
				Object v = pair.getValue();
				if (k instanceof String && v instanceof TermList)
					pairs.add(new StringSet((String)k, ContentList.of(parser, context, (TermList)v), t.getOperator()));
				else
					throw new IllegalArgumentException("unexpected term in string-set list: " + t);
			} else
				throw new IllegalArgumentException("unexpected term in string-set list: " + t);
		}
		return new StringSetList(pairs);
	}

	@Override
	public Term<?> get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public StringSetList getValue() {
		return this;
	}

	@Override
	public Operator getOperator() {
		return null;
	}

	@Override
	public StringSetList shallowClone() {
		try {
			return (StringSetList)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("coding error");
		}
	}

	@Override
	public StringSetList clone() {
		StringSetList clone = shallowClone();
		clone.list = new ArrayList<>(list);
		return clone;
	}

	@Override
	public StringSetList setValue(StringSetList value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public StringSetList setOperator(Operator operator) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	/**
	 * Evaluate <code>attr()</code> and <code>content()</code> values.
	 *
	 * This method is mutating, but we can still say that the object is immutable because the method
	 * is package private and only used by {@link BrailleCssStyle} and {@link BrailleCssParser}
	 * _before_ the object is made available.
	 */
	void evaluate(Element context) {
		for (int i = 0; i < list.size(); i++)
			list.set(i, ((StringSet)list.get(i)).evaluate(context));
	}
}
