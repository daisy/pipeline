package org.daisy.pipeline.braille.common;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import static com.xmlcalabash.core.XProcConstants.NS_XPROC_STEP;

import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;

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
		public Optional<String> getValue();
		public Optional<String> getLiteral();
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
					boolean isString = false;
					if (value != null) {
						Matcher m2 = VALUE_RE.matcher(value);
						if (!m2.matches())
							throw new RuntimeException("Coding error");
						String ident = m2.group("ident");
						String string = m2.group("string");
						String integer = m2.group("integer");
						if (ident != null)
							value = ident;
						else if (string != null && !string.equals("")) {
							value = string.substring(1,string.length()-1).replace("\\A", "\n").replace("\\22", "\"").replace("\\27", "'");
							isString = true; }
						else if (integer != null && !integer.equals(""))
							value = integer;
						else
							throw new RuntimeException("Coding error"); }
					mq.add(new FeatureImpl(key, Optional.ofNullable(value), isString)); }
				return mq.asImmutable(); }
			throw new RuntimeException("Could not parse query: " + query);
		}
		
		public static Query query(XMLStreamReader query) throws XMLStreamException {
			return unmarshallQuery(query);
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
		
		/* marshallQuery & unmarshallQuery */

		private static final QName C_PARAM_SET = new QName(NS_XPROC_STEP, "param-set", "c");
		private static final QName C_PARAM = new QName(NS_XPROC_STEP, "param", "c");
		private static final QName _NAME = new QName("name");
		private static final QName _NAMESPACE = new QName("namespace");
		private static final QName _VALUE = new QName("value");

		/**
		 * Convert query to c:param-set document
		 */
		public static void marshallQuery(Query query, XMLStreamWriter writer) throws XMLStreamException {
			writeStartElement(writer, C_PARAM_SET);
			for (Feature f : query) {
				writeStartElement(writer, C_PARAM);
				writeAttribute(writer, _NAME, f.getKey());
				writeAttribute(writer, _NAMESPACE, "");
				writeAttribute(writer, _VALUE, f.getValue().orElse("true"));
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}

		/**
		 * Convert c:param-set document to query
		 */
		public static Query unmarshallQuery(XMLStreamReader reader) throws XMLStreamException {
			MutableQuery query = mutableQuery();
			int depth = 0;
			int event = reader.next();
			while (true)
				try {
					switch (event) {
					case START_ELEMENT:
						if (depth == 0 && C_PARAM.equals(reader.getName())) {
							String name = null;
							String namespace = null;
							String value = null;
							for (int i = 0; i < reader.getAttributeCount(); i++) {
								QName attrName = reader.getAttributeName(i);
								String attrValue = reader.getAttributeValue(i);
								if (_NAME.equals(attrName))
									name = attrValue;
								else if (_NAMESPACE.equals(attrName))
									namespace = attrValue;
								else if (_VALUE.equals(attrName))
									value = attrValue;
							}
							if ((namespace == null || "".equals(namespace)) && name != null && value != null)
								query.add(name, value);
						}
						depth++;
						break;
					case END_ELEMENT:
						depth--;
						break;
					default:
					}
					event = reader.next();
				} catch (NoSuchElementException e) {
					break;
				}
			return query.asImmutable();
		}

		private static abstract class AbstractQueryImpl extends AbstractCollection<Feature> implements Query {

			protected final List<Feature> list;
			
			protected AbstractQueryImpl() {
				this(new ArrayList<Feature>());
			}
			
			protected AbstractQueryImpl(List<Feature> list) {
				this.list = list;
			}

			public int size() {
				return list.size();
			}
			
			public Iterator<Feature> iterator() {
				return list.iterator();
			}
			
			public boolean containsKey(String key) {
				return get(key).iterator().hasNext();
			}
			
			public Iterable<Feature> get(final String key) {
				final ListIterator<Feature> features = list.listIterator();
				while (features.hasNext()) {
					Feature next = features.next();
					if (next.getKey().equals(key)) {
						features.previous();
						return new Iterable<Feature>() {
							public Iterator<Feature> iterator() {
								return new Iterator<Feature>() {
									private boolean canRemove = false;
									public boolean hasNext() {
										return features.hasNext() && list.get(features.nextIndex()).getKey().equals(key);
									}
									public Feature next() {
										Feature next = features.next();
										if (next.getKey().equals(key)) {
											canRemove = true;
											return next; }
										else
											throw new NoSuchElementException();
									}
									public void remove() {
										features.remove();
										canRemove = false;
									}
								};
							}
						};
					}
				}
				return Collections.emptyList();
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
			public int hashCode() {
				return list.hashCode();
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
			
			public boolean add(Feature feature) {
				int index = 0;
				String key = feature.getKey();
				for (Feature f : list) {
					int cmp = f.getKey().compareTo(key);
					if (cmp > 0) {
						break;
					} else if (cmp > 0) {
						index++;
						break;
					}
					index++;
				}
				list.add(index, feature);
				return true;
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
			
			private ImmutableQueryImpl(MutableQueryImpl query) {
				super(ImmutableList.copyOf(query.list));
			}
		}
		
		private static class FeatureImpl implements Feature {
			
			final String key;
			final Optional<String> value;
			final Optional<String> literal;
			
			private FeatureImpl(String key, Optional<String> value) {
				this(key, value, false);
			}
			
			private FeatureImpl(String key, Optional<String> value, boolean specifiedAsString) {
				this.key = key;
				this.value = value;
				if (value.isPresent()) {
					String v = value.get();
					if (!specifiedAsString && (v.matches(IDENT_RE) || v.matches(INTEGER_RE)))
						this.literal = Optional.of(v);
					else
						this.literal = Optional.of("\"" + v.replace("\n", "\\A").replace("\"","\\22") + "\"");
				} else
					this.literal = Optional.empty();
			}
			
			public String getKey() {
				return key;
			}
			
			public boolean hasValue() {
				return getValue().isPresent();
			}
			
			public Optional<String> getValue() {
				return value;
			}
			
			public Optional<String> getLiteral() {
				return literal;
			}
			
			public String toString() {
				StringBuilder b = new StringBuilder();
				String k = getKey();
				if (!k.matches(IDENT_RE))
					throw new RuntimeException();
				b.append("(" + k);
				if (hasValue()) {
					b.append(":");
					b.append(getLiteral().get());
				}
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
