package org.daisy.pipeline.braille.css.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermPair;

/**
 * This class is immutable
 */
public class CounterSetList extends AbstractList<Term<?>> implements Term<CounterSetList> {

	/**
	 * This class is immutable
	 */
	public static class CounterSet extends UnmodifiableTerm<Integer> implements org.daisy.pipeline.braille.css.CounterSet {
		private final String name;
		private final Integer value;
		private CounterSet(String name, Integer value) {
			super();
			this.name = name;
			this.value = value;
		}
		@Override
		public String getKey() {
			return name;
		}
		@Override
		public Integer getValue() {
			return value;
		}
		@Override
		public TermPair<String,Integer> setKey(String key) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
	}

	private List<Term<?>> list;

	private CounterSetList(List<Term<?>> list) {
		super();
		this.list = list;
	}

	/**
	 * @param list assumed to not change
	 */
	public static CounterSetList of(List<Term<?>> list) {
		Map<String,Integer> pairs = new LinkedHashMap<>(); // preserves order of insertion
		for (Term<?> t : list) {
			if (t instanceof TermPair) {
				TermPair pair = (TermPair)t;
				Object k = pair.getKey();
				Object v = pair.getValue();
				if (k instanceof String && v instanceof Integer) {
					pairs.remove((String)k);
					pairs.put((String)k, (Integer)v);
				} else
					throw new IllegalArgumentException("unexpected term in counter-set list: " + t);
			} else
				throw new IllegalArgumentException("unexpected term in counter-set list: " + t);
		}
		List<Term<?>> pairsList = new ArrayList();
		for (Map.Entry<String,Integer> e : pairs.entrySet())
			pairsList.add(new CounterSet(e.getKey(), e.getValue()));
		return new CounterSetList(pairsList);
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
	public CounterSetList getValue() {
		return this;
	}

	@Override
	public Operator getOperator() {
		return null;
	}

	@Override
	public CounterSetList shallowClone() {
		try {
			return (CounterSetList)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("coding error");
		}
	}

	@Override
	public CounterSetList clone() {
		return shallowClone();
	}

	@Override
	public CounterSetList setValue(CounterSetList value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public CounterSetList setOperator(Operator operator) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
}
