package org.daisy.pipeline.braille.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public interface Query extends Iterable<Query.Feature> {
	
	@Override
	public String toString();
	
	public boolean containsKey(String key);
	public Iterable<Feature> get(String key);
	public Feature getOnly(String key) throws IllegalStateException;
	public boolean isEmpty();
	
	public static interface Feature {
		public String getKey();
		public boolean hasValue();
		public String getValueOrNull();
		public Optional<String> getValue();
		@Override public String toString();
	}
	
	public static interface MutableQuery extends Query {
		public MutableQuery add(String key);
		public MutableQuery add(String key, String value);
		public MutableQuery add(String key, Optional<String> value);
		public boolean add(Feature feature);
		public MutableQuery addAll(Iterable<Feature> features);
		public boolean remove(Feature feature);
		public Iterable<Feature> removeAll(String key);
		public Feature removeOnly(String key) throws IllegalStateException;
		public Query asImmutable();
	}
	
	/* ================== */
	/*       UTILS        */
	/* ================== */
	
	public static abstract class util {
		
		/* query() */
		
		public static Query query(String query) {
			if (FEATURES_RE.matcher(query).matches()) {
				MutableQuery mq = mutableQuery();
				Matcher m = FEATURE_RE.matcher(query);
				while(m.find()) {
					String key = m.group("key");
					String value = m.group("value");
					if (value != null) {
						Matcher m2 = VALUE_RE.matcher(value);
						if (!m2.matches())
							throw new RuntimeException("Coding error");
						String ident = m2.group("ident");
						String string = m2.group("string");
						String integer = m2.group("integer");
						if (ident != null)
							value = ident;
						else if (string != null && !string.equals(""))
							value = string.substring(1,string.length()-1);
						else if (integer != null && !integer.equals(""))
							value = integer;
						else
							throw new RuntimeException("Coding error"); }
					mq.add(key, Optional.ofNullable(value)); }
				return mq.asImmutable(); }
			throw new RuntimeException("Could not parse query: " + query);
		}
		
		/* mutableQuery() */
		
		public static MutableQuery mutableQuery() {
			return new MutableQueryImpl();
		}
		
		public static MutableQuery mutableQuery(Query copyOf) {
			MutableQuery q = new MutableQueryImpl();
			q.addAll(copyOf);
			return q;
		}
		
		private static abstract class AbstractQueryImpl extends ForwardingList<Feature> implements Query {
			
			public boolean containsKey(String key) {
				return get(key).iterator().hasNext();
			}
			
			public Iterable<Feature> get(final String key) {
				final ListIterator<Feature> features = listIterator();
				return new Iterable<Feature>() {
					public Iterator<Feature> iterator() {
						return new Iterator<Feature>() {
							public boolean hasNext() {
								for (int i = features.nextIndex(); i < size(); i++)
									if (get(i).getKey().equals(key))
										return true;
								return false;
							}
							public Feature next() {
								while (true) {
									Feature next = features.next();
									if (next.getKey().equals(key))
										return next; }
							}
							public void remove() {
								features.remove();
							}
						};
					}
				};
			}
			
			public Feature getOnly(String key) throws IllegalStateException {
				Iterator<Feature> features = get(key).iterator();
				if (!features.hasNext())
					throw new IllegalStateException();
				Feature f = features.next();
				if (features.hasNext())
					throw new IllegalStateException();
				return f;
			}
			
			public String toString() {
				StringBuilder b = new StringBuilder();
				for (Feature f : this)
					b.append(f);
				return b.toString();
			}
			
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (!(obj instanceof Query))
					return false;
				return Iterables.elementsEqual(this, (Query)obj);
			}
		}
		
		private static class MutableQueryImpl extends AbstractQueryImpl implements MutableQuery {
			
			private final List<Feature> list = new ArrayList<Feature>();
			
			protected List<Feature> delegate() {
				return list;
			}
			
			public MutableQuery add(String key) {
				add(key, Optional.<String>empty());
				return this;
			}
			
			public MutableQuery add(String key, String value) {
				add(key, Optional.<String>ofNullable(value));
				return this;
			}
			
			public MutableQuery add(String key, Optional<String> value) {
				add(new FeatureImpl(key, value));
				return this;
			}
			
			public MutableQuery addAll(Iterable<Feature> features) {
				for (Feature f : features)
					add(f);
				return this;
			}
			
			public boolean remove(Feature feature) {
				return super.remove(feature);
			}
			
			public Iterable<Feature> removeAll(final String key) {
				ImmutableList.Builder<Feature> list = new ImmutableList.Builder<Feature>();
				Iterator<Feature> features = iterator();
				while (features.hasNext()) {
					Feature next = features.next();
					if (next.getKey().equals(key)) {
						features.remove();
						list.add(next); }}
				return list.build();
			}
			
			public Feature removeOnly(String key) throws IllegalStateException {
				Iterator<Feature> features = get(key).iterator();
				if (!features.hasNext())
					throw new IllegalStateException();
				Feature f = features.next();
				if (features.hasNext())
					throw new IllegalStateException();
				features.remove();
				return f;
			}
			
			/**
			 * Returned value is a snapshot, i.e. will not change after the call.
			 */
			public Query asImmutable() {
				return new ImmutableQueryImpl(this);
			}
		}
		
		private static class ImmutableQueryImpl extends AbstractQueryImpl {
			
			private final ImmutableList<Feature> list;
			
			private ImmutableQueryImpl(List<Feature> list) {
				this.list = ImmutableList.copyOf(list);
			}
				
			protected List<Feature> delegate() {
				return list;
			}
		}
		
		private static class FeatureImpl implements Feature {
			
			final String key;
			final Optional<String> value;
			
			private FeatureImpl(String key, Optional<String> value) {
				this.key = key;
				this.value = value;
			}
			
			public String getKey() {
				return key;
			}
			
			public boolean hasValue() {
				return getValue().isPresent();
			}
			
			public String getValueOrNull() {
				return getValue().orElse(null);
			}
			
			public Optional<String> getValue() {
				return value;
			}
			
			public String toString() {
				StringBuilder b = new StringBuilder();
				String k = getKey();
				if (!k.matches(IDENT_RE))
					throw new RuntimeException();
				b.append("(" + k);
				if (hasValue()) {
					String v = getValue().get();
					b.append(":");
					if (v.matches(IDENT_RE) || v.matches(INTEGER_RE))
						b.append(v);
					else if (v.contains("'")) {
						if (v.contains("\""))
							throw new RuntimeException();
						b.append("\"" + v + "\""); }
					else
						b.append("'" + v + "'"); }
				b.append(")");
				return b.toString();
			}
			
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((key == null) ? 0 : key.hashCode());
				result = prime * result + ((value == null) ? 0 : value.hashCode());
				return result;
			}
			
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Feature other = (Feature)obj;
				if (key == null) {
					if (other.getKey() != null)
						return false;
				} else if (!key.equals(other.getKey()))
					return false;
				if (value == null) {
					if (other.getValue() != null)
						return false;
				} else if (!value.equals(other.getValue()))
					return false;
				return true;
			}
		}
		
		private static final String IDENT_RE = "[_a-zA-Z][_a-zA-Z0-9-]*";
		private static final String STRING_RE = "'[^']*'|\"[^\"]*\"";
		private static final String INTEGER_RE = "0|-?[1-9][0-9]*";
		private static final Pattern VALUE_RE = Pattern.compile(
			"(?<ident>" + IDENT_RE + ")|(?<string>" + STRING_RE + ")|(?<integer>" + INTEGER_RE + ")"
		);
		private static final Pattern FEATURE_RE = Pattern.compile(
			"\\(\\s*(?<key>" + IDENT_RE+ ")(?:\\s*\\:\\s*(?<value>" + VALUE_RE.pattern() + "))?\\s*\\)"
		);
		private static final Pattern FEATURES_RE = Pattern.compile(
			"\\s*(?:" + FEATURE_RE.pattern() + "\\s*)*"
		);
		public static final Pattern QUERY = FEATURES_RE;
	}
}
