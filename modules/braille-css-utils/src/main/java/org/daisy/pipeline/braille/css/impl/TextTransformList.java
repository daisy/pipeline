package org.daisy.pipeline.braille.css.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.PropertyValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.csskit.TermIdentImpl;

/**
 * This class is immutable except when <code>locked</code> is set to false in which case terms may be removed.
 */
public class TextTransformList extends AbstractList<Term<?>> implements TermList {

	private List<TermIdent> list;

	// object should always be cloned before it is unlocked
	boolean locked = true;

	private final static ImmutableList<TermIdent> INITIAL = ImmutableList.of(new UnmodifiableTermIdent(new TermIdentImpl(){}.setValue("initial")));
	private final static ImmutableList<TermIdent> AUTO = ImmutableList.of(new UnmodifiableTermIdent(new TermIdentImpl(){}.setValue("auto")));
	private final static ImmutableList<TermIdent> NONE = ImmutableList.of(new UnmodifiableTermIdent(new TermIdentImpl(){}.setValue("none")));

	private TextTransformList(List<TermIdent> list) {
		super();
		this.list = list;
	}

	/**
	 * @param value assumed to not change
	 */
	public static TextTransformList of(TermIdent value) {
		String v = value.getValue();
		if (v.equalsIgnoreCase("initial"))
			return new TextTransformList(INITIAL);
		else if (v.equalsIgnoreCase("auto"))
			return new TextTransformList(AUTO);
		else if (v.equalsIgnoreCase("none"))
			return new TextTransformList(NONE);
		else if (v.equalsIgnoreCase("inherit"))
			throw new IllegalArgumentException();
		else {
			List<TermIdent> list = new ArrayList<>();
			list.add(new UnmodifiableTermIdent(value));
			return new TextTransformList(list);
		}
	}

	/**
	 * @param value assumed to not change
	 */
	public static TextTransformList of(TermList value) {
		List<TermIdent> list = new ArrayList<>();
		for (Term<?> t : value) {
			if (t instanceof TermIdent)
				list.add(new UnmodifiableTermIdent((TermIdent)t));
			else
				throw new IllegalArgumentException("unexpected term in text-transform list: " + t);
		}
		return new TextTransformList(list);
	}

	/**
	 * @param value assumed to not change
	 */
	public static TextTransformList of(PropertyValue value) {
		CSSProperty p = value.getCSSProperty();
		if (p instanceof TextTransform) {
			if (p == TextTransform.list_values) {
				Term<?> v = value.getValue();
				if (v instanceof TextTransformList)
					return (TextTransformList)v;
				else if (v instanceof TermList)
					return of((TermList)v);
				else
					throw new IllegalStateException(); // should not happen
			} else if (p == TextTransform.AUTO)
				return new TextTransformList(AUTO);
			else if (p == TextTransform.INITIAL)
				return new TextTransformList(INITIAL);
			else if (p == TextTransform.NONE)
				return new TextTransformList(NONE);
		}
		throw new IllegalArgumentException();
	}
	
	@Override
	public TermIdent get(int index) {
		return list.get(index);
	}

	@Override
	public TermIdent remove(int index) {
		if (locked)
			throw new UnsupportedOperationException("Unmodifiable");
		return list.remove(index);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public TextTransformList getValue() {
		return this;
	}

	@Override
	public Operator getOperator() {
		return null;
	}

	@Override
	public TextTransformList shallowClone() {
		try {
			return (TextTransformList)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("coding error");
		}
	}

	@Override
	public TextTransformList clone() {
		TextTransformList clone = shallowClone();
		if (!(list instanceof ImmutableList))
			clone.list = new ArrayList<>(list);
		return clone;
	}

	@Override
	public TextTransformList setValue(List<Term<?>> value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public TextTransformList setOperator(Operator operator) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	/**
	 * This method is mutating, but we can still say that the object is immutable because the method
	 * is package private and only used by {@link BrailleCssStyle} _before_ the object is made
	 * available.
	 */
	void inheritFrom(TextTransformList parent) {
		if (equalsNone() || parent.equalsAuto() || parent.equalsInitial() || parent.equalsNone())
			;
		else if (equalsAuto() || equalsInitial())
			list = parent.list;
		else {
			List<TermIdent> list = new ArrayList<>();
			list.addAll(this.list);
			for (TermIdent t : parent.list)
				if (!Iterables.any(list, x -> x.getValue().equalsIgnoreCase(t.getValue())))
					list.add(t);
			this.list = list;
		}
	}

	public boolean equalsAuto() {
		return list == AUTO;
	}

	public boolean equalsInitial() {
		return list == INITIAL;
	}

	public boolean equalsNone() {
		return list == NONE;
	}
}
